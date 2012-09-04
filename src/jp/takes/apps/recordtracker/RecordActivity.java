package jp.takes.apps.recordtracker;

import java.util.List;

import jp.takes.apps.recordtracker.service.GPSCollectService;
import android.os.SystemClock;
import android.os.Vibrator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.CycleInterpolator;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

/**
 * GPS情報取得開始／停止画面
 * @author take
 *
 */
public class RecordActivity extends Activity {

	// GPS開始フラグ
	private boolean isStart = false;
	// タイマー
	public Chronometer mChronometer = null;
	
	// GPS情報収集サービス用のブロードキャストレシーバ
	private GpsInfoBroadcastReceiver receiver = null;
	
	private MediaPlayer startSound = null; 
	private MediaPlayer stopSound = null; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.record);


		// タイマーインスタンス取得
		this.mChronometer = (Chronometer) findViewById(R.id.chronometer);
//		this.mChronometer.setBase(SystemClock.elapsedRealtime());
		
		// 開始音を生成
		this.startSound = MediaPlayer.create(this, R.raw.start);
		// 停止音を生成
		this.stopSound = MediaPlayer.create(this, R.raw.stop);

		// ブロードキャストレシーバの登録
		this.registBroadcastReceiver();

		boolean isRunning = this.isServiceRunning(this, GPSCollectService.class);
		if(isRunning) {
			this.startView();
		}
	}
	

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		// ブロードキャストレシーバの登録解除
		this.unRegistBroadcastReceiver();
	}

	/**
	 * 開始／停止ボタンクリックした場合
	 * @param v
	 */
	public void onClicRecord(View v) {
		
		// ボタン押下で振動
		((Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);

		if(!this.isStart) {
			// 開始音を鳴らす
			this.startSound.start();
			this.startView();
		}
		else {
			// 停止音を鳴らす
			this.stopSound.start();
			this.stopView();
		}
	}
	
	private void startViewRecText() {
		// REC表示開始
		TextView textView = (TextView)this.findViewById(R.id.rec);
		textView.setText("■REC");
		textView.setTextColor(Color.RED);
		// RECを点滅させる設定
		AlphaAnimation alpha = new AlphaAnimation(1, 0);
		//1000msの間で
		alpha.setDuration(3600000);
		//10回繰り返す
		alpha.setInterpolator(new CycleInterpolator(3600));
		//アニメーションスタート
		textView.startAnimation(alpha);
	}
	
	private void stopViewRecText() {
		// REC表示停止
		TextView textView = (TextView)this.findViewById(R.id.rec);
		textView.setText(" ");
	}
	
	/**
	 * 開始状態画面を設定
	 */
	private void startView() {
	
		// ボタンを停止ボタンに切り替え
		Button button = (Button)this.findViewById(R.id.recordButton);
		button.setBackgroundDrawable((Drawable)this.getResources().getDrawable(R.drawable.stopbutton_stateful));

		// REC表示開始
		this.startViewRecText();
		
		// 記録中は履歴参照不可のため、ボタン無効にする
		Button viewTracksButton = (Button)this.findViewById(R.id.viewTracksbutton);
		viewTracksButton.setEnabled(false);

		// GPS情報取得サービス開始
		Intent serviceIntent = new Intent(this.getBaseContext(), GPSCollectService.class);
		serviceIntent.setAction("FROM_ACTIVITY");
		this.startService(serviceIntent);
//		boolean isRunning = isServiceRunning(this, GPSCollectService.class);
//		if(!isRunning) {
//			this.startService(new Intent(this.getBaseContext(), GPSCollectService.class));
//		}

		// 開始フラグを変更
		this.isStart = true;
		
		// カウントアップスタート
//		this.mChronometer.setBase(SystemClock.elapsedRealtime());
		this.mChronometer.start();
		
	}
	
	private void stopView() {
		// GPS情報取得サービス停止
		boolean isRunning = isServiceRunning(this, GPSCollectService.class);
		if(isRunning) {
			this.stopService(new Intent(this.getBaseContext(), GPSCollectService.class));
		}

		// カウントアップストップ
		this.mChronometer.stop();
		
		// REC表示停止
		this.stopViewRecText();

		// ボタンを開始ボタンに切り替え
		Button button = (Button)this.findViewById(R.id.recordButton);
		button.setBackgroundDrawable((Drawable)this.getResources().getDrawable(R.drawable.startbutton_stateful));
		
		// 記録が終了したので、ボタン無効化解除
		Button viewTracksButton = (Button)this.findViewById(R.id.viewTracksbutton);
		viewTracksButton.setEnabled(true);

		this.isStart = false;		// 開始フラグを変更

	}
	
	/**
	 * 履歴参照ボタン押下時の処理
	 * 履歴一覧画面へ遷移する。
	 * @param v
	 */
	public void onClicDisplayTrackList(View v) {
		// ボタン押下で振動
		((Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);

		// 履歴一覧画面へ遷移
		this.startActivity(new Intent(this, TracksListActivity.class));
		
	}
	
	
	/**
	 * サービスが実行中であるか確認
	 * @param c　コンテキスト
	 * @param cls 状態を確認したいサービスクラス
	 * @return true:実行中　false:停止状態
	 */
	public boolean isServiceRunning(Context c, Class<?> cls) {
		ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> runningService = am.getRunningServices(Integer.MAX_VALUE);

		for (RunningServiceInfo i : runningService) {
			Log.d("isServiceRunning", "service: " + i.service.getClassName() + " : "
					+ i.started);
			if (cls.getName().equals(i.service.getClassName())) {
				Log.d("isServiceRunning", "running");
				return true;
			}
		}
		return false;
	}

	/**
	 * ブロードキャストレシーバの登録
	 */
	private void registBroadcastReceiver() {
		this.receiver = new GpsInfoBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("SEND_START_TIME");
		this.registerReceiver(this.receiver, intentFilter);
	}

	/**
	 * ブロードキャストレシーバの登録解除
	 */
	private void unRegistBroadcastReceiver() {
		this.unregisterReceiver(this.receiver);
	}

}
