package com.mingmay.bulan.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.hyphenate.easeui.ui.EaseShowBigImageActivity;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mingmay.bulan.R;
import com.mingmay.bulan.adapter.FriendBuLanAdapter;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.BuLanModel;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.util.DensityUtil;
import com.mingmay.bulan.util.ImageLoadUtil;
import com.mingmay.bulan.util.ProgressDialogUtil;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpUtils;
import com.mingmay.bulan.view.CircularImageView;

public class FriendInfoPage extends Activity implements OnClickListener {
	private User friend;
	CircularImageView asyImg;

	private ListView listview;
	private FriendBuLanAdapter adapter;
	private int curPage = 1;
	private PullToRefreshScrollView pullToRefreshScrollView;
	private ScrollView mScrollView;

	private JSONObject room;

	private ImageView addFriendView;
	private ImageView addBlockView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_friend_info);
		friend = (User) getIntent().getSerializableExtra("friend");
		initTop();
		initScrollView();
		loadInfo(false);
	}

	private void initTop() {
		findViewById(R.id.back).setOnClickListener(this);
		findViewById(R.id.setting).setOnClickListener(this);
		findViewById(R.id.image).setOnClickListener(this);
	}

	private void initScrollView() {
		pullToRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.pull_refresh_scrollview);
		if (friend.isFriend()) {
			pullToRefreshScrollView.setMode(Mode.PULL_UP_TO_REFRESH);
		} else {
			pullToRefreshScrollView.setMode(Mode.DISABLED);
		}
		// 上拉监听函数
		OnRefreshListener<ScrollView> onr = new OnRefreshListener<ScrollView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				loadInfo(true);
			}
		};
		pullToRefreshScrollView.setOnRefreshListener(onr);

		// 获取ScrollView布局，此文中用不到
		mScrollView = pullToRefreshScrollView.getRefreshableView();
		if (adapter != null) {
			adapter.clear();
		}
	}

	private void friendship() {
		findViewById(R.id.to_chat).setOnClickListener(this);

		addFriendView = (ImageView) findViewById(R.id.add_friend_img);
		addBlockView = (ImageView) findViewById(R.id.add_block_img);

		addFriendView.setOnClickListener(this);
		addBlockView.setOnClickListener(this);

		if (friend.isFan == 1) {
			findViewById(R.id.to_chat_layout).setVisibility(View.VISIBLE);
			findViewById(R.id.show_add_friend_layout).setVisibility(View.GONE);
		} else {
			LinearLayout to_chat_layout=(LinearLayout)findViewById(R.id.to_chat_layout);
			int visible=to_chat_layout.getVisibility();
			int i=visible++;
			to_chat_layout.setVisibility(View.GONE);
			findViewById(R.id.show_add_friend_layout).setVisibility(
					View.VISIBLE);

			addFriendView.setEnabled(false);
			addFriendView.setImageResource(R.drawable.user_addfriend_fade);

			if (friend.isFan == 0) {

				addBlockView.setEnabled(true);
				addBlockView.setImageResource(R.drawable.user_addblack);

			} else if (friend.isFan == 2) {

				addFriendView.setEnabled(true);
				addFriendView.setImageResource(R.drawable.user_addfriend);

				addBlockView.setEnabled(true);
				addBlockView.setImageResource(R.drawable.user_addblack);

			} else if (friend.isFan == 3) {
				addBlockView.setEnabled(true);
				addBlockView.setImageResource(R.drawable.user_deleteblack);
			}
		}
		if (friend.isFriend()) {
			pullToRefreshScrollView.setMode(Mode.PULL_UP_TO_REFRESH);
		} else {
			pullToRefreshScrollView.setMode(Mode.DISABLED);
		}
	}

	private void loadInfo(boolean loadMore) {
		String URL = CCApplication.HTTPSERVER + "/m_user!getUserHome.action";
		if (loadMore) {
			curPage++;
		}
		RequestParams params = new RequestParams();
		User u = UserManager.getInstance().getLoginUser();
		params.put("userId", String.valueOf(u.ID));
		params.put("ccukey", String.valueOf(u.ccukey));
		params.put("userId_info", String.valueOf(friend.ID));
		params.put("curPage", String.valueOf(curPage));
		params.put("pageSize", String.valueOf(20));

		HttpUtils.post(URL, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				handle(response);
				pullToRefreshScrollView.onRefreshComplete();
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				super.onFailure(statusCode, headers, responseString, throwable);
				pullToRefreshScrollView.onRefreshComplete();
			}
		});
	}

	private void handle(JSONObject response) {
		findViewById(R.id.loading).setVisibility(View.GONE);
		JSONObject body = response.optJSONObject("body");

		if(body==null){
			ToastUtil.show("页面异常");
			finish();
			return;
		}
		if (curPage == 1) {
			JSONObject userInfo = body.optJSONObject("userInfo");
			if (userInfo != null) {
				User friend = User.jsonToFriend(userInfo);
				FriendInfoPage.this.friend = friend;
				UserManager.getInstance().saveUser(FriendInfoPage.this.friend);
				setUserInfo();
			} else {
				finish();
			}
			room = body.optJSONObject("roomInfo");
			setRoomInfo();
		} else {

		}

		JSONArray bulanInfos = body.optJSONArray("bulanInfo");

		if (bulanInfos != null) {
			List<BuLanModel> bulans = new ArrayList<BuLanModel>();
			int len = bulanInfos.length();
			for (int i = 0; i < len; i++) {
				bulans.add(BuLanModel.jsonToModel(bulanInfos.optJSONObject(i)));
			}
			if (adapter == null) {
				adapter = new FriendBuLanAdapter(FriendInfoPage.this, bulans);
				refreshBottom();
				listview.setAdapter(adapter);
			} else {
				adapter.add(bulans);
			}

		}
	}

	private void setRoomInfo() {

		if (room == null) {
			findViewById(R.id.zhuanlan_item).setVisibility(View.GONE);
		} else {
			findViewById(R.id.zhuanlan_item).setVisibility(View.VISIBLE);
			String icon = room.optString("wardrobeImage");
			String name = room.optString("name");
			ImageView zhuanlan_icon = (ImageView) findViewById(R.id.zhuanlan_icon);
			TextView zhuanlan_name = (TextView) findViewById(R.id.zhuanlan_name);
			findViewById(R.id.to_watch).setOnClickListener(this);
			int wh = DensityUtil.dip2px(50);
			ImageLoadUtil.load(this, zhuanlan_icon, icon, new int[] { wh, wh });
			if (!TextUtils.isEmpty(name)) {
				zhuanlan_name.setText(name);
			}
		}
	}

	private void setUserInfo() {
		asyImg = ((CircularImageView) findViewById(R.id.image));
		int wh = DensityUtil.dip2px(70);
		ImageLoadUtil.load(this, asyImg, friend.userImg, new int[] { wh, wh });

		((TextView) findViewById(R.id.single)).setText(friend.signature);
		((TextView) findViewById(R.id.friend_name)).setText(friend.firstName);
		((TextView) findViewById(R.id.city)).setText("上海");
		friendship();
	}

	private void refreshBottom() {
		findViewById(R.id.friend_bulans_layout).setVisibility(View.VISIBLE);
	//	findViewById(R.id.to_chat_layout).setVisibility(View.VISIBLE);
		//findViewById(R.id.show_add_friend_layout).setVisibility(View.GONE);
		listview = (ListView) findViewById(R.id.friend_bulan);
		findViewById(R.id.setting).setVisibility(View.VISIBLE);
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				BuLanModel bm = adapter.getItem(position);

				Intent toBulanDetail = new Intent(FriendInfoPage.this,
						BulanDetialPage.class);
				toBulanDetail.putExtra("bulan", bm);
				startActivity(toBulanDetail);

			}
		});
	}

	public void callBack(User user) {
		setUserInfo();
	}

	public void callBulan(List<BuLanModel> result) {
		if (result != null) {
			adapter = new FriendBuLanAdapter(this, result);
			listview.setAdapter(adapter);
			// setListViewHeightBasedOnChilren(listview);
		}
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.back:
			onBackPressed();
			break;
		case R.id.setting:

			Intent toSetting = new Intent(this, FriendSettingPage.class);
			toSetting.putExtra("friend", friend);
			startActivity(toSetting);
			finish();
			break;
		case R.id.image:
			showBigImage();

			break;
		case R.id.add_friend_img:
			ProgressDialogUtil.showProgress(this, "正在加为好友...");
			addFriend();
			break;
		case R.id.add_block_img:
			addBlock();
			break;
		case R.id.to_chat:
			Intent toChat = new Intent(this, ChatActivity.class);
			toChat.putExtra("userId", String.valueOf(friend.ID));
			startActivity(toChat);
			break;
		case R.id.to_watch:
			ProgressDialogUtil.showProgress(this, "正在 关注...");
			toWatch();
			break;
		default:
			break;
		}
	}

	private void showBigImage() {
		Intent intent = new Intent(this, EaseShowBigImageActivity.class);
		intent.putExtra("remotepath", friend.userImg);
		startActivity(intent);
	}

	private void addFriend() {
		User u = UserManager.getInstance().getLoginUser();
		RequestParams params = new RequestParams();
		params.add("userId", String.valueOf(u.ID));
		params.add("ccukey", String.valueOf(u.ccukey));
		params.add("hostUserId", String.valueOf(friend.ID));
		String url = CCApplication.HTTPSERVER
				+ "/m_relationShip!addRelationShip.action";
		HttpUtils.post(url, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				super.onSuccess(statusCode, headers, response);

				JSONObject body = response.optJSONObject("body");
				int cstatus = body.optInt("cstatus");
				friend.isFan = body.optInt("isFan");
				ProgressDialogUtil.dismiss();
				friendship();
				ToastUtil.show("申请添加好友成功,等待对方同意!");
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				super.onFailure(statusCode, headers, responseString, throwable);
				ProgressDialogUtil.dismiss();
			}
		});
	}

	private void addBlock() {

		User u = UserManager.getInstance().getLoginUser();
		RequestParams params = new RequestParams();
		params.add("userId", String.valueOf(u.ID));
		params.add("ccukey", String.valueOf(u.ccukey));
		params.put("userId_info", String.valueOf(friend.ID));

		String url = CCApplication.HTTPSERVER
				+ "/m_relationShip!addBlackRElationShip.action";
		if (friend.isFan == 3) {
			url = CCApplication.HTTPSERVER
					+ "/m_relationShip!delBlackRElationShip.action";
			ProgressDialogUtil.showProgress(this, "正在 取消黑名单。。。");
		} else {
			ProgressDialogUtil.showProgress(this, "正在 加入黑名单。。。");
		}

		HttpUtils.post(url, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				super.onSuccess(statusCode, headers, response);
				JSONObject body = response.optJSONObject("body");
				int cstatus = body.optInt("cstatus");

				if (cstatus == 0) {
					if (friend.isFan == 3) {
						ToastUtil.show("取消黑名单成功");
					} else {
						ToastUtil.show("添加黑名单成功");
					}
				}
				friend.isFan = body.optInt("isFan");
				ProgressDialogUtil.dismiss();
				friendship();
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				super.onFailure(statusCode, headers, responseString, throwable);
				ProgressDialogUtil.dismiss();
			}
		});
	}

	private void toWatch() {
		User u = UserManager.getInstance().getLoginUser();
		RequestParams params = new RequestParams();
		params.add("userId", String.valueOf(u.ID));
		params.add("ccukey", String.valueOf(u.ccukey));
		params.add("wardrobeId", String.valueOf(room.opt("id")));
		String url = CCApplication.HTTPSERVER
				+ "/m_wardrobe!addUserWardrobe.action";
		HttpUtils.post(url, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				super.onSuccess(statusCode, headers, response);
				JSONObject body = response.optJSONObject("body");
				int cstatus = body.optInt("cstatus");
				if (cstatus == 0) {
					ToastUtil.show("关注成功！");
				} else {
					ToastUtil.show("关注失败！");
				}
				ProgressDialogUtil.dismiss();
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				super.onFailure(statusCode, headers, responseString, throwable);
				ProgressDialogUtil.dismiss();
				ToastUtil.show("关注失败！");
			}
		});
	}

	public void infoCallBack(User friend) {
		if (friend != null) {
			this.friend = friend;
		}
		setUserInfo();
	}

}
