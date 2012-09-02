package jp.takes.apps.recordtracker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Scroller;

public class ScrollView extends Button {

    // Scrollerの実装
    private Scroller mScroller;

	public ScrollView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mScroller = new Scroller(context);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
			animationStart();
		}
		return true;
	}

	@Override
	public void computeScroll() {
		if (this.mScroller.computeScrollOffset()) {
			// Scrollerから移動位置を決定する
			scrollTo(this.mScroller.getCurrX(), this.mScroller.getCurrY());
			postInvalidate();
		}
	}

	private void animationStart() {
		// 3000msで、座標を右下へ移動させる
		this.mScroller.startScroll(this.mScroller.getCurrX(), this.mScroller.getCurrY(), 300,
				0, 3000);
		invalidate();
	}	

}
