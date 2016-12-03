package com.mingmay.bulan.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.support.v4.util.LruCache;

/**
 * 图片缓存，使用SoftReference保证内存不足时会被系统自动回收<br>
 * 
 *
 */
public class ImageCache {

	private static ImageCache imageCache;
	/**
	 * 缓存
	 */
	private LruCache<String, Bitmap> mMemoryCache;

	private ImageCache() {
		long maxSize = Runtime.getRuntime().maxMemory();
		int cacheSize = (int) (maxSize / 8);
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize);
	}

	public static ImageCache getInstance() {
		if (imageCache == null) {
			imageCache = new ImageCache();
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

	public Bitmap decodeSimpleBitMapFromResource(String path, int mClolumnWidth) {
		int digree=ImageUtils.getBitmapDegree(path);
		
		final Options options = new Options();
		options.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		options.inSampleSize = decodeSimpleSize(options, mClolumnWidth);
		options.inJustDecodeBounds = false;
		Bitmap bmp=BitmapFactory.decodeFile(path, options);
		return ImageUtils.ratoteBitmap(bmp, digree);

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