package jp.takes.apps.recordtracker;

import jp.takes.apps.recordtracker.service.GPSCollectService;
import android.os.Vibrator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
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
	private Chronometer mChronometer = null;
	
	private MediaPlayer startSound = null; 
	private MediaPlayer stopSound = null; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.record);

		// タイマーインスタンス取得
		this.mChronometer = (Chronometer) findViewById(R.id.chronometer);
		
		// 開始音を生成
		this.startSound = MediaPlayer.create(this, R.raw.start);
		// 停止音を生成
		this.stopSound = MediaPlayer.create(this, R.raw.stop);
		
	}
	

	@Override
	protected void onStop() {
		super.onStop();
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

			// ボタンを停止ボタンに切り替え
			Button button = (Button)this.findViewById(R.id.recordButton);
			button.setBackgroundDrawable((Drawable)this.getResources().getDrawable(R.drawable.stopbutton_stateful));

			// 記録中は履歴参照不可のため、ボタン無効にする
			Button viewTracksButton = (Button)this.findViewById(R.id.viewTracksbutton);
			viewTracksButton.setEnabled(false);

			// GPS情報取得サービス開始
			this.startService(new Intent(this.getBaseContext(), GPSCollectService.class));

			// 開始フラグを変更
			this.isStart = true;
			
			// カウントアップスタート
			this.mChronometer.setBase(SystemClock.elapsedRealtime());
			this.mChronometer.start();
		}
		else {
			// 開始音を鳴らす
			this.stopSound.start();

			// GPS情報取得サービス停止
			this.stopService(new Intent(this.getBaseContext(), GPSCollectService.class));

			// カウントアップストップ
			this.mChronometer.stop();
			
			// REC表示停止
			TextView textView = (TextView)this.findViewById(R.id.rec);
			textView.setText(" ");

			// ボタンを開始ボタンに切り替え
			Button button = (Button)this.findViewById(R.id.recordButton);
			button.setBackgroundDrawable((Drawable)this.getResources().getDrawable(R.drawable.startbutton_stateful));
			
			// 記録が終了したので、ボタン無効化解除
			Button viewTracksButton = (Button)this.findViewById(R.id.viewTracksbutton);
			viewTracksButton.setEnabled(true);

			this.isStart = false;		// 開始フラグを変更
		}
		
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

}
