package com.mingmay.bulan.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.mingmay.bulan.R;
import com.mingmay.bulan.model.Group;

public class SimpleZhuanLanAdapter extends BaseAdapter {
	private Activity activity;
	private List<Group> groups;

	private List<Group> selected;

	public List<Group> getSelected() {
		return selected;
	}

	public SimpleZhuanLanAdapter(Activity activity, List<Group> groups) {
		// TODO Auto-generated constructor stub
		this.activity = activity;
		this.groups = groups;
		selected = new ArrayList<Group>();
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
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public Group getItem(int arg0) {
		return groups.get(arg0);
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
					R.layout.layout_item_simple_zhuanlan, parent, false);
			viewHold = new ViewHolder();
			viewHold.name = (TextView) convertView.findViewById(R.id.name);
			viewHold.selected = (CheckBox) convertView
					.findViewById(R.id.selected);
			convertView.setTag(viewHold);
		} else {
			viewHold = (ViewHolder) convertView.getTag();
		}
		final Group group = getItem(position);
		viewHold.name.setText(group.name);
		viewHold.selected.setOnCheckedChangeListener(null);
		viewHold.name.setTextColor(activity.getResources().getColor(
				R.color.black));
		if (selected.contains(group)) {
			viewHold.selected.setChecked(true);
			viewHold.name.setTextColor(activity.getResources().getColor(
					R.color.actionsheet_red));
		} else {
			viewHold.selected.setChecked(false);
		}
		viewHold.selected
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {

						if (selected.contains(group)) {
							selected.remove(group);
						} else {
							selected.add(group);
						}

						notifyDataSetChanged();
					}
				});
		return convertView;
	}

	static class ViewHolder {
		TextView name;
		CheckBox selected;

	}

}
