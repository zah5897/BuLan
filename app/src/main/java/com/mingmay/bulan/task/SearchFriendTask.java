package com.mingmay.bulan.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.ui.friend.SearchFriend;
import com.mingmay.bulan.util.http.HttpProxy;

public class SearchFriendTask extends AsyncTask<String, String, Integer> {
	private ArrayList<User> data;
	private SearchFriend page;

	public SearchFriendTask(SearchFriend page) {
		this.page = page;
	}

	@Override
	protected Integer doInBackground(String... arg0) {
		// TODO Auto-generated method stub

		String URL = CCApplication.HTTPSERVER + "/m_user!searchUser.action";

		List<NameValuePair> param = new ArrayList<NameValuePair>();
		param.add(new BasicNameValuePair("userId", String
				.valueOf(UserManager.getInstance().getLoginUser().ID)));
		param.add(new BasicNameValuePair("keyWord", arg0[0]));
		param.add(new BasicNameValuePair("curPage", arg0[1]));
		param.add(new BasicNameValuePair("pageSize", "20"));
		try {
			HttpResponse response = new HttpProxy().post(URL, param);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
				JSONObject obj = new JSONObject(rev);
				JSONArray friends = obj.getJSONObject("body").getJSONArray(
						"userInfo");
				int len = friends.length();
				data = new ArrayList<User>();
				for (int i = 0; i < len; i++) {
					User f = User.jsonToSearchFriend(friends.getJSONObject(i));
					data.add(f);
				}

				return 1;
			} else {
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	protected void onPostExecute(Integer result) {
		page.callBack(data);
		super.onPostExecute(result);
	}
}
