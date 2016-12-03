package com.mingmay.bulan.view;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewParent;

public class ViewPagerOnTouchListener implements OnTouchListener {

	private int mNum;

	public ViewPagerOnTouchListener(int mNum) {
		this.mNum = mNum;
	}

	private float lastTime;
	private ItemClickListener listener;

	public void setListener(ItemClickListener listener) {
		this.listener = listener;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (mNum < 2)
			return true;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			lastTime = event.getX();
		case MotionEvent.ACTION_MOVE:
			if(v!=null){
				ViewParent parent = v.getParent();
				if(parent!=null){
					parent.requestDisallowInterceptTouchEvent(true);	
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			float temp = event.getX();
			if (Math.abs(lastTime-temp)<=10) {
				if (listener != null) {
					listener.itemClick();
				}
			}
		case MotionEvent.ACTION_CANCEL:
			if (v != null) {
				ViewParent parent = v.getParent();
				if(parent!=null){
					parent.requestDisallowInterceptTouchEvent(false);
				}
			}
			break;
		}
		return false;
	}

}
