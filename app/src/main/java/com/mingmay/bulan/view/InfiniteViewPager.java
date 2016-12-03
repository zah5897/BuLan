package com.mingmay.bulan.view;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
public class InfiniteViewPager extends ViewPager {
	private int realCount = 0;

	public InfiniteViewPager(Context context) {
		super(context);
	}

	public void setRealCount(int realCount) {
		this.realCount = realCount;
	}
    
	
	public InfiniteViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setAdapter(PagerAdapter adapter) {
		super.setAdapter(adapter);
		// offset first element so that we can scroll to the left
		setCurrentItem(0);
	}

	@Override
	public void setCurrentItem(int item) {
		// offset the current item to ensure there is space to scroll
		item = getOffsetAmount() + (item % getAdapter().getCount());
		super.setCurrentItem(item);

	}

	private int getOffsetAmount() {
		return realCount * 100;
	}

}
