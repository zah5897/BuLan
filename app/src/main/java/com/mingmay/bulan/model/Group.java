package com.mingmay.bulan.model;

import org.json.JSONObject;

public class Group {
	public long id;
	public String name;
	public String userName;
	public long userId;
	public String icon;
	public int count;

	public int watched=1;  //0未关注
	public static Group parse(JSONObject obj) {
		Group g = new Group();
		g.userName = obj.optString("userName");
		g.userId = obj.optLong("userId");
		g.id = obj.optLong("id");
		g.name = obj.optString("name");
		g.icon = obj.optString("wardrobeImage");
		g.count = obj.optInt("attentioncount");
		g.watched=obj.optInt("isAttention");
		return g;
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		Group g = (Group) o;
		return g.id == id;
	}
}
