package com.mingmay.bulan.model;

import java.io.Serializable;

import org.json.JSONObject;

public class NotifyModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public long noticeId;
	public int noticeKey;
	
	public long bulanId;
	public long commentId;
	public String commentTxt;
	public long commentMainId;
	
	public String userName;
	public long userId;
	public String userImag;
	
	public int isRead;
	
	public String createDate;
	
	public String noticeContent;
	public int noticeStatus;
	
	 
	public NotifyModel(JSONObject obj) {
		this.noticeId = obj.optLong("noticeId");
		this.noticeKey = obj.optInt("noticeKey");
		
		this.bulanId=obj.optLong("bulanId");
		this.commentId=obj.optLong("commentId");
		this.commentTxt=obj.optString("commentTxt");
		this.commentMainId=obj.optLong("commentMainId");
		
		this.isRead = obj.optInt("isRead");
		this.userName=obj.optString("userName");
		this.userImag=obj.optString("userImag");
		this.createDate=obj.optString("createDate");
		this.userId=obj.optLong("userId");
		
		this.noticeContent=obj.optString("noticeContent");
		this.noticeStatus=obj.optInt("noticeStatus");
	}
	
}
