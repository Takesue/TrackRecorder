package jp.takes.apps.recordtracker.map;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

/**
 * 
 * @author take
 *
 */
public class PinOverlayItem extends OverlayItem {

	public PinOverlayItem(GeoPoint point) {
		super(point, "", "");
	}

}
