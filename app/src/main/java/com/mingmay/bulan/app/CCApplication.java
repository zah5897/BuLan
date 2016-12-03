package com.mingmay.bulan.app;

import android.app.Application;
import cn.sharesdk.framework.ShareSDK;

import com.hyphenate.easeui.controller.EaseUI;
import com.mingmay.bulan.MainActivity;
import com.mingmay.bulan.app.err.CrashApplication;
import com.ucloud.live.UEasyStreaming;

public class CCApplication extends Application {

	public static int screenWidth, screenHeight;
	public static float density;

	public static final String HTTPSERVER = "http://www.mingmay.com.cn:8080/cc";
//	 public static final String HTTPSERVER = "http://115.28.168.181:8080/cc";
//	 public static final String HTTPSERVER = "http://192.168.123.59:8081/cc";

	public static MainActivity mianActivity;
	public static boolean bulanNeedRefresh = false;
	public static boolean sessionNeedRefresh = false;
	public static boolean needRefreshUserInfo, caogaoxiangNeedRefresh;
	public static CCApplication app;

	public String currentChatUser;

	public static int unreadMsgCount = 0; //消息中心消息
	public static int guanzhu_MsgCount = 0; //专栏消息
	public static int friendCount = 0; //好友请求消息
	

	
	public static int getTotalCount(){
		return unreadMsgCount+guanzhu_MsgCount;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		app = this;
		ParamManager.getInstance(app);
		
		SettingManager.getInstance();
		
		UserManager.getInstance(getApplicationContext());
		ShareSDK.initSDK(this);

		CrashApplication.getInstance(this).onCreate();
		EaseUI.getInstance().init(this, null);
		DemoHelper.getInstance().init(app);
		UEasyStreaming.initStreaming("publish3-key");
	}

}
