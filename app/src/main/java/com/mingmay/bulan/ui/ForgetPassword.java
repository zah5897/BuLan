package com.mingmay.bulan.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.mingmay.bulan.R;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpProxy;

public class ForgetPassword extends Activity {
	private EditText mobileView;
	private String mobileStr;

	private TextView loadCodeView;

	private EditText validateCode;
	private EditText newPwd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_forget_pwd);
		mobileView = (EditText) findViewById(R.id.mobile);
		validateCode = (EditText) findViewById(R.id.validate_code);
		newPwd = (EditText) findViewById(R.id.new_pwd);
		loadCodeView = (TextView) findViewById(R.id.get_validate_code);
		loadCodeView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				getCode();
			}
		});
		findViewById(R.id.next_step).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				forget();
			}
		});
	}

	public void back(View v) {
		finish();
	}

	private void getCode() {
		mobileStr = mobileView.getText().toString();
		if (TextUtils.isEmpty(mobileStr) || mobileStr.length() != 11) {
			ToastUtil.show("手机号不能为空");
			return;
		}
		timer();
		new Thread() {
			public void run() {
				String url = CCApplication.HTTPSERVER;
				url += "/m_phonecode!sendVerificationCodeService.action";
				List<NameValuePair> param = new ArrayList<NameValuePair>();
				param.add(new BasicNameValuePair("cellPhone", mobileStr));
				param.add(new BasicNameValuePair("type", "2"));
				try {
					HttpResponse response = new HttpProxy().post(url, param);
					int code = response.getStatusLine().getStatusCode();
					if (code == 200) {
						String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
						JSONObject obj = new JSONObject(rev);
						int cstatus = obj.getJSONObject("body").getInt(
								"cstatus");
						Bundle data = new Bundle();
						data.putInt("code", cstatus);
						Message msg = handler.obtainMessage();
						msg.setData(data);
						msg.what = 11;
						handler.sendMessage(msg);
					} else {
						handler.sendEmptyMessage(-1);
					}
				} catch (Exception e) {
					handler.sendEmptyMessage(-1);
				}
			}
		}.start();

	}

	private void forget() {
		if (TextUtils.isEmpty(mobileStr)) {
			ToastUtil.show("手机号为空");
			return;
		}
		final String code = validateCode.getText().toString().trim();

		if (TextUtils.isEmpty(code)) {
			ToastUtil.show("验证码不能为空");
			return;
		}

		final String newPassword = newPwd.getText().toString();

		if (TextUtils.isEmpty(newPassword)) {
			ToastUtil.show("新密码不能为空");
			return;
		}

		new Thread() {
			public void run() {
				String url = CCApplication.HTTPSERVER;
				url += "/m_user!wangjiPassword.action";
				List<NameValuePair> param = new ArrayList<NameValuePair>();
				param.add(new BasicNameValuePair("cellPhone", mobileStr));
				param.add(new BasicNameValuePair("verificationCode", code));
				param.add(new BasicNameValuePair("password", newPassword));
				try {
					HttpResponse response = new HttpProxy().post(url, param);
					int code = response.getStatusLine().getStatusCode();
					if (code == 200) {
						String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
						JSONObject obj = new JSONObject(rev);
						JSONObject body = obj.getJSONObject("body");
						int cstatus = body.getInt("cstatus");
						if (cstatus == 0) {
							Bundle data = new Bundle();
							data.putString("new_pwd", newPassword);
							Message msg = handler.obtainMessage();
							msg.setData(data);
							msg.what = 1;
							handler.sendMessage(msg);
						} else {
							handler.sendEmptyMessage(cstatus);
						}
					} else {
						handler.sendEmptyMessage(-2);
					}
				} catch (Exception e) {
					handler.sendEmptyMessage(-2);
				}
			}
		}.start();

	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 100:
				loadCodeView.setText("已发送(" + timeUp + ")");
				if (timeUp < 0) {
					loadCodeView.setText("获取验证码");
					loadCodeView.setEnabled(true);
				}
				break;
			case 11:
				ToastUtil.show("验证码稍后将以短信的方式发生到您输入的手机号码,请注意查收!");
				break;
			case -1:
				ToastUtil.show("获取验证码失败");
				break;
			case 0: {
				new AlertDialog.Builder(ForgetPassword.this)
						.setMessage(
								"找回成功,您的新密码是："
										+ msg.getData().getString("new_pwd"))
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										// TODO Auto-generated method stub
										arg0.dismiss();
										finish();
									}
								}).show();
			}
				break;

			case 1:
				Intent toLogin = new Intent(ForgetPassword.this,
						LoginPage.class);
				ForgetPassword.this.startActivity(toLogin);
				finish();
				// PropertyUtil.putValue(ForgetPassword.this, "user_info", "");
				ToastUtil.show("找回密码成功,请重新登录");
				break;
			case -2:
				ToastUtil.show("找回密码失败");
				break;
			default:

				break;
			}
		}
	};

	private int timeUp = 0;

	private void timer() {
		timeUp = 60;
		loadCodeView.setEnabled(false);
		new Thread() {
			@Override
			public void run() {
				while (timeUp >= 0) {
					handler.sendEmptyMessage(100);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					timeUp--;
				}
				handler.sendEmptyMessage(100);
			}
		}.start();

	}
}
