package com.mingmay.bulan.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mingmay.bulan.app.err.ErrorCode;
import com.mingmay.bulan.app.listener.ContactListener;
import com.mingmay.bulan.app.listener.LoginListener;
import com.mingmay.bulan.model.Tag;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.ui.LoginPage;
import com.mingmay.bulan.ui.SettingPage;
import com.mingmay.bulan.util.CharacterParser;
import com.mingmay.bulan.util.PinyinComparator;
import com.mingmay.bulan.util.PropertyUtil;
import com.mingmay.bulan.util.http.HttpUtils;

/**
 * 
 * @author zah
 *
 */
public class UserManager {
	private static UserManager usernManager;
	private User loginUser;

	private Map<String, User> userInfos = new HashMap<String, User>();

	public User getUser(long userId) {
		String user_id=String.valueOf(userId);
		if (userInfos.containsKey(user_id)) {
			User u = userInfos.get(user_id);
			if (u != null) {
				return u;
			}
		}
		getUserinfo(userId);
		return null;
	}
	public User getUser(String userId){
		return getUser(Long.parseLong(userId));
	}
	
	public void saveUser(User friend) {
		userInfos.put(String.valueOf(friend.ID), friend);
	}

	private UserManager(Context context) {
		String info = PropertyUtil.getStringValue("user_info");
		if (!TextUtils.isEmpty(info)) {
			try {
				loginUser = User.jsonToUser(new JSONObject(info));
				saveUser(loginUser);
			} catch (JSONException e) {
				e.printStackTrace();
				loginUser = null;
				PropertyUtil.putValue("user_info", "");
			}
		}
	}

	public User getLoginUser() {
		return loginUser;
	}

	public static UserManager getInstance(Context context) {
		if (usernManager == null) {
			usernManager = new UserManager(context);
		}
		return usernManager;
	}

	public static UserManager getInstance() {
		return getInstance(CCApplication.app);
	}

