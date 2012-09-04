package jp.takes.apps.recordtracker.widget;

import java.util.Date;
import java.util.List;

import jp.takes.apps.recordtracker.GpsInfoBroadcastReceiver;
import jp.takes.apps.recordtracker.R;
import jp.takes.apps.recordtracker.service.GPSCollectService;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.RemoteViews;

public class ServiceWidget extends Service {

	private final String BUTTON_CLICK_ACTION = "BUTTON_CLICK_ACTION";

	private final String FIRST_ACTION = "FIRST_ACTION";
	
	// GPS情報収集サービス用のブロードキャストレシーバ
	private GpsInfoBroadcastReceiver receiver = null;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	// ボタンに表示している文字　START　or STOP
	private String buttonDisp = "";
	
	// タイマー
	public Long chronoTime = null;

	
	
	@Override
	public void onCreate() {
		super.onCreate();
		// ブロードキャストレシーバの登録
		this.registBroadcastReceiver();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// ブロードキャストレシーバの登録解除
		this.unRegistBroadcastReceiver();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		Log.d("HelloAndroidWidietProvider", "onStart " + this.buttonDisp);

		
		Context context = this.getApplicationContext();

		RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.widget);

		boolean isRunning = this.isServiceRunning(this, GPSCollectService.class);
		if (this.FIRST_ACTION.equals(intent.getAction())) {
			// 最初にwidget画面が表示された場合
			Log.d("HelloAndroidWidietProvider", "onStart FIRST_ACTION");
			
			if (isRunning) {
				// GPSサービスが起動状態の場合
				// GPS情報取得サービスを呼び出し、クロノメータの基礎時間をブロードキャスト送信してもらう。
				Intent serviceIntent = new Intent(this.getBaseContext(), GPSCollectService.class);
				serviceIntent.setAction("FROM_WIDGET");
				this.startService(serviceIntent);
			}

		}
		else if (this.BUTTON_CLICK_ACTION.equals(intent.getAction())) {
			// ウィジェットのボタンがクリックされた場合
//			remoteViews.setTextViewText(R.id.textView1, new Date().toGMTString());
			
			Log.d("HelloAndroidWidietProvider", "onStart " + this.buttonDisp + " isRunning=" + isRunning);
			
			if((isRunning) && ("STOP".equals(this.buttonDisp))) {
				// GPSサービスが起動状態でウィジェットのSTOPボタンが押下された場合
				// GPS情報取得サービス停止
				Intent serviceIntent = new Intent(this.getBaseContext(), GPSCollectService.class);
				serviceIntent.setAction("FROM_WIDGET");
				this.stopService(serviceIntent);
			}
			else if ((!isRunning) && ("START".equals(this.buttonDisp))){
				// GPSサービスが停止状態でウィジェットのSTARTボタンが押下された場合
				// GPS情報取得サービス開始
				Intent serviceIntent = new Intent(this.getBaseContext(), GPSCollectService.class);
				serviceIntent.setAction("FROM_WIDGET");
				this.startService(serviceIntent);
			}
			else {
				// 上記以外は何もしない
			}
		}

		if(this.isServiceRunning(this, GPSCollectService.class)) {
			// サービスは起動しているので、STOPボタンを表示
			this.buttonDisp = "STOP";

			// タイマーの開始
			remoteViews.setChronometer(R.id.widgetChronometer, 
					(chronoTime == null ) ? SystemClock.elapsedRealtime() : this.chronoTime, null, true);
		}
		else {
			// サービスは停止しているので、STARTボタンを表示
			this.buttonDisp = "START";

			// タイマーの停止
			remoteViews.setChronometer(R.id.widgetChronometer, SystemClock.elapsedRealtime(), null, false);
			this.chronoTime = null;
		}
		remoteViews.setTextViewText(R.id.widgetButton, this.buttonDisp);

		//ボタンをクリックしたらPendingIntentによりサービスが発動するよう設定する。
		Intent buttonIntent = new Intent();
		buttonIntent.setAction(this.BUTTON_CLICK_ACTION);
		PendingIntent pendingIntent = PendingIntent.getService(context, 0, buttonIntent, 0);

		//widgetのボタンクリックイベントに呼び出したいIntentを設定する
		remoteViews.setOnClickPendingIntent(R.id.widgetButton, pendingIntent);

		// widgetの更新
		ComponentName thisWidget = new ComponentName(context, AppWidget.class);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(thisWidget, remoteViews);

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
