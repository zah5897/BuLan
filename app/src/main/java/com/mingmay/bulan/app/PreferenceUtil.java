package com.mingmay.bulan.app;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtil {
	public static final String PREFERENCE_NAME = "config";

	public static String readConfig(Context context, String name) {
		SharedPreferences spf = context.getSharedPreferences(PREFERENCE_NAME,
				Context.MODE_PRIVATE);
		return spf.getString(name, null);
	}

	public static void writeConfig(Context context, String name, String value) {
		SharedPreferences spf = context.getSharedPreferences(PREFERENCE_NAME,
				Context.MODE_PRIVATE);
		spf.edit().putString(name, value).commit();
	}
}
