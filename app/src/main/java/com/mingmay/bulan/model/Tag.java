package com.mingmay.bulan.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.mingmay.bulan.R;
import com.mingmay.bulan.app.TagManager;

public class Tag implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int id;
	public String name;

	public List<Tag> childs;

	public int bgResId;

	public Tag() {
	}

	public static Tag jsonToTag(JSONObject obj) {
		Tag tag = new Tag();
		try {
			tag.id = obj.optInt("id");
			tag.name = obj.optString("name");
			int bgResId = obj.optInt("color");
			if (bgResId > 2) {
				bgResId = 2;
			} else if (bgResId < 0) {
				bgResId = 0;
			}
			tag.bgResId = bgResId + R.drawable.background_0;
			if (obj.has("childArray")) {
				JSONArray child = obj.getJSONArray("childArray");
				int count = child.length();
				if (count > 0) {
					tag.childs = new ArrayList<Tag>();
					for (int i = 0; i < count; i++) {
						Tag c = Tag.jsonToTag(child.getJSONObject(i));
						tag.childs.add(c);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return tag;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		if (TextUtils.isEmpty(name)) {
			return "";
		}
		return name;
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if (o == null) {
			return false;
		}
		Tag t = (Tag) o;
		return t.id == (this.id);
	}
}
