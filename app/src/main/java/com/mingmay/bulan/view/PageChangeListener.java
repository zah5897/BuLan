package com.mingmay.bulan.view;

import android.support.v4.view.ViewPager;

public abstract class PageChangeListener extends
		ViewPagerOnPageChangeListener {
	public PageChangeListener(ViewPager pager,int realSize) {
		super(pager, realSize);
	}

	@Override
	public abstract void setDisplayContent(int realPosition);  
}
