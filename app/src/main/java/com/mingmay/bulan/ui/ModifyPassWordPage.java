package com.mingmay.bulan.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.mingmay.bulan.R;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpProxy;

public class ModifyPassWordPage extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_modify_password);
		findViewById(R.id.to_modify).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				modify(v);
			}
		});
	}

	public void back(View v) {
		finish();
	}

	public void modify(View v) {

		final String oldPwd = ((EditText) findViewById(R.id.old_pwd)).getText()
				.toString();
		final String newPwd = ((EditText) findViewById(R.id.new_pwd)).getText()
				.toString();
		String makeSurePwd = ((EditText) findViewById(R.id.makesure_pwd))
				.getText().toString();
		if (TextUtils.isEmpty(oldPwd)) {
			ToastUtil.show("请输入原始密码");
			return;
		}
		if (TextUtils.isEmpty(newPwd)) {
			ToastUtil.show("请输入新密码");
			return;
		}
		if (!newPwd.equals(makeSurePwd)) {
			ToastUtil.show("新密码两次不一致，请确认!");
			return;
		}

		new Thread() {
			public void run() {
				new Thread() {
					public void run() {
						User loginUser = UserManager.getInstance()
								.getLoginUser();
						String url = CCApplication.HTTPSERVER;
						url += "/m_user!updatePassword.action";
						List<NameValuePair> param = new ArrayList<NameValuePair>();
						param.add(new BasicNameValuePair("userId", String
								.valueOf(loginUser.ID)));
						param.add(new BasicNameValuePair("ccukey",
								loginUser.ccukey));
						param.add(new BasicNameValuePair("oldPassword", oldPwd));
						param.add(new BasicNameValuePair("password", newPwd));
						int cstatus = -1;
						try {
							HttpResponse response = new HttpProxy().post(url,
									param);
							int code = response.getStatusLine().getStatusCode();
							if (code == 200) {
								String rev = EntityUtils.toString(response
										.getEntity());// 返回json格式：
								JSONObject obj = new JSONObject(rev);
								cstatus = obj.getJSONObject("body").optInt(
										"cstatus");
							}
						} catch (Exception e) {

						}
						handler.sendEmptyMessage(cstatus);
					}
				}.start();

			};
		}.start();
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				ToastUtil.show("修改密码成功");
				UserManager.getInstance().logout();
				Intent toLogin = new Intent(ModifyPassWordPage.this,
						LoginPage.class);
				toLogin.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				finish();
				startActivity(toLogin);
				break;
			default:
				ToastUtil.show("修改密码失败");
				break;
			}
		};
	};
}
