package com.mingmay.bulan.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

@SuppressLint("SetJavaScriptEnabled")
public class MyWebView extends WebView {
	private OnScrollChangedListener onScrollChangeListener;

	public MyWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		WebSettings webSettings = getSettings();
		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
		webSettings.setAppCacheEnabled(true);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setAllowFileAccess(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setDefaultTextEncodingName("utf-8");
		// webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		setHorizontalScrollBarEnabled(false);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		// TODO Auto-generated method stub
		if (onScrollChangeListener != null) {
			onScrollChangeListener.onScrollChanged(l, t, oldl, oldt);
		}
	}

	public void setOnScrollChangeListener(
			OnScrollChangedListener onScrollChangeListener) {
		this.onScrollChangeListener = onScrollChangeListener;
	}

	public interface OnScrollChangedListener {
		public void onScrollChanged(int l, int t, int oldl, int oldt);
	}
}
