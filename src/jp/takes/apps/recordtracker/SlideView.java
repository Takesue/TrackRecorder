package jp.takes.apps.recordtracker;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * アニメーションで開閉するコンテナ この View は必ずレイアウトファイルから呼び出すこと。
 * 
 * Attributes
 * 
 * init_opened (boolean) 起動時の初期状態。 false : 閉じた状態で起動する。（初期値） true : 開いた状態で起動する。
 * 
 * location (String) 設置場所。パネルのスライドの開始方向。 right : 右から表示する。（初期値） bottom : 下から表示する。
 * left : 左から表示する。 top : 上から表示する。
 * 
 * duration (Integer) 全開、全閉までの時間。(ms) 初期値は 1000 ms
 * 
 * distance (Dimension) パネルの移動量。単位は dp で指定する。 基本的に
 * android:layout_width（right/left の場合）または android:layout_height (top/bottom
 * の場合）と同じ値を設定する。 この値を違う値にしてパネルを完全には隠れないようにすることもできる。 このパラメータは必ず指定すること。
 */

public class SlideView extends FrameLayout {
	private static String RIGHT = "right";
	private static String BOTTOM = "bottom";
	private static String LEFT = "left";
	@SuppressWarnings("unused")
	private static String TOP = "top";
	private String location = RIGHT;
	// 設置場所
	private int duration = 1000;
	// 全開までの時間
	private int distance = 0;
	// 移動量
	private boolean init_opened = false;
	// 初期状態はオープンか？
	private Scroller mScroller;
	private boolean opened = true;
	// パネルが開いているかどうか？
	private int startX = 0;
	private int startY = 0;
	private int finalX = 0;
	private int finalY = 0;

	public SlideView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mScroller = new Scroller(context);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.SlideView);
		init_opened = a.getBoolean(R.styleable.SlideView_init_opened, init_opened);
		location = a.getString(R.styleable.SlideView_location);
		duration = a.getInt(R.styleable.SlideView_duration, duration);
		distance = (int) a.getDimension(R.styleable.SlideView_distance,
				(float) distance);

		a.recycle();
		if (location.equals(RIGHT)) {
			startX = -distance;
		} else if (location.equals(BOTTOM)) {
			startY = -distance;
		} else if (location.equals(LEFT)) {
			startX = distance;
		} else {
			// TOP
			startY = distance;
		}

		/*
		 * 初期状態がオープンならアニメーション無しで開く。
		 */
		if (!init_opened) {
			close(false);
			opened = false;
		}
	}

	public boolean isOpened() {
		return opened;
	} 
	
	
	/*
	 * パネルが開いていれば閉じる。閉じていれば開く。 開閉途中であれば動作をキャンセルして元に戻す。
	 */
	public synchronized void toggle() {
		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
			int currX = mScroller.getCurrX();
			int currY = mScroller.getCurrY();
			if (opened) {
				mScroller.startScroll(currX, currY, (startX - currX),
						(startY - currY));
				opened = false;
			} else {
				mScroller.startScroll(currX, currY, (finalX - currX),
						(finalY - currY));
				opened = true;
			}
			invalidate();
			return;
		}
		if (opened) {
			close(true);
			opened = false;
		} else {
			open(true);
			opened = true;
		}
	}

	private void open(boolean anim) {
		mScroller.startScroll(startX, startY, (finalX - startX),
				(finalY - startY), anim ? duration : 0);
		invalidate();
	}

	private void close(boolean anim) {
		mScroller.startScroll(finalX, finalY, (startX - finalX),
				(startY - finalY), anim ? duration : 0);
		invalidate();
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
	}

//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		final int action = event.getAction();
//		if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
//			this.toggle();
//		}
//		return true;
//	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		return opened;
	}
}
