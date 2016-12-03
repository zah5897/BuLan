package com.mingmay.bulan.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.ui.fragment.FriendFragment;
import com.mingmay.bulan.util.http.HttpProxy;

public class SessionListTask extends AsyncTask<String, String, List<User>> {
	FriendFragment friendFragment;

	SharedPreferences spf;

	public SessionListTask(FriendFragment friendFragment) {
		this.friendFragment = friendFragment;
		spf = CCApplication.app.getSharedPreferences("session_list",
				Context.MODE_PRIVATE);
	}

	@Override
	protected List<User> doInBackground(String... arg0) {

		String URL = CCApplication.HTTPSERVER
				+ "/m_chatMessage!queryToUserRecently.action";
		List<NameValuePair> param = new ArrayList<NameValuePair>();
		User u = UserManager.getInstance().getLoginUser();
		param.add(new BasicNameValuePair("toUserId", String.valueOf(u.ID)));
		param.add(new BasicNameValuePair("ccukey", u.ccukey));
		param.add(new BasicNameValuePair("curPage", arg0[0]));
		param.add(new BasicNameValuePair("pageSize", "100"));
		List<User> sessions = null;
		try {
			HttpResponse response = new HttpProxy().post(URL, param);
			int code = response.getStatusLine().getStatusCode();

			if (code == 200) {
				String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
				JSONObject root = new JSONObject(rev);
				JSONObject body = root.getJSONObject("body");
				JSONArray userInfos = body.getJSONArray("userInfo");

				if (userInfos != null) {
					int len = userInfos.length();
					if (len > 0) {
						spf.edit().putString("sessions", userInfos.toString())
								.commit();
						sessions = new ArrayList<User>();
						for (int i = 0; i < len; i++) {
							User ur = User.jsonToSession(userInfos
									.getJSONObject(i));
							ur.isFan = 1;
							sessions.add(ur);
						}
					}

				}
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return sessions;
	}

	@Override
	protected void onPostExecute(List<User> result) {
		super.onPostExecute(result);
		if (friendFragment != null) {
			// friendFragment.callBack(result);
		}
	}

}
