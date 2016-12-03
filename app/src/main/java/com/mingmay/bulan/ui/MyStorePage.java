package com.mingmay.bulan.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import cn.sharesdk.onekeyshare.EditPage;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mingmay.bulan.R;
import com.mingmay.bulan.adapter.BuLanAdapter;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.BuLanModel;
import com.mingmay.bulan.util.PropertyUtil;
import com.mingmay.bulan.util.http.HttpUtils;

public class MyStorePage extends Activity {
	private PullToRefreshListView mPullRefreshListView;
	private int pageIndex = 1;
	private boolean isLoading = false;
	private BuLanAdapter adapter;

	private EditText search;
	private String lastKeyword;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_my_store);
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
			};

			public void onPullUpToRefresh(
					com.handmark.pulltorefresh.library.PullToRefreshBase<ListView> refreshView) {
				loadBuLan(true);
			};
		};
		mPullRefreshListView.setOnRefreshListener(onr);
		ListView actualListView = mPullRefreshListView.getRefreshableView();
		actualListView.setCacheColorHint(R.color.transparent);
		adapter = new BuLanAdapter(this, new ArrayList<BuLanModel>());
		actualListView.setAdapter(adapter);
		actualListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				BuLanModel model = adapter.getItem(arg2 - 1);
				Intent i = new Intent(MyStorePage.this, BulanDetialPage.class);
				i.putExtra("bulan", model);
				startActivity(i);
			}
		});
		
		search=(EditText) findViewById(R.id.search);
		search.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

				if(actionId==EditorInfo.IME_ACTION_SEARCH){
					String currentKeyWord=search.getText().toString();
					if(currentKeyWord.trim().equals(lastKeyword)){
						return false;
					}
					lastKeyword=currentKeyWord.trim();
					adapter.clear();
					pageIndex=1;
					mPullRefreshListView.setRefreshing();
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

		if (loadMore) {
			pageIndex++;
		} else {
			pageIndex = 1;
		}

		RequestParams params = new RequestParams();
		params.put("curPage", pageIndex);
		params.put("userId", UserManager.getInstance().getLoginUser().ID);
		params.put("pageSize", 20);
		String URL = CCApplication.HTTPSERVER
				+ "/m_bp!findCollectionBulans.action";
		String currKeyWord=search.getText().toString();
		if(TextUtils.isEmpty(currKeyWord.trim())){
			search.setText("");
		}else{
			params.put("keyWord", currKeyWord);
			lastKeyword=currKeyWord;
		}
		HttpUtils.post(URL, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				JSONArray bulanArray = response.optJSONObject("body")
						.optJSONArray("bulanInfo");

				if (bulanArray != null) {
					List<BuLanModel> bulans = toList(bulanArray);
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

	private List<BuLanModel> toList(JSONArray bulanArray) {
		int len = bulanArray.length();
		List<BuLanModel> bulans = new ArrayList<BuLanModel>();
		for (int i = 0; i < len; i++) {
			bulans.add(BuLanModel.jsonToModel(bulanArray.optJSONObject(i)));
		}
		return bulans;
	}

	public void back(View v) {
		finish();
	}
}
