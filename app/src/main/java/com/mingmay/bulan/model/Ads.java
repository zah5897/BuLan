package com.mingmay.bulan.model;

import org.json.JSONObject;

public class Ads {
    
    public int id;
    public String value;
    public String recommendImg;
    public String createDate;
    public int type; //1为value跳转地址，2为value布栏编号
    
    public static Ads jsonToAds(JSONObject jsonObj){
    	Ads ads=new Ads();
    	ads.id=jsonObj.optInt("recommendId");
    	ads.value=jsonObj.optString("value");
    	ads.recommendImg=jsonObj.optString("recommendImg");
    	ads.createDate=jsonObj.optString("createDate");
    	ads.type=jsonObj.optInt("type");
		return ads;
    }
}
