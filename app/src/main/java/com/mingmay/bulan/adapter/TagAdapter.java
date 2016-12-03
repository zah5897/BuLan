package com.mingmay.bulan.adapter;

import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mingmay.bulan.model.Tag;

public class TagAdapter extends BaseAdapter {
	 
	private List<Tag> data;
	private Context context;
	public TagAdapter(Context context, List<Tag> data) {
		this.context =context;
		this.data = data;
	}
	public void addItem(Tag tag) {
		// TODO Auto-generated method stub
		data.add(tag);
		notifyDataSetChanged();
	}
	public void removeItem(int position) {
		if(position>=0&&position<getCount()){
			data.remove(position);
			notifyDataSetChanged();
		}
	}
	
	public List<Tag> getSelectedTag(){
	      return data;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		LinearLayout layout=new LinearLayout(context);
		layout.setGravity(Gravity.CENTER);
		final Tag item = (Tag) getItem(position);
		TextView tv = new TextView(context);
		tv.setText(item.name);
		tv.setGravity(Gravity.CENTER);
		tv.setBackgroundResource(item.bgResId);
		tv.setHeight(45);
		
		int h=(int) (30*(context).getResources().getDisplayMetrics().density);
		int w=(int) (h*1.6);
		layout.addView(tv, w,h );
		return layout;
	}
	public void clear() {
		// TODO Auto-generated method stub
		data.clear();
		notifyDataSetChanged();
	}

	
}
