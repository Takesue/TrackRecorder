package jp.takes.apps.recordtracker.map;

import java.util.ArrayList;

import jp.takes.apps.recordtracker.R;
import jp.takes.apps.recordtracker.R.drawable;
import jp.takes.apps.recordtracker.R.id;
import jp.takes.apps.recordtracker.R.layout;
import jp.takes.apps.recordtracker.R.menu;
import jp.takes.apps.recordtracker.db.TracksDBHelper;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

/**
 * GoogleMap表示画面クラス
 * @author take
 *
 */
public class TrackMapActivity extends MapActivity {

	private MapController m_controller;
	
	private ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.simple_map);
		
		Intent i = this.getIntent();
		String key = i.getStringExtra("key");

		this.showMapTracks(key);

//		final MyLocationOverlay overlay = new MyLocationOverlay(
//				getApplicationContext(), map);
//		overlay.onProviderEnabled(LocationManager.GPS_PROVIDER); // GPS を使用する
//		overlay.enableMyLocation();
//		overlay.runOnFirstFix(new Runnable() {
//			@Override
//			public void run() {
//				map.getController().animateTo(overlay.getMyLocation()); // 現在位置を自動追尾する
//			}
//		});
//		map.getOverlays().add(overlay);
//		map.invalidate();


	}

	public void showMapTracks(String key) {
		
		// keyをもとにGPSデータを取得
		
		// GPS情報書き込み用DBのオープン
		TracksDBHelper tracksDB = new TracksDBHelper(this);
		Cursor cursor = tracksDB.getGPSDataList(key);
		
		// DBデータの取り出し
		this.startManagingCursor(cursor);
		if(cursor != null) {
			if (cursor.moveToFirst()) {
				int iRecCnt = cursor.getCount();
				for (int i = 0; i < iRecCnt; i++) {
					GeoPoint gp = 
							new GeoPoint((int)(Double.valueOf(cursor.getString(3))*1E6),
								         (int)(Double.valueOf(cursor.getString(4))*1E6));
					this.geoPoints.add(gp);
					cursor.moveToNext();
				}
			}
			else {
				cursor.close();
				tracksDB.close();
				Toast.makeText(this, "履歴に該当するGPS情報がありません", Toast.LENGTH_LONG).show();
				this.finish();
				return;
			}
			cursor.close();
		}
		tracksDB.close();

		LineOverlay lineOverlay = new LineOverlay(
				(GeoPoint[]) this.geoPoints.toArray(new GeoPoint[0]));

		// MapViewの設定
		MapView map = (MapView) findViewById(R.id.map);
		map.setClickable(true);
		map.setBuiltInZoomControls(true);
		map.getOverlays().add(lineOverlay);
		
		// ピンを置く
		Drawable pin = this.getResources().getDrawable(R.drawable.pin);
		PinItemizedOverlay pinOverlay = new PinItemizedOverlay(pin);
		// GPSの最初と最後にピンを設定する
		pinOverlay.addPoint(this.geoPoints.get(0));
		pinOverlay.addPoint(this.geoPoints.get(this.geoPoints.size() - 1));
		map.getOverlays().add(pinOverlay);

		// 画面のズーム、表示位置の調整
		this.m_controller = map.getController();
		this.m_controller.setZoom(16);
		this.m_controller.animateTo(this.geoPoints.get(this.geoPoints.size() - 1));

	}

	@Override
	protected void onStart() {
		super.onStart(); 
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
