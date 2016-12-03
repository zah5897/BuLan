package com.mingmay.bulan.adapter;

import java.util.ArrayList;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mingmay.bulan.R;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.ui.friend.ContactPage;

public class FriendPageAdapter extends BaseAdapter {
	private ContactPage activity;

	public FriendPageAdapter(ContactPage activity, ArrayList<User> friends) {
		// TODO Auto-generated constructor stub
		this.activity = activity;
		this.friends = friends;
	}

	private ArrayList<User> friends;

	public void clearAndAdd(ArrayList<User> friends) {
		this.friends.clear();
		this.friends.addAll(friends);
		notifyDataSetChanged();
	}

	public void clear() {
		this.friends.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return friends.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return friends.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHold;
		if (convertView == null) {
			convertView = activity.getLayoutInflater().inflate(
					R.layout.layout_item_friend_page, parent, false);
			viewHold = new ViewHolder();
			viewHold.icon = (ImageView) convertView
					.findViewById(R.id.head_icon);
			viewHold.name = (TextView) convertView.findViewById(R.id.name);
			viewHold.lastmsg = (TextView) convertView
					.findViewById(R.id.last_msg);
			viewHold.gender = (TextView) convertView.findViewById(R.id.gender);
			convertView.setTag(viewHold);
		} else {
			viewHold = (ViewHolder) convertView.getTag();
		}
		User friend = (User) getItem(position);
		viewHold.name.setText(friend.firstName);
		viewHold.gender.setText("男");
		return convertView;
	}

	static class ViewHolder {
		ImageView icon;
		TextView name, lastmsg, gender;
	}

}
