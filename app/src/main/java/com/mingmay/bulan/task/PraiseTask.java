package com.mingmay.bulan.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;

import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.ui.BulanDetialPage;
import com.mingmay.bulan.util.http.HttpProxy;

public class PraiseTask extends AsyncTask<String, String, Integer> {
	BulanDetialPage page;

	public PraiseTask(BulanDetialPage page) {
		this.page = page;
	}

	@Override
	protected Integer doInBackground(String... arg0) {
		List<NameValuePair> param = new ArrayList<NameValuePair>();
		User u = UserManager.getInstance().getLoginUser();
		String URL = CCApplication.HTTPSERVER + "/m_praise!createPraise.action";
		param.add(new BasicNameValuePair("bulanId", arg0[0]));
		param.add(new BasicNameValuePair("userId", String.valueOf(u.ID)));
		param.add(new BasicNameValuePair("ccukey", u.ccukey));
		try {
			HttpResponse response = new HttpProxy().post(URL, param);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return -1;
	}

	@Override
	protected void onPostExecute(Integer result) {
		page.praiseCallBack(result);
		super.onPostExecute(result);
	}
}
