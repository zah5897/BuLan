package com.mingmay.bulan.adapter;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mingmay.bulan.R;
import com.mingmay.bulan.model.BuLanModel;
import com.mingmay.bulan.ui.friend.FriendCirclePage;
import com.mingmay.bulan.util.ImageLoadUtil;
import com.mingmay.bulan.util.TimeUtil;
import com.mingmay.bulan.view.CircularImageView;

public class FriendCircleAdapter extends BaseAdapter {

	private List<BuLanModel> data;
	private FriendCirclePage page;

	public FriendCircleAdapter(FriendCirclePage page, List<BuLanModel> data) {
		this.page = page;
		this.data = data;
	}

	public void addData(List<BuLanModel> data2) {
		this.data.addAll(data2);
		notifyDataSetChanged();
	}

	public void addAndClearData(List<BuLanModel> data2) {
		this.data.clear();
		this.data.addAll(data2);
		notifyDataSetChanged();
	}

	 

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public BuLanModel getItem(int arg0) {
		return data.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		ViewHolder viewHolder;
		if (arg1 == null) {
			arg1 = page.getLayoutInflater().inflate(
					R.layout.layout_item_friend_circle, arg2,false);
			viewHolder = new ViewHolder();
			viewHolder.icon = (CircularImageView) arg1
					.findViewById(R.id.head_icon);

			viewHolder.subIcon = (ImageView) arg1
					.findViewById(R.id.sub_head_icon);

			viewHolder.name = (TextView) arg1.findViewById(R.id.name);
			viewHolder.title = (TextView) arg1.findViewById(R.id.title);
			viewHolder.time = (TextView) arg1.findViewById(R.id.time);
			viewHolder.bulan_title = (TextView) arg1
					.findViewById(R.id.bulan_title);
			viewHolder.content = (TextView) arg1.findViewById(R.id.content);
			arg1.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) arg1.getTag();
		}
		BuLanModel info = (BuLanModel) getItem(arg0);

		viewHolder.name.setText(info.firstName);
		if (info.bulanTags != null && info.bulanTags.length > 0) {
			viewHolder.title.setText("分享布栏至" + info.bulanTags[0]);
		} else {
			viewHolder.title.setText("");
		}

		viewHolder.bulan_title.setText(info.bulanTitle);
		if (info.bulanContent != null) {
			viewHolder.content.setText(info.bulanContent);
		}

//		viewHolder.subIcon.setImageUrl(info.bulanImage);
		ImageLoadUtil.load(page, viewHolder.subIcon, info.bulanImage);
		viewHolder.time.setText(TimeUtil.getTopicTime(info.createDate));
//		viewHolder.icon.setImageUrl(info.userImg, true);
		ImageLoadUtil.load(page, viewHolder.icon, info.userImg);
		return arg1;
	}

	static class ViewHolder {
		CircularImageView icon;
		ImageView subIcon;
		TextView name, title, time, bulan_title, content;

	}

}
