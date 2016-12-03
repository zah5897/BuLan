package com.mingmay.bulan.adapter;

import java.util.List;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mingmay.bulan.R;
import com.mingmay.bulan.model.BuLanModel;
import com.mingmay.bulan.util.ImageLoadUtil;

public class FriendBuLanAdapter extends BaseAdapter {
	private Activity activity;

	public FriendBuLanAdapter(Activity activity, List<BuLanModel> bulans) {
		// TODO Auto-generated constructor stub
		this.activity = activity;
		this.bulans = bulans;
	}

	private List<BuLanModel> bulans;

	public void add(List<BuLanModel> bulans) {

		this.bulans.addAll(bulans);
		notifyDataSetChanged();
	}

	public void clear() {
		this.bulans.clear();
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

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHold;
		if (convertView == null) {
			convertView = LayoutInflater.from(activity).inflate(
					R.layout.layout_item_bulan, parent, false);
			viewHold = new ViewHolder();
			viewHold.icon = (ImageView) convertView.findViewById(R.id.image);
			viewHold.summary = (TextView) convertView
					.findViewById(R.id.summary);
			viewHold.scanCount = (TextView) convertView
					.findViewById(R.id.scan_count);
			viewHold.author = (TextView) convertView.findViewById(R.id.author);
			convertView.setTag(viewHold);
		} else {
			viewHold = (ViewHolder) convertView.getTag();
		}
		BuLanModel bulan = (BuLanModel) getItem(position);
		// viewHold.icon.setImageUrl(bulan.bulanImage);
		ImageLoadUtil.load(activity, viewHold.icon, bulan.bulanImage);
		viewHold.summary.setText(bulan.bulanTitle);
		viewHold.scanCount.setText(String.valueOf(bulan.commentCount));
		if (TextUtils.isEmpty(bulan.firstName)) {
			viewHold.author.setText("匿名");
		} else {
			viewHold.author.setText(bulan.firstName + "");
		}

		return convertView;
	}

	static class ViewHolder {
		ImageView icon;
		TextView summary;
		TextView scanCount;
		TextView author;

	}

	// public void addItemToTop(BuLanModel publishModel) {
	// bulans.add(0, publishModel);
	// notifyDataSetChanged();
	// }

}
