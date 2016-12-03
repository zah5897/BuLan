package com.mingmay.bulan.app;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Base64;

import com.mingmay.bulan.util.TimeUtil;

public class ParamManager {
	public static final String ROOT_EXTERNAL = "bulan";
	public static final String ROOT_EXTERNAL_DATA = "data";
	public static final String ROOT_EXTERNAL_ERR = "log";
	public static String sid;
	public static String androidID;
	public static String versionName;
	public static String ln;
	public static String mod = android.os.Build.MODEL;
	private static ParamManager paramManager;
	public static List<NameValuePair> headers;

	private ParamManager(Context context) {
		final TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		sid = tm.getDeviceId();
		versionName = getVersion(context);
		androidID = "1002";
		createHeader();
	}

	private String getAndroidID(Context context) {
		return android.provider.Settings.Secure.getString(
				context.getContentResolver(),
				android.provider.Settings.Secure.ANDROID_ID);
	}

	public String getVersion(Context context) {
		// try {
		// PackageManager manager = this.getPackageManager();
		// PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
		// String version = info.versionName;
		// return version;
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		return "1.0.0";
	}

	private void createHeader() {
		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		parameters.add(new BasicNameValuePair("aid", androidID));
		parameters.add(new BasicNameValuePair("ver", versionName));
		parameters.add(new BasicNameValuePair("ln", "zh_CN"));
		// parameters.add(new
		// BasicNameValuePair("cd",getVerByJava(CCApplication.androidID+CCApplication.versionName)));
		parameters
				.add(new BasicNameValuePair("cd", "khc5+/1MxDiZ1bK77Jpt7A=="));
		parameters.add(new BasicNameValuePair("sid", sid));
		parameters.add(new BasicNameValuePair("mos", "ANDROID"));
		parameters.add(new BasicNameValuePair("mod", mod));
		parameters.add(new BasicNameValuePair("de", TimeUtil
				.currentLocalTimeString()));
		parameters.add(new BasicNameValuePair("sync", "1"));
		headers = parameters;
	}

	private String getVer(String hasDe) throws UnsupportedEncodingException {

		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("md5");
			byte[] by = md5.digest(hasDe.getBytes("UTF-8"));
			String md5Str = new String(by);
			return android.util.Base64.encodeToString(md5Str.getBytes(),
					Base64.DEFAULT);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	private String getVerByJava(String text)
			throws UnsupportedEncodingException {

		MessageDigest digester = null;

		try {
			digester = MessageDigest.getInstance("MD5");

			digester.update(text.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException nsae) {
		} catch (UnsupportedEncodingException uee) {
		}

		byte[] bytes = digester.digest();

		String result = android.util.Base64.encodeToString(bytes,
				Base64.DEFAULT);
		result = result.replace("\\n", "");
		return result;
	}

	public static ParamManager getInstance(Context context) {
		if (paramManager == null) {
			paramManager = new ParamManager(context);
		}
		return paramManager;
	}
}
