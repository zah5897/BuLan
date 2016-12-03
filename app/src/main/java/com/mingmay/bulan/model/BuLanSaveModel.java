package com.mingmay.bulan.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.database.Cursor;

public class BuLanSaveModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int STATE_CAN_EDIT = 0;
	public static final int STATE_HAS_TO_COMMIT = 1;

	public long id;
	public String title;
	public String iconPath;
	public String iconServerPath;
	public String isOpen;
	public String tag;
	public String content;
	public long createDate;
	public int state;

	public ArrayList<BulanEditModel> elements;

	public static BuLanSaveModel cursorToModel(Cursor c) {
		BuLanSaveModel saveModel = new BuLanSaveModel();
		saveModel.id = c.getLong(c.getColumnIndex("_ID"));
		saveModel.title = c.getString(c.getColumnIndex("title"));
		saveModel.iconPath = c.getString(c.getColumnIndex("iconPath"));
		saveModel.isOpen = c.getString(c.getColumnIndex("isOpen"));
		saveModel.tag = c.getString(c.getColumnIndex("tag"));
		saveModel.content = c.getString(c.getColumnIndex("content"));
		saveModel.state = c.getInt(c.getColumnIndex("state"));
		saveModel.createDate = c.getLong(c.getColumnIndex("lastTime"));
		saveModel.getModels();
		return saveModel;
	}

	public ArrayList<BulanEditModel> getModels() {
		elements = new ArrayList<BulanEditModel>();
		try {
			JSONArray jsonArray = new JSONArray(content);
			int len = jsonArray.length();

			for (int i = 0; i < len; i++) {
				BulanEditModel bm = new BulanEditModel(
						jsonArray.optJSONObject(i));
				elements.add(bm);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return elements;
	}

	public String getPublishBulanJson() {
		JSONArray ja = new JSONArray();
		for (BulanEditModel bm : elements) {
			ja.put(bm.toPublishJson());
		}
		return ja.toString();
	}

	public String getModelJson() {
		JSONArray ja = new JSONArray();
		for (BulanEditModel bm : elements) {
			ja.put(bm.toString());
		}
		return ja.toString();
	}

}
