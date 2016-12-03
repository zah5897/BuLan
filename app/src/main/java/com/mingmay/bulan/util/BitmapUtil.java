package com.mingmay.bulan.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.os.Environment;

public class BitmapUtil {

	// public static final int IMAGE_MAX_SIZE_LIMIT = 100;

	// private int mWidth;
	// private int mHeight;
	public static String saveBitmap(Bitmap toSave, String name) {
		File parent = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/+bulan");
		if (!parent.exists()) {
			parent.mkdir();
		}

		File f = new File(parent.getAbsolutePath(), name);
		if (f.exists()) {
			f.delete();
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			toSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return f.getAbsolutePath();
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

	public static Bitmap getSmallBitmap(String filePath, int w, int h) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, w, h);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(filePath, options);
	}

	public static int[] getDecodeBitmap(String filePath) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		int[] wh = new int[] { options.outWidth, options.outHeight };
		return wh;
	}

	public static Bitmap getBitmap(String filePath) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		// options.inJustDecodeBounds = true;
		// BitmapFactory.decodeFile(filePath, options);

		// Calculate inSampleSize
		// options.inSampleSize = calculateInSampleSize(options, w, h);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(filePath, options);
	}

	public static Bitmap toRoundCorner(Context context, Bitmap src, Bitmap dst) {

		if (src == null || dst == null) {
			return null;
		}
		// NinePatchDrawable nine = (NinePatchDrawable)
		// context.getResources().getDrawable(R.drawable.gotye_bg_msg_text_normal_right);
		// nine.setBounds(0, 0, src.getWidth(), src.getHeight());

		Bitmap output = Bitmap.createBitmap(dst.getWidth(), dst.getHeight(),
				Config.ARGB_8888);

		Canvas canvas = new Canvas(output);
		Paint paint = new Paint();
		canvas.drawBitmap(dst, 0, 0, paint);

		int width = dst.getWidth();

		src = Bitmap.createScaledBitmap(src, width, dst.getHeight(), true);
		if (src == null) {
			return null;
		}

		paint.setAntiAlias(true);
		// canvas.drawBitmap(dst, 0, 0, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(src, 0, 0, paint);
		canvas.save();

		return output;
	}
}
