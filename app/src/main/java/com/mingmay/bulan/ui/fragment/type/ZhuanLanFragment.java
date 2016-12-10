package com.mingmay.bulan.ui.fragment.type;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mingmay.bulan.R;
import com.mingmay.bulan.adapter.ZhuanLanAdapter;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.base.BaseFragment;
import com.mingmay.bulan.model.Group;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.ui.ZhuanLanListActivity;
import com.mingmay.bulan.util.http.HttpUtils;

public class ZhuanLanFragment extends BaseFragment {
	private int pageIndex = 1;
	private PullToRefreshListView mPullRefreshListView;
	private ListView mListview;

	private ZhuanLanAdapter adapter;

	private Handler handler;

	public ZhuanLanFragment(){

	}

	public  ZhuanLanFragment setHandler(Handler handler) {
		this.handler = handler;
		return this;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_zhuanlan_fragment,
				container, false);
		initRefreshView(view);
		return view;
	}

	private void initRefreshView(View view) {
		mPullRefreshListView = (PullToRefreshListView) view
				.findViewById(R.id.pull_refresh_list);

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
		mListview.setCacheColorHint(getResources().getColor(R.color.transparent));
		adapter = new ZhuanLanAdapter(getActivity(), new ArrayList<Group>());
		mListview.setAdapter(adapter);
		mListview.setOnTouchListener(onTouchListener);
		mListview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				Group group = adapter.getItem(arg2 - 1);

				if (group.id < 0) {
					startActivityForResult(new Intent(getActivity(),
							CreateZhuanLanPage.class), 2);
				} else {
					Intent i = new Intent(getActivity(),
							ZhuanLanListActivity.class);
					i.putExtra("id", group.id);
					i.putExtra("user_nickname", group.userName);
					if(group.userId==UserManager.getInstance().getLoginUser().ID){
						i.putExtra("can_show_live", true);
					}
					if (group.userId == UserManager.getInstance()
							.getLoginUser().ID) {
						i.putExtra("canShowManager", true);
					}
					startActivity(i);
				}
			}
		});
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (!hasInit) {
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					mPullRefreshListView.setRefreshing(true);
				}
			}, 1000);

			hasInit = true;
		}
	}

	boolean hasInit = false;

	private void loadData(boolean isLoadMore) {

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
							adapter.addAndClear(groups);
						} else {
							adapter.add(groups);
						}
					} else {
						adapter.clear();
					}
				}
				addCreateItem();
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

	private void addCreateItem() {

		User loginUser = UserManager.getInstance().getLoginUser();
		Group group=adapter.getItem(0);
		
		if(group==null||group.userId!=loginUser.ID){
			Group toCreate = new Group();
			toCreate.id = -1;
			toCreate.name = "创建" + loginUser.firstName + "的专栏";
			adapter.addToTop(toCreate);
		}
//		if(group.userId!=UserManager.getInstance().getLoginUser().ID){
//			if (adapter.getCount() == 0 || adapter.getItem(0).id != -1) {
//				
//			}
//		}
	}

	public void refresh() {
		pageIndex = 1;
		mPullRefreshListView.setRefreshing();

	}

	public Group canModify() {
		if (adapter.getCount() > 0) {
			Group g = adapter.getItem(0);
			if (g.userId == UserManager.getInstance().getLoginUser().ID) {
				return g;
			}
		}
		return null;
	}

	int touchSlop = 10;
	View.OnTouchListener onTouchListener = new View.OnTouchListener() {

		// 下面两个表示滑动的方向，大于0表示向下滑动，小于0表示向上滑动，等于0表示未滑动
		int lastDirection = 0;
		int currentDirection = 0;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// currentDirection = 0;
				// lastDirection = 0;
				break;
			case MotionEvent.ACTION_MOVE:
				// 只有在listView.getFirstVisiblePosition()>0的时候才判断是否进行显隐动画。因为listView.getFirstVisiblePosition()==0时，
				// ToolBar——也就是头部元素必须是可见的，如果这时候隐藏了起来，那么占位置用了headerview就被用户发现了
				// 但是当用户将列表向下拉露出列表的headerview的时候，应该要让头尾元素再次出现才对——这个判断写在了后面onScrollListener里面……
				float tmpCurrentY = event.getY();
				Log.w("onTouch", "---ACTION_MOVE-----CurrentY=" + tmpCurrentY);
				if (Math.abs(tmpCurrentY - mPullRefreshListView.lastY) > touchSlop) {// 滑动距离大于touchslop时才进行判断
					float currentY = tmpCurrentY;
					currentDirection = (int) (currentY - mPullRefreshListView.lastY);
					Log.w("onTouch", "---ACTION_MOVE-----currentDirection="
							+ currentDirection);
					if (lastDirection != currentDirection) {
						// 如果与上次方向不同，则执行显/隐动画
						if (currentDirection < 0) {
							handler.sendEmptyMessage(0);
						} else {
							handler.sendEmptyMessage(1);
						}
					}
				}
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				// 手指抬起的时候要把currentDirection设置为0，这样下次不管向哪拉，都与当前的不同（其实在ACTION_DOWN里写了之后这里就用不着了……）
				currentDirection = 0;
				lastDirection = 0;
				break;
			}
			return false;
		}

	};

	public void scrollToTop() {
		mPullRefreshListView.getRefreshableView().smoothScrollToPosition(0);
	}

}
