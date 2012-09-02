package jp.takes.apps.recordtracker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.os.Vibrator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.CycleInterpolator;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

public class RecordActivity extends Activity implements LocationListener {

	// ロケーションマネージャ
	private LocationManager locationManager = null;
	// DBヘルパー
	private TracksDBHelper tracksDB = null;
	// GPS開始フラグ
	private boolean isStart = false;
	// 開始時間
	private long startTimeMillis = 0;
	// GPS取得数
	private Integer gpsCnt = 0;
	// タイマー
	private Chronometer mChronometer = null;
	
	private MediaPlayer startSound = null; 
	private MediaPlayer stopSound = null; 

//	final SlideViewGroup vg = new SlideViewGroup();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.record);

		// ロケーションマネージャのインスタンスを取得する
		// 位置情報の更新を受け取るように設定
		this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE); 
		
		// TODO タイマーは1時間を超える場合、別の実装を検討する必要があるかも
		// タイマーインスタンス取得
		this.mChronometer = (Chronometer) findViewById(R.id.chronometer);
		
		this.startSound = MediaPlayer.create(this, R.raw.start);
		this.stopSound = MediaPlayer.create(this, R.raw.stop);
		
//		SlideView sView1 = (SlideView)this.findViewById(R.id.myview1);
//		vg.add(sView1);     // 0

	}
	

	@Override
	protected void onStop() {
		super.onStop();
		// 位置情報の更新を止める 　念のため
		this.locationManager.removeUpdates(this); 
	}

	/**
	 * 開始／停止ボタンクリックした場合
	 * @param v
	 */
	public void onClicRecord(View v) {
		
		// ボタン押下で振動
		((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(50);

		if(!isStart) {

			// 開始音を鳴らす
			this.startSound.start();

			// 履歴名設定
			this.startTimeMillis = System.currentTimeMillis();

			// GPS情報書き込み用DBのオープン
			this.tracksDB = new TracksDBHelper(this);

			// GPS取得数リセット
			this.gpsCnt = 0;

			// 開始していなければ、GPS取得を開始
			this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
					0,		// 通知のための最小時間間隔
					0,		// 通知のための最小距離間隔
					this);	// 位置情報リスナー
			
			
			TextView textView = (TextView)this.findViewById(R.id.rec);
			textView.setText("■REC");
			textView.setTextColor(Color.RED);
			// RECを点滅させるの設定
			//AlphaAnimation(float fromAlpha, float toAlpha)
			AlphaAnimation alpha = new AlphaAnimation(1, 0);
			//1000msの間で
			alpha.setDuration(3600000);
			//10回繰り返す
			alpha.setInterpolator(new CycleInterpolator(3600));
			//アニメーションスタート
			textView.startAnimation(alpha);
			

			// ボタンを停止ボタンに切り替え
			Button button = (Button)this.findViewById(R.id.recordButton);
			button.setBackgroundDrawable((Drawable)this.getResources().getDrawable(R.drawable.stopbutton_stateful));

			// 記録中は履歴参照不可のため、ボタン無効にする
			Button viewTracksButton = (Button)this.findViewById(R.id.viewTracksbutton);
			viewTracksButton.setEnabled(false);

			// 開始フラグを変更
			this.isStart = true;
			
			// カウントアップスタート
			this.mChronometer.setBase(SystemClock.elapsedRealtime());
			this.mChronometer.start();
		}
		else {
			// 開始音を鳴らす
			this.stopSound.start();

			// 開始してれば、GPS取得を終了
			// 位置情報の更新を止める 
			this.locationManager.removeUpdates(this);
			
			// カウントアップストップ
			this.mChronometer.stop();
			
			//  開始時間からの差分で経過時間を算出
			SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss SSS");
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			String time = dateFormat.format(new Date(System.currentTimeMillis() - startTimeMillis));

			// 履歴名：記録開始日付（表示用）
			String recStart = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(startTimeMillis));

			// キー：記録開始日付＋時間（内部処理での使用用途）
			String key = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date(startTimeMillis));

			// 総移動距離を取得
			String distance = this.getDistance(key);
			
			Log.d("distance", distance);
			
			this.tracksDB.insertTracksData(recStart, key, time, "走行時間：" + time.substring(0, 8) + "    走行距離：" + distance + "m");
			
			// GPS情報書き込み用DBのログ出力
			dbLogPrint();

			TextView textView = (TextView)this.findViewById(R.id.rec);
			textView.setText(" ");

			// ボタンを開始ボタンに切り替え
			Button button = (Button)this.findViewById(R.id.recordButton);
//			button.setText("□ START");
//			button.setTextColor(Color.BLACK);
			button.setBackgroundDrawable((Drawable)this.getResources().getDrawable(R.drawable.startbutton_stateful));
			
			// 記録が終了したので、ボタン無効化解除
			Button viewTracksButton = (Button)this.findViewById(R.id.viewTracksbutton);
			viewTracksButton.setEnabled(true);

			this.isStart = false;		// 開始フラグを変更
			this.tracksDB.close();		// DBクローズ
			this.tracksDB = null;		// クリア
		}
		
	}
	
	
	public void dbLogPrint() {
		this.tracksDB.getReadableDatabase();
		Cursor cursor = this.tracksDB.getGPSDataList();

		this.startManagingCursor(cursor);
		if(cursor != null) {
			if (cursor.moveToFirst()) {
				int iRecCnt = cursor.getCount();
				for (int i = 0; i < iRecCnt; i++) {
					Log.d("DB RECORD",
							" ," + cursor.getString(0)
							+" ," + cursor.getString(1)
							+" ," + cursor.getString(2)
							+" ," + cursor.getString(3)
							+" ," + cursor.getString(4)
							+" ," + cursor.getString(5)
							+" ," + cursor.getString(6)
							);
					cursor.moveToNext();
				}
			}
			cursor.close();
		}
		this.tracksDB.close();
	}
	
	private String getDistance(String key){

		double distance = 0;

		TracksDBHelper tracksDB = new TracksDBHelper(this);
		Cursor cursor = tracksDB.getGPSDataList(key);

		this.startManagingCursor(cursor);
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

	
	@Override
	public void onLocationChanged(Location location) {
		this.gpsCnt++;
		this.tracksDB.insertTrackGPSData(
				new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date(startTimeMillis)), this.gpsCnt, location);
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO 自動生成されたメソッド・スタブ
	}

	/**
	 * 履歴参照ボタン押下時の処理
	 * 履歴一覧画面へ遷移する。
	 * @param v
	 */
	public void onClicDisplayTrackList(View v) {
		// ボタン押下で振動
		((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(50);

		Intent i = new Intent(this, TracksListActivity.class);
		this.startActivity(i);
		
	}

}
