package com.mingmay.bulan.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMChatManager;
import com.hyphenate.chat.EMClient;
import com.mingmay.bulan.R;
import com.mingmay.bulan.app.UserManager;

public class SettingPage extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_setting);
		findViewById(R.id.to_modify_pwd).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(SettingPage.this,
								ModifyPassWordPage.class));
					}
				});
	}

	public void back(View v) {
		finish();
	}

	public void logout(View v) {
		UserManager.getInstance().logout();
		EMClient.getInstance().logout(true,new EMCallBack() {
			@Override
			public void onSuccess() {
				Intent toLogin = new Intent(SettingPage.this, LoginPage.class);
				toLogin.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				finish();
				startActivity(toLogin);
			}

			@Override
			public void onProgress(int progress, String status) {

			}

			@Override
			public void onError(int code, String error) {

			}
		});

	}

}
