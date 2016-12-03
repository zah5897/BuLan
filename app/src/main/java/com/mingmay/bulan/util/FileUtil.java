package com.mingmay.bulan.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;

import com.mingmay.bulan.app.ParamManager;

public class FileUtil {
	public static File createLogFile() {
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			String name = TimeUtil.currentLocalTimeString() + ".txt";
			File parent = new File(Environment.getExternalStorageDirectory()
					+ File.separator + ParamManager.ROOT_EXTERNAL
					+ File.separator + ParamManager.ROOT_EXTERNAL_ERR);
			if (!parent.exists()) {
				parent.mkdirs();
			}
			File log = new File(parent.getAbsolutePath(), name);
			try {
				log.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return log;
		} else {
			return null;
		}

	}

	public static File createAppCacheDir() {
		File parent = new File(getRootPath());
		if (!parent.exists()) {
			parent.mkdirs();
		}
		return parent;
	}

	public static String getRootPath() {
		return Environment.getExternalStorageDirectory() + File.separator
				+ ParamManager.ROOT_EXTERNAL + File.separator
				+ ParamManager.ROOT_EXTERNAL_DATA;
	}

	public static String toFile(byte[] bfile, String fileName) {
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		File file = null;
		try {
			// int len = bfile.length;
			file = new File(fileName);
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			bos.write(bfile);
			return fileName;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return null;
	}

	public static byte[] getBytes(String filePath) {
		if (TextUtils.isEmpty(filePath)) {
			return null;
		}
		byte[] buffer = null;
		try {
			File file = new File(filePath);
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1000];
			int n;
			while ((n = fis.read(b)) != -1) {
				bos.write(b, 0, n);
			}
			fis.close();
			bos.close();
			buffer = bos.toByteArray();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer;
	}

	public static File saveBitmap(Bitmap bitmap) {

		String rootPath = getRootPath();

		File p = new File(rootPath);
		p.mkdirs();

		File[] files = p.listFiles();
		for (File lf : files) {
			lf.delete();
		}
		File f = new File(p.getAbsoluteFile(), System.currentTimeMillis()
				+ ".jpg");
		if (f.exists()) {
			f.delete();
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return f;
	}

	public static File saveBuLanIconBitmap(Bitmap bitmap) {

		String rootPath = getRootPath();

		File p = new File(rootPath);
		p.mkdirs();
		File f = new File(p.getAbsoluteFile(), "i_"
				+ System.currentTimeMillis() + ".png");
		if (f.exists()) {
			f.delete();
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return f;
	}

	public static boolean isExist(String localPath) {
		// TODO Auto-generated method stub
		if (TextUtils.isEmpty(localPath)) {
			return false;
		} else if (new File(localPath).exists()) {
			return true;
		}
		return false;
	}
}
