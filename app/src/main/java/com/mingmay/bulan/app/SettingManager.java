package com.mingmay.bulan.app;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingManager {
	private static SettingManager settingManager;

	private Set<String> disNoiceGroups;

	private SettingManager() {
		disNoiceGroups = new HashSet<String>();
		loadPerference();
	}

	private void loadPerference() {
		SharedPreferences spf = CCApplication.app.getSharedPreferences(
				"group_disnoice", Context.MODE_PRIVATE);
		disNoiceGroups=spf.getStringSet("groups", disNoiceGroups);
	}

	public static SettingManager getInstance() {
		if (settingManager == null) {
			settingManager = new SettingManager();
		}
		return settingManager;
	}
	

	public void addDisNoiceGroup(String groupId) {
		disNoiceGroups.add(groupId);
		SharedPreferences spf = CCApplication.app.getSharedPreferences(
				"group_disnoice", Context.MODE_PRIVATE);
		spf.edit().putStringSet("groups", disNoiceGroups).commit();
	}

	
	public void removeDisNoiceGroup(String groupId) {
		disNoiceGroups.remove(groupId);
		SharedPreferences spf = CCApplication.app.getSharedPreferences(
				"group_disnoice", Context.MODE_PRIVATE);
		spf.edit().putStringSet("groups", disNoiceGroups).commit();
	}
	
	
	public boolean isDisNoice(String groupId){
		return disNoiceGroups.contains(groupId);
	}

}
