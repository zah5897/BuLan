package com.mingmay.bulan.adapter;

import java.util.List;

import org.apache.http.Header;
import org.json.JSONObject;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mingmay.bulan.R;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.Group;
import com.mingmay.bulan.util.ImageLoadUtil;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpUtils;

public class HotBuLanAdapter extends BaseAdapter {
	private Activity activity;

	public HotBuLanAdapter(Activity activity, List<Group> bulans) {
		this.activity = activity;
		this.bulans = bulans;
	}

	private List<Group> bulans;

	public void addAndClear(List<Group> bulans) {
		this.bulans.clear();
		this.bulans.addAll(bulans);
		notifyDataSetChanged();
	}

	public void setNewData(List<Group> bulans) {
		this.bulans = bulans;
		notifyDataSetChanged();
	}

	public void add(List<Group> bulans) {
		this.bulans.addAll(bulans);
		notifyDataSetChanged();
	}

	public void addTop(Group bulans) {
		this.bulans.add(0, bulans);
		notifyDataSetChanged();
	}

	public List<Group> getData() {
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
	public Group getItem(int arg0) {
		return bulans.get(arg0);
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
					R.layout.layout_item_hot_bulan, parent, false);
			viewHold = new ViewHolder();
			viewHold.icon = (ImageView) convertView.findViewById(R.id.image);
			viewHold.summary = (TextView) convertView
					.findViewById(R.id.summary);
			viewHold.scanCount = (TextView) convertView
					.findViewById(R.id.scan_count);
			viewHold.author = (TextView) convertView.findViewById(R.id.author);
			viewHold.watch = (TextView) convertView.findViewById(R.id.watch);
			convertView.setTag(viewHold);
		} else {
			viewHold = (ViewHolder) convertView.getTag();
		}

		int type = getItemViewType(position);
		final Group bulan = getItem(position);

		int wh = (int) (CCApplication.density * 50);
		ImageLoadUtil.load(activity, viewHold.icon, bulan.icon, new int[] { wh,
				wh });
		viewHold.summary.setText(bulan.name);
		viewHold.scanCount.setText(String.valueOf(bulan.count));
		if (TextUtils.isEmpty(bulan.userName)) {
			viewHold.author.setText("作者:布栏助手");
		} else {
			viewHold.author.setText("作者:" + bulan.userName);
		}

		if (bulan.watched != 0) {
			viewHold.watch
					.setBackgroundResource(R.drawable.background_has_selected);
			viewHold.watch.setText("已关注");
		} else {
			viewHold.watch.setBackgroundResource(R.drawable.background_1);
			viewHold.watch.setText("关注");

			viewHold.watch.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					RequestParams params = new RequestParams();
					params.put("userId", UserManager.getInstance()
							.getLoginUser().ID);
					params.put("ccukey", UserManager.getInstance()
							.getLoginUser().ccukey);
					params.put("wardrobeId", bulan.id);

					String url = CCApplication.HTTPSERVER
							+ "/m_wardrobe!addUserWardrobe.action";
					HttpUtils.post(url, params, new JsonHttpResponseHandler() {
						@Override
						public void onSuccess(int statusCode, Header[] headers,
								JSONObject response) {
							JSONObject body = response.optJSONObject("body");
							int cstatus = body.optInt("cstatus");
							if (cstatus == 0) {
								bulan.watched = 1;
								ToastUtil.show("关注成功");
							}else{
								ToastUtil.show("关注失败");
							}
							notifyDataSetChanged();
							super.onSuccess(statusCode, headers, response);
						}
					});

				}
			});
		}
		return convertView;
	}

	static class ViewHolder {
		ImageView icon;
		TextView summary;
		TextView scanCount;
		TextView author;
		TextView watch;

	}

	public void addItemToTop(Group publishModel) {
		bulans.add(1, publishModel);
		notifyDataSetChanged();
	}
}
