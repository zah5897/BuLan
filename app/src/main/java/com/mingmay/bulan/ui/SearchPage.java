package com.mingmay.bulan.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
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

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mingmay.bulan.R;
import com.mingmay.bulan.adapter.BuLanAdapter;
import com.mingmay.bulan.adapter.ZhuanLanAdapter;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.BuLanModel;
import com.mingmay.bulan.model.Group;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.util.AppUtil;
import com.mingmay.bulan.util.http.HttpUtils;

public class SearchPage extends Activity {

	private PullToRefreshListView mPullRefreshListView;

	private EditText input;
	private BuLanAdapter buLanadapter;
	private ZhuanLanAdapter zhuanLanadapter;
	private int pageIndex = 1;
	private String keyWord;

	private int tag_id = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_search);//
		tag_id = getIntent().getIntExtra("tag_id", -1);
		input = (EditText) findViewById(R.id.search);
		input.clearFocus();
		input.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				AppUtil.closeSoftKeyBoard(v);
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					String text = v.getText().toString().trim();
					if (TextUtils.isEmpty(text)) {
						return true;
					}

					if (text.equals(keyWord)) {
						return true;
					}
					if (tag_id > -1) {
						buLanadapter.clear();
					} else {
						zhuanLanadapter.clear();
					}
					keyWord = text;
					mPullRefreshListView.setRefreshing();
					return true;
				}
				return false;
			}
		});
		initRefreshView();
	}

	public void back(View v) {
		finish();
	}

	private void initRefreshView() {
		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);

		mPullRefreshListView.setMode(Mode.BOTH);
		OnRefreshListener2<ListView> onr = new OnRefreshListener2<ListView>() {
			public void onPullDownToRefresh(
					com.handmark.pulltorefresh.library.PullToRefreshBase<ListView> refreshView) {
				loadData(false);
			};

			public void onPullUpToRefresh(
					com.handmark.pulltorefresh.library.PullToRefreshBase<ListView> refreshView) {
				loadData(true);
			};
		};
		mPullRefreshListView.setOnRefreshListener(onr);
		ListView actualListView = mPullRefreshListView.getRefreshableView();
		actualListView.setCacheColorHint(R.color.transparent);
		if (tag_id > -1) {
			buLanadapter = new BuLanAdapter(this, new ArrayList<BuLanModel>());
			actualListView.setAdapter(buLanadapter);
		} else {
			zhuanLanadapter = new ZhuanLanAdapter(this, new ArrayList<Group>());
			actualListView.setAdapter(zhuanLanadapter);
		}

		actualListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				// BuLanModel bm = adapter.getItem(arg2 - 1);
				// if (bm.bulanId == -1) {
				// Intent toChat = new Intent(getActivity(),
				// ChatActivity.class);
				// toChat.putExtra(EaseConstant.EXTRA_USER_ID, bm.bulanKey);
				// toChat.putExtra("root_name", bm.bulanTitle);
				// toChat.putExtra("chatType", EaseConstant.CHATTYPE_CHATROOM);
				// startActivity(toChat);
				// } else {
				// Intent detial = new Intent(getActivity(),
				// BulanDetialPage.class);
				// detial.putExtra("bulan", bm);
				// startActivity(detial);
				// }
			}
		});
	}

	protected void loadData(boolean b) {

		if (tag_id > -1) {
			loadBuLan(b);
		} else {
			loadZhuanLan(b);
		}
	}

	private void loadBuLan(boolean loadMore) {

		if (loadMore) {
			pageIndex++;
		} else {
			pageIndex = 1;
		}

		RequestParams param = new RequestParams();
		param.put("curPage", pageIndex);
		param.put("userId",
				String.valueOf(UserManager.getInstance().getLoginUser().ID));
		param.put("wardrobeId", tag_id);
		param.put("pageSize", 20);
		param.put("keyWord", keyWord);

		String url = CCApplication.HTTPSERVER
				+ "/m_bp!findBulansByWardrobe.action";
		HttpUtils.post(url, param, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				JSONObject body = response.optJSONObject("body");
				JSONArray bulanArray = body.optJSONArray("bulanInfo");
				JSONObject room = body.optJSONObject("roomInfo");
				if (bulanArray != null) {
					int len = bulanArray.length();
					if (len > 0) {
						List<BuLanModel> bulans = new ArrayList<BuLanModel>();
						for (int i = 0; i < len; i++) {
							bulans.add(BuLanModel.jsonToModel(bulanArray
									.optJSONObject(i)));
						}
						if (pageIndex == 1) {
							buLanadapter.setNewData(bulans);
						} else {
							buLanadapter.add(bulans);
						}
					} else {
						if (pageIndex == 1) {
							buLanadapter.clear();
						}
					}

				}
				mPullRefreshListView.onRefreshComplete();
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				super.onFailure(statusCode, headers, responseString, throwable);
				mPullRefreshListView.onRefreshComplete();
			}
		});
	}

	private void loadZhuanLan(boolean isLoadMore) {

		if (isLoadMore) {
			pageIndex++;
		} else {
			pageIndex = 1;
		}

		String url = CCApplication.HTTPSERVER
				+ "/m_wardrobe!findUserWardobes.action";
		User u = UserManager.getInstance().getLoginUser();
		RequestParams params = new RequestParams();
		params.add("userId", String.valueOf(u.ID));
		params.add("ccukey", u.ccukey);
		params.add("curPage", String.valueOf(pageIndex));
		params.add("pageSize", "20");
		params.add("keyWord", keyWord);
		HttpUtils.post(url, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				JSONArray tagArray = response.optJSONObject("body")
						.optJSONArray("zhuanlan");
				if (tagArray != null) {
					int len = tagArray.length();
					if (len > 0) {
						List<Group> groups = new ArrayList<Group>();
						for (int i = 0; i < len; i++) {
							JSONObject obj = tagArray.optJSONObject(i);
							Group g = Group.parse(obj);
							groups.add(g);
						}
						if (pageIndex == 1) {
							zhuanLanadapter.addAndClear(groups);
						} else {
							zhuanLanadapter.add(groups);
						}
					} else {
						zhuanLanadapter.clear();
					}
				}
				mPullRefreshListView.onRefreshComplete();
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, responseString, throwable);
				mPullRefreshListView.onRefreshComplete();
			}
		});
	}

}