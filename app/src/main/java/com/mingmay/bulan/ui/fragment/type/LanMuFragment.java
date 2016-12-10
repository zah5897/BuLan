
package com.mingmay.bulan.ui.fragment.type;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hyphenate.easeui.EaseConstant;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mingmay.bulan.R;
import com.mingmay.bulan.adapter.BuLanAdapter;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.DataBaseManager;
import com.mingmay.bulan.app.TagManager;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.base.BaseFragment;
import com.mingmay.bulan.model.BuLanModel;
import com.mingmay.bulan.model.Tag;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.task.AddTagTask;
import com.mingmay.bulan.ui.BulanDetialPage;
import com.mingmay.bulan.ui.ChatActivity;
import com.mingmay.bulan.util.PropertyUtil;
import com.mingmay.bulan.util.http.HttpUtils;

public class LanMuFragment extends BaseFragment {
	private PullToRefreshListView mPullRefreshListView;
	private LinearLayout tagContainer, cursor_layout;
	private ProgressBar loadingTag;
	private BuLanAdapter adapter;
	private int pageIndex = 1;
	private Tag currentTag;
	private View guideTip;
	private ImageView cursor;
	private Handler handler;

	public LanMuFragment(){

	}

	public LanMuFragment setHandler(Handler handler) {
		this.handler = handler;
		return this;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.layout_category_fragment, container,
				false);
	}

	boolean hasInit = false;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (!hasInit) {
			initRefreshView();
			tagContainer = (LinearLayout) getView().findViewById(
					R.id.tag_container);
			cursor_layout = (LinearLayout) getView().findViewById(
					R.id.cursor_layout);
			loadingTag = (ProgressBar) getView().findViewById(R.id.loading_tag);
			guideTip = getView().findViewById(R.id.guide_tip);
			TagManager.myTags = UserManager.getInstance().loadDefaultTags();

			loadTags();
			hasInit = true;
		}
	}

	private void loadTags() {
		if (TagManager.myTags == null || TagManager.myTags.size() == 0) {
			guideTip.setVisibility(View.VISIBLE);
		} else {
			guideTip.setVisibility(View.GONE);
			clearPage(TagManager.myTags.get(0));
		}
		String url = CCApplication.HTTPSERVER
				+ "/m_wardrobe!findLatelyWardRobes.action";
		User u = UserManager.getInstance().getLoginUser();
		RequestParams params = new RequestParams();
		params.add("userId", String.valueOf(u.ID));
		params.add("ccukey", u.ccukey);
		HttpUtils.post(url, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				if (response != null) {
					JSONArray tagArray = response.optJSONObject("body")
							.optJSONArray("wardrobeInfo");
					PropertyUtil.putValue("my_tags", tagArray.toString());
					TagManager.myTags = UserManager.getInstance()
							.loadDefaultTags();
					clearPage(TagManager.myTags.get(0));
				}
				loadingTag.setVisibility(View.GONE);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				loadingTag.setVisibility(View.GONE);
			}
		});
	}

	private void initRefreshView() {
		mPullRefreshListView = (PullToRefreshListView) getView().findViewById(
				R.id.pull_refresh_list);

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
		adapter = new BuLanAdapter(getActivity(), new ArrayList<BuLanModel>());
		actualListView.setAdapter(adapter);
		actualListView.setOnTouchListener(onTouchListener);
		actualListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				BuLanModel bm = adapter.getItem(arg2 - 1);
				if (bm.bulanId == -1) {
					Intent toChat = new Intent(getActivity(),
							ChatActivity.class);
					toChat.putExtra(EaseConstant.EXTRA_USER_ID, bm.bulanKey);
					toChat.putExtra("root_name", bm.bulanTitle);
					toChat.putExtra("chatType", EaseConstant.CHATTYPE_CHATROOM);
					startActivity(toChat);
				} else {
					Intent detial = new Intent(getActivity(),
							BulanDetialPage.class);
					detial.putExtra("bulan", bm);
					startActivity(detial);
				}
			}
		});
	}

	private void loadBuLan(boolean loadMore) {
		if (currentTag == null) {
			mPullRefreshListView.onRefreshComplete();
			return;
		}

		if (loadMore) {
			pageIndex++;
		} else {
			pageIndex = 1;
		}

		RequestParams param = new RequestParams();
		param.put("curPage", pageIndex);
		param.put("userId",
				String.valueOf(UserManager.getInstance().getLoginUser().ID));
		param.put("wardrobeId", currentTag.id);
		param.put("pageSize", 20);

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
							adapter.setNewData(bulans);
							PropertyUtil.putValue(currentTag.id + "_tag_cache",
									body.toString());
						} else {
							adapter.add(bulans);
						}
					} else {
						if (pageIndex == 1) {
							adapter.clear();
						}
					}

				}

				if (room != null) {
					BuLanModel bmRoom = createRoom(room);
					if (bmRoom != null) {
						adapter.addTop(bmRoom);
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

	private BuLanModel createRoom(JSONObject room) {
		if (room != null) {
			BuLanModel bm = new BuLanModel();
			bm.bulanId = -1;
			bm.bulanTitle = room.optString("name");
			bm.bulanTitle = room.optString("description");
			bm.browseCount = room.optInt("affiliations_count");
			bm.bulanImage = room.optString("wardrobeImage");
			String room_id = room.optString("id");
			bm.bulanKey = room_id;
			return bm;
		}
		return null;
	}

	int itemWidth;

	private void refreshTagLabel() {
		if (TagManager.myTags != null && TagManager.myTags.size() > 0) {
			loadingTag.setVisibility(View.GONE);
			tagContainer.removeAllViews();
			guideTip.setVisibility(View.GONE);
			itemWidth = (int) ((CCApplication.screenWidth - CCApplication.density * 5 * 5) / 5);
			// int h = (int) (25 * getResources().getDisplayMetrics().density);
			int size = TagManager.myTags.size();
			if (size > 5) {
				size = 5;
			}
			if (cursor == null) {
				cursor = new ImageView(getActivity());
				cursor.setBackgroundColor(getResources()
						.getColor(R.color.gray_));

				int padding = (int) (10 * CCApplication.density);
				cursor.setPadding(padding, 0, padding, 0);
			}
			LinearLayout.LayoutParams cursor_param = new LinearLayout.LayoutParams(
					itemWidth, (int) (2 * CCApplication.density));
			cursor_param.setMargins(5, 0, 0, 0);
			cursor_layout.removeAllViews();
			cursor_layout.addView(cursor, cursor_param);
			for (int i = 0; i < size; i++) {
				final Tag tag = TagManager.myTags.get(i);

				TextView tagView = new TextView(getActivity());
				tagView.setTag(i);
				tagView.setText(tag.toString());
				tagView.setGravity(Gravity.CENTER);
				tagView.setSingleLine(true);

				if (currentTag.id == tag.id) {
					tagView.setBackgroundResource(tag.bgResId);
					tagView.setTextColor(getResources().getColor(R.color.white));
				} else {
					tagView.setBackgroundResource(R.drawable.unselected);
					tagView.setTextColor(getResources().getColor(R.color.red));
				}

				tagView.setTypeface(Typeface.DEFAULT);
				tagView.setTextSize(10);
				LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
						itemWidth, (int) (30 * CCApplication.density));
				param.setMargins(5, 0, 0, 0);
				tagContainer.addView(tagView, param);
				tagView.setTag(tag);
				tagView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						Tag temp = (Tag) arg0.getTag();
						if (temp.equals(currentTag)) {
							return;
						}
						clearPage(temp);
					}

				});
			}
		} else {
			guideTip.setVisibility(View.VISIBLE);
		}
	}

	private void clearPage(Tag selectTag) {
		if (currentTag == null) {
			currentTag = selectTag;
		} else if (currentTag.equals(selectTag)) {
			return;
		} else {
			currentTag = selectTag;
		}
		if(handler!=null){
			Message msg=handler.obtainMessage(2);
			Bundle data=new Bundle();
			data.putInt("tag_id", currentTag.id);
			msg.setData(data);
			handler.sendMessage(msg);
			adapter.clear();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mPullRefreshListView.setRefreshing();
				}
			}, 500);
		}


		refreshTagLabel();
	}

	private int mTagIndex = 0;
	TranslateAnimation animation;

	private void tranform(final int index) {
		animation = new TranslateAnimation((mTagIndex + 1) * 5 + mTagIndex
				* itemWidth, index * itemWidth + index * 5, 0, 0);
		animation.setDuration(500);// 设置动画持续时间
		animation.setFillAfter(true);
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				LinearLayout.LayoutParams param = (LayoutParams) cursor
						.getLayoutParams();
				param.setMargins(index * itemWidth + index * 5, 0, 0, 0);
				mTagIndex = index;
			}
		});
		cursor.startAnimation(animation);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			Tag t = (Tag) data.getSerializableExtra("tag");
			if (t != null) {
				addTag(t);
			}
		}
	}

	public void addTag(Tag tag) {
		if (TagManager.myTags == null) {
			TagManager.myTags = new ArrayList<Tag>();

		}
		if (TagManager.myTags.contains(tag)) {
			return;
		}
		if (TagManager.myTags.size() >= 5) {
			Tag toRemove = TagManager.myTags.get(TagManager.myTags.size() - 1);
			TagManager.myTags.remove(TagManager.myTags.size() - 1);
			DataBaseManager.getInstance(getActivity()).deleteTag(toRemove);
		}
		TagManager.myTags.add(0, tag);
		clearPage(tag);
		new AddTagTask().execute(String.valueOf(tag.id));
		guideTip.setVisibility(View.GONE);
	}

	public void addBulan(BuLanModel publishModel) {
		// TODO Auto-generated method stub
		if (adapter != null) {
			adapter.addItemToTop(publishModel);
		}
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
		// TODO Auto-generated method stub
		mPullRefreshListView.getRefreshableView().smoothScrollToPosition(0);
	}

}
