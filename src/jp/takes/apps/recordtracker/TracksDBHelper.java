package jp.takes.apps.recordtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

public class TracksDBHelper extends SQLiteOpenHelper {

	static final String name = "tracks.db";	// DB Name
	static final int version = 1;			// DB version
	static final CursorFactory factory = null;

	/* テーブル名 */
	public final String MAIN_TBL = "TRACKS_MAIN_TBL";
	public final String GPS_TBL = "TRACKS_GPS_TBL";

	public final String TRACKS_CODE = "tracks_code";
	public final String TRACKS_NAME = "tracks_name";
	public final String TRACKS_NUM = "tracks_num";
	public final String LATITUDE = "latitude";
	public final String LONGITUDE = "longitude";
	public final String ALTITUDE = "altitude";
	public final String ACCURACY = "accuracy";
	public final String TIME = "time";
	public final String SPEED = "speed";

	public final String KEY = "fkey";
	public final String TRACKS_TIME = "tracks_time";
	public final String DISTANCE = "distance";

	final String[] cols = {TRACKS_CODE, TRACKS_NAME, TRACKS_NUM, LATITUDE, LONGITUDE, TIME, SPEED};

	final String[] tracksCols = {android.provider.BaseColumns._ID, TRACKS_NAME, KEY, TRACKS_TIME, DISTANCE};

	// DBインスタンス
	private SQLiteDatabase db = null;
	
	// INSERT時に使用するContentValues
	public ContentValues contentValues = null;

	public TracksDBHelper(Context context) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		/* 走行履歴テーブル */
		String sql1 = "CREATE TABLE " + this.MAIN_TBL + " ("
				+ android.provider.BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ " tracks_name TEXT,"		// 履歴名
				+ " fkey TEXT,"				// fkey TRACKS_TBLの履歴名
				+ " tracks_time TEXT,"		// 移動時間
				+ " distance TEXT);";		// 移動距離(m/s)
		db.execSQL(sql1);

		/* GPS履歴情報テーブル */
		String sql2 = "CREATE TABLE " + this.GPS_TBL + " ("
				+ " tracks_code INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ " tracks_name TEXT,"		// 履歴名
				+ " tracks_num INTEGER,"	// 地点番号(記録開始からGPS情報取得毎にカウントアップ)
				+ " latitude TEXT,"			// Location情報 緯度 
				+ " longitude TEXT,"		// Location情報 経度
				+ " altitude TEXT,"			// Location情報 標高
				+ " accuracy TEXT,"			// Location情報 精度
				+ " time TEXT,"				// 時間
				+ " speed TEXT);";			// 速度(m/s)
		db.execSQL(sql2);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
	
	@Override
	public synchronized SQLiteDatabase getWritableDatabase() {
		if (db != null) {
			db.close();
			db = null;
		}
		db = super.getWritableDatabase();
		return db;
	}

	@Override
	public synchronized SQLiteDatabase getReadableDatabase() {
		if (db != null) {
			db.close();
			db = null;
		}
		db = super.getReadableDatabase();
		return db; 
	}
	
	/**
	 * 走行履歴情報テーブル用のインサートするレコード値を用意する。
	 * @param name
	 * @param startTime
	 * @param reqTime
	 * @param distance
	 */
	private void putTracksDataToContentValue(String name, String key, String reqTime, String distance) {
		
		contentValues = new ContentValues();
		contentValues.put(this.TRACKS_NAME, name);				// デフォルトでは日付
		contentValues.put(this.KEY, key);						// GPS情報との連携に使用するkey
		contentValues.put(this.TRACKS_TIME, reqTime);			// 所要時間
		contentValues.put(this.DISTANCE, distance);				// 移動距離
	}

	/**
	 * GPS履歴情報テーブル用のインサートするレコード値を用意する。
	 */
	private void putGPSDataToContentValue(String name, int num, Location location) {

		contentValues = new ContentValues();
		contentValues.put(this.TRACKS_NAME, name);
		contentValues.put(this.TRACKS_NUM, num);
		contentValues.put(this.LATITUDE, location.getLatitude());
		contentValues.put(this.LONGITUDE, location.getLongitude());
		contentValues.put(this.ALTITUDE, location.getAltitude());
		contentValues.put(this.ACCURACY, location.getAccuracy());
		contentValues.put(this.TIME, location.getTime());
		contentValues.put(this.SPEED, location.getSpeed());
//		contentValues.put(this.SPEED, 1.0);
		
		Log.d("DB RECORD",
				" name = " + name
				+" num = " + num
				+" getLatitude() = " + location.getLatitude()
				+" getLongitude() = " + location.getLongitude()
				+" getAltitude() = " + location.getAltitude()
				+" getAccuracy() = " + location.getAccuracy()
				+" getTime() = " + location.getTime()
				+" getSpeed() = " + location.getSpeed()
				);
	}

	// 走行履歴テーブルにレコードをインサートする
	public void insertTracksData(String name, String key, String reqTime, String distance) {
		this.getWritableDatabase();
		this.putTracksDataToContentValue(name, key, reqTime, distance);
		db.insertOrThrow(this.MAIN_TBL, null, contentValues);
		db.close();
		db = null;
		contentValues = null;
	}

	// GPS履歴情報テーブルにレコードをインサートする
	public void insertTrackGPSData(String name, int num, Location location) {
		this.getWritableDatabase();
		this.putGPSDataToContentValue(name, num, location);
		long tetNum = db.insertOrThrow(this.GPS_TBL, null, contentValues);
		if (tetNum == -1) {
			Log.d("insertTrackGPSData", "ERROR　tetNum = -1");
		}
		db.close();
		db = null;
		contentValues = null;
	}

	// GPS履歴情報テーブルから情報を取得
	public Cursor getTracksList() {
		this.getReadableDatabase();
		return db.query(this.MAIN_TBL, tracksCols, null, null, null, null, null);
	}

	// GPS履歴情報テーブルから情報を取得
	public Cursor getTracksList(String key) {
		this.getReadableDatabase();
		return db.query(this.MAIN_TBL, tracksCols, "_ID=" + key, null, null, null, null);
	}

	// GPS履歴情報テーブルから情報を取得
	public Cursor getGPSDataList() {
		this.getReadableDatabase();
		return db.query(this.GPS_TBL, cols, null, null, null, null, null);
	}

	// GPS履歴情報テーブルから情報を取得
	public Cursor getGPSDataList(String key) {
		this.getReadableDatabase();
		return db.query(this.GPS_TBL, cols, this.TRACKS_NAME + "=" + key, null, null, null, null);
	}
	
	// テーブルからkeyに該当する情報を削除
	public void deleteDataForKey(String key) {
		this.getWritableDatabase();
		db.delete(this.GPS_TBL, this.TRACKS_NAME + "=" + key, null);
		db.delete(this.MAIN_TBL, this.KEY + "=" + key, null);
		db.close();
		db = null;
	}

}
