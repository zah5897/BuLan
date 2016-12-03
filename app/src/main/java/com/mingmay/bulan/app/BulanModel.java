package com.mingmay.bulan.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

public class BulanModel {
	protected Context context = null;
	protected Map<Key, Object> valueCache = new HashMap<Key, Object>();

	public BulanModel(Context ctx) {
		context = ctx;
	}

	/**
	 * 设置当前用户的环信id
	 * 
	 * @param username
	 */

	public void setSettingMsgNotification(boolean paramBoolean) {
		valueCache.put(Key.VibrateAndPlayToneOn, paramBoolean);
	}

	public boolean getSettingMsgNotification() {
		Object val = valueCache.get(Key.VibrateAndPlayToneOn);
		return (Boolean) (val != null ? val : true);
	}

	public void setSettingMsgSound(boolean paramBoolean) {
		valueCache.put(Key.PlayToneOn, paramBoolean);
	}

	public boolean getSettingMsgSound() {
		Object val = valueCache.get(Key.PlayToneOn);


		return (Boolean) (val != null ? val : true);
	}

	public void setSettingMsgVibrate(boolean paramBoolean) {
		valueCache.put(Key.VibrateOn, paramBoolean);
	}

	public boolean getSettingMsgVibrate() {
		Object val = valueCache.get(Key.VibrateOn);

		return (Boolean) (val != null ? val : true);
	}

	public void setSettingMsgSpeaker(boolean paramBoolean) {
		valueCache.put(Key.SpakerOn, paramBoolean);
	}

	public boolean getSettingMsgSpeaker() {
		Object val = valueCache.get(Key.SpakerOn);


		return (Boolean) (val != null ? val : true);
	}

	public void setDisabledGroups(List<String> groups) {
		valueCache.put(Key.DisabledGroups, groups);
	}

 

	enum Key {
		VibrateAndPlayToneOn, VibrateOn, PlayToneOn, SpakerOn, DisabledGroups, DisabledIds
	}
}
