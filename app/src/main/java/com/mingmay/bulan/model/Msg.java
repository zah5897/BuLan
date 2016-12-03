package com.mingmay.bulan.model;

import org.json.JSONObject;

public class Msg {
	
	public long forSendId;
	
	public long chatMessageId;
	public long fromUserId;

	public String fromUserName;
	public String fromUserImg;
	public String fromLoginName;

	public String chatImg;

	public String context;

	public String createDate;

	public Msg(JSONObject msgobj) {
		this.chatMessageId = msgobj.optLong("chatMessageId");
		this.fromUserId = msgobj.optLong("fromUserId");

		this.fromUserName = msgobj.optString("fromUserName");
		this.fromUserImg = msgobj.optString("fromUserImg");
		this.fromLoginName = msgobj.optString("fromLoginName");

		this.chatImg = msgobj.optString("chatImg");
		this.context = msgobj.optString("context");

		this.createDate = msgobj.optString("createDate");
	}
	public Msg() {
		forSendId=System.currentTimeMillis();
	}
}
