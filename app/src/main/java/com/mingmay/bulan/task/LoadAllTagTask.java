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
import android.os.AsyncTask;
import android.text.TextUtils;

import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.PreferenceUtil;
import com.mingmay.bulan.app.TagManager;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.Tag;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.util.http.HttpProxy;

public class LoadAllTagTask extends AsyncTask<String, String, Integer> {
	public Context context;

	public LoadAllTagTask(Context context) {
		this.context = context;
	}

	@Override
	protected Integer doInBackground(String... arg0) {

		String cache = PreferenceUtil.readConfig(context, "all_tags");
		load(cache);
		// String URL = CCApplication.HTTPSERVER
		// + "/m_wardrobe!getWardrobes.action";
		String URL = CCApplication.HTTPSERVER
				+ "/m_wardrobe!findWardrobes.action";
		List<NameValuePair> param = new ArrayList<NameValuePair>();
		try {
			User user = UserManager.getInstance().getLoginUser();
			param.add(new BasicNameValuePair("userId", String.valueOf(user.ID)));
			param.add(new BasicNameValuePair("ccukey", String
					.valueOf(user.ccukey)));
			param.add(new BasicNameValuePair("curPage", "1"));
			param.add(new BasicNameValuePair("pageSize", "100"));
			HttpResponse response = new HttpProxy().post(URL, param);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
				PreferenceUtil.writeConfig(context, "all_tags", rev);
				load(rev);

				return 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	private static void load(String json) {
		if (TextUtils.isEmpty(json)) {
			return;
		}

		ArrayList<Tag> cacheTags = null;
		try {
			JSONObject obj = new JSONObject(json);
			JSONObject wardrobeInfo = obj.getJSONObject("body").getJSONObject(
					"wardrobeInfo");
			if (wardrobeInfo != null) {
				JSONArray systemtag = wardrobeInfo.optJSONArray("systemtag");
				if (systemtag != null) {
					int len = systemtag.length();
					if (len > 0) {
						cacheTags = new ArrayList<Tag>();
						for (int i = 0; i < len; i++) {
							cacheTags.add(Tag.jsonToTag(systemtag
									.getJSONObject(i)));
						}
					}
				}

			}
		} catch (Exception e) {
		}
		TagManager.allTags = cacheTags;
	}

}
