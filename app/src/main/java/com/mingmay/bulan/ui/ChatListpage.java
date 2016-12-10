package com.mingmay.bulan.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.mingmay.bulan.R;
import com.mingmay.bulan.adapter.FriendAdapter;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.task.LatestChatFriendTask;

public class ChatListpage extends Activity implements OnClickListener {
	private ListView mListView;
//	private ArrayList<User> mData;
	private FriendAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_latest_chat);
		initView();
		loadData();
	}

	private void initView() {
		ImageView back = (ImageView) findViewById(R.id.left_btn);
		back.setImageResource(R.drawable.back_selector);
		back.setOnClickListener(this);
		mListView = (ListView) findViewById(R.id.list_view);

		if (adapter != null) {
			mListView.setAdapter(adapter);
		}
	}

	private void loadData() {
		LatestChatFriendTask task = new LatestChatFriendTask(this);
		task.execute();
	}

	public void loadDataBallBack(ArrayList<User> data) {
		if (adapter == null && data != null) {
			adapter = new FriendAdapter(this, data);
			mListView.setAdapter(adapter);
			mListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					User f = adapter.getItem(arg2);
					toChat(f);
				}
			});
		} else if (adapter != null) {
			adapter.clear();
		}

		if (adapter != null && adapter.getCount() > 0) {
			findViewById(R.id.loading).setVisibility(View.GONE);
		} else {
			findViewById(R.id.loading).setVisibility(View.VISIBLE);
		}
	}

	public void toChat(final User friend) {
		// final String USERID = CCApplication.loginUser.loginName;
		// final String PWD = CCApplication.loginUser.ccukey;
		// Thread t = new Thread(new Runnable() {
		// public void run() {
		// try {
		// XmppTool.getConnection().login(USERID, PWD);
		// // Log.i("XMPPClient", "Logged in as " +
		// // XmppTool.getConnection().getUser());
		// Presence presence = new Presence(Presence.Type.available);
		// XmppTool.getConnection().sendPacket(presence);
		//
		// Intent intent = new Intent();
		// intent.setClass(ChatListpage.this, FormClient.class);
		// intent.putExtra("USERID", USERID);
		// intent.putExtra("friend", friend);
		// startActivity(intent);
		// } catch (XMPPException e) {
		// XmppTool.closeConnection();
		// }
		// }
		// });
		// t.start();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		default:
			break;
		}
	}
}
