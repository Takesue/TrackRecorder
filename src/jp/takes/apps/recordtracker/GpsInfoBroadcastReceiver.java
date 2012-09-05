package jp.takes.apps.recordtracker;

import jp.takes.apps.recordtracker.widget.ServiceWidget;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class GpsInfoBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		String startTime = bundle.getString("starttime");
		
		Log.d("GpsInfoBroadcastReceiver", startTime);
		
		// サービスから受け取った時間をクロノメータの開始時間に設定
		if (context instanceof RecordActivity) {
			((RecordActivity)context).mChronometer.setBase(Long.parseLong(startTime));
			
			// 画面表示をサービスの状態（起動／停止）に合わせる
			((RecordActivity)context).refreshView();

		}
		if (context instanceof ServiceWidget) {
			Log.d("HelloAndroidWidietProvider", "GpsInfoBroadcastReceiver startTime=" + startTime);

			((ServiceWidget)context).chronoTime = Long.parseLong(startTime);
			((ServiceWidget)context).onStart(intent, this.getResultCode());
			
		}
	}

}
