package jp.takes.apps.recordtracker.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import jp.takes.apps.recordtracker.db.TracksDBHelper;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

/**
 * GPS情報を収集／記録するサービス
 * @author take
 *
 */
public class GPSCollectService extends Service implements LocationListener{

	final static String TAG = "GPSCollectService";
	public static final String SEND_START_TIME = "SEND_START_TIME";
	
	// ロケーションマネージャ
	private LocationManager locationManager = null;
	// DBヘルパー
	private TracksDBHelper tracksDB = null;
	// 開始時間
	private long startTimeMillis = 0;
	// 開始時間
	private long elapsedRealtime = 0;
	// GPS取得数
	private Integer gpsCnt = 0;

	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		// メモリ不足時にKILLされないように対処 （ステータスバーに表示してユーザに明示的になっているということでKILLの対象外になる）
		Notification lNotification = new Notification(0, "ticker", System.currentTimeMillis());
		this.startForeground(1, lNotification);
		
		Log.d(GPSCollectService.TAG, "onCreate");
		
		// タイマー用の開始時間設定
		this.elapsedRealtime = SystemClock.elapsedRealtime();

		// 履歴名設定
		this.startTimeMillis = System.currentTimeMillis();

		// GPS情報書き込み用DBのオープン
		this.tracksDB = new TracksDBHelper(this);

		// ロケーション管理インスタンス取得
		this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		// ロケーション情報取得開始
		this.registerLocationListener();
		
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(GPSCollectService.TAG, "onStartCommand");
		
		if ("FROM_WIDGET".equals(intent.getAction())) {
			// ウィジェットから起動された場合
			this.createBroadcastIntent();
		}
		else if ("FROM_ACTIVITY".equals(intent.getAction())) {
			this.createBroadcastIntent();
		}
		return Service.START_STICKY;
	}
	
	/**
	 * ブロードキャストレシーバでGPS開始時刻を送信する。
	 */
	private void createBroadcastIntent() {
		
		Intent broadcastIntent = new Intent();
		broadcastIntent.putExtra("starttime", String.valueOf(this.elapsedRealtime));
		broadcastIntent.setAction(GPSCollectService.SEND_START_TIME);
		this.getBaseContext().sendBroadcast(broadcastIntent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(GPSCollectService.TAG, "onDestroy");
		
		// 位置情報の更新を止める 
		this.locationManager.removeUpdates(this); 
		this.locationManager = null;

		//  開始時間からの差分で経過時間を算出
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss SSS");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		String time = dateFormat.format(new Date(System.currentTimeMillis() - this.startTimeMillis));

		// 履歴名：記録開始日付（表示用）
		String recStart = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(this.startTimeMillis));

		// キー：記録開始日付＋時間（内部処理での使用用途）
		String key = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date(this.startTimeMillis));

		// 総移動距離を取得
		String distance = this.getDistance(key);
		
		Log.d("distance", distance);
		
		this.tracksDB.insertTracksData(recStart, key, time, "走行時間：" + time.substring(0, 8) + "    走行距離：" + distance + "m");
		
		// 停止時もブロードキャストして、停止する旨をウィジェットに通知する。
		this.createBroadcastIntent();
		
		// サービス終了するので、KILL対処を終了する
		this.stopForeground(true);

	}
	
	private String getDistance(String key) {

		double distance = 0;

		TracksDBHelper tracksDB = new TracksDBHelper(this);
		Cursor cursor = tracksDB.getGPSDataList(key);
		if(cursor != null) {
			// 前のGPS取得時間
			long preTime = this.startTimeMillis;
			if (cursor.moveToFirst()) {
				int iRecCnt = cursor.getCount();
				for (int i = 0; i < iRecCnt; i++) {
					if (i == 0) {
						// 初期値がなぜか過去時間のためうまく算出できないのでとりあえずの対処
						preTime = Long.parseLong(cursor.getString(5));
					}
					
					// GPS情報取得時間の取得
					long currentTime = Long.parseLong(cursor.getString(5));
					double speed = Double.valueOf(cursor.getString(6));

					// GSP取得単位毎の走行距離の計算(m)
					// (現在時間-前の時間(ms)）×1000×早さ(m/s) 
					distance += ((currentTime - preTime)*speed)/1000;

					Log.d("distance currentTime", String.valueOf(currentTime));
					Log.d("distance preTime", String.valueOf(preTime));
					Log.d("distance speed", String.valueOf(speed));
					Log.d("distance speed str", cursor.getString(6));
					Log.d("distance gps", String.valueOf(distance));

					// 前のGPS取得時間を設定
					preTime = currentTime;

					// 次のカーソル位置へ変更
					cursor.moveToNext();
				}
			}
			else {
				cursor.close();
				tracksDB.close();
				Toast.makeText(this, "GPS情報が取得できていません", Toast.LENGTH_SHORT).show();
				return "0";
			}
			cursor.close();
		}
		tracksDB.close();

		return String.format("%2.2f",distance);
	}

	
	private void registerLocationListener() {
		
		// ロケーション管理インスタンスがある場合
		if (this.locationManager != null) {
			try {
				// GPS取得数リセット
				this.gpsCnt = 0;
				
				// ポーリングの最小間隔(ms)
				long desiredInterval = 30*1000;
				
				this.locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER,
						desiredInterval,		// 通知のための最小時間間隔
						0,						// 通知のための最小距離間隔
						this);					// 位置情報リスナー
			} catch (RuntimeException e) {
				Log.e(TAG,
						"Could not register location listener: "
								+ e.getMessage(), e);
			}
		}
	}
	

	@Override
	public void onLocationChanged(Location location) {
		this.gpsCnt++;
		this.tracksDB.insertTrackGPSData(
				new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date(this.startTimeMillis)), this.gpsCnt, location);
		Log.d("GPS", "LocationChanged");
	}

	@Override
	public void onProviderDisabled(String provider) {
		// Do nothing
	}

	@Override
	public void onProviderEnabled(String provider) {
		// Do nothing
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// Do nothing
	}


}
