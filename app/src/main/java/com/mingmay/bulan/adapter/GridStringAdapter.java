package com.mingmay.bulan.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.mingmay.bulan.R;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.ui.friend.ContactPage;

public class GridStringAdapter extends ArrayAdapter<User> {

	private int res;
	public boolean isInDeleteMode;
	private List<User> objects;
	private static final int REQUEST_CODE_ADD_USER = 0;
	private Context context;

	public GridStringAdapter(Context context, int textViewResourceId,
			List<User> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
		this.objects = objects;
		res = textViewResourceId;
		isInDeleteMode = false;
	}

	public List<User> getObjects() {
		return objects;
	}

	@Override
	public void addAll(User... items) {
		if (objects == null) {
			return;
		}
		for (User s : items) {
			objects.add(s);
		}
		notifyDataSetChanged();
	}

	@Override
	public View getView(final int position, View convertView,
			final ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(getContext()).inflate(res, null);
			holder.imageView = (ImageView) convertView
					.findViewById(R.id.iv_avatar);
			holder.textView = (TextView) convertView.findViewById(R.id.tv_name);
			holder.badgeDeleteView = (ImageView) convertView
					.findViewById(R.id.badge_delete);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final LinearLayout button = (LinearLayout) convertView
				.findViewById(R.id.button_avatar);
		// 最后一个item，减人按钮
		if (position == getCount() - 1) {
			holder.textView.setText("");
			// 设置成删除按钮
			holder.imageView.setImageResource(R.drawable.em_smiley_minus_btn);
			if (isInDeleteMode) {
				// 正处于删除模式下，隐藏删除按钮
				convertView.setVisibility(View.INVISIBLE);
			} else {
				// 正常模式
				convertView.setVisibility(View.VISIBLE);
				convertView.findViewById(R.id.badge_delete).setVisibility(
						View.INVISIBLE);
			}
			final String st10 = context.getResources().getString(
					R.string.The_delete_button_is_clicked);
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					isInDeleteMode = true;
					notifyDataSetChanged();
				}
			});
			// }
		} else if (position == getCount() - 2) { // 添加群组成员按钮
			holder.textView.setText("");
			holder.imageView.setImageResource(R.drawable.em_smiley_add_btn);
			if (isInDeleteMode) {
				convertView.setVisibility(View.INVISIBLE);
			} else {
				convertView.setVisibility(View.VISIBLE);
				convertView.findViewById(R.id.badge_delete).setVisibility(
						View.INVISIBLE);
			}
			final String st11 = context.getResources().getString(
					R.string.Add_a_button_was_clicked);
			button.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent toAddMember = new Intent(context,ContactPage.class);
					toAddMember.putExtra("need_back", true);
					((Activity)context).startActivityForResult(toAddMember, REQUEST_CODE_ADD_USER);
				}
			});
		} else { // 普通item，显示群组成员
			User u = getItem(position);
			convertView.setVisibility(View.VISIBLE);
			button.setVisibility(View.VISIBLE);

			String nickName = u.firstName;
			String avatar = u.userImg;
			String username = u.ID + "";
			if (nickName == null) {
				nickName = EaseUserUtils.getUserInfo(username).getNick();
			}

			if (avatar == null) {
				avatar = EaseUserUtils.getUserInfo(username).getAvatar();
			}

			// Drawable avatar =
			// getResources().getDrawable(R.drawable.default_avatar);
			// avatar.setBounds(0, 0, referenceWidth, referenceHeight);
			// button.setCompoundDrawables(null, avatar, null, null);
			EaseUserUtils.setUserNick(nickName, holder.textView);
			// EaseUserUtils.setUserAvatar(getContext(), username,
			// holder.imageView);
			Glide.with(getContext()).load(avatar).override(50, 50)
					.into(holder.imageView);
			if (isInDeleteMode) {
				// 如果是删除模式下，显示减人图标
				convertView.findViewById(R.id.badge_delete).setVisibility(
						View.VISIBLE);
			} else {
				convertView.findViewById(R.id.badge_delete).setVisibility(
						View.INVISIBLE);
			}
			final String st12 = context.getResources().getString(
					R.string.not_delete_myself);
			final String st13 = context.getResources().getString(R.string.Are_removed);
			final String st14 = context.getResources()
					.getString(R.string.Delete_failed);
			final String st15 = context.getResources().getString(
					R.string.confirm_the_members);
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (isInDeleteMode) {
						// 如果是删除自己，return
						objects.remove(position);
						notifyDataSetChanged();
					} else {
						// 正常情况下点击user，可以进入用户详情或者聊天页面等等
						// startActivity(new
						// Intent(GroupDetailsActivity.this,
						// ChatActivity.class).putExtra("userId",
						// user.getUsername()));

					}
				}
			});
		}
		return convertView;
	}

	@Override
	public int getCount() {
		return super.getCount() + 2;
	}

	static class ViewHolder {
		ImageView imageView;
		TextView textView;
		ImageView badgeDeleteView;
	}
}
