package com.mingmay.bulan.adapter;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mingmay.bulan.R;
import com.mingmay.bulan.model.User;

public class FriendAdapter extends BaseAdapter {
	private Context activity;

	public FriendAdapter(Context activity, List<User> friends) {
		this.activity = activity;
		this.friends = friends;
	}

	private List<User> friends;

//	public void clearAndAdd(ArrayList<User> friends) {
//		this.friends.clear();
//		this.friends.addAll(friends);
//		notifyDataSetChanged();
//	}

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
	public User getItem(int arg0) {
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
			convertView = LayoutInflater.from(activity).inflate(
					R.layout.layout_item_friend, parent,false);
			viewHold = new ViewHolder();
			viewHold.icon = (ImageView) convertView
					.findViewById(R.id.head_icon);
			viewHold.name = (TextView) convertView.findViewById(R.id.name);
			viewHold.signature_last_msg = (TextView) convertView
					.findViewById(R.id.signature_last_msg);
			viewHold.time = (TextView) convertView.findViewById(R.id.time);
			convertView.setTag(viewHold);
		} else {
			viewHold = (ViewHolder) convertView.getTag();
		}
		User f = getItem(position);
		viewHold.name.setText(f.firstName);
		if (TextUtils.isEmpty(f.signature)) {
			viewHold.signature_last_msg.setVisibility(View.GONE);
			// viewHold.signature_last_msg.setText(f.signature);
		} else {
			viewHold.signature_last_msg.setVisibility(View.VISIBLE);
			viewHold.signature_last_msg.setText(f.signature);
		}

		viewHold.time.setText(f.chatMessageDate);
		return convertView;
	}

	static class ViewHolder {
		 ImageView icon;
		TextView name, signature_last_msg, time;
	}

}
