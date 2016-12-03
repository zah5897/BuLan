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
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.model.BuLanModel;
import com.mingmay.bulan.util.ImageLoadUtil;

public class BuLanAdapter extends BaseAdapter {
	private Activity activity;
	int wh;
	public BuLanAdapter(Activity activity, List<BuLanModel> bulans) {
		// TODO Auto-generated constructor stub
		this.activity = activity;
		this.bulans = bulans;
		 wh = (int) (CCApplication.density * 50);
	}

	private List<BuLanModel> bulans;

	public void addAndClear(List<BuLanModel> bulans) {
		this.bulans.clear();
		this.bulans.addAll(bulans);
		notifyDataSetChanged();
	}

	public void setNewData(List<BuLanModel> bulans) {
		this.bulans = bulans;
		notifyDataSetChanged();
	}

	public void add(List<BuLanModel> bulans) {
		this.bulans.addAll(bulans);
		notifyDataSetChanged();
	}

	public void addTop(BuLanModel bulans) {
		this.bulans.add(0, bulans);
		notifyDataSetChanged();
	}

	public List<BuLanModel> getData() {
		return bulans;
	}

	public void clear() {
		this.bulans.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return bulans.size();
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		BuLanModel bulan = getItem(position);
		if (bulan.bulanId == -1) {
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	public BuLanModel getItem(int arg0) {
		return bulans.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	private View getTypeView(int position, ViewGroup parent) {
		int type = getItemViewType(position);
		if (type == 0) {
			return LayoutInflater.from(activity).inflate(
					R.layout.layout_item_chat_room, parent, false);
		} else {
			return LayoutInflater.from(activity).inflate(
					R.layout.layout_item_bulan, parent, false);
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHold;
		if (convertView == null) {
			convertView = getTypeView(position, parent);
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

		int type = getItemViewType(position);
		BuLanModel bulan = getItem(position);
		ImageLoadUtil.load(activity, viewHold.icon, bulan.bulanImage,
				new int[] { wh, wh });
		if (type == 0) {
			viewHold.summary.setText(bulan.bulanTitle);
			viewHold.author.setText(bulan.browseCount + "人");
		} else {
			viewHold.summary.setText(bulan.bulanTitle);
			viewHold.scanCount.setText(String.valueOf(bulan.browseCount));
			if (TextUtils.isEmpty(bulan.firstName)) {
				viewHold.author.setText("来自:布栏助手");
			} else {
				viewHold.author.setText("来自:" + bulan.firstName);
			}

		}

		return convertView;
	}

	static class ViewHolder {
		ImageView icon;
		TextView summary;
		TextView scanCount;
		TextView author;

	}

	public void addItemToTop(BuLanModel publishModel) {
		bulans.add(1, publishModel);
		notifyDataSetChanged();
	}
}
