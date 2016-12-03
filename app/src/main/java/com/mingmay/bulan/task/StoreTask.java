package com.mingmay.bulan.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.BuLanModel;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.ui.BulanDetialPage;
import com.mingmay.bulan.ui.MyStorePage;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpProxy;

public class StoreTask extends AsyncTask<String, String, Integer> {
	public static final int ACTION_LOAD = 0;
	public static final int ACTION_ADD = 1;
	public static final int ACTION_DELETE = -1;
	public static final int PAGE_SIZE = 20;
	MyStorePage myStorePage;
	BulanDetialPage detialPage;
	private int command = ACTION_LOAD;

	public StoreTask(MyStorePage myStorePage, int command) {
		this.myStorePage = myStorePage;
		this.command = command;
	}

	public StoreTask(BulanDetialPage detialPage) {
		this.detialPage = detialPage;
		this.command = ACTION_ADD;
	}

	public StoreTask(BulanDetialPage detialPage, boolean del) {
		this.detialPage = detialPage;
		this.command = ACTION_DELETE;
	}

	private ArrayList<BuLanModel> bulans;

	@Override
	protected Integer doInBackground(String... arg0) {
		String URL = CCApplication.HTTPSERVER;
		List<NameValuePair> param = new ArrayList<NameValuePair>();
		User u = UserManager.getInstance().getLoginUser();
		if (command == ACTION_LOAD) {
			URL += "/m_bp!findCollectionBulans.action";
			param.add(new BasicNameValuePair("curPage", arg0[0]));
			param.add(new BasicNameValuePair("userId", String.valueOf(u.ID)));
			param.add(new BasicNameValuePair("pageSize", String
					.valueOf(PAGE_SIZE)));
		} else if (command == ACTION_ADD) {
			URL += "/m_collection!createCollection.action";
			param.add(new BasicNameValuePair("bulanId", arg0[0]));
			param.add(new BasicNameValuePair("userId", String.valueOf(u.ID)));
			param.add(new BasicNameValuePair("ccukey", u.ccukey));
		} else if (command == ACTION_DELETE) {
			URL += "/m_collection!deleteCollection.action";
			param.add(new BasicNameValuePair("bulanId", arg0[0]));
			param.add(new BasicNameValuePair("userId", String.valueOf(u.ID)));
			param.add(new BasicNameValuePair("ccukey", u.ccukey));
		}
		try {
			HttpResponse response = new HttpProxy().post(URL, param);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
				return handleResult(rev);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return -1;
	}

	private int handleResult(String result) throws JSONException {
		JSONObject body = new JSONObject(result).getJSONObject("body");
		if (command == ACTION_LOAD) {
			JSONArray bulanArray = body.getJSONArray("bulanInfo");
			if (bulanArray != null) {
				int len = bulanArray.length();
				if (len > 0) {
					bulans = new ArrayList<BuLanModel>();
					for (int i = 0; i < len; i++) {
						bulans.add(BuLanModel.jsonToModel(bulanArray
								.getJSONObject(i)));
					}
				}
			}
		}
		return body.getInt("cstatus");
	}

	@Override
	protected void onPostExecute(Integer result) {

		if (detialPage != null) {
			if (result == 0 || result == 3) {
				if (command == ACTION_ADD) {
					detialPage.storeCallBack(true);
				} else {
					detialPage.storeCallBack(false);
				}
			} else {
				if (command == ACTION_ADD) {
					ToastUtil.show("收藏失败");
				} else {
					ToastUtil.show("删除收藏失败");
				}

			}
		} else {
			if (command == ACTION_ADD) {
				if (result == 0) {
					ToastUtil.show("收藏成功");
				} else {
					ToastUtil.show("收藏失败");
				}
			} else if (command == ACTION_DELETE) {
				if (result == 0) {
					ToastUtil.show("删除成功");
				} else {
					ToastUtil.show("删除失败");
				}
			}

		}

		super.onPostExecute(result);
	}
}
