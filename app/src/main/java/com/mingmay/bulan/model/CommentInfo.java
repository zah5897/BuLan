package com.mingmay.bulan.model;

import java.io.Serializable;

import org.json.JSONObject;

public class CommentInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public long commentId;
	public String commentText;
	public String createDate;
	public String createUserName;
	public String createUserImg;
	public long createUserId;
	
	public String commentUserName;
	public String commentUserImg;
	public long commentUserId;
	
	public static CommentInfo jsonToCommentInfo(JSONObject obj){
		CommentInfo info=new CommentInfo();
        info.commentId=obj.optLong("commentId")	;	
        info.commentText=obj.optString("commentText")	;	
        info.createDate=obj.optString("createDate")	;	
        info.createUserName=obj.optString("createUserName")	;	
        info.createUserImg=obj.optString("createUserImg")	;	
        info.createUserId=obj.optLong("createUserId")	;	
        info.commentUserName=obj.optString("commentUserName");
        info.commentUserImg=obj.optString("commentUserImg");
        info.commentUserId=obj.optLong("commentUserId");
		return info;
	}
}
