package jp.takes.apps.recordtracker;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;

/**
 * 指定座標にインスタンス生成時に指定した画像をマーカーとして置く
 * @author take
 *
 */
public class PinItemizedOverlay extends ItemizedOverlay<PinOverlayItem> {
	
	private List<GeoPoint> points = new ArrayList<GeoPoint>();

	/**
	 * コンストラクタ
	 * @param defaultMarker  マーカーの画像
	 */
	public PinItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}

	@Override
	protected PinOverlayItem createItem(int i) {
		GeoPoint point = this.points.get(i);
		return new PinOverlayItem(point);
	}

	@Override
	public int size() {
		return this.points.size();
	}

	public void addPoint(GeoPoint point) {
		this.points.add(point);
		populate();
	}

	public void clearPoint() {
		this.points.clear();
		populate();
	}

}
