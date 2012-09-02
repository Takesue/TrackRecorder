package jp.takes.apps.recordtracker;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;


public class LineOverlay extends Overlay {

	private GeoPoint[] geoList = null;

	public LineOverlay(GeoPoint[] _geoList) {
		this.geoList = _geoList;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);

		if (!shadow) {
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setStyle(Paint.Style.STROKE);
			paint.setAntiAlias(true);
			paint.setStrokeWidth(3);
			paint.setColor(Color.RED);

			Path path = new Path();
			Projection projection = mapView.getProjection();
			
			if (geoList.length >= 2) {
				
				// 開始位置
				Point pxStart = projection.toPixels(geoList[0], null);
				path.moveTo(pxStart.x, pxStart.y);
				
				for (int i = 1; i < geoList.length; i++) {
					Point pxPoint = projection.toPixels(geoList[i], null);
					path.lineTo(pxPoint.x, pxPoint.y);
				}
			}
			
			

			canvas.drawPath(path, paint);
		}
	}
	
}
