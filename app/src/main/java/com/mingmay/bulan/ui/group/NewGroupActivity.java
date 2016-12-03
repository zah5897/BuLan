package com.mingmay.bulan.ui.group;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMGroupManager.EMGroupOptions;
import com.hyphenate.chat.EMGroupManager.EMGroupStyle;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseExpandGridView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mingmay.bulan.R;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.base.BaseActivity;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.ui.ChatActivity;
import com.mingmay.bulan.ui.friend.ContactPage;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpUtils;

public class NewGroupActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = "NewGroupActivity";
	private static final int REQUEST_CODE_ADD_USER = 0;
	private static final int REQUEST_CODE_EXIT = 1;

	private EaseExpandGridView userGridview;
	private Button createGroup;
	private GridAdapter adapter;
	private ProgressDialog progressDialog;

	public static NewGroupActivity instance;

	String st = "";
	// 清空所有聊天记录

	private String fristNumber;

	public String groupId;

	private EditText groupName, desc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fristNumber = getIntent().getStringExtra("user_id");
		setContentView(R.layout.em_activity_new_group);

		((TextView) findViewById(R.id.group_name)).setText("创建群聊");
		groupName = (EditText) findViewById(R.id.tv_group_id_value);
		desc = (EditText) findViewById(R.id.desc);
		User firstMemberUser = UserManager.getInstance().getUser(fristNumber);
		if (firstMemberUser != null) {
			groupName
					.setText(UserManager.getInstance().getLoginUser().firstName
							+ "和" + firstMemberUser.firstName + "的群");
		} else {
			groupName
					.setText(UserManager.getInstance().getLoginUser().firstName
							+ "的群");
		}

		desc.setText(groupName.getText());
		instance = this;
		st = getResources().getString(R.string.people);
		userGridview = (EaseExpandGridView) findViewById(R.id.gridview);
		createGroup = (Button) findViewById(R.id.create_group);
		createGroup.setOnClickListener(this);

		// 如果自己是群主，显示解散按钮

		List<String> members = new ArrayList<String>();
		members.add(fristNumber);

		adapter = new GridAdapter(this, R.layout.em_grid, members);
		userGridview.setAdapter(adapter);
		adapter.showAddAndDel();

		// 保证每次进详情看到的都是最新的group

		// 设置OnTouchListener
		userGridview.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (adapter.isInDeleteMode) {
						adapter.showAddAndDel();
						return true;
					}
					break;
				default:
					break;
				}
				return false;
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE_ADD_USER:// 添加群成员
				final String[] newmembers = data
						.getStringArrayExtra("newmembers");
				if (newmembers != null) {
					adapter.addAll(newmembers);
				}
				break;
			default:
				break;
			}
		}
	}

	// private void refreshMembers() {
	// adapter.clear();
	//
	// List<String> members = new ArrayList<String>();
	// members.addAll(group.getMembers());
	// adapter.addAll(members);
	//
	// adapter.notifyDataSetChanged();
	// }

	/**
	 * 增加群成员
	 * 
	 * @param newmembers
	 */

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.create_group:
			createGroup();
			break;
		default:
			break;
		}

	}

	private void createGroup() {
		String groupnameStr = groupName.getText().toString();
		String descStr = desc.getText().toString();

		if (TextUtils.isEmpty(groupnameStr)) {
			ToastUtil.show("群名称不能为空");
			return;
		}

		if (TextUtils.isEmpty(descStr)) {
			ToastUtil.show("群描述不能为空");
			return;
		}

		List<String> memberIds = adapter.getObjects();

		
		
		JSONArray membersArray = new JSONArray();
		for (String id : memberIds) {
			if ("0".equals(id)) {
				continue;
			}
			if ("-1".equals(id)) {
				continue;
			}
			membersArray.put(id);
		}

		if (membersArray.length() < 1) {
			progressDialog.dismiss();
			ToastUtil.show("群成员不能为空");
			return;
		}
		
		
		
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("正在创建群...");
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();

		String url = CCApplication.HTTPSERVER
				+ "/m_group!addEasemobGroup.action";
		RequestParams params = new RequestParams();
		params.add("userId",
				String.valueOf(UserManager.getInstance().getLoginUser().ID));
		params.add("ccukey",
				String.valueOf(UserManager.getInstance().getLoginUser().ccukey));
		params.add("groupName", groupnameStr);
		params.add("desc", descStr);

		

		params.add("members", membersArray.toString());

		HttpUtils.post(url, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				progressDialog.dismiss();
				
				
				JSONObject body=response.optJSONObject("body");
				
				if(body==null){
					ToastUtil.show("创建群失败");
					return;
				}
				
				JSONObject gorupInfo = body.optJSONObject("groupInfo");
				if (gorupInfo != null) {
					String gid = gorupInfo.optString("groupId");
					
					if (TextUtils.isEmpty(gid)) {
						ToastUtil.show("创建群失败");
						return;
					}
					Intent toChat = new Intent(getBaseContext(),
							ChatActivity.class);
					toChat.putExtra("userId", gid);
					toChat.putExtra("chatType", EaseConstant.CHATTYPE_GROUP);
					startActivity(toChat);
					finish();
				} else {
					ToastUtil.show("创建群失败");
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				progressDialog.dismiss();
				ToastUtil.show("创建群失败");
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				progressDialog.dismiss();
				ToastUtil.show("创建群失败");
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONArray errorResponse) {
				progressDialog.dismiss();
				ToastUtil.show("创建群失败");
			}
		});

	}

	/**
	 * 群组成员gridadapter
	 * 
	 * @author admin_new
	 * 
	 */
	private class GridAdapter extends ArrayAdapter<String> {

		private int res;
		private boolean isInDeleteMode;
		private List<String> objects;

		public GridAdapter(Context context, int textViewResourceId,
				List<String> objects) {
			super(context, textViewResourceId, objects);
			this.objects = objects;
			res = textViewResourceId;

		}

		public void showAddAndDel() {

			if (!objects.contains("-1")) {
				objects.add("-1");
			}

			if (!objects.contains("0")) {
				objects.add(objects.size() - 1, "0");
			}
			isInDeleteMode = false;
			notifyDataSetChanged();
		}

		public void dismissAddAndDel() {
			if (objects.contains("-1")) {
				objects.remove("-1");
			}
			if (objects.contains("0")) {
				objects.remove("0");
			}
			isInDeleteMode = true;
			notifyDataSetChanged();
		}

		public List<String> getObjects() {
			return objects;
		}

		@Override
		public void addAll(String... items) {
			if (objects == null) {
				return;
			}
			for (String s : items) {
				if (!objects.contains(s)) {
					objects.add(s);
				}

			}
			notifyDataSetChanged();
		}

		@Override
		public View getView(final int position, View convertView,
				final ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(getContext()).inflate(res,
						null);
				holder.imageView = (ImageView) convertView
						.findViewById(R.id.iv_avatar);
				holder.textView = (TextView) convertView
						.findViewById(R.id.tv_name);
				holder.badgeDeleteView = (ImageView) convertView
						.findViewById(R.id.badge_delete);
				holder.button_avatar = convertView
						.findViewById(R.id.button_avatar);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			String user_id = getItem(position);

			if ("-1".equals(user_id)) {
				// del
				holder.textView.setText("删除好友");
				holder.imageView
						.setImageResource(R.drawable.em_smiley_minus_btn);
				holder.button_avatar.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dismissAddAndDel();
					}
				});
				holder.badgeDeleteView.setVisibility(View.INVISIBLE);
			} else if ("0".equals(user_id)) {
				// add
				holder.textView.setText("邀请好友");
				holder.imageView.setImageResource(R.drawable.em_smiley_add_btn);
				holder.button_avatar.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent toAddMember = new Intent(NewGroupActivity.this,
								ContactPage.class);
						toAddMember.putExtra("groupId", groupId);
						toAddMember.putExtra("need_back", true);
						startActivityForResult(toAddMember,
								REQUEST_CODE_ADD_USER);
					}
				});
				holder.badgeDeleteView.setVisibility(View.INVISIBLE);
			} else {
				holder.button_avatar.setOnClickListener(null);

				 
				String avatar = EaseUserUtils.getUserInfo(user_id).getAvatar();

				EaseUserUtils.setUserNick(user_id, holder.textView);
				Glide.with(getContext()).load(avatar).override(60, 60)
						.into(holder.imageView);

				if (isInDeleteMode) {
					// 如果是删除模式下，显示减人图标
					convertView.findViewById(R.id.badge_delete).setVisibility(
							View.VISIBLE);
				} else {
					convertView.findViewById(R.id.badge_delete).setVisibility(
							View.INVISIBLE);
				}

				holder.button_avatar.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						objects.remove(position);
						notifyDataSetChanged();
					}
				});

			}

			return convertView;
		}

		@Override
		public int getCount() {
			return super.getCount();
		}
	}

	public void back(View view) {
		setResult(RESULT_OK);
		finish();
	}

	@Override
	public void onBackPressed() {
		setResult(RESULT_OK);
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		instance = null;
	}

	private static class ViewHolder {
		ImageView imageView;
		TextView textView;
		ImageView badgeDeleteView;

		View button_avatar;
	}

}
