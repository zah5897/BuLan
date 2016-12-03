package com.mingmay.bulan.app;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;

import com.mingmay.bulan.util.PropertyUtil;

public class AppContent {
	private static AppContent appContent;

	public static boolean newMsgVoiceTip = true;
	public static boolean newMsgVerberTip = true;
	public static boolean donotDisturb = true;

	private AppContent(Context context) {
		newMsgVoiceTip = PropertyUtil.getBooleantValue("voice_tip");
		newMsgVerberTip = PropertyUtil.getBooleantValue("verber_tip");
		donotDisturb = PropertyUtil.getBooleantValue("donot_disturb");

		if (false && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
					.detectAll().penaltyDialog().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectAll().penaltyDeath().build());
		}
	}

	private Handler handler = new Handler();

	public void runInUi(Runnable run) {
		handler.post(run);
	}

	public static AppContent getInstance(Context context) {
		return appContent == null ? appContent = new AppContent(context)
				: appContent;
	}

	public static AppContent getInstance() {
		return appContent;
	}

	public void newMessageVoiceTip(boolean isOpen) {
		newMsgVoiceTip = isOpen;
		PropertyUtil.putValue("voice_tip", isOpen);
	}

	public void newMessageVerberTip(boolean isOpen) {
		newMsgVoiceTip = isOpen;
		PropertyUtil.putValue("verber_tip", isOpen);
	}

	public void donotDisturb(boolean isOpen) {
		newMsgVoiceTip = isOpen;
		PropertyUtil.putValue("donot_disturb", isOpen);
	}

}
