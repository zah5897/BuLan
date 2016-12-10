package com.mingmay.bulan.adapter;

import java.util.List;

import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mingmay.bulan.R;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.model.Tag;
import com.mingmay.bulan.ui.fragment.publish.BuLanPublishActivity;
import com.mingmay.bulan.view.MyGridView;

public class TagPublishAdapter extends BaseAdapter {
	private BuLanPublishActivity bulanPublishActivity;
	private List<Tag> data;
	private int width;

	public Tag currendSelected;

	public TagPublishAdapter(BuLanPublishActivity bulanPublishActivity,
			List<Tag> data) {
		this.bulanPublishActivity = bulanPublishActivity;
		this.data = data;
		width = (int) ((CCApplication.screenWidth - 100 * CCApplication.density) / 4);
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHold hold = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(bulanPublishActivity).inflate(
					R.layout.item_tag_type, parent, false);
			hold = new ViewHold();
			hold.name = (TextView) convertView.findViewById(R.id.name);
			hold.tagGrid = (MyGridView) convertView.findViewById(R.id.tag_grid);
			convertView.setTag(hold);
		} else {
			hold = (ViewHold) convertView.getTag();
		}
		Tag type = (Tag) getItem(position);
		hold.name.setText(type.name);
		if (type.childs != null && type.childs.size() > 0) {
			ArrayAdapter<Tag> tagAdapter = new ArrayAdapter<Tag>(
					bulanPublishActivity, R.layout.simple_list_item_1,
					type.childs) {
				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					LinearLayout layout = new LinearLayout(bulanPublishActivity);
					Tag tag = getItem(position);
					TextView tx = new TextView(bulanPublishActivity);
					tx.setGravity(Gravity.CENTER);
					tx.setPadding(3, 5, 3, 5);
					tx.setText(getItem(position).toString());

					tx.setBackgroundResource(R.drawable.unselected);
					tx.setTextColor(getContext().getResources().getColor(
							R.color.actionsheet_red));
					if (currendSelected != null && currendSelected.id == tag.id) {
						tx.setBackgroundResource(getItem(position).bgResId);
						tx.setTextColor(getContext().getResources().getColor(
								R.color.white));
					}
					tx.setTextSize(12);
					layout.setGravity(Gravity.CENTER);
					layout.addView(tx, width, 60);
					return layout;
				}
			};
			hold.tagGrid.setAdapter(tagAdapter);
		}
		if (hold.tagGrid != null) {
			hold.tagGrid.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					Tag selected = (Tag) arg0.getAdapter().getItem(arg2);
					currendSelected = selected;
					bulanPublishActivity.setTag(currendSelected);
					handler.sendEmptyMessage(0);

				}
			});
		}

		return convertView;
	}

	class ViewHold {
		TextView name;
		MyGridView tagGrid;
	}

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			TagPublishAdapter.this.notifyDataSetChanged();
		}
	};

}
