package com.mingmay.bulan.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mingmay.bulan.R;
import com.mingmay.bulan.model.BuLanSaveModel;
import com.mingmay.bulan.util.TimeUtil;

public class  SaveBuLanAdapter extends BaseAdapter {

	private Context context;
	private List<BuLanSaveModel> data;

	public SaveBuLanAdapter(Context context, List<BuLanSaveModel> data) {
		this.context = context;
		this.data = data;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public BuLanSaveModel getItem(int arg0) {
		return data.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHold;
		if (convertView == null) {
			viewHold = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_save, parent,false);
			viewHold.name = (TextView) convertView.findViewById(R.id.name);
			viewHold.time = (TextView) convertView.findViewById(R.id.time);
			//viewHold.content = (TextView) convertView.findViewById(R.id.content);
			convertView.setTag(viewHold);
		} else {
			viewHold = (ViewHolder) convertView.getTag();
		}
		BuLanSaveModel notify = getItem(position);
		viewHold.name.setText("标题："+notify.title);
		viewHold.time.setText("上次保存时间："+TimeUtil.currentLocalTimeString(notify.createDate));
		return convertView;
	}

	static class ViewHolder {
		TextView name, time, content;
	}

	public void remove(BuLanSaveModel model) {
		// TODO Auto-generated method stub
		data.remove(model);
		notifyDataSetChanged();
	}
 

}
