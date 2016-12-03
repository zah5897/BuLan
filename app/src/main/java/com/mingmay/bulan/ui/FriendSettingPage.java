package com.mingmay.bulan.ui;

import org.apache.http.Header;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

import com.hyphenate.chat.EMChatManager;
import com.hyphenate.chat.EMClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mingmay.bulan.R;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.util.ProgressDialogUtil;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpUtils;

public class FriendSettingPage extends Activity {
	ToggleButton blockControll;
	private User friend;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		friend = (User) getIntent().getSerializableExtra("friend");
		setContentView(R.layout.layout_friend_setting);
		findViewById(R.id.report_friend).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						ToastUtil.show("举报成功");
					}
				});
		blockControll = (ToggleButton) findViewById(R.id.block_controll);
		if (friend.isFan == 3) {
			blockControll.setChecked(true);
		} else {
			blockControll.setChecked(false);
		}
		blockControll.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean checked) {
				addBlock(checked);
			}
		});
	}

	public void back(View v) {
		onBackPressed();
	}

	private void addBlock(boolean addOrCancel) {

		String url = CCApplication.HTTPSERVER
				+ "/m_relationShip!addBlackRElationShip.action";
		if (!addOrCancel) {
			url = CCApplication.HTTPSERVER
					+ "m_relationShip!delBlackRElationShip.action";
			ProgressDialogUtil.showProgress(this, "正在 取消黑名单。。。");
		} else {
			ProgressDialogUtil.showProgress(this, "正在 加入黑名单。。。");
		}

		RequestParams param = new RequestParams();
		User u = UserManager.getInstance().getLoginUser();
		param.put("userId_info", String.valueOf(friend.ID));
		param.put("userId", String.valueOf(u.ID));
		param.put("ccukey", u.ccukey);
		HttpUtils.post(url, param, new JsonHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				int status = response.optJSONObject("body").optInt("cstatus");
				if (status == 0) {
					ToastUtil.show("操作成功");
				} else {
					ToastUtil.show("操作失败");
				}
				ProgressDialogUtil.dismiss();
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				super.onFailure(statusCode, headers, responseString, throwable);
				ProgressDialogUtil.dismiss();
				ToastUtil.show("操作失败");
			}
		});
	}

	public void toDeleteFriend(View v) {

		String URL = CCApplication.HTTPSERVER
				+ "/m_relationShip!delRelationShip.action";
		RequestParams param = new RequestParams();
		User u = UserManager.getInstance().getLoginUser();
		param.put("userId_info", String.valueOf(friend.ID));
		param.put("userId", String.valueOf(u.ID));
		param.put("ccukey", u.ccukey);
		HttpUtils.post(URL, param, new JsonHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				int status = response.optJSONObject("body").optInt("cstatus");
				if (status == 0) {
					ToastUtil.show("成功删除好友");
					EMClient.getInstance().chatManager().deleteConversation(String.valueOf(friend.ID), true);
					
					Intent i = new Intent();
					i.setAction("del.friend");
					i.putExtra("type", 2);
					i.putExtra("text", String.valueOf(friend.ID));
					sendBroadcast(i);
					onBackPressed();
				} else {
					ToastUtil.show("删除好友失败");
				}
				ProgressDialogUtil.dismiss();
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				super.onFailure(statusCode, headers, responseString, throwable);
				ProgressDialogUtil.dismiss();
				ToastUtil.show("删除好友失败");
			}
		});
	}

	@Override
	public void onBackPressed() {
		Intent i = new Intent(this, FriendInfoPage.class);
		i.putExtra("friend", friend);
		startActivity(i);
		super.onBackPressed();
	}
}
