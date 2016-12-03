package com.mingmay.bulan.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.ParamManager;

public class PropertyUtil {
	public static void putValue(String key, String value) {
		if("user_info".equals(key)){
			Log.d("","");
		}
		SharedPreferences spf = CCApplication.app.getSharedPreferences(
				ParamManager.ROOT_EXTERNAL, Context.MODE_PRIVATE);
		spf.edit().putString(key, value).commit();
	}
	public static void putValue(String key, long value) {
		SharedPreferences spf = CCApplication.app.getSharedPreferences(
				ParamManager.ROOT_EXTERNAL, Context.MODE_PRIVATE);
		spf.edit().putLong(key, value).commit();
	}

	public static void putValue(String key, boolean value) {
		SharedPreferences spf = CCApplication.app.getSharedPreferences(
				ParamManager.ROOT_EXTERNAL, Context.MODE_PRIVATE);
		spf.edit().putBoolean(key, value).commit();
	}

	public static String getStringValue(String key) {
		SharedPreferences spf = CCApplication.app.getSharedPreferences(
				ParamManager.ROOT_EXTERNAL, Context.MODE_PRIVATE);
		return spf.getString(key, null);
	}

	public static boolean getBooleantValue(String key) {
		SharedPreferences spf = CCApplication.app.getSharedPreferences(
				ParamManager.ROOT_EXTERNAL, Context.MODE_PRIVATE);
		return spf.getBoolean(key, false);
	}
	public static long getLongValue(String key) {
		SharedPreferences spf = CCApplication.app.getSharedPreferences(
				ParamManager.ROOT_EXTERNAL, Context.MODE_PRIVATE);
		return spf.getLong(key, 0);
	}
}
