package jp.takes.apps.recordtracker;

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


public class TracksListActivity extends ListActivity implements OnItemLongClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.trackslist);
		
		ListView list = (ListView)this.findViewById(android.R.id.list);
		list.setOnItemLongClickListener(this);
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		this.showViewList();
	}

	private void showViewList () {
		
		TracksDBHelper tracksDB = new TracksDBHelper(this);

		Cursor cursor = tracksDB.getTracksList();
		this.startManagingCursor(cursor);

		if(cursor != null) {
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
		
		TracksDBHelper tracksDB = new TracksDBHelper(this);
		Cursor cursor = tracksDB.getTracksList(String.valueOf(id));
		
		this.startManagingCursor(cursor);
		int idx = cursor.getColumnIndex(tracksDB.KEY);
		cursor.moveToFirst();
		Intent i = new Intent(this, TrackMapActivity.class);
		i.putExtra("key", cursor.getString(idx));
		this.startActivity(i);
		tracksDB.close();

	}


	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long id) {
		final TracksListActivity currentAct = this;
		final TracksDBHelper tracksDB = new TracksDBHelper(this);
		Cursor cursor = tracksDB.getTracksList(String.valueOf(id));
		this.startManagingCursor(cursor);
		cursor.moveToFirst();
		final String key = cursor.getString(cursor.getColumnIndex(tracksDB.KEY));
		String name = cursor.getString(cursor.getColumnIndex(tracksDB.TRACKS_NAME));

		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("履歴削除");
		ab.setMessage("[" + name +"]を削除しますか？" );
		ab.setPositiveButton(android.R.string.ok, 
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						tracksDB.deleteDataForKey(key);
						tracksDB.close();
						// 削除した状態で再表示
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
