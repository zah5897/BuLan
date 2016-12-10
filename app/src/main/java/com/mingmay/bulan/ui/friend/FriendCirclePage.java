package com.mingmay.bulan.ui.friend;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
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
import com.mingmay.bulan.adapter.FriendCircleAdapter;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.BuLanModel;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.ui.BulanDetialPage;
import com.mingmay.bulan.util.http.HttpUtils;

public class FriendCirclePage extends Activity implements OnClickListener {

	private PullToRefreshListView mPullRefreshListView;
	private FriendCircleAdapter adapter;
	private int pageIndex = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_friend_circle);
		initRefreshView();
		initView();
	}

	boolean hasResume = false;

	@Override
	protected void onResume() {
		super.onResume();
		if (!hasResume) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					mPullRefreshListView.setRefreshing(false);
				}
			}, 500);
		}
		hasResume = true;
	}

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
		ListView actualListView = mPullRefreshListView.getRefreshableView();
		actualListView.setCacheColorHint(R.color.transparent);
		adapter = new FriendCircleAdapter(this, new ArrayList<BuLanModel>());
		actualListView.setAdapter(adapter);
		actualListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent i = new Intent(FriendCirclePage.this,
						BulanDetialPage.class);
				i.putExtra("bulan", adapter.getItem(arg2 - 1));
				startActivity(i);
			}
		});
	}

	private void initView() {
		((TextView) findViewById(R.id.title)).setText("圈子");
		findViewById(R.id.left_btn).setOnClickListener(this);
	}

	private boolean isLoading = false;

	private void loadData(boolean loadMore) {
		if (isLoading) {
			return;
		}
		isLoading = true;

		if (loadMore) {
			pageIndex++;
		} else {
			pageIndex = 1;
		}

		mPullRefreshListView.setRefreshing();
		String URL = CCApplication.HTTPSERVER
				+ "/m_bp!findFriendsBulans.action";
		User loginUser = UserManager.getInstance().getLoginUser();
		RequestParams params = new RequestParams();
		params.put("userId", String.valueOf(loginUser.ID));
		params.put("curPage", pageIndex);
		params.put("pageSize", 20);
		HttpUtils.post(URL, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				JSONObject body = response.optJSONObject("body");
				JSONArray clothesinfos = body.optJSONArray("bulanInfo");
				if (clothesinfos != null) {
					int len = clothesinfos.length();
					List<BuLanModel> data = new ArrayList<BuLanModel>();
					for (int i = 0; i < len; i++) {
						data.add(BuLanModel.jsonToModel(clothesinfos
								.optJSONObject(i)));
					}
					if (pageIndex == 1) {
						adapter.addAndClearData(data);
					} else {
						adapter.addData(data);
					}

				}
				mPullRefreshListView.onRefreshComplete();
				isLoading = false;
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, responseString, throwable);
				mPullRefreshListView.onRefreshComplete();
				isLoading = false;
			}
		});
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.left_btn:
			finish();
			break;
		default:
			break;
		}
	}

}
