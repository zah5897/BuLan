package com.mingmay.bulan.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.util.http.HttpProxy;

public class AddTagTask extends AsyncTask<String, String, Integer> {
	@Override
	protected Integer doInBackground(String... arg0) {
		// TODO Auto-generated method stub

		// String URL = CCApplication.HTTPSERVER + "/m_tags!addTags.action";
		String URL = CCApplication.HTTPSERVER
				+ "/m_wardrobe!addLatelyWardRobes.action";

		try {
			List<NameValuePair> param = new ArrayList<NameValuePair>();
			User u = UserManager.getInstance().getLoginUser();
			param.add(new BasicNameValuePair("userId", String.valueOf(u.ID)));
			param.add(new BasicNameValuePair("wardrobeId", arg0[0]));
			param.add(new BasicNameValuePair("ccukey", u.ccukey));
			HttpResponse response = new HttpProxy().post(URL, param);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
				JSONObject obj = new JSONObject(rev);
				int cstatus = obj.getJSONObject("body").getInt("cstatus");
				return cstatus;
			} else {
				return 2;
			}
		} catch (Exception e) {
		}
		return 2;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
	}
}
