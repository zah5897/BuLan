package com.mingmay.bulan.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.mingmay.bulan.MainActivity;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.NotifyModel;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.util.http.HttpProxy;

public class TipTask extends AsyncTask<String, String, Integer> {
	public static final int  PAGE_SIZE=100;
	private List<NotifyModel> notifies;
	MainActivity main;
	public TipTask(MainActivity main){
		this.main=main;
	}
	@Override
	protected Integer doInBackground(String... arg0) {
		// TODO Auto-generated method stub

		String URL = CCApplication.HTTPSERVER + "/m_notice!getNotreadCount.action";

		try {
			List<NameValuePair> param = new ArrayList<NameValuePair>();
			User u =UserManager.getInstance().getLoginUser();
			param.add(new BasicNameValuePair("userId", String.valueOf(u.ID)));
			HttpResponse response = new HttpProxy().post(URL, param);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
				JSONObject obj = new JSONObject(rev);
				int count = obj.getJSONObject("body").optInt("count");
				 return count;
			}
		} catch (Exception e) {
		}
		return 0;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		//main.callback(result);
	}
}
