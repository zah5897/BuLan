//package com.mingmay.bulan.app;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import android.content.Context;
//
//import com.hyphenate.EMValueCallBack;
//import com.hyphenate.chat.EMClient;
//import com.hyphenate.easeui.domain.EaseUser;
//import com.hyphenate.easeui.utils.EaseCommonUtils;
//import com.hyphenate.util.EMLog;
//import com.parse.FindCallback;
//import com.parse.GetCallback;
//import com.parse.Parse;
//import com.parse.ParseException;
//import com.parse.ParseFile;
//import com.parse.ParseObject;
//import com.parse.ParseQuery;
//import com.parse.SaveCallback;
//
//public class ParseManager {
//
//	private static final String TAG = ParseManager.class.getSimpleName();
//
//	private static final String ParseAppID = "UUL8TxlHwKj7ZXEUr2brF3ydOxirCXdIj9LscvJs";
//	private static final String ParseClientKey = "B1jH9bmxuYyTcpoFfpeVslhmLYsytWTxqYqKQhBJ";
//
//	// private static final String ParseAppID = "task";
//	// private static final String ParseClientKey = "123456789";
//
//	private static final String CONFIG_TABLE_NAME = "hxuser";
//	private static final String CONFIG_USERNAME = "username";
//	private static final String CONFIG_NICK = "nickname";
//	private static final String CONFIG_AVATAR = "avatar";
//
//	private Context appContext;
//	private static ParseManager instance = new ParseManager();
//
//	private ParseManager() {
//	}
//
//	public static ParseManager getInstance() {
//		return instance;
//	}
//
//	public void onInit(Context context) {
//		this.appContext = context.getApplicationContext();
//		Parse.enableLocalDatastore(appContext);
//		Parse.initialize(context, ParseAppID, ParseClientKey);
//		// Parse.initialize(new Parse.Configuration.Builder(appContext)
//		// .applicationId(ParseAppID)
//		// .server("http://114.215.141.221:1337/parse/")
//		// .build());
//	}
//
//	public boolean updateParseNickName(final String nickname) {
//		String username = EMClient.getInstance().getCurrentUser();
//		ParseQuery<ParseObject> pQuery = ParseQuery.getQuery(CONFIG_TABLE_NAME);
//		pQuery.whereEqualTo(CONFIG_USERNAME, username);
//		ParseObject pUser = null;
//		try {
//			pUser = pQuery.getFirst();
//			if (pUser == null) {
//				return false;
//			}
//			pUser.put(CONFIG_NICK, nickname);
//			pUser.save();
//			return true;
//		} catch (ParseException e) {
//			if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
//				pUser = new ParseObject(CONFIG_TABLE_NAME);
//				pUser.put(CONFIG_USERNAME, username);
//				pUser.put(CONFIG_NICK, nickname);
//				try {
//					pUser.save();
//					return true;
//				} catch (ParseException e1) {
//					e1.printStackTrace();
//					EMLog.e(TAG, "parse error " + e1.getMessage());
//				}
//
//			}
//			e.printStackTrace();
//			EMLog.e(TAG, "parse error " + e.getMessage());
//		}
//		return false;
//	}
//
//	public void asyncGetCurrentUserInfo(final EMValueCallBack<EaseUser> callback) {
//		final String username = EMClient.getInstance().getCurrentUser();
//		asyncGetUserInfo(username, new EMValueCallBack<EaseUser>() {
//
//			@Override
//			public void onSuccess(EaseUser value) {
//				callback.onSuccess(value);
//			}
//
//			@Override
//			public void onError(int error, String errorMsg) {
//				if (error == ParseException.OBJECT_NOT_FOUND) {
//					ParseObject pUser = new ParseObject(CONFIG_TABLE_NAME);
//					pUser.put(CONFIG_USERNAME, username);
//				} else {
//					callback.onError(error, errorMsg);
//				}
//			}
//		});
//	}
//
//	public void asyncGetUserInfo(final String username,
//			final EMValueCallBack<EaseUser> callback) {
//		ParseQuery<ParseObject> pQuery = ParseQuery.getQuery(CONFIG_TABLE_NAME);
//		pQuery.whereEqualTo(CONFIG_USERNAME, username);
//
//	}
//
//	public String uploadParseAvatar(byte[] data) {
//		String username = EMClient.getInstance().getCurrentUser();
//		ParseQuery<ParseObject> pQuery = ParseQuery.getQuery(CONFIG_TABLE_NAME);
//		pQuery.whereEqualTo(CONFIG_USERNAME, username);
//		ParseObject pUser = null;
//		try {
//			pUser = pQuery.getFirst();
//			if (pUser == null) {
//				pUser = new ParseObject(CONFIG_TABLE_NAME);
//				pUser.put(CONFIG_USERNAME, username);
//			}
//			ParseFile pFile = new ParseFile(data);
//			pUser.put(CONFIG_AVATAR, pFile);
//			pUser.save();
//			return pFile.getUrl();
//		} catch (ParseException e) {
//			if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
//				try {
//					pUser = new ParseObject(CONFIG_TABLE_NAME);
//					pUser.put(CONFIG_USERNAME, username);
//					ParseFile pFile = new ParseFile(data);
//					pUser.put(CONFIG_AVATAR, pFile);
//					pUser.save();
//					return pFile.getUrl();
//				} catch (ParseException e1) {
//					e1.printStackTrace();
//					EMLog.e(TAG, "parse error " + e1.getMessage());
//				}
//			} else {
//				e.printStackTrace();
//				EMLog.e(TAG, "parse error " + e.getMessage());
//			}
//		}
//		return null;
//	}
//
//}
