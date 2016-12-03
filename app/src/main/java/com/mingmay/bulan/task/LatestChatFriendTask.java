package com.mingmay.bulan.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.ui.ChatListpage;
import com.mingmay.bulan.util.http.HttpProxy;

public class LatestChatFriendTask extends AsyncTask<String, String, Integer> {
	ArrayList<User> data;
	ChatListpage page;

	public LatestChatFriendTask(ChatListpage page) {
		this.page = page;
	}

	@Override
	protected Integer doInBackground(String... params) {
		// TODO Auto-generated method stub
		// /m_chatMessage!queryToUserRecently.action
		String URL = CCApplication.HTTPSERVER
				+ "/m_chatMessage!queryToUserRecently.action";

		List<NameValuePair> param = new ArrayList<NameValuePair>();
		param.add(new BasicNameValuePair("toUserId", String.valueOf(UserManager
				.getInstance().getLoginUser().ID)));
		param.add(new BasicNameValuePair("ccukey", UserManager.getInstance()
				.getLoginUser().ccukey));
		param.add(new BasicNameValuePair("curPage", "1"));
		param.add(new BasicNameValuePair("pageSize", "20"));
		try {
			HttpResponse response = new HttpProxy().post(URL, param);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
				// {"head":{"st":0,"msg":"消息成功返回.","cd":"g4lIP6pOMY+xYiNfD4wpKw=="},"body":{"userInfo":"","cstatus":"2"}}
				try {
					JSONObject obj = new JSONObject(rev);
					JSONObject body = obj.getJSONObject("body");
					int status = body.getInt("cstatus");
					if (status == 0) {
						JSONArray friends = body.getJSONArray("userInfo");
						int len = friends.length();
						if (len > 0) {
							data = new ArrayList<User>();
							for (int i = 0; i < len; i++) {
								User f = User.jsonToFriend(friends
										.getJSONObject(i));
								data.add(f);
							}
						}
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return 1;
			} else {
				return 0;
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}

	@Override
	protected void onPostExecute(Integer result) {
		// TODO Auto-generated method stub
		page.loadDataBallBack(data);
		super.onPostExecute(result);
	}
}
