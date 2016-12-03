package com.mingmay.bulan.util;

import android.widget.Toast;

import com.mingmay.bulan.app.CCApplication;

public class ToastUtil {
	public static void show(String message) {
		Toast.makeText(CCApplication.app, message, Toast.LENGTH_SHORT).show();
	}
}
