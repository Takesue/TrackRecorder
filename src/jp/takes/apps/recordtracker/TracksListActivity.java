package jp.takes.apps.recordtracker;

import jp.takes.apps.recordtracker.db.TracksDBHelper;
import jp.takes.apps.recordtracker.map.TrackMapActivity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

/**
 * 履歴一覧画面のクラス
 * @author take
 *
 */
public class TracksListActivity extends ListActivity implements OnItemLongClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.trackslist);
		
		ListView list = (ListView)this.findViewById(android.R.id.list);
		
		// 長押しのリスナを設定
		list.setOnItemLongClickListener(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		this.showViewList();
	}

	
	/**
	 * リストに履歴情報を設定する
	 */
	private void showViewList () {
		
		// DBアクセス用インスタンス生成
		TracksDBHelper tracksDB = new TracksDBHelper(this);
		// 履歴一覧情報をDBから取得する
		Cursor cursor = tracksDB.getTracksList();
		
		if(cursor != null) {
			// カーソルの管理を開始
			this.startManagingCursor(cursor);
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(
					this, android.R.layout.simple_list_item_2,
					cursor,
					new String[] {tracksDB.TRACKS_NAME, tracksDB.DISTANCE},
					new int[] {android.R.id.text1, android.R.id.text2});
			this.setListAdapter(adapter);
		}
		tracksDB.close();
	}
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		// 押されたアイテムを元に、対象の情報をDBから抽出する。
		TracksDBHelper tracksDB = new TracksDBHelper(this);
		Cursor cursor = tracksDB.getTracksList(String.valueOf(id));

		// カーソルの管理を開始　いる？
		this.startManagingCursor(cursor);
		cursor.moveToFirst();
		int idx = cursor.getColumnIndex(tracksDB.KEY);
		
		//  * GoogleMap表示画面へ遷移
		Intent i = new Intent(this, TrackMapActivity.class);
		i.putExtra("key", cursor.getString(idx));
		this.startActivity(i);
		tracksDB.close();

	}


	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long id) {
		// 履歴アイテムを長押しされた場合、押下された情報の削除処理をおこなう。
		final TracksListActivity currentAct = this;
		final TracksDBHelper tracksDB = new TracksDBHelper(this);
		Cursor cursor = tracksDB.getTracksList(String.valueOf(id));
		this.startManagingCursor(cursor);
		cursor.moveToFirst();
		final String key = cursor.getString(cursor.getColumnIndex(tracksDB.KEY));
		String name = cursor.getString(cursor.getColumnIndex(tracksDB.TRACKS_NAME));

		// 削除確認のポップアップ生成
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("履歴削除");
		ab.setMessage("[" + name +"]を削除しますか？" );
		ab.setPositiveButton(android.R.string.ok, 
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 「はい」を選択されたら削除する
						tracksDB.deleteDataForKey(key);
						tracksDB.close();
						// 1行削除したリストを再表示
						currentAct.showViewList();
						Toast.makeText(currentAct, "削除しました。", Toast.LENGTH_SHORT).show();

					}
		});

		ab.setNegativeButton(android.R.string.cancel, 
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
		});
		ab.show();
		
		return true;
	}
	
	
	

}
