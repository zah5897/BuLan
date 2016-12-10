package com.mingmay.bulan.ui.fragment.type;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mingmay.bulan.R;
import com.mingmay.bulan.adapter.HotBuLanAdapter;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.Group;
import com.mingmay.bulan.ui.ZhuanLanListActivity;
import com.mingmay.bulan.util.http.HttpUtils;

public class HotZhanLanPage extends Activity {
	private PullToRefreshListView mPullRefreshListView;
	private int pageIndex = 1;
	private boolean isLoading = false;
	private HotBuLanAdapter adapter;

	private EditText search;
	private String keyWord;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_hot_zhuanlan);
		initRefreshView();
	}

	boolean hasResume = false;

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

	private void initRefreshView() {
		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);

		mPullRefreshListView.setMode(Mode.BOTH);
		OnRefreshListener2<ListView> onr = new OnRefreshListener2<ListView>() {
			public void onPullDownToRefresh(
					com.handmark.pulltorefresh.library.PullToRefreshBase<ListView> refreshView) {
				loadBuLan(false);
			}

			public void onPullUpToRefresh(
					com.handmark.pulltorefresh.library.PullToRefreshBase<ListView> refreshView) {
				loadBuLan(true);
			}
		};
		mPullRefreshListView.setOnRefreshListener(onr);
		ListView actualListView = mPullRefreshListView.getRefreshableView();
		actualListView.setCacheColorHint(R.color.transparent);
		adapter = new HotBuLanAdapter(this, new ArrayList<Group>());
		actualListView.setAdapter(adapter);
		actualListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Group group = adapter.getItem(arg2 - 1);
				Intent i = new Intent(HotZhanLanPage.this,
						ZhuanLanListActivity.class);
				i.putExtra("id", group.id);
				i.putExtra("user_nickname", group.userName);
				startActivity(i);
			}
		});
		search = (EditText) findViewById(R.id.search);
		search.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {

				if (event != null
						&& event.getKeyCode() == KeyEvent.KEYCODE_SEARCH) {
					String key = v.getText().toString().trim();
					search.setText(key);
					search.setSelection(key.length());
					if (!TextUtils.isEmpty(key)) {
						keyWord = key;
						pageIndex = 1;
						mPullRefreshListView.setRefreshing(true);
						adapter.clear();
					}
					return true;
				}
				return false;
			}
		});
	}

	private void loadBuLan(boolean loadMore) {
		if (isLoading) {
			return;
		}
		isLoading = true;
		String URL = CCApplication.HTTPSERVER
				+ "/m_wardrobe!findHotspotWardrobes.action";

		if (loadMore) {
			pageIndex++;
		} else {
			pageIndex = 1;
		}

		RequestParams params = new RequestParams();
		params.put("curPage", pageIndex);
		params.put("keyWord", keyWord);
		params.put("userId", UserManager.getInstance().getLoginUser().ID);
		params.put("ccukey", UserManager.getInstance().getLoginUser().ccukey);
		params.put("pageSize", 20);

		HttpUtils.post(URL, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				JSONArray bulanArray = response.optJSONObject("body")
						.optJSONArray("wardrobeInfo");
				if (bulanArray != null) {
					List<Group> bulans = toList(bulanArray);
					if (pageIndex == 1) {
						adapter.addAndClear(bulans);
					} else {
						adapter.add(bulans);
					}
				}
				mPullRefreshListView.onRefreshComplete();
				isLoading = false;
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				mPullRefreshListView.onRefreshComplete();
				isLoading = false;
			}
		});
	}

	private List<Group> toList(JSONArray bulanArray) {
		int len = bulanArray.length();
		List<Group> bulans = new ArrayList<Group>();
		for (int i = 0; i < len; i++) {
			bulans.add(Group.parse(bulanArray.optJSONObject(i)));
		}
		return bulans;
	}

	public void back(View v) {
		finish();
	}

}
