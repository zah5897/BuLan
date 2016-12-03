package com.mingmay.bulan.adapter;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mingmay.bulan.R;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.model.Group;
import com.mingmay.bulan.util.ImageLoadUtil;

public class ZhuanLanAdapter extends BaseAdapter {
	private Activity activity;
	private List<Group> groups;

	public ZhuanLanAdapter(Activity activity, List<Group> groups) {
		// TODO Auto-generated constructor stub
		this.activity = activity;
		this.groups = groups;
	}

	public void addAndClear(List<Group> groups) {
		this.groups.clear();
		this.groups.addAll(groups);
		notifyDataSetChanged();
	}

	public void setNewData(List<Group> bulans) {
		this.groups = bulans;
		notifyDataSetChanged();
	}

	public void add(List<Group> bulans) {
		this.groups.addAll(bulans);
		notifyDataSetChanged();
	}

	public void addToTop(Group group) {
		this.groups.add(0, group);
		notifyDataSetChanged();
	}

	public List<Group> getData() {
		return groups;
	}

	public void clear() {
		this.groups.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return groups.size();
	}

	@Override
	public Group getItem(int arg0) {
		if (arg0 >= 0 && arg0 < groups.size()) {
			return groups.get(arg0);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHold;
		if (convertView == null) {
			convertView = LayoutInflater.from(activity).inflate(
					R.layout.layout_item_zhuanlan, parent, false);
			viewHold = new ViewHolder();
			viewHold.icon = (ImageView) convertView.findViewById(R.id.image);
			viewHold.name = (TextView) convertView.findViewById(R.id.name);
			viewHold.group_owner = (TextView) convertView
					.findViewById(R.id.group_owner);
			viewHold.count = (TextView) convertView.findViewById(R.id.count);
			convertView.setTag(viewHold);
		} else {
			viewHold = (ViewHolder) convertView.getTag();
		}

		Group group = getItem(position);

		if (group.id == -1) {
			viewHold.icon.setImageResource(R.drawable.em_smiley_add_btn);
			viewHold.name.setText(group.name);
			viewHold.group_owner.setText("邀上您的好友组建自己的兴趣社团");
			viewHold.count.setText("");
		} else {
			int wh = (int) (CCApplication.density * 50);

			ImageLoadUtil.load(activity, viewHold.icon, group.icon, new int[] {
					wh, wh });
			viewHold.name.setText(group.name);
			viewHold.group_owner.setText(group.userName);
			viewHold.count.setText(String.valueOf(group.count));
		}
		return convertView;
	}

	static class ViewHolder {
		ImageView icon;
		TextView name;
		TextView group_owner;
		TextView count;

	}

}
