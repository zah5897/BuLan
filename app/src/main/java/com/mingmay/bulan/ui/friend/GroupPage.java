package com.mingmay.bulan.ui.friend;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMGroupManager;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.exceptions.HyphenateException;
import com.mingmay.bulan.R;
import com.mingmay.bulan.adapter.GroupAdapter;
import com.mingmay.bulan.ui.ChatActivity;

public class GroupPage extends Activity {
	public static final String TAG = "GroupsActivity";
	private ListView groupListView;
	protected List<EMGroup> grouplist;
	private GroupAdapter groupAdapter;
	private InputMethodManager inputMethodManager;
	public static GroupPage instance;
	private View progressBar;
	private SwipeRefreshLayout swipeRefreshLayout;

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			swipeRefreshLayout.setRefreshing(false);
			switch (msg.what) {
			case 0:
				refresh();
				break;
			case 1:
				Toast.makeText(GroupPage.this,
						R.string.Failed_to_get_group_chat_information,
						Toast.LENGTH_LONG).show();
				break;

			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.em_fragment_groups);

		instance = this;
		inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		grouplist = EMClient.getInstance().groupManager().getAllGroups();
		groupListView = (ListView) findViewById(R.id.list);
		// show group list
		groupAdapter = new GroupAdapter(this, 1, grouplist);
		groupListView.setAdapter(groupAdapter);

		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
		swipeRefreshLayout.setColorSchemeResources(R.color.holo_blue_bright,
				R.color.holo_green_light, R.color.holo_orange_light,
				R.color.holo_red_light);
		// 下拉刷新
		swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				new Thread() {
					@Override
					public void run() {
						try {
							grouplist = EMClient.getInstance().groupManager()
									.getJoinedGroupsFromServer();
						} catch (HyphenateException e) {
							e.printStackTrace();
						}
						handler.sendEmptyMessage(0);
					}
				}.start();
			}
		});

		groupListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 进入群聊
				Intent intent = new Intent(GroupPage.this, ChatActivity.class);
				// it is group chat
				intent.putExtra("chatType", EaseConstant.CHATTYPE_GROUP);
				intent.putExtra("userId", groupAdapter.getItem(position)
						.getGroupId());
				startActivityForResult(intent, 0);
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onResume() {
		refresh();
		super.onResume();
	}

	private void refresh() {
		grouplist = EMClient.getInstance().groupManager().getAllGroups();
		groupAdapter = new GroupAdapter(this, 1, grouplist);
		groupListView.setAdapter(groupAdapter);
		groupAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		instance = null;
	}

	/**
	 * 返回
	 * 
	 * @param view
	 */
	public void back(View view) {
		finish();
	}

}
