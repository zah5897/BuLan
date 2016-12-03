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
import android.text.TextUtils;

import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.BuLanModel;
import com.mingmay.bulan.model.Tag;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.ui.BulanDetialPage;
import com.mingmay.bulan.util.http.HttpProxy;

public class BuLanDetialTask extends AsyncTask<String, String, Integer> {
	BuLanModel model;
	BulanDetialPage page;

	public BuLanDetialTask(BulanDetialPage page) {
		this.page = page;
	}

	@Override
	protected Integer doInBackground(String... arg0) {
		// TODO Auto-generated method stub

		String URL = CCApplication.HTTPSERVER + "/m_bp!getBulan.action";

		List<NameValuePair> param = new ArrayList<NameValuePair>();
		User u=UserManager.getInstance().getLoginUser();
		param.add(new BasicNameValuePair("bulanId",arg0[0]));
		param.add(new BasicNameValuePair("userId",String.valueOf(u.ID)));
		try {
			HttpResponse response = new HttpProxy().post(URL, param);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
				JSONObject root=new JSONObject(rev);
				model=BuLanModel.jsonToModel(root.getJSONObject("body").getJSONObject("bulanInfo"));
				
				return 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	private static ArrayList<Tag> load(String json) {
		if (TextUtils.isEmpty(json)) {
			return null;
		}

		ArrayList<Tag> cacheTags = null;
		try {
			JSONObject obj = new JSONObject(json);
			JSONArray tagArray = obj.getJSONObject("body").getJSONArray(
					"wardrobeInfo");
			if (tagArray != null) {
				int len = tagArray.length();
				if (len > 0) {
					cacheTags = new ArrayList<Tag>();
					for (int i = 0; i < len; i++) {
						cacheTags.add(Tag.jsonToTag(tagArray.getJSONObject(i)));
					}
				}
			}
		} catch (Exception e) {

		}
		return cacheTags;
	}

	@Override
	protected void onPostExecute(Integer result) {
		page.callBack(model);
		super.onPostExecute(result);
	}
}
