package com.mingmay.bulan.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.mingmay.bulan.R;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.ui.friend.ContactPage;

public class GridAdapter extends BaseAdapter {

	private int res;
	public boolean isInDeleteMode;
	private List<User> objects;
	private static final int REQUEST_CODE_ADD_USER = 0;
	private Context context;

	public GridAdapter(Context context, int textViewResourceId,
			List<User> objects) {
		this.context = context;
		this.objects = new ArrayList<User>();
		this.objects.add(new User(-1));
		this.objects.add(new User(-2));
		this.objects.addAll(0, objects);
		res = textViewResourceId;
		isInDeleteMode = false;
	}

	public void setFlag(boolean isInDeleteMode) {
		this.isInDeleteMode = isInDeleteMode;

		if (isInDeleteMode) {
			objects.remove(objects.size() - 1);
			objects.remove(objects.size() - 1);
		} else {
			this.objects.add(new User(-1));
			this.objects.add(new User(-2));
		}
		notifyDataSetChanged();
	}

	public List<User> getObjects() {
		return objects;
	}

	@Override
	public View getView(final int position, View convertView,
			final ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(res, null);
			holder.imageView = (ImageView) convertView
					.findViewById(R.id.iv_avatar);
			holder.textView = (TextView) convertView.findViewById(R.id.tv_name);
			holder.badgeDeleteView = (ImageView) convertView
					.findViewById(R.id.badge_delete);
			holder.button = (LinearLayout) convertView
					.findViewById(R.id.button_avatar);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		User user = getItem(position);
		// 最后一个item，减人按钮
		if (user.ID == -2) {
			holder.textView.setText("");
			// 设置成删除按钮
			holder.imageView.setImageResource(R.drawable.em_smiley_minus_btn);
			holder.button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					setFlag(true);
				}
			});
			// }
		} else if (user.ID == -1) { // 添加群组成员按钮
			holder.textView.setText("");
			holder.imageView.setImageResource(R.drawable.em_smiley_add_btn);

			holder.button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent toAddMember = new Intent(context, ContactPage.class);
					toAddMember.putExtra("need_back", true);
					((Activity) context).startActivityForResult(toAddMember,
							REQUEST_CODE_ADD_USER);
				}
			});
		} else { // 普通item，显示群组成员
			User u = getItem(position);
			String nickName = u.firstName;
			String avatar = u.userImg;
			String username = u.ID + "";
			if (nickName == null) {
				nickName = EaseUserUtils.getUserInfo(username).getNick();
			}

			if (avatar == null) {
				avatar = EaseUserUtils.getUserInfo(username).getAvatar();
			}

			EaseUserUtils.setUserNick(username, holder.textView);
			Glide.with(context).load(avatar).override(50, 50)
					.into(holder.imageView);
			if (isInDeleteMode) {
				convertView.findViewById(R.id.badge_delete).setVisibility(
						View.VISIBLE);
			} else {
				convertView.findViewById(R.id.badge_delete).setVisibility(
						View.INVISIBLE);
			}
			holder.button.setOnClickListener(new OnClickListener() {
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

	@Override
	public int getCount() {
		return objects.size();
	}

	static class ViewHolder {
		ImageView imageView;
		TextView textView;
		ImageView badgeDeleteView;
		LinearLayout button;
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

	public void addAll(List<User> userList) {
		this.objects.addAll(0, userList);
		notifyDataSetChanged();
	}
}
