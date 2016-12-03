package com.mingmay.bulan.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class User implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public long ID;
	public String loginName;
	public String createDate;
	public String emailAddress;
	public String cellPhone;
	public String lastFailedLoginDate;
	public String firstName;
	public Gender gender = Gender.Female;
	public String cc_accessToken;
	public String cc_accessTokenType;
	public String ccukey;
	public String userImg;
	public String address;
	public String signature;

	public String planCover;
	public String verificationCode;
	public String userCover;

	public int chatMessageCount;
	public String lastChatMessage;
	public String chatMessageDate;

	public int isFan = 2;// 0验证中，1 已经添加，2未添加过,3黑名单

	public String firstChar;
	public int type; // 0 session,1申请加好友

	public User(long ID){
		this.ID=ID;
	}
	public User(){}
	public static User jsonToUser(JSONObject json) {
		User user = new User();
		user.ID = json.optLong("id");
		user.loginName = json.optString("loginName");
		user.createDate = json.optString("createDate");
		user.emailAddress = json.optString("emailAddress");
		user.lastFailedLoginDate = json.optString("lastFailedLoginDate");
		user.firstName = json.optString("firstName");
		int gender = json.optInt("gender");
		user.gender = Gender.values()[gender];
		user.cc_accessToken = json.optString("cc_accessToken");
		user.cc_accessTokenType = json.optString("cc_accessTokenType");
		user.ccukey = json.optString("ccukey");
		user.userImg = json.optString("userImg");
		user.address = json.optString("address");
		user.signature = json.optString("signature");
		user.userCover = json.optString("userCover");
		return user;
	}

	public static User jsonToFriend(JSONObject json) {
		User user = new User();
		user.ID = json.optLong("id");
		user.loginName = json.optString("loginName");
		user.createDate = json.optString("createDate");
		user.emailAddress = json.optString("emailAddress");
		user.cellPhone = json.optString("cellPhone");
		user.lastFailedLoginDate = json.optString("lastFailedLoginDate");
		user.firstName = json.optString("firstName");
		int gender = json.optInt("gender");
		user.gender = Gender.values()[gender];
		user.cc_accessToken = json.optString("cc_accessToken");
		user.cc_accessTokenType = json.optString("cc_accessTokenType");
		user.ccukey = json.optString("ccukey");
		user.address = json.optString("address");
		user.userImg = json.optString("userImg");
		user.signature = json.optString("signature");
		user.verificationCode = json.optString("verificationCode");
		user.isFan = json.optInt("isFan");
		// user.userCover=json.optString("userCover");
		return user;
	}

	public static User jsonToSession(JSONObject json) throws JSONException {
		User user = new User();
		user.ID = json.optLong("id");
		user.firstName = json.optString("firstName");
		user.userImg = json.optString("userImg");
		user.chatMessageCount = json.optInt("chatMessageCount");
		user.chatMessageDate = json.optString("chatMessageDate");
		user.lastChatMessage = json.optString("lastChatMessage");
		// user.userCover=json.optString("userCover");
		return user;
	}

	public String getGener() {
		switch (gender) {
		case Female:
			return "女";
		case Male:
			return "男";
		default:
			return "保密";
		}
	}

	public int getGenerIntValue() {
		return gender.ordinal();
	}

	public boolean isFriend() {
		return this.isFan == 1;
	}

	@Override
	public String toString() {
		return firstName;
	}

	public static User jsonToSearchFriend(JSONObject jsonObject) {
		User user = new User();
		user.ID = jsonObject.optLong("id");
		user.loginName = jsonObject.optString("loginName");
		user.cellPhone = jsonObject.optString("cellPhone");
		user.firstName = jsonObject.optString("firstName");
		user.isFan = jsonObject.optInt("isFan");

		return user;
	}
  
	@Override
	public boolean equals(Object o) {
		User u=(User) o;
		return u.ID==ID;
	}
}
