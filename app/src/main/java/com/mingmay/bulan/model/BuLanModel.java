package com.mingmay.bulan.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class BuLanModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int browseCount;
	public long bulanId;

	public String bulanImage;
	public String bulanKey;

	public String bulanTitle;
	public String bulanContent;

	public ArrayList<BulanEditModel> bulanInfo;
	public int commentCount;

	public String createDate;

	public String firstName;
	public int forwardCount;
	public int isPraise;
	public String loginName;
	public int praiseCount;
	public String tempId;
	public long userId;
	public boolean isCollection;

	public String[] bulanTags;
	public boolean isOpen = true;

	public int from; // 0 是普通用户 ，1 系统管理员

	public String userImg;

	public static BuLanModel jsonToModel(JSONObject obj) {
		BuLanModel model = new BuLanModel();
		model.browseCount = obj.optInt("browseCount");
		model.bulanId = obj.optInt("bulanId");
		model.bulanImage = obj.optString("bulanImage");
		model.bulanKey = obj.optString("bulanKey");
		model.bulanTitle = obj.optString("bulanTitle");
		try {
			model.bulanInfo = getElement(obj.getJSONArray("bulanContents"));
		} catch (JSONException e) {
		}

		model.commentCount = obj.optInt("commentCount");
		model.createDate = obj.optString("createDate");
		model.firstName = obj.optString("firstName");
		model.forwardCount = obj.optInt("forwardCount");

		model.isPraise = obj.optInt("isPraise");
		model.loginName = obj.optString("loginName");
		model.praiseCount = obj.optInt("praiseCount");
		model.tempId = obj.optString("tempId");
		model.userId = obj.optLong("userId");
		String tags = obj.optString("bulanTags");
		if (!TextUtils.isEmpty(tags)) {
			model.bulanTags = tags.split(",");
		}
		model.isOpen = obj.optBoolean("isOpen");
		model.from = obj.optInt("from");
		model.userImg = obj.optString("userImg");
		model.bulanContent = obj.optString("bulanContent");
		model.isCollection = false;
		int i = obj.optInt("isCollection");
		if (i == 1) {
			model.isCollection = true;
		}
		return model;
	}

	// public String getElementImage(int parentIndex, int romoteNameIndxe) {
	// return bulanInfo.get(parentIndex).remoteNames[romoteNameIndxe];
	// }
	//
	private static ArrayList<BulanEditModel> getElement(JSONArray items) {

		ArrayList<BulanEditModel> elements = new ArrayList<BulanEditModel>();
		try {
			JSONArray child = items;
			if (child != null) {
				int len = child.length();
				if (len > 0) {
					for (int i = 0; i < len; i++) {

						JSONObject item = child.getJSONObject(i);
						int type = item.optInt("type");
						if (type == 1) {
							String thumbnailPic = item
									.optString("thumbnailPic");
							String originalPic = item.optString("originalPic");
							BulanEditModel bm = new BulanEditModel(type,
									thumbnailPic);
							bm.originalPic = originalPic;
							elements.add(bm);
						} else {
							String txt = item.optString("text");
							BulanEditModel bm = new BulanEditModel(type, txt);
							elements.add(bm);
						}
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return elements;
	}

}
