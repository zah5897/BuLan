/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mingmay.bulan.ui;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;

import com.mingmay.bulan.R;
import com.mingmay.bulan.base.BaseActivity;
import com.mingmay.bulan.util.ImageLoadUtil;
import com.mingmay.bulan.view.photoview.PhotoView;

/**
 * 下载显示大图
 * 
 */
public class ShowBigImage extends BaseActivity {
	private static final String TAG = "ShowBigImage";
	private ProgressDialog pd;
	private PhotoView image;
	private int default_res = R.drawable.default_image;
	private String localFilePath;
	private Bitmap bitmap;
	private boolean isDownloaded;
	private ProgressBar loadLocalPb;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_big_image);
		image = (PhotoView) findViewById(R.id.image);
		loadLocalPb = (ProgressBar) findViewById(R.id.pb_load_local);
		default_res = getIntent().getIntExtra("default_image",
				R.drawable.headlogo);
		String uri = getIntent().getStringExtra("localPath");
		String remotepath = getIntent().getExtras().getString("remotepath");
		String secret = getIntent().getExtras().getString("secret");

		// 本地存在，直接显示本地的图片
		if (uri != null && new File(uri).exists()) {
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			// int screenWidth = metrics.widthPixels;
			// int screenHeight =metrics.heightPixels;
			// bitmap = ImageCache.getInstance().get(uri.getPath());
			// if (bitmap == null) {
			// LoadLocalBigImgTask task = new LoadLocalBigImgTask(this,
			// uri.getPath(), image, loadLocalPb,
			// ImageUtils.SCALE_IMAGE_WIDTH,
			// ImageUtils.SCALE_IMAGE_HEIGHT);
			// if (android.os.Build.VERSION.SDK_INT > 10) {
			// task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			// } else {
			// task.execute();
			// }
			// } else {
			// image.setImageBitmap(bitmap);
			// }
			ImageLoadUtil.load(this, image, "file://" + uri.toString(),
					R.drawable.headlogo, R.drawable.headlogo);
		} else if (remotepath != null) { // 去服务器下载图片
			Map<String, String> maps = new HashMap<String, String>();
			if (!TextUtils.isEmpty(secret)) {
				maps.put("share-secret", secret);
			}
			
			ImageLoadUtil.load(this, image, remotepath,
					R.drawable.default_image, R.drawable.default_image);
		} else {
			image.setImageResource(default_res);
		}

		image.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	/**
	 * 通过远程URL，确定下本地下载后的localurl
	 * 
	 * @param remoteUrl
	 * @return
	 */
	// public String getLocalFilePath(String remoteUrl) {
	// String localPath;
	// if (remoteUrl.contains("/")) {
	// localPath = PathUtil.getInstance().getImagePath().getAbsolutePath()
	// + "/" + remoteUrl.substring(remoteUrl.lastIndexOf("/") + 1);
	// } else {
	// localPath = PathUtil.getInstance().getImagePath().getAbsolutePath()
	// + "/" + remoteUrl;
	// }
	// return localPath;
	// }

	/**
	 * 下载图片
	 * 
	 * @param remoteFilePath
	 */
	// private void downloadImage(final String remoteFilePath,
	// final Map<String, String> headers) {
	// String str1 = "下载中";
	// pd = new ProgressDialog(this);
	// pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	// pd.setCanceledOnTouchOutside(false);
	// pd.setMessage(str1);
	// pd.show();
	// localFilePath = getLocalFilePath(remoteFilePath);
	// final EMCallBack callback = new EMCallBack() {
	// public void onSuccess() {
	//
	// runOnUiThread(new Runnable() {
	// @Override
	// public void run() {
	// DisplayMetrics metrics = new DisplayMetrics();
	// getWindowManager().getDefaultDisplay().getMetrics(
	// metrics);
	// int screenWidth = metrics.widthPixels;
	// int screenHeight = metrics.heightPixels;
	//
	// bitmap = ImageUtils.decodeScaleImage(localFilePath,
	// screenWidth, screenHeight);
	// if (bitmap == null) {
	// image.setImageResource(default_res);
	// } else {
	// image.setImageBitmap(bitmap);
	// ImageCache.getInstance().put(localFilePath, bitmap);
	// isDownloaded = true;
	// }
	// if (pd != null) {
	// pd.dismiss();
	// }
	// }
	// });
	// }
	//
	// public void onError(int error, String msg) {
	// File file = new File(localFilePath);
	// if (file.exists() && file.isFile()) {
	// file.delete();
	// }
	// runOnUiThread(new Runnable() {
	// @Override
	// public void run() {
	// pd.dismiss();
	// image.setImageResource(default_res);
	// }
	// });
	// }
	//
	// public void onProgress(final int progress, String status) {
	// final String str2 = "下载中";
	// runOnUiThread(new Runnable() {
	// @Override
	// public void run() {
	//
	// pd.setMessage(str2 + progress + "%");
	// }
	// });
	// }
	// };
	//
	// EMChatManager.getInstance().downloadFile(remoteFilePath, localFilePath,
	// headers, callback);
	//
	// }

	@Override
	public void onBackPressed() {
		if (isDownloaded)
			setResult(RESULT_OK);
		finish();
	}
}
