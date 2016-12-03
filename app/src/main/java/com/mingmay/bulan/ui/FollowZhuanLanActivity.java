package com.mingmay.bulan.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseExpandGridView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mingmay.bulan.R;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.base.BaseActivity;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.ui.friend.ContactPage;
import com.mingmay.bulan.util.DensityUtil;
import com.mingmay.bulan.util.ProgressDialogUtil;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpUtils;

public class FollowZhuanLanActivity extends BaseActivity {
	private PullToRefreshListView mPullRefreshListView;
	private int pageIndex;
	private long id;
	private String nickName;
	private EaseExpandGridView userGridview;
	private GridAdapter adapter;

	private static final int REQUEST_CODE_ADD_USER = 0;
	private static final int REQUEST_CODE_EXIT = 1;
	private List<User> userList_old;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		id = getIntent().getLongExtra("id", 0);
		nickName = getIntent().getStringExtra("user_nickname");
		if (id <= 0) {
			finish();
		}
		setContentView(R.layout.activity_follow_list);
		userGridview = (EaseExpandGridView) findViewById(R.id.gridview);
		findViewById(R.id.add_new_user).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						openContact();
					}
				});

		findViewById(R.id.manager).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				submit();
			}
		});
		List<User> members = new ArrayList<User>();

		members.add(new User(0));
		members.add(new User(-1));
		adapter = new GridAdapter(this, members);
		userGridview.setAdapter(adapter);
		userGridview.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (adapter.isInDeleteMode) {
						adapter.isInDeleteMode = false;
						adapter.addOption(new User(0));
						adapter.addOption(new User(-1));
						return true;
					}
					break;
				default:
					break;
				}
				return false;
			}
		});

		((TextView) findViewById(R.id.title)).setText(nickName + "的专栏");
		loadDetial();
	}

	public void back(View c) {
		finish();
	}

	private void loadDetial() {

		ProgressDialogUtil.showProgress(this, "正在加载数据....");
		RequestParams params = new RequestParams();
		params.put("userId", UserManager.getInstance().getLoginUser().ID);
		params.put("wardrobeId", id);
		HttpUtils.post(CCApplication.HTTPSERVER
				+ "/m_wardrobe!getWardrobe.action", params,
				new JsonHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers,
							JSONObject response) {

						JSONObject body = response.optJSONObject("body");
						JSONObject wardrobeInfo = body
								.optJSONObject("wardrobeInfo");

						if (wardrobeInfo != null) {

							JSONArray users = wardrobeInfo
									.optJSONArray("userInfo");

							if (users != null && users.length() > 0) {
								List<User> userList = new ArrayList<User>();
								for (int i = 0; i < users.length(); i++) {
									JSONObject userObj = users.optJSONObject(i);
									User u = new User();
									u.ID = userObj.optLong("userId");
									u.firstName = userObj.optString("userName");
									u.userImg = userObj.optString("userImg");
									userList.add(u);
								}
								userList_old = userList;
								for (User user : userList) {
									adapter.addToEnd(user);
								}
							}
						} else {
							ToastUtil.show("服务器异常");
							finish();
						}

						ProgressDialogUtil.dismiss();
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							String responseString, Throwable throwable) {
						ToastUtil.show("加载数据失败..");
						ProgressDialogUtil.dismiss();
						finish();
					}
				});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE_ADD_USER:// 添加群成员
				if (data == null) {
					return;
				}
				final String[] newmembers = data
						.getStringArrayExtra("newmembers");
				if (newmembers == null) {
					return;
				}
				for (String id : newmembers) {
					long i = Long.parseLong(id);
					adapter.addToEnd(new User(i));
				}
				adapter.notifyDataSetChanged();
				break;
			}
		}
	}

	private void submit() {

		String[] members = adapter.getMemberList();

		if (members == null || members.length == 0) {
			ToastUtil.show("您没有邀请任何人！");
			return;
		}

		ProgressDialogUtil.showProgress(this, "正在提交数据....");
		RequestParams params = new RequestParams();

		int size = members.length;
		if (size > 0) {
			String str = null;
			for (int i = 0; i < size; i++) {
				if (i == 0)
					str = members[i];
				else
					str += "," + members[i];
			}
			params.add("friendUserIds", str);
		}
		params.put("userId", UserManager.getInstance().getLoginUser().ID);
		params.put("ccukey", UserManager.getInstance().getLoginUser().ccukey);
		params.put("wardrobeId", id);
		HttpUtils.post(CCApplication.HTTPSERVER
				+ "/m_wardrobe!invitationUesrWardrobe.action", params,
				new JsonHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers,
							JSONObject response) {

						JSONObject body = response.optJSONObject("body");
						JSONObject wardrobeInfo = body
								.optJSONObject("wardrobeInfo");

						int cstatus = body.optInt("cstatus");

						if (cstatus == 0) {
							ToastUtil.show("邀请成功");
							finish();
						} else {
							ToastUtil.show("服务器异常");
						}
						ProgressDialogUtil.dismiss();
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							String responseString, Throwable throwable) {
						ToastUtil.show("加载数据失败..");
						ProgressDialogUtil.dismiss();
					}
				});
	}

	private void openContact() {

		Intent toAddMember = new Intent(FollowZhuanLanActivity.this,
				ContactPage.class);
		String[] menmbers = adapter.getMemberList();
		if (menmbers != null && menmbers.length > 0) {
			toAddMember.putExtra("exist_members", menmbers);
		}
		toAddMember.putExtra("need_back", true);
		startActivityForResult(toAddMember, REQUEST_CODE_ADD_USER);
	}

	private class GridAdapter extends BaseAdapter {

		private int res;
		public boolean isInDeleteMode;
		private List<User> objects;

		private Context context;

		int w;

		public GridAdapter(Context context, List<User> objects) {
			this.context = context;
			this.objects = objects;
			isInDeleteMode = false;
			w = DensityUtil.dip2px(60);
		}

		public void addToEnd(User user) {
			if (!objects.contains(user)) {
				
				if(objects.size()>=2){
					this.objects.add(getCount() - 2, user);
				}else{
					this.objects.add(0, user);
				}
				
			}
			notifyDataSetChanged();
		}

		public void addOption(User user) {
			if (!objects.contains(user)) {
				this.objects.add(user);
			}
			notifyDataSetChanged();
		}

		@Override
		public View getView(final int position, View convertView,
				final ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(context).inflate(
						R.layout.em_grid, parent, false);
				holder.imageView = (ImageView) convertView
						.findViewById(R.id.iv_avatar);
				holder.textView = (TextView) convertView
						.findViewById(R.id.tv_name);
				holder.badgeDeleteView = (ImageView) convertView
						.findViewById(R.id.badge_delete);
				holder.button_avatar = (LinearLayout) convertView
						.findViewById(R.id.button_avatar);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final User user = getItem(position);
			holder.badgeDeleteView.setVisibility(View.GONE);
			holder.textView.setVisibility(View.GONE);

			if (user.ID == -1) {
				// del

				Glide.with(context).load("-1")
						.error(R.drawable.em_smiley_minus_btn)
						.placeholder(R.drawable.em_smiley_minus_btn)
						.override(60, 60).into(holder.imageView);

				holder.button_avatar.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// 进入选人页面
						objects.remove(new User(0));
						objects.remove(new User(-1));
						isInDeleteMode = true;
						notifyDataSetChanged();

					}
				});
			} else if (user.ID == 0) {
				// add
				Glide.with(context).load("0")
						.error(R.drawable.em_smiley_add_btn)
						.placeholder(R.drawable.em_smiley_add_btn)
						.override(w, w).into(holder.imageView);
				holder.button_avatar.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// 进入选人页面
						openContact();
					}
				});
			} else {
				holder.textView.setVisibility(View.VISIBLE);
				final String username = String.valueOf(getItem(position).ID);
				String nickName = 	EaseUserUtils.getUserInfo(username).getNick();
				String avatar =	EaseUserUtils.getUserInfo(username).getAvatar();
				EaseUserUtils.setUserNick(nickName, holder.textView);
				Glide.with(context).load(avatar).error(R.drawable.headlogo)
						.override(w, w).into(holder.imageView);

				if (isInDeleteMode) {
					holder.badgeDeleteView.setVisibility(View.VISIBLE);
				} else {
					holder.badgeDeleteView.setVisibility(View.GONE);
				}
				holder.button_avatar.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (isInDeleteMode) {
							objects.remove(position);
							notifyDataSetChanged();
						}
					}

				});
			}
			return convertView;
		}

		public String[] getMemberList() {
			String[] member;
			int i = 0;
			if (isInDeleteMode) {
				member = new String[getCount()];
				for (User u : objects) {
					member[i] = String.valueOf(u.ID);
					i++;
				}
			} else {
				member = new String[getCount() - 2];
				for (User u : objects) {
					if (u.ID == 0) {
						break;
					}
					member[i] = String.valueOf(u.ID);
					i++;
				}
			}
			return member;
		}

		@Override
		public int getCount() {
			return objects.size();
		}

		@Override
		public User getItem(int position) {
			return objects.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}
	}

	private static class ViewHolder {
		ImageView imageView;
		TextView textView;
		ImageView badgeDeleteView;
		LinearLayout button_avatar;
	}

}
