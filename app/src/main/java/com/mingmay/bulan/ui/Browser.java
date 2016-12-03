package com.mingmay.bulan.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.mingmay.bulan.R;
import com.mingmay.bulan.view.MyWebView;

public class Browser extends Activity {
	private MyWebView browser;
	private String url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_browser2);

		String title = getIntent().getStringExtra("title");

		TextView titleView = (TextView) findViewById(R.id.chat_title_text);

		titleView.setText(title);

		findViewById(R.id.back).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		url = getIntent().getStringExtra("url");
		browser = (MyWebView) findViewById(R.id.browser);
		browser.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				view.loadUrl(url);
				return true;
			}
		});
		browser.setDownloadListener(new DownloadListener() {

			@Override
			public void onDownloadStart(String url, String userAgent,
					String contentDisposition, String mimetype,
					long contentLength) {
				Uri uri = Uri.parse(url);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
				finish();
			}
		});
		browser.loadUrl(url);
	}

	@Override
	public void onBackPressed() {
		if (browser.canGoBack()) {
			browser.goBack();
			return;
		}
		super.onBackPressed();
	}

	public void back(View view) {
		finish();
	}
}
