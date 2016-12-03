package com.mingmay.bulan.view;

import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.widget.Toast;

/**
 * You should always use a subclass of this by overriding
 * {@link #setDisplayContent} method.
 */
public class ViewPagerOnPageChangeListener implements OnPageChangeListener {

	private ViewPager mPager;
	private int realSize;

	public ViewPagerOnPageChangeListener(ViewPager pager, int realSize) {
		this.mPager = pager;
		this.realSize = realSize;
	}

	@Override
	public void onPageSelected(int position) {
		int realPosition = position % realSize;
		setDisplayContent(realPosition);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	/**
	 * Do what you want when another page is selected.
	 * 
	 * @param realPosition
	 *            Position index of the new selected page.
	 */
	public void setDisplayContent(int realPosition) {
		Toast.makeText(mPager.getContext(), "Page " + realPosition,
				Toast.LENGTH_SHORT).show();
	}

}
