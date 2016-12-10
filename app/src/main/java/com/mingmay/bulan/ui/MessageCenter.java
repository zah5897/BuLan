package com.mingmay.bulan.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mingmay.bulan.R;
import com.mingmay.bulan.adapter.NotifyAdapter;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.DataBaseManager;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.base.BaseActivity;
import com.mingmay.bulan.model.BuLanModel;
import com.mingmay.bulan.model.CommentInfo;
import com.mingmay.bulan.model.NotifyModel;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.service.CCService;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpUtils;
import com.mingmay.bulan.view.widget.AlertDialog;

public class MessageCenter extends BaseActivity {
	private PullToRefreshListView mPullRefreshListView;
	private NotifyAdapter adapter;
	private int pageIndex = 1;
	private TextView title;

	private String url;
	private int type = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_msg_center);
		title = (TextView) findViewById(R.id.title);
		type = getIntent().getIntExtra("type", 2); // 默认为好友请求消息

		if (type == 0) { // 关注申请
			url = CCApplication.HTTPSERVER
					+ "/m_notice!findWardrobeNotices.action";
			title.setText("关注申请");
		} else if (type == 1) { // 消息中心
			url = CCApplication.HTTPSERVER + "/m_notice!findNotices.action";
		} else if (type == 2) { // 好友申请
			title.setText("好友申请");
			url = CCApplication.HTTPSERVER
					+ "/m_notice!findFriendsNotices.action";
		}

		initRefreshView();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!hasResume) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					mPullRefreshListView.setRefreshing();
				}
			}, 500);
		}
		hasResume = true;
	}
 
	boolean hasResume = false;

	private void initRefreshView() {
		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);

		mPullRefreshListView.setMode(Mode.BOTH);
		OnRefreshListener2<ListView> onr = new OnRefreshListener2<ListView>() {
			public void onPullDownToRefresh(
					com.handmark.pulltorefresh.library.PullToRefreshBase<ListView> refreshView) {
				loadMessage(false);
			}

			public void onPullUpToRefresh(
					com.handmark.pulltorefresh.library.PullToRefreshBase<ListView> refreshView) {
				loadMessage(true);
			}
		};
		mPullRefreshListView.setOnRefreshListener(onr);
		ListView actualListView = mPullRefreshListView.getRefreshableView();
		actualListView.setCacheColorHint(R.color.transparent);
		adapter = new NotifyAdapter(this, new ArrayList<NotifyModel>());
		actualListView.setAdapter(adapter);
		actualListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				final NotifyModel model = adapter.getItem(arg2 - 1);

				if (model.noticeKey == 1 || model.noticeKey == 2) {
					Intent comment = new Intent(MessageCenter.this,
							CommentPage.class);
					BuLanModel bulan = new BuLanModel();
					bulan.bulanId = model.bulanId;
					comment.putExtra("bulan", bulan);
					startActivity(comment);
				}
			}
		});
	}

	public void back(View v) {
		finish();
	}

	public void submit(View v) {
		ToastUtil.show("提交成功!");
		finish();
	}

	boolean isLoading = false;

	public void loadMessage(boolean loadMore) {
		if (isLoading) {
			return;
		}
		isLoading = true;

		if (loadMore) {
			pageIndex++;
		} else {
			pageIndex = 1;
		}

		RequestParams param = new RequestParams();
		param.put("userId", UserManager.getInstance().getLoginUser().ID);
		param.put("curPage", pageIndex);
		param.put("pageSize", 20);
		HttpUtils.post(url, param, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				JSONObject body = response.optJSONObject("body");
				if (pageIndex == 1) {
					adapter.clear();
				}
				if (body != null) {
					JSONArray notifiesJson = body.optJSONArray("noticeInfo");
					if (notifiesJson != null) {
						int len = notifiesJson.length();
						if (len > 0) {
							List<NotifyModel> notifies = new ArrayList<NotifyModel>();
							for (int i = 0; i < len; i++) {
								NotifyModel notify = new NotifyModel(
										notifiesJson.optJSONObject(i));
								notifies.add(notify);
							}
							if (pageIndex == 1) {
								adapter.addAndClear(notifies);
							} else {
								adapter.addData(notifies);
							}

						}
					}
				}
				isLoading = false;
				mPullRefreshListView.onRefreshComplete();
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				isLoading = false;
				mPullRefreshListView.onRefreshComplete();
			}
		});

	}

	private void agree(final NotifyModel notify) {
		String url = CCApplication.HTTPSERVER + "/m_notice!agree.action";

		RequestParams params = new RequestParams();
		User loginUser = UserManager.getInstance().getLoginUser();
		params.put("ccukey", loginUser.ccukey);
		params.put("userId", String.valueOf(loginUser.ID));
		params.put("noticeId", String.valueOf(notify.noticeId));

		HttpUtils.post(url, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {

				JSONObject body = response.optJSONObject("body");

				// "noticeStatus":"1","cstatus":"0","noticeId":314,"msg":"","isRead":"1"}
				int cstatus = body.optInt("cstatus");
				int noticeStatus = body.optInt("noticeStatus");
				int noticeId = body.optInt("noticeId");
				int isRead = body.optInt("isRead");

				notify.isRead = isRead;
				notify.noticeStatus = noticeStatus;

				adapter.notifyDataSetChanged();
				super.onSuccess(statusCode, headers, response);
				ToastUtil.show("操作成功");
				Intent refresh = new Intent(MessageCenter.this, CCService.class);
				refresh.setAction(CCService.Action_REFRESH_MSG_CENTER);
				startService(refresh);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				super.onFailure(statusCode, headers, responseString, throwable);
				ToastUtil.show("操作失败");
			}
		});
	}

	private void reject(final NotifyModel notify) {
		String url = CCApplication.HTTPSERVER + "/m_notice!against.action";
		RequestParams params = new RequestParams();
		User loginUser = UserManager.getInstance().getLoginUser();
		params.put("ccukey", loginUser.ccukey);
		params.put("userId", String.valueOf(loginUser.ID));
		params.put("noticeId", String.valueOf(notify.noticeId));

		HttpUtils.post(url, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				JSONObject body = response.optJSONObject("body");

				// "noticeStatus":"1","cstatus":"0","noticeId":314,"msg":"","isRead":"1"}
				int cstatus = body.optInt("cstatus");
				int noticeStatus = body.optInt("noticeStatus");
				int noticeId = body.optInt("noticeId");
				int isRead = body.optInt("isRead");

				notify.isRead = isRead;
				notify.noticeStatus = noticeStatus;

				adapter.notifyDataSetChanged();
				super.onSuccess(statusCode, headers, response);
				ToastUtil.show("操作成功");
				Intent refresh = new Intent(MessageCenter.this, CCService.class);
				refresh.setAction(CCService.Action_REFRESH_MSG_CENTER);
				startService(refresh);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				super.onFailure(statusCode, headers, responseString, throwable);
				ToastUtil.show("操作失败");
			}
		});
	}

	private void iKnow(final NotifyModel notify) {
		String url = CCApplication.HTTPSERVER + "/m_notice!getNotice.action";
		RequestParams params = new RequestParams();
		User loginUser = UserManager.getInstance().getLoginUser();
		params.put("ccukey", loginUser.ccukey);
		params.put("userId", String.valueOf(loginUser.ID));
		params.put("noticeId", String.valueOf(notify.noticeId));

		HttpUtils.post(url, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				JSONObject body = response.optJSONObject("body");

				// "noticeStatus":"1","cstatus":"0","noticeId":314,"msg":"","isRead":"1"}
				int cstatus = body.optInt("cstatus");
				int noticeStatus = body.optInt("noticeStatus");
				int noticeId = body.optInt("noticeId");
				int isRead = body.optInt("isRead");

				notify.isRead = isRead;
				notify.noticeStatus = noticeStatus;

				CCApplication.unreadMsgCount--;

				adapter.notifyDataSetChanged();
				super.onSuccess(statusCode, headers, response);
				ToastUtil.show("操作成功");
				Intent refresh = new Intent(MessageCenter.this, CCService.class);
				refresh.setAction(CCService.Action_REFRESH_MSG_CENTER);
				startService(refresh);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				super.onFailure(statusCode, headers, responseString, throwable);
				ToastUtil.show("操作失败");
			}
		});
	}

	public void commentCallBack(CommentInfo comment, NotifyModel notify) {
		//
		// // ProgressDialogUtil.dismiss();
		// //
		// // if (comment == null) {
		// // ToastUtil.show("无法获取评论内容!");
		// // return;
		// // }
		// // Intent tocomment = new Intent(MessageCenter.this,
		// // CommentDetialPage.class);
		// // tocomment.putExtra("comment", comment);
		// // BuLanModel bulan = new BuLanModel();
		// // bulan.bulanId = notify.bulanId;
		// // bulan.bulanTitle = notify.bulanTitle;
		// // bulan.createDate = notify.createDate;
		// // tocomment.putExtra("bulan", bulan);
		// // startActivity(tocomment);
	}
}
