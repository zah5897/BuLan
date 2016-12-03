package com.mingmay.bulan.util;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ImageLoadUtil {
	public static void load(Activity activity, ImageView imageView, String url) {
		if (TextUtils.isEmpty(url)) {
			return;
		}
		Glide.with(activity).load(url).into(imageView);
	}

	public static void load(Fragment fragment, ImageView imageView, String url) {
		if (TextUtils.isEmpty(url)) {
			return;
		}
		Glide.with(fragment).load(url).into(imageView);
	}

	public static void load(Activity activity, ImageView imageView, String url,
			int replaceId, int errorId) {
		if (TextUtils.isEmpty(url)) {
			return;
		}
		if (replaceId > 0 && errorId > 0) {
			Glide.with(activity).load(url).placeholder(replaceId)
					.error(errorId).into(imageView);
		} else if (replaceId > 0) {
			Glide.with(activity).load(url).placeholder(replaceId)
					.into(imageView);
		} else if (errorId > 0) {
			Glide.with(activity).load(url).error(errorId).into(imageView);
		} else {
			Glide.with(activity).load(url).into(imageView);
		}
	}

	public static void load(Fragment fragment, ImageView imageView, String url,
			int replaceId, int errorId) {
		if (TextUtils.isEmpty(url)) {
			return;
		}
		if (replaceId > 0 && errorId > 0) {
			Glide.with(fragment).load(url).placeholder(replaceId)
					.error(errorId).into(imageView);
		} else if (replaceId > 0) {
			Glide.with(fragment).load(url).placeholder(replaceId)
					.into(imageView);
		} else if (errorId > 0) {
			Glide.with(fragment).load(url).error(errorId).into(imageView);
		} else {
			Glide.with(fragment).load(url).into(imageView);
		}
	}

	public static void load(Activity activity, ImageView imageView, String url,
			int[] wh) {
		if (TextUtils.isEmpty(url)) {
			return;
		}
		Glide.with(activity).load(url).override(wh[0], wh[1]).into(imageView);
	}

	public static void load(Fragment fragment, ImageView imageView, String url,
			int[] wh) {
		if (TextUtils.isEmpty(url)) {
			return;
		}
		Glide.with(fragment).load(url).override(wh[0], wh[1]).into(imageView);
	}

	public static void load(Activity activity, ImageView imageView, String url,
			int replaceId, int errorId, int[] wh) {
		if (TextUtils.isEmpty(url)) {
			return;
		}
		if (replaceId > 0 && errorId > 0) {
			Glide.with(activity).load(url).override(wh[0], wh[1])
					.placeholder(replaceId).error(errorId).into(imageView);
		} else if (replaceId > 0) {
			Glide.with(activity).load(url).override(wh[0], wh[1])
					.placeholder(replaceId).into(imageView);
		} else if (errorId > 0) {
			Glide.with(activity).load(url).override(wh[0], wh[1])
					.error(errorId).into(imageView);
		} else {
			Glide.with(activity).load(url).override(wh[0], wh[1])
					.into(imageView);
		}
	}

	public static void load(Context context, ImageView imageView, File filePath) {
		Glide.with(context).load(filePath).into(imageView);
	}

	public static void load(Context context, ImageView imageView,
			File filePath, int[] wh) {
		Glide.with(context).load(filePath).override(wh[0], wh[1])
				.into(imageView);
	}

	// public static void get(Context context, File filePath, int width,
	// int height, final BitmapLoadListener listener) {
	// Glide.with(context).load(filePath).override(width, height).into();
	// }
}
