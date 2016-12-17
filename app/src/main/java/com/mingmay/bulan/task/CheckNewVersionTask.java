package com.mingmay.bulan.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.util.ProgressDialogUtil;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpProxy;

public class CheckNewVersionTask extends AsyncTask<String, String, String> {
	private Activity activity;
	private String SerVersion;

	public CheckNewVersionTask(Activity activity) {
		this.activity = activity;
	}

	@Override
	protected String doInBackground(String... arg0) {

		String URL = CCApplication.HTTPSERVER
				+ "/m_appMobile!findMaxApp.action";

		try {
			List<NameValuePair> param = new ArrayList<NameValuePair>();
			HttpResponse response = new HttpProxy().post(URL, param);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
				JSONObject obj = new JSONObject(rev);
				JSONObject body = obj.getJSONObject("body");
				JSONObject appInfo = body.getJSONObject("appInfo");
				String version = appInfo.optString("ver");
				SerVersion=version;
				if (!TextUtils.isEmpty(version)) {
					String versionName = getVersionName();
					if(versionName.compareTo(SerVersion)<0){
						return appInfo.optString("url");
					}
				}
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getVersionName() throws NameNotFoundException {
		PackageManager packageManager = activity.getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo = packageManager.getPackageInfo(
				activity.getPackageName(), 0);
		String version = packInfo.versionName;
		return version;
	}

	@Override
	protected void onPostExecute(final String result) {
		ProgressDialogUtil.dismiss();
		if (!TextUtils.isEmpty(result)&&result.startsWith("http")) {
			AlertDialog.Builder builder = new Builder(activity);
			builder.setTitle("提示");
			builder.setMessage("有更高版本可以更新");
			builder.setNegativeButton("取消", null);
			builder.setPositiveButton("更新",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							new UpdateTask(activity)
									.execute(
											result,
											SerVersion);
							dialog.dismiss();
						}
					});
			builder.create().show();

		} else {
			if (activity != null) {
				ToastUtil.show("当前已经是最新版本!");
			}

		}
		super.onPostExecute(result);
	}
}
