package com.mingmay.bulan.view;

import android.content.Context;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ListView;

public class HomeListView extends ListView {
	private int mLastMotionY;
	private int mLastMotionX;
	private int mTouchSlop;

	public HomeListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		final ViewConfiguration configuration = ViewConfiguration.get(context);
		mTouchSlop = ViewConfigurationCompat
				.getScaledPagingTouchSlop(configuration);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent e) {
		int y = (int) e.getRawY();
		int x = (int) e.getRawX();
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// 首先拦截down事件,记录y坐标
			mLastMotionY = y;
			mLastMotionX = x;
			break;
		case MotionEvent.ACTION_MOVE:
			// deltaY > 0 是向下运动,< 0是向上运动
			int deltaY = y - mLastMotionY;
			int deltaX = x - mLastMotionX;
			final float xDiff = Math.abs(deltaX);
			final float yDiff = Math.abs(deltaY);
			return !(yDiff < mTouchSlop || xDiff >= yDiff);
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			break;
		}
		return false;

	}
}
