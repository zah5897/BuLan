package com.mingmay.bulan.adapter;

import java.util.List;

import org.apache.http.Header;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mingmay.bulan.R;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.NotifyModel;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpUtils;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class NotifyAdapter extends BaseAdapter {

	private Activity context;
	private List<NotifyModel> data;

	public NotifyAdapter(Activity context, List<NotifyModel> data) {
		this.context = context;
		this.data = data;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public NotifyModel getItem(int arg0) {
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
					R.layout.item_notify, parent, false);
			viewHold.icon = (ImageView) convertView.findViewById(R.id.icon);
			viewHold.name = (TextView) convertView.findViewById(R.id.name);
			viewHold.content = (TextView) convertView
					.findViewById(R.id.content);
			viewHold.agree = (TextView) convertView.findViewById(R.id.agree);
			viewHold.status = (TextView) convertView.findViewById(R.id.status);
			convertView.setTag(viewHold);
		} else {
			viewHold = (ViewHolder) convertView.getTag();
		}
		
		viewHold.agree.setVisibility(View.GONE);
		viewHold.status.setVisibility(View.GONE);
		
		final NotifyModel notify = getItem(position);

		viewHold.name.setText(notify.userName);
		
		
		String content = "";
		if (notify.noticeKey == 1) {
			content = "您的布栏收到新评论";
		} else if (notify.noticeKey == 2) {
			content = "您收到新回复";
		} else if (notify.noticeKey == 3) { // 邀请您加入专栏
			content = notify.noticeContent;
			viewHold.agree.setVisibility(View.VISIBLE);
			viewHold.agree.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					agree(notify);
				}
			});
		} else if (notify.noticeKey == 4) {
			content = notify.noticeContent;
		} else if (notify.noticeKey == 5) { //申请好友
			content = notify.noticeContent;
			if(notify.isRead==0){
				viewHold.status.setVisibility(View.VISIBLE);
				viewHold.status.setText("已加入");
			}else{
				viewHold.agree.setVisibility(View.VISIBLE);
				viewHold.agree.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						agree(notify);
					}
				});
			}
		}else if(notify.noticeKey==6){ //好友申请 同意和拒绝
			content = notify.noticeContent;
//			viewHold.agree.setVisibility(View.VISIBLE);
		} else {
			content = notify.noticeContent;
		}
		viewHold.content.setText(content);
		Glide.with(context).load(notify.userImag).error(R.drawable.headlogo)
				.placeholder(R.drawable.headlogo).into(viewHold.icon);

		return convertView;
	}

	static class ViewHolder {
		ImageView icon;
		TextView name, agree, status, content;
	}

	public void addData(List<NotifyModel> result) {
		data.addAll(result);
		notifyDataSetChanged();
	}

	public void clear() {
		data.clear();
		notifyDataSetChanged();
	}

	public void addAndClear(List<NotifyModel> result) {
		data.clear();
		data.addAll(result);
		notifyDataSetChanged();
	}

	private void agree(final NotifyModel notify) {
		String url = CCApplication.HTTPSERVER + "/m_notice!agree.action";

		RequestParams params = new RequestParams();
		User loginUser = UserManager.getInstance().getLoginUser();
		params.put("ccukey", loginUser.ccukey);
		params.put("userId", String.valueOf(loginUser.ID));
		params.put("noticeId", String.valueOf(notify.noticeId));

		HttpUtils.post(url, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {

				JSONObject body = response.optJSONObject("body");
				// "noticeStatus":"1","cstatus":"0","noticeId":314,"msg":"","isRead":"1"}
				int cstatus = body.optInt("cstatus");
				int noticeStatus = body.optInt("noticeStatus");
				int noticeId = body.optInt("noticeId");
				int isRead = body.optInt("isRead");

				notify.isRead = isRead;
				notify.noticeStatus = noticeStatus;

				if(notify.noticeKey == 5){
					CCApplication.friendCount--;
				}else if(notify.noticeKey == 3){
					CCApplication.guanzhu_MsgCount--;
				}
				notifyDataSetChanged();
				super.onSuccess(statusCode, headers, response);
				ToastUtil.show("操作成功");
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				super.onFailure(statusCode, headers, responseString, throwable);
				ToastUtil.show("操作失败");
			}
		});
	}

}
