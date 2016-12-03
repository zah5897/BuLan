package com.mingmay.bulan.view;

import android.content.Intent;
import android.net.Uri;
import android.text.style.ClickableSpan;
import android.view.View;

public class MyURLSpan extends ClickableSpan {
	private String mUrl;

	public MyURLSpan(String url) {
		mUrl = url;
	}

	@Override
	public void onClick(View widget) {
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		Uri content_url = Uri.parse(mUrl);
		intent.setData(content_url);
		widget.getContext().startActivity(intent);

	}
}
