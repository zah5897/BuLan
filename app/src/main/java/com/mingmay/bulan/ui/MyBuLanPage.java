package com.mingmay.bulan.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ListView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshSwipeListView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mingmay.bulan.R;
import com.mingmay.bulan.adapter.MyBuLanAdapter;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.BuLanModel;
import com.mingmay.bulan.util.DensityUtil;
import com.mingmay.bulan.util.PropertyUtil;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpUtils;

public class MyBuLanPage extends Activity {
	private PullToRefreshSwipeListView mPullRefreshListView;
	private int pageIndex = 1;
	private boolean isLoading = false;
	private MyBuLanAdapter adapter;

	private EditText search;
	private String lastKeyword;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_my_bulan);
		initRefreshView();
		// loadCache();
	}

	private void loadCache() {
		String cache = PropertyUtil.getStringValue("my_bulan_"
				+ UserManager.getInstance().getLoginUser().ID);

		if (!TextUtils.isEmpty(cache)) {
			try {
				JSONArray cacheData = new JSONArray(cache);
				List<BuLanModel> cacheList = toList(cacheData);
				adapter.addAndClear(cacheList);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
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
		mPullRefreshListView = (PullToRefreshSwipeListView) findViewById(R.id.pull_refresh_list);

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
		adapter = new MyBuLanAdapter(this, new ArrayList<BuLanModel>());
		actualListView.setAdapter(adapter);
		createMenu((SwipeMenuListView) actualListView);
		actualListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent detial = new Intent(MyBuLanPage.this,
						BulanDetialPage.class);
				detial.putExtra("bulan", adapter.getItem(arg2 - 1));
				startActivity(detial);
			}
		});
		search = (EditText) findViewById(R.id.search);
		search.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {

				
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					String currentKeyWord = search.getText().toString();
					if (currentKeyWord.trim().equals(lastKeyword)) {
						return true;
					}
					lastKeyword = currentKeyWord.trim();
					adapter.clear();
					pageIndex = 1;
					mPullRefreshListView.setRefreshing();
				}
				return true;
			}
		});
	}

	private void loadBuLan(boolean loadMore) {
		if (isLoading) {
			return;
		}
		isLoading = true;
		String URL = CCApplication.HTTPSERVER + "/m_bp!findMyBulans.action";

		if (loadMore) {
			pageIndex++;
		} else {
			pageIndex = 1;
		}

		RequestParams params = new RequestParams();
		params.put("curPage", pageIndex);
		params.put("userId", UserManager.getInstance().getLoginUser().ID);
		params.put("pageSize", 20);

		String currKeyWord = search.getText().toString();
		if (TextUtils.isEmpty(currKeyWord.trim())) {
			search.setText("");
		} else {
			params.put("keyWord", currKeyWord);
			lastKeyword = currKeyWord;
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
						PropertyUtil.putValue("my_bulan_"
								+ UserManager.getInstance().getLoginUser().ID,
								bulanArray.toString());
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

	private void createMenu(SwipeMenuListView listView) {
		SwipeMenuCreator creator = new SwipeMenuCreator() {

			@Override
			public void create(SwipeMenu menu) {
				// create "open" item
				// SwipeMenuItem openItem = new SwipeMenuItem(
				// getApplicationContext());
				// // set item background
				// openItem.setBackground(new ColorDrawable(Color.rgb(0xC9,
				// 0xC9,
				// 0xCE)));
				// // set item width
				// openItem.setWidth(DensityUtil.dip2px(90));
				// // set item title
				// openItem.setTitle("Open");
				// // set item title fontsize
				// openItem.setTitleSize(18);
				// // set item title font color
				// openItem.setTitleColor(Color.WHITE);
				// // add to menu
				// menu.addMenuItem(openItem);

				// create "delete" item
				SwipeMenuItem deleteItem = new SwipeMenuItem(
						getApplicationContext());
				// set item background
				deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
						0x3F, 0x25)));
				// set item width
				deleteItem.setWidth(DensityUtil.dip2px(90));
				// set a icon
				deleteItem.setIcon(R.drawable.ic_delete);
				// add to menu
				menu.addMenuItem(deleteItem);
			}
		};

		// set creator
		listView.setMenuCreator(creator);

		listView.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(int position, SwipeMenu menu,
					int index) {
				switch (index) {
				// case 0:
				// // open
				// break;
				case 0:
					// delete
					delete(position, adapter.getItem(position).bulanId);
					break;
				}
				// false : close the menu; true : not close the menu
				return false;
			}
		});
	}

	private void delete(final int position, long id) {

		RequestParams params = new RequestParams();
		params.put("ccukey", UserManager.getInstance().getLoginUser().ccukey);
		params.put("userId", UserManager.getInstance().getLoginUser().ID);
		params.put("bulanId", id);

		String url = CCApplication.HTTPSERVER + "/m_bp!deleteBulan.action";
		HttpUtils.post(url, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				JSONObject body = response.optJSONObject("body");
				int cstatus = body.optInt("cstatus");
				if (cstatus == 0) {
					adapter.remove(position);
					ToastUtil.show("删除成功");

				} else {
					ToastUtil.show("删除失败");
				}
				// TODO Auto-generated method stub

				super.onSuccess(statusCode, headers, response);
			}
		});

	}
}
