package jp.takes.apps.recordtracker;

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
		((RecordActivity)context).mChronometer.setBase(Long.parseLong(startTime));
	}

}