	public void getUserinfo(long friendId) {
		
		
		if(userInfos.containsKey(String.valueOf(friendId))){
			Intent i = new Intent();
			i.setAction("refresh.friend");
			CCApplication.app.sendBroadcast(i);
			return;
		}
		
		String loginUrl = CCApplication.HTTPSERVER
				+ "/m_user!getUserById.action";

		RequestParams params = new RequestParams();
		params.put("userId", loginUser.ID);
		params.put("ccukey", loginUser.ccukey);
		params.put("userId_info", String.valueOf(friendId));
		HttpUtils.post(loginUrl, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				JSONObject body = response.optJSONObject("body");
				if (body != null) {
					JSONObject userObj = body.optJSONObject("userInfo");
					if (userObj != null) {
						User friend = User.jsonToUser(userObj);
						userInfos.put(String.valueOf(friend.ID), friend);
						Intent i = new Intent();
						i.setAction("refresh.friend");
						CCApplication.app.sendBroadcast(i);
					}

				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
			}
		});
	}

	public List<Tag> loadDefaultTags() {
		String defaultTags = PropertyUtil.getStringValue("my_tags");
		List<Tag> tags = null;
		if (!TextUtils.isEmpty(defaultTags)) {
			try {
				JSONArray tagArray = new JSONArray(defaultTags);
				int len = tagArray.length();
				if (len > 0) {
					tags = new ArrayList<Tag>();
					for (int i = 0; i < len; i++) {
						tags.add(Tag.jsonToTag(tagArray.getJSONObject(i)));
					}
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return tags;
	}

	public void login(String name, String password, final LoginListener listener) {
		String loginUrl = CCApplication.HTTPSERVER + "/m_login!login.action";

		RequestParams params = new RequestParams();
		params.put("cellPhone", name);
		params.put("password", password);
		HttpUtils.post(loginUrl, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				if (response == null) {
					listener.onFailure(ErrorCode.SYSTEM_ERROR);
					return;
				}
				JSONObject body = response.optJSONObject("body");
				if (body == null) {
					listener.onFailure(ErrorCode.SYSTEM_ERROR);
					return;
				}
				int cstatus = body.optInt("cstatus");
				JSONObject userObj = body.optJSONObject("userInfo");
				if (userObj == null) {
					listener.onFailure(cstatus);
					return;
				}
				JSONArray myTags = userObj.optJSONArray("wardrobeInfo");
				if (myTags != null) {
					PropertyUtil.putValue("my_tags", myTags.toString());
				}

				loginUser = User.jsonToUser(userObj);
				if (loginUser.ID <= 0) {
					listener.onFailure(cstatus);
					return;
				}
				userInfos.put(String.valueOf(loginUser.ID), loginUser);
				PropertyUtil.putValue("user_info", userObj.toString());
				HXCreateAndLogin(listener);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				listener.onFailure(statusCode);
			}
		});
	}

	public void regist(String mobileStr, String code, String nick,
			String password, int gender, final LoginListener listener) {

		String url = CCApplication.HTTPSERVER;
		url += "/m_user!reg.action";
		RequestParams params = new RequestParams();
		params.put("cellPhone", mobileStr);
		params.put("verificationCode", code);
		params.put("firstName", nick);
		params.put("gender", gender);
		params.put("password", password);
		HttpUtils.post(url, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				JSONObject body = response.optJSONObject("body");
				int cstatus = body.optInt("cstatus");
				if (cstatus == 0) {
					JSONObject userObj = body.optJSONObject("userInfo");
					loginUser = User.jsonToUser(userObj);
					PropertyUtil.putValue("user_info", userObj.toString());
					HXCreateAndLogin(listener);
				} else {
					listener.onFailure(cstatus);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				listener.onFailure(2);
			}
		});
	}

	private void HXCreateAndLogin(final LoginListener listener) {
		new Thread(new Runnable() {
			public void run() {
				// 调用sdk注册方法
				String username = String.valueOf(loginUser.ID);
				String password = loginUser.ccukey;
				try {

					EMClient.getInstance().createAccount(username, password);
					HXlogin(username, password, listener);
				} catch (HyphenateException e) {
					e.printStackTrace();
					int errorCode = e.getErrorCode();
					if (errorCode == EMError.USER_ALREADY_EXIST) {
						HXlogin(username, password, listener);
					} else {
					}
				}

			}
		}).start();
	}

	private void HXlogin(String username, String password,
			final LoginListener listener) {
		EMClient.getInstance().login(username, password, new EMCallBack() {

			@Override
			public void onSuccess() {
				listener.onSuccess(loginUser);
				EMClient.getInstance().groupManager().loadAllGroups();
				EMClient.getInstance().chatManager().loadAllConversations();

				// update current user's display name for APNs
				boolean updatenick = EMClient
						.getInstance()
						.updateCurrentUserNick(
								UserManager.getInstance().getLoginUser().firstName);
				if (!updatenick) {
					Log.e("LoginActivity", "update current user nick fail");
				}

				// get user's info (this should be get from App's server or 3rd
				// party service)

			}

			@Override
			public void onProgress(int arg0, String arg1) {
				listener.onFailure(arg0);
				logout();
			}

			@Override
			public void onError(int arg0, String arg1) {
				listener.onFailure(arg0);
				logout();
			}
		});
	}

	// public void loadUserDetail() {
	// String URL = CCApplication.HTTPSERVER + "/m_user!getUserById.action";
	// RequestParams params = new RequestParams();
	// params.put("userId", loginUser.ID);
	// HttpUtils.post(URL, params, new JsonHttpResponseHandler() {
	// @Override
	// public void onSuccess(int statusCode, Header[] headers,
	// JSONObject response) {
	// JSONObject body = response.optJSONObject("body");
	// if (body != null) {
	// JSONObject userInfo = body.optJSONObject("userInfo");
	// if (userInfo != null) {
	// User user = User.jsonToUser(userInfo);
	// if(user.ID==loginUser.ID){
	//
	// }
	//
	// loginUser.address = user.address;
	// loginUser.firstName = user.firstName;
	// loginUser.signature = user.signature;
	// loginUser.gender = user.gender;
	// loginUser.userImg = user.userImg;
	// CCApplication.needRefreshUserInfo = true;
	// PropertyUtil.putValue("user_info", userInfo.toString());
	// }
	// }
	// }
	// });
	// }

	public void loadContact(int cursor, final ContactListener listener) {
		final PinyinComparator pinyinComparator = new PinyinComparator();
		final CharacterParser characterParser = new CharacterParser();
		String URL = CCApplication.HTTPSERVER + "/m_user!getFriends.action";
		RequestParams params = new RequestParams();
		params.put("userId", String.valueOf(loginUser.ID));
		params.put("curPage", cursor);
		params.put("pageSize", "100");
		HttpUtils.post(URL, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				JSONObject body = response.optJSONObject("body");
				if (body != null) {
					JSONArray friends = body.optJSONArray("userInfo");
					if (friends != null) {
						int len = friends.length();
						List<User> data = new ArrayList<User>();
						for (int i = 0; i < len; i++) {
							User u = User.jsonToFriend(friends.optJSONObject(i));
							userInfos.put(String.valueOf(u.ID), u);
							u.isFan = 1;
							String pinyin = characterParser
									.getSelling(u.firstName);
							String sortString = pinyin.substring(0, 1)
									.toUpperCase();
							if (sortString.matches("[A-Z]")) {
								u.firstChar = sortString.toUpperCase();
							} else {
								u.firstChar = "#";
							}
							data.add(u);
						}
						Collections.sort(data, pinyinComparator);
						listener.onSuccess(data);
					} else {
						listener.onFailure(-1);
					}
				} else {
					listener.onFailure(-1);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				super.onFailure(statusCode, headers, responseString, throwable);
				listener.onFailure(-1);
			}
		});

	}

	public void updateUser(User user) {
		this.loginUser = user;
		userInfos.put(String.valueOf(user.ID), user);
	}

	public void logout() {
		PropertyUtil.putValue("user_info", "");
		loginUser = null;

		EMClient.getInstance().logout(true,null);
	}

}
