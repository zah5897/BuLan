package com.mingmay.bulan.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mingmay.bulan.R;
import com.mingmay.bulan.model.CommentInfo;
import com.mingmay.bulan.util.ImageLoadUtil;

public class CommentAdapter extends BaseAdapter {
	private Activity activity;

	public CommentAdapter(Activity activity, List<CommentInfo> comments) {
		// TODO Auto-generated constructor stub
		this.activity = activity;
		this.comments = comments;
	}

	private List<CommentInfo> comments;

	public void add(ArrayList<CommentInfo> comments) {

		this.comments.addAll(comments);
		notifyDataSetChanged();
	}
	public void add(CommentInfo comments) {
		this.comments.add(0,comments);
		notifyDataSetChanged();
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return comments.size();
	}

	@Override
	public CommentInfo getItem(int arg0) {
		// TODO Auto-generated method stub
		return comments.get(arg0);
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
					R.layout.layout_item_comment, parent ,false);
			viewHold = new ViewHolder();
			viewHold.icon = (ImageView) convertView.findViewById(R.id.image);
			viewHold.name = (TextView) convertView.findViewById(R.id.name);
			viewHold.time = (TextView) convertView.findViewById(R.id.time);
			viewHold.content = (TextView) convertView
					.findViewById(R.id.content);
			convertView.setTag(viewHold);
		} else {
			viewHold = (ViewHolder) convertView.getTag();
		}
		CommentInfo info = getItem(position);
//		viewHold.icon
//				.setImageUrl(info.createUserImg);
		ImageLoadUtil.load(activity, viewHold.icon, info.createUserImg);
		viewHold.name.setText(info.createUserName);
		viewHold.time.setText(String.valueOf(info.createDate));
		viewHold.content.setText(info.commentText);
		return convertView;
	}

	private static class ViewHolder {
		ImageView icon;
		TextView name;
		TextView time;
		TextView content;
	}

}
