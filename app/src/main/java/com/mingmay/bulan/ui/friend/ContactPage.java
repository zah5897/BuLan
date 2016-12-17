package com.mingmay.bulan.ui.friend;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mingmay.bulan.R;
import com.mingmay.bulan.adapter.ContactsAdapter;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.app.listener.ContactListener;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.ui.Browser;
import com.mingmay.bulan.ui.ChatActivity;
import com.mingmay.bulan.ui.MessageCenter;
import com.mingmay.bulan.util.CharacterParser;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpUtils;
import com.mingmay.bulan.view.SideBar;
import com.mingmay.bulan.view.SideBar.OnTouchingLetterChangedListener;
import com.zbar.lib.CaptureActivity;

public class ContactPage extends Activity {
	private final static int SCANNIN_GREQUEST_CODE = 1;
	private PullToRefreshListView mPullRefreshListView;
	private int cursor = 1;
	private ContactsAdapter adapter;

	private PopupWindow popupWindow;

	public CharacterParser characterParser = new CharacterParser();
	private SideBar sideBar;
	private ListView mListview;
	public boolean need_back;
	public String[] exist_members;
	private TextView right_btn_sure, tip;
	private String forward_msg_id;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_contact);
		need_back = getIntent().getBooleanExtra("need_back", false);
		
		if(need_back){
			findViewById(R.id.extral_layout).setVisibility(View.GONE);
		}
		
		forward_msg_id = getIntent().getStringExtra("forward_msg_id");
		right_btn_sure = (TextView) findViewById(R.id.right_btn_sure);
		exist_members = getIntent().getStringArrayExtra("exist_members");

		if (need_back) {
			right_btn_sure.setVisibility(View.VISIBLE);
			findViewById(R.id.right_btn).setVisibility(View.GONE);
			right_btn_sure.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					onBackPressed();
				}
			});
		}
		initRefreshView();
		tip = (TextView) findViewById(R.id.tip);
		((TextView) findViewById(R.id.title)).setText("通讯录");
		sideBar = (SideBar) findViewById(R.id.sidrbar);
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				if (adapter != null) {
					int position = adapter.getPositionForSection(s.charAt(0));
					if (position != -1) {
						mListview.setSelection(position);
					}
				}
			}
		});

		findViewById(R.id.group_layout).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						startActivity(new Intent(ContactPage.this,
								GroupPage.class));
					}
				});
		findViewById(R.id.msg_layout).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(ContactPage.this,
						MessageCenter.class));
			}
		});

		IntentFilter intentFilter = new IntentFilter("del.friend");
		intentFilter.addAction("refresh.friend");
		// intentFilter.setPriority(3);
		registerReceiver(delFriendBroadcast, intentFilter);
	}

	boolean hasResume = false;

	private void initRefreshView() {
		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);

		mPullRefreshListView.setMode(Mode.BOTH);
		OnRefreshListener2<ListView> onr = new OnRefreshListener2<ListView>() {
			public void onPullDownToRefresh(
					com.handmark.pulltorefresh.library.PullToRefreshBase<ListView> refreshView) {
				loadData(false);
			}

			public void onPullUpToRefresh(
					com.handmark.pulltorefresh.library.PullToRefreshBase<ListView> refreshView) {
				loadData(true);
			}
		};
		mPullRefreshListView.setOnRefreshListener(onr);
		mListview = mPullRefreshListView.getRefreshableView();
		mListview.setCacheColorHint(R.color.transparent);
		adapter = new ContactsAdapter(this, new ArrayList<User>());
		mListview.setAdapter(adapter);
		mListview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				User friend = adapter.getItem(arg2 - 1);

				if (need_back) {
					adapter.itemClick(friend);
					// Intent result = new Intent();
					// result.putExtra("newmembers",
					// new String[] { String.valueOf(friend.ID) });
					// setResult(RESULT_OK, result);
					// finish();
				} else {
					toChat(friend);
				}
			}
		});

	}

	boolean isLoading = false;

	public void loadData(boolean loadMore) {
		if (isLoading) {
			return;
		}
		isLoading = true;
		if (loadMore) {
			cursor++;
		} else {
			cursor = 1;
		}
		UserManager.getInstance().loadContact(cursor, new ContactListener() {
			@Override
			public void onSuccess(List<User> users) {
				mListview.setEmptyView(new View(getBaseContext()));
				if (cursor == 1) {
					adapter.addAndClear(users);
					mListview.setEmptyView(getLayoutInflater().inflate(
							R.layout.list_empty, null));
				} else {
					adapter.add(users);
				}
				mPullRefreshListView.onRefreshComplete();
				isLoading = false;
			}

			@Override
			public void onFailure(int code) {
				if (cursor == 1) {
					adapter.claer();
				}
				mListview.setEmptyView(new View(getBaseContext()));
				mPullRefreshListView.onRefreshComplete();
				mListview.setEmptyView(getLayoutInflater().inflate(
						R.layout.list_empty, null));
				isLoading = false;
			}
		});
	}

	public void back(View v) {
		onBackPressed();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// setTipLayout();
		if (!hasResume) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					mPullRefreshListView.setRefreshing();
				}
			}, 500);
		}
		hasResume = true;

		loadFriendRequestCount();

	}

	public void toChat(final User friend) {

		if (forward_msg_id != null) {
			Intent intent = new Intent(ContactPage.this, ChatActivity.class);
			// it is single chat
			intent.putExtra("userId", String.valueOf(friend.ID));
			intent.putExtra("forward_msg_id", forward_msg_id);
			startActivity(intent);
			finish();
		} else {
			Intent toChat = new Intent(this, ChatActivity.class);
			toChat.putExtra("userId", String.valueOf(friend.ID));
			startActivity(toChat);
		}
	}

	public void showPanel(View v) {
		View toolsLayout = LayoutInflater.from(this).inflate(
				R.layout.layout_tools, null);
		toolsLayout.findViewById(R.id.scan_layout).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent();
						intent.setClass(ContactPage.this, CaptureActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
						popupWindow.dismiss();
					}
				});
		toolsLayout.findViewById(R.id.search).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent i = new Intent(ContactPage.this,
								SearchFriend.class);
						startActivity(i);
						popupWindow.dismiss();
					}
				});
		popupWindow = new PopupWindow(toolsLayout, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, true);
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		popupWindow.setOutsideTouchable(false);
		popupWindow.showAsDropDown(v, 0, 20);
		popupWindow.update();
	}

	@Override
	public void onBackPressed() {

		if (need_back) {
			Intent result = new Intent();
			result.putExtra("newmembers", adapter.getSelectedIds());
			setResult(RESULT_OK, result);
		}
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(delFriendBroadcast);
	}

	private void addByQr(long userId) {
		String URL = CCApplication.HTTPSERVER + "/authorizMgtAction.action";
		User loginUser = UserManager.getInstance().getLoginUser();
		RequestParams params = new RequestParams();
		params.put("hostUserId", userId);
		params.put("userId", String.valueOf(loginUser.ID));
		params.put("ccukey", loginUser.ccukey);

		HttpUtils.post(URL, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				if (statusCode == 200) {
					JSONObject body = response.optJSONObject("body");
					int result = body.optInt("cstatus");

					if (result == 0) {
						ToastUtil.show("添加好友成功!");
						mPullRefreshListView.setRefreshing(false);
					} else if (result == 3) {
						ToastUtil.show("你们已经是好友了");
					} else {
						ToastUtil.show("添加好友失败");
					}
				}
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case SCANNIN_GREQUEST_CODE:
			if (resultCode == RESULT_OK && data != null) {
				String result = data.getStringExtra("result");
				if (!TextUtils.isEmpty(result)) {
					if (result.contains("=")) {
						String ra[] = result.split("=");
						if (ra.length >= 2) {
							try {
								long userid = Long.parseLong(ra[1]);
								addByQr(userid);
							} catch (NumberFormatException e) {

							}
						}

					} else {
						Intent i = new Intent(ContactPage.this, Browser.class);
						i.putExtra("url", result);
						startActivity(i);
					}
				}
			}
			break;
		}
	}

	private void loadFriendRequestCount() {
		tip.setVisibility(View.GONE);
		RequestParams params = new RequestParams();
		params.put("userId", UserManager.getInstance().getLoginUser().ID);
		HttpUtils.post(CCApplication.HTTPSERVER
				+ "/m_notice!getFriendsNotreadCount.action", params,
				new JsonHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers,
							JSONObject response) {
						super.onSuccess(statusCode, headers, response);
						if (response != null) {
							JSONObject body = response.optJSONObject("body");
							if (body != null) {
								int count = body.optInt("count");
								if (count > 0) {
									tip.setVisibility(View.VISIBLE);
									tip.setText(String.valueOf(count));
								}
							}

						}
					}
				});
	}

	private BroadcastReceiver delFriendBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				if ("del.friend".equals(intent.getAction())) {
					String id = intent.getStringExtra("text");
					long contactId = Long.parseLong(id);
					adapter.del(contactId);
				} else if ("refresh.friend".equals(intent.getAction())) {
					adapter.notifyDataSetChanged();
				}

			}
		}
	};

}
