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
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.model.BuLanModel;
import com.mingmay.bulan.util.ImageLoadUtil;

public class MyBuLanAdapter extends BaseAdapter {

	private Activity activity;

	public MyBuLanAdapter(Activity activity, ArrayList<BuLanModel> bulans) {
		this.activity = activity;
		this.bulans = bulans;
	}

	private ArrayList<BuLanModel> bulans;

	public void add(List<BuLanModel> bulans2) {
		this.bulans.addAll(bulans2);
		notifyDataSetChanged();
	}

	public void addAndClear(List<BuLanModel> bulans2) {
		// TODO Auto-generated method stub
		bulans.clear();
		bulans.addAll(bulans2);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return bulans.size();
	}

	@Override
	public BuLanModel getItem(int arg0) {
		// TODO Auto-generated method stub
		return bulans.get(arg0);
	}

	public void remove(int position){
		bulans.remove(position);
		notifyDataSetChanged();
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
			viewHold = new ViewHolder();
			convertView = LayoutInflater.from(activity).inflate(
					R.layout.layout_item_bulan_mine, parent,false);
			viewHold.icon = (ImageView) convertView.findViewById(R.id.image);
			viewHold.day = (TextView) convertView.findViewById(R.id.day);
			viewHold.type = (TextView) convertView.findViewById(R.id.type);
			viewHold.summary = (TextView) convertView
					.findViewById(R.id.summary);
			viewHold.scan = (TextView) convertView
					.findViewById(R.id.scan_count);
			viewHold.forword = (TextView) convertView
					.findViewById(R.id.forward_count);
			convertView.setTag(viewHold);
		} else {
			viewHold = (ViewHolder) convertView.getTag();
		}
		BuLanModel bulan = getItem(position);
//		viewHold.icon.setImageUrl(bulan.bulanImage);
		int w=(int) (70*CCApplication.density);
		ImageLoadUtil.load(activity, viewHold.icon, bulan.bulanImage,new int[]{w,w});
		viewHold.summary.setText(bulan.bulanTitle);

		viewHold.scan.setText(bulan.browseCount + "");
		viewHold.forword.setText(bulan.forwardCount + "");

		viewHold.day.setText((bulan.createDate).substring(5, 10));

		if (bulan.bulanTags != null && bulan.bulanTags.length > 0) {
			viewHold.type.setText(bulan.bulanTags[0]);
		} else {
			viewHold.type.setText("全部");
		}

		return convertView;
	}

	static class ViewHolder {
		ImageView icon;
		TextView day, type, summary, scan, forword;
	}

	public void clear() {
		this.bulans.clear();
		notifyDataSetChanged();
	}
}
