package jp.takes.apps.recordtracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

/**
 * GPS関連のDB情報アクセス用クラス
 * @author take
 *
 */
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

	/**
	 * コンストラクタ
	 * @param context
	 */
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
		if (this.db != null) {
			this.db.close();
			this.db = null;
		}
		return super.getWritableDatabase();
	}

	@Override
	public synchronized SQLiteDatabase getReadableDatabase() {
		if (this.db != null) {
			this.db.close();
			this.db = null;
		}
		return super.getReadableDatabase();
	}
	
	/**
	 * 走行履歴情報テーブル用のインサートするレコード値を用意する。
	 * @param name
	 * @param startTime
	 * @param reqTime
	 * @param distance
	 */
	private void putTracksDataToContentValue(String name, String key, String reqTime, String distance) {
		
		this.contentValues = new ContentValues();
		this.contentValues.put(this.TRACKS_NAME, name);				// デフォルトでは日付
		this.contentValues.put(this.KEY, key);						// GPS情報との連携に使用するkey
		this.contentValues.put(this.TRACKS_TIME, reqTime);			// 所要時間
		this.contentValues.put(this.DISTANCE, distance);			// 移動距離
	}

	/**
	 * GPS履歴情報テーブル用のインサートするレコード値を用意する。
	 */
	private void putGPSDataToContentValue(String name, int num, Location location) {

		this.contentValues = new ContentValues();
		this.contentValues.put(this.TRACKS_NAME, name);
		this.contentValues.put(this.TRACKS_NUM, num);
		this.contentValues.put(this.LATITUDE, location.getLatitude());
		this.contentValues.put(this.LONGITUDE, location.getLongitude());
		this.contentValues.put(this.ALTITUDE, location.getAltitude());
		this.contentValues.put(this.ACCURACY, location.getAccuracy());
		this.contentValues.put(this.TIME, location.getTime());
		this.contentValues.put(this.SPEED, location.getSpeed());
		
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
		this.db = this.getWritableDatabase();
		this.putTracksDataToContentValue(name, key, reqTime, distance);
		this.db.insertOrThrow(this.MAIN_TBL, null, this.contentValues);
		this.db.close();
		this.db = null;
		this.contentValues = null;
	}

	// GPS履歴情報テーブルにレコードをインサートする
	public void insertTrackGPSData(String name, int num, Location location) {
		this.db = this.getWritableDatabase();
		this.putGPSDataToContentValue(name, num, location);
		long retVal = this.db.insertOrThrow(this.GPS_TBL, null, this.contentValues);
		if (retVal == -1) {
			Log.d("insertTrackGPSData", "ERROR　Return Value = -1");
		}
		this.db.close();
		this.db = null;
		this.contentValues = null;
	}

	// GPS履歴情報テーブルから情報を取得
	public Cursor getTracksList() {
		this.db = this.getReadableDatabase();
		return this.db.query(this.MAIN_TBL, this.tracksCols, null, null, null, null, null);
	}

	// GPS履歴情報テーブルから情報を取得
	public Cursor getTracksList(String key) {
		this.db = this.getReadableDatabase();
		return this.db.query(this.MAIN_TBL, this.tracksCols, "_ID=" + key, null, null, null, null);
	}

	// GPS履歴情報テーブルから情報を取得
	public Cursor getGPSDataList() {
		this.db = this.getReadableDatabase();
		return this.db.query(this.GPS_TBL, this.cols, null, null, null, null, null);
	}

	// GPS履歴情報テーブルから情報を取得
	public Cursor getGPSDataList(String key) {
		this.db = this.getReadableDatabase();
		return this.db.query(this.GPS_TBL, this.cols, this.TRACKS_NAME + "=" + key, null, null, null, null);
	}
	
	// テーブルからkeyに該当する情報を削除
	public void deleteDataForKey(String key) {
		this.db = this.getWritableDatabase();
		this.db.delete(this.GPS_TBL, this.TRACKS_NAME + "=" + key, null);
		this.db.delete(this.MAIN_TBL, this.KEY + "=" + key, null);
		this.db.close();
		this.db = null;
	}

}
