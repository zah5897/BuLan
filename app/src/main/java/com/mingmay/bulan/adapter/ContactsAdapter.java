package com.mingmay.bulan.adapter;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mingmay.bulan.R;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.ui.FriendInfoPage;
import com.mingmay.bulan.ui.friend.ContactPage;

@SuppressLint("DefaultLocale")
public class ContactsAdapter extends BaseAdapter {
	private List<User> mData;
	private ContactPage mContext;

	private List<Long> selectedIds;

	public ContactsAdapter(ContactPage mContext, List<User> mData) {
		this.mContext = mContext;
		this.mData = mData;
		if (mContext.need_back) {
			selectedIds = new ArrayList<Long>();
		}
	}

	public String[] getSelectedIds() {
		if (selectedIds != null && selectedIds.size() > 0) {
			int size = selectedIds.size();
			Long[] arr = selectedIds.toArray(new Long[size]);

			String ids[] = new String[size];

			for (int i = 0; i < size; i++) {
				ids[i] = String.valueOf(arr[i]);
			}
			return ids;
		}
		return null;
	}

	public void del(long contactId) {
		if (mData != null) {
			for (User user : mData) {
				if (user.ID == contactId) {
					mData.remove(user);
					notifyDataSetChanged();
					break;
				}
			}
		}

	}

	public void itemClick(User user) {
		if (selectedIds.contains(user.ID)) {
			selectedIds.remove(user.ID);
		} else {
			selectedIds.add(user.ID);
		}
		notifyDataSetChanged();
	}

	public void claer() {
		mData.clear();
		notifyDataSetChanged();
	}

	public void addAndClear(List<User> data) {
		mData.clear();
		mData.addAll(data);
		notifyDataSetChanged();
	}

	public void add(List<User> data) {
		mData.addAll(data);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mData.size();
	}

	@Override
	public User getItem(int arg0) {
		// TODO Auto-generated method stub
		return mData.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View arg1, ViewGroup arg2) {
		ViewHolder viewHolder;

		if (arg1 == null) {
			arg1 = LayoutInflater.from(mContext).inflate(
					R.layout.layout_contacts_item, arg2, false);
			viewHolder = new ViewHolder();
			viewHolder.icon = (ImageView) arg1.findViewById(R.id.icon);
			viewHolder.firstChar = (TextView) arg1
					.findViewById(R.id.first_char);
			viewHolder.name = (TextView) arg1.findViewById(R.id.name);
			viewHolder.selecte_state = (CheckBox) arg1
					.findViewById(R.id.selecte_state);
			arg1.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) arg1.getTag();
		}
		if (viewHolder == null) {
			arg1 = LayoutInflater.from(mContext).inflate(
					R.layout.layout_contacts_item, arg2, false);
			viewHolder = new ViewHolder();
			viewHolder.icon = (ImageView) arg1.findViewById(R.id.icon);
			viewHolder.firstChar = (TextView) arg1
					.findViewById(R.id.first_char);
			viewHolder.name = (TextView) arg1.findViewById(R.id.name);
			arg1.setTag(viewHolder);
		}
		final User user = getItem(position);
		viewHolder.firstChar.setVisibility(View.GONE);
		String name = user.firstName;
		viewHolder.name.setText(name);
		int section = getSectionForPosition(position);
		if (position == getPositionForSection(section)) {
			viewHolder.firstChar.setText(user.firstChar);
			viewHolder.firstChar.setVisibility(View.VISIBLE);
		} else {
			viewHolder.firstChar.setVisibility(View.GONE);
		}
		Glide.with(mContext).load(user.userImg)
				.placeholder(R.drawable.headlogo).error(R.drawable.headlogo)
				.override(60, 60).into(viewHolder.icon);

		viewHolder.icon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(mContext, FriendInfoPage.class);
				i.putExtra("friend", user);
				mContext.startActivity(i);
			}
		});

		if (mContext.need_back) {
			viewHolder.selecte_state.setOnCheckedChangeListener(null);
			viewHolder.selecte_state.setChecked(selectedIds.contains(user.ID));

			viewHolder.selecte_state.setVisibility(View.VISIBLE);
			viewHolder.selecte_state
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton arg0,
								boolean arg1) {
							if (arg1) {
								if (!selectedIds.contains(user.ID)) {
									selectedIds.add(user.ID);
								}
							} else {
								if (selectedIds.contains(user.ID)) {
									selectedIds.remove(user.ID);
								}
							}
							notifyDataSetChanged();
						}
					});
		} else {
			viewHolder.selecte_state.setVisibility(View.GONE);
		}
		return arg1;
	}

	public int getSectionForPosition(int position) {
		return mData.get(position).firstChar.charAt(0);
	}

	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = mData.get(i).firstChar;
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar == section) {
				return i;
			}
		}

		return -1;
	}

	static class ViewHolder {
		ImageView icon;
		TextView firstChar, name;
		CheckBox selecte_state;
	}

}
