package com.mingmay.bulan.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.support.v4.util.LruCache;

import com.mingmay.bulan.R;

/**
 * 图片缓存，使用SoftReference保证内存不足时会被系统自动回收<br>
 * 
 *
 */
public class GuideImageCache {

	private static GuideImageCache imageCache;
	/**
	 * 缓存
	 */
	private LruCache<String, Bitmap> mMemoryCache;

	private GuideImageCache() {
		long maxSize = Runtime.getRuntime().maxMemory();
		int cacheSize = (int) (maxSize / 8);
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize);
	}

	public static GuideImageCache getInstance() {
		if (imageCache == null) {
			imageCache = new GuideImageCache();
		}
		return imageCache;
	}

	/**
	 * 将图片放入内存
	 * 
	 * @param path
	 * @param d
	 */
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getMemoryCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	/**
	 * 通过key获取bitmap
	 * 
	 * @param path
	 * @return
	 */
	public Bitmap getMemoryCache(String key) {
		if (mMemoryCache != null) {
			Bitmap bitmap = mMemoryCache.get(key);
			if (bitmap != null) {
				return bitmap;
			}
		}
		return null;
	}

	public Bitmap decodeSimpleBitMapFromResource(Resources res,int resId, int mClolumnWidth) {
		final Options options = new Options();
		options.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.frist, options);
		options.inSampleSize = decodeSimpleSize(options, mClolumnWidth);
		options.inJustDecodeBounds = false;
		return  BitmapFactory.decodeResource(res, resId, options);

	}

	/**
	 * 获得simpleSize
	 * 
	 * @param options
	 * @return
	 */
	private int decodeSimpleSize(Options options, int reqwidth) {
		int width = options.outWidth;
		int simplesize = 1;
		if (width > reqwidth) {
			simplesize = Math.round(width / reqwidth);
		}
		return simplesize;
	}
}