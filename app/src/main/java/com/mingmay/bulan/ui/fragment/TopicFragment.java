package com.mingmay.bulan.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mingmay.bulan.R;
import com.mingmay.bulan.adapter.BuLanAdapter;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.Ads;
import com.mingmay.bulan.model.BuLanModel;
import com.mingmay.bulan.ui.Browser;
import com.mingmay.bulan.ui.BulanDetialPage;
import com.mingmay.bulan.util.ImageLoadUtil;
import com.mingmay.bulan.util.PropertyUtil;
import com.mingmay.bulan.util.http.HttpUtils;

@SuppressLint("ClickableViewAccessibility")
public class TopicFragment extends Fragment {
	private BuLanAdapter adapter;
	private MyPageAdapter pageAdapter;
	private int pageIndex = 1;
	private boolean isLoading = false;
	protected ViewPager mPager;
	private int mPagerW;
	private final float aspectRatio = 0.515f;
	private LinearLayout round_point_layout;
	private List<Ads> ads;
	private List<BuLanModel> buLanModels;
	private ImageView toTopView;

	private ListView mListview;
	private PullToRefreshListView mPullRefreshListView;

	private RelativeLayout topContainer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.layout_topic_fragment, container,
				false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		toTopView = (ImageView) getView().findViewById(R.id.to_top);
		toTopView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				toTopView.setVisibility(View.GONE);
				mListview.smoothScrollToPosition(0);
			}
		});
		mPagerW = getResources().getDisplayMetrics().widthPixels;
		loadCache();
		initRefreshView();
	}

	private void initRefreshView() {
		mPullRefreshListView = (PullToRefreshListView) getView().findViewById(
				R.id.pull_refresh_list);

		mPullRefreshListView.setMode(Mode.BOTH);
		OnRefreshListener2<ListView> onr = new OnRefreshListener2<ListView>() {
			public void onPullDownToRefresh(
					com.handmark.pulltorefresh.library.PullToRefreshBase<ListView> refreshView) {
				pageDataLoad(false);
			};

			public void onPullUpToRefresh(
					com.handmark.pulltorefresh.library.PullToRefreshBase<ListView> refreshView) {
				pageDataLoad(true);
			};
		};
		mPullRefreshListView.setOnRefreshListener(onr);
		mListview = mPullRefreshListView.getRefreshableView();
		mListview.setCacheColorHint(getResources().getColor(R.color.transparent));
		if (buLanModels == null) {
			buLanModels = new ArrayList<BuLanModel>();
		}
		adapter = new BuLanAdapter(getActivity(), buLanModels);
		mListview.setAdapter(adapter);
		mListview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent detial = new Intent(getActivity(), BulanDetialPage.class);
				detial.putExtra("bulan", adapter.getItem(arg2 - 2));
				startActivity(detial);
			}
		});

		topContainer = new RelativeLayout(this.getActivity());
		initPageView();
		mListview.addHeaderView(topContainer);
		mListview.setOnTouchListener(onTouchListener);
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				mPullRefreshListView.setRefreshing();
			}
		}, 1000);
	}

	private void pageDataLoad(boolean loadMore) {
		if (loadMore) {
			loadTopic(true);
		} else {
			loadAds();
			loadTopic(false);
		}

	}

	private void loadCache() {
		String ad = PropertyUtil.getStringValue("ad");
		if (!TextUtils.isEmpty(ad)) {
			JSONArray recommendInfo;
			try {
				recommendInfo = new JSONArray(ad);
				if (recommendInfo != null) {
					int size = recommendInfo.length();
					if (size > 0) {
						ads = new ArrayList<Ads>();
						for (int i = 0; i < size; i++) {
							ads.add(Ads.jsonToAds(recommendInfo
									.optJSONObject(i)));
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
		String bl = PropertyUtil.getStringValue("bulan");
		if (!TextUtils.isEmpty(bl)) {
			JSONArray bulanArray;
			try {
				bulanArray = new JSONArray(bl);
				if (bulanArray != null) {
					if (bulanArray != null) {
						int len = bulanArray.length();
						if (len > 0) {
							buLanModels = new ArrayList<BuLanModel>();
							for (int i = 0; i < len; i++) {
								buLanModels.add(BuLanModel
										.jsonToModel(bulanArray
												.optJSONObject(i)));
							}
						}
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	private void initPageView() {
		topContainer.removeAllViews();
		if (ads == null) {
			return;
		}
		int mPagerH = (int) (mPagerW * aspectRatio);
		if (ads.size() == 1) {
			initSingleImage();
		} else if (ads.size() > 1) {
			mPager = new ViewPager(this.getActivity());
			mPager.setId(R.id.topic_viewpager);
			if (pageAdapter == null) {
				pageAdapter = new MyPageAdapter();
			}
			mPager.setAdapter(pageAdapter);
			topContainer.addView(mPager, mPagerW, mPagerH);
			RelativeLayout.LayoutParams progressparam = new RelativeLayout.LayoutParams(
					-1, 60);
			progressparam.addRule(RelativeLayout.ALIGN_BOTTOM, 1);
			round_point_layout = new LinearLayout(getActivity());
			topContainer.addView(round_point_layout, progressparam);
			round_point_layout.setGravity(Gravity.CENTER);
			setpointView();
			mPager.setOnPageChangeListener(new OnPageChangeListener() {

				@Override
				public void onPageSelected(int arg0) {
					setIndicator(arg0 % ads.size());
				}

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {
				}

				@Override
				public void onPageScrollStateChanged(int arg0) {
				}
			});
		}
	}

	private ImageView[] images;

	private void setpointView() {
		images = new ImageView[ads.size()];
		for (int i = 0; i < images.length; i++) {
			ImageView iv = new ImageView(this.getActivity());
			LinearLayout.LayoutParams prarm = new LinearLayout.LayoutParams(10,
					10);
			prarm.setMargins(10, 0, 10, 0);
			iv.setLayoutParams(prarm);
			if (i == 0) {
				iv.setBackgroundResource(R.drawable.page_indicator_focused);
			} else {
				iv.setBackgroundResource(R.drawable.page_indicator);
			}
			images[i] = iv;
			round_point_layout.addView(images[i]);
		}
	}

	private void setIndicator(int position) {
		for (int i = 0; i < images.length; i++) {
			if (i == position) {
				images[i]
						.setBackgroundResource(R.drawable.page_indicator_focused);
			} else {
				images[i].setBackgroundResource(R.drawable.page_indicator);
			}
		}
	}

	private void initSingleImage() {
		int mPagerH = (int) (mPagerW * aspectRatio);
		ImageView singleImg = new ImageView(getActivity());
		Ads ad = ads.get(0);

		ImageLoadUtil.load(getActivity(), singleImg, ad.recommendImg);
		singleImg.setAdjustViewBounds(true);
		singleImg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Ads ad = ads.get(0);
				if (ad.type == 1) {
					Intent intent = new Intent(getActivity(), Browser.class);
					intent.putExtra("url", ad.value);
					intent.putExtra("title", "布栏推荐");

					startActivity(intent);
				} else if (ad.type == 2) {
					Intent intent = new Intent(getActivity(),
							BulanDetialPage.class);
					intent.putExtra("url", ad.value);
					intent.putExtra("title", "布栏推荐");
					startActivity(intent);
				}
			}
		});
		topContainer.addView(singleImg, mPagerW, mPagerH);
	}

	private void loadAds() {
		HttpUtils.post(CCApplication.HTTPSERVER
				+ "/m_recommend!getRecommends.action", null,
				new JsonHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers,
							JSONObject response) {

						JSONObject body = response.optJSONObject("body");
						if (body == null) {
							// ToastUtil.show("无数据");
							if (mPager != null) {
								topContainer.removeAllViews();
							}
						} else {
							JSONArray recommendInfo = body
									.optJSONArray("recommendInfo");
							if (recommendInfo != null) {
								int size = recommendInfo.length();
								ads = new ArrayList<Ads>();
								for (int i = 0; i < size; i++) {
									ads.add(Ads.jsonToAds(recommendInfo
											.optJSONObject(i)));
								}
								initPageView();
								PropertyUtil.putValue("ad",
										recommendInfo.toString());
							} else {
								PropertyUtil.putValue("ad", "");
								topContainer.removeAllViews();
							}

						}

					}
				});
	}

	private void loadTopic(boolean loadMore) {
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
		params.put("userId",
				String.valueOf(UserManager.getInstance().getLoginUser().ID));
		params.put("pageSize", "20");

		String URL = CCApplication.HTTPSERVER + "/m_bp!findHomeBulans.action";
		HttpUtils.post(URL, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				JSONObject body = response.optJSONObject("body");
				if (body != null) {
					JSONArray bulanArray = body.optJSONArray("bulanInfo");
					if (bulanArray != null) {
						int len = bulanArray.length();
						List<BuLanModel> bulans = new ArrayList<BuLanModel>();
						for (int i = 0; i < len; i++) {
							bulans.add(BuLanModel.jsonToModel(bulanArray
									.optJSONObject(i)));
						}
						callBackBuLan(bulans);
						if (pageIndex == 1) {
							PropertyUtil.putValue("bulan",
									bulanArray.toString());

						}
					} else {
						if (pageIndex == 1) {
							PropertyUtil.putValue("bulan", "");
							adapter.clear();
						}
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

	public void callBackBuLan(List<BuLanModel> bulans) {

		if (pageIndex == 1) {
			this.buLanModels.clear();
		}
		this.buLanModels.addAll(bulans);
		adapter.notifyDataSetChanged();
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
							animateHide();
						} else {
							animateBack();
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

	private void animateBack() {
		TopicFragment.this.toTopView.setVisibility(View.VISIBLE);
		TopicFragment.this.toTopView.removeCallbacks(dismiss);
		TopicFragment.this.toTopView.postDelayed(dismiss, 2000);
	}

	private void animateHide() {
		TopicFragment.this.toTopView.setVisibility(View.GONE);
	}

	private Runnable dismiss = new Runnable() {
		@Override
		public void run() {
			TopicFragment.this.toTopView.setVisibility(View.GONE);
		}
	};

	class MyPageAdapter extends PagerAdapter {
		@Override
		public int getCount() {
			return Integer.MAX_VALUE;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}

		@Override
		public Object instantiateItem(View container, int position) {
			// TODO Auto-generated method stub
			final Ads ad = ads.get(position % ads.size());
			RelativeLayout temp = new RelativeLayout(getActivity());
			temp.setGravity(Gravity.CENTER);
			ImageView img = new ImageView(getActivity());
			// img.setDefaultImage(R.drawable.ic_launcher);
			ImageLoadUtil.load(getActivity(), img, ad.recommendImg, new int[] {
					CCApplication.screenWidth,
					(int) (CCApplication.density * 120) });
			img.setScaleType(ScaleType.FIT_XY);

			img.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (ad.type == 1) {
						Intent intent = new Intent(getActivity(), Browser.class);
						intent.putExtra("url", ad.value);
						intent.putExtra("title", "布栏推荐");
						startActivity(intent);
					} else if (ad.type == 0) {
						Intent intent = new Intent(getActivity(),
								BulanDetialPage.class);
						intent.putExtra("bulan_id", Long.parseLong(ad.value));
						startActivity(intent);
					}
				}
			});
			temp.addView(img, -1, -1);
			((ViewPager) container).addView(temp);
			return temp;
		}

		@Override
		public void finishUpdate(View arg0) {

		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}
	}
}
