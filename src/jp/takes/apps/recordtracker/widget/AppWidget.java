package jp.takes.apps.recordtracker.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AppWidget extends AppWidgetProvider {

	// Action name ウィジェットからGPSサービスを起動する場合
	public static final String FIRST_ACTION = "FIRST_ACTION";

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// TODO 自動生成されたメソッド・スタブ
		super.onDeleted(context, appWidgetIds);
		Log.d("HelloAndroidWidietProvider", "onDeleted");
	}

	@Override
	public void onDisabled(Context context) {
		// TODO 自動生成されたメソッド・スタブ
		super.onDisabled(context);
		Log.d("HelloAndroidWidietProvider", "onDisabled");
	}

	@Override
	public void onEnabled(Context context) {
		// TODO 自動生成されたメソッド・スタブ
		super.onEnabled(context);
		Log.d("HelloAndroidWidietProvider", "onEnabled");
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO 自動生成されたメソッド・スタブ
		super.onReceive(context, intent);
		Log.d("HelloAndroidWidietProvider", "onReceive");
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Log.d("HelloAndroidWidietProvider", "onUpdate");
		
		Intent intent = new Intent(context, ServiceWidget.class);
		intent.setAction(AppWidget.FIRST_ACTION);
		context.startService(intent);

	}

	
}
