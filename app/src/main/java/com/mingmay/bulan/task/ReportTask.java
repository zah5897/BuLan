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
import com.mingmay.bulan.ui.Submit;
import com.mingmay.bulan.util.NetWorkUtil;
import com.mingmay.bulan.util.ProgressDialogUtil;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpProxy;

public class ReportTask extends AsyncTask<String, String, Integer> {

	private Submit page;

	public ReportTask(Submit page) {
		this.page = page;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		ProgressDialogUtil.showProgress(page, "提交中,感谢您的支持!");
	}

	@Override
	protected Integer doInBackground(String... params) {
		String URL = CCApplication.HTTPSERVER + "/m_complaint!add.action";
		List<NameValuePair> param = new ArrayList<NameValuePair>();
		param.add(new BasicNameValuePair("userId", UserManager.getInstance()
				.getLoginUser().ID + ""));
		param.add(new BasicNameValuePair("complaintType", "3"));
		param.add(new BasicNameValuePair("complaintLinkId", ""));
		param.add(new BasicNameValuePair("complaintTxt", params[0]));
		param.add(new BasicNameValuePair("ccukey", UserManager.getInstance()
				.getLoginUser().ccukey));

		try {
			HttpResponse response = new HttpProxy().post(URL, param);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
				JSONObject root = new JSONObject(rev);
				int cstatus = root.getJSONObject("body").optInt("cstatus");
				return cstatus;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 2;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		ProgressDialogUtil.dismiss();
		if (result == 0) {
			ToastUtil.show("提交成功!");
			page.finish();
		} else {
			if (NetWorkUtil.isNetworkAvailable(page)) {
				ToastUtil.show("提交失败...");
			} else {
				ToastUtil.show("网络不可用");
			}
		}
	}
}
