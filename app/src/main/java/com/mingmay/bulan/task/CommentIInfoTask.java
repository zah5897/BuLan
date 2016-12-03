package com.mingmay.bulan.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.CommentInfo;
import com.mingmay.bulan.model.NotifyModel;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.ui.MessageCenter;
import com.mingmay.bulan.util.http.HttpProxy;

public class CommentIInfoTask extends AsyncTask<String, String, Integer> {

	MessageCenter center;
	NotifyModel notify;
	public CommentIInfoTask(MessageCenter center,NotifyModel notify) {
		this.center = center;
		this.notify=notify;
	}

	private CommentInfo comment;

	@Override
	protected Integer doInBackground(String... arg0) {
		String URL = CCApplication.HTTPSERVER
				+ "/m_comment!getMainComment.action";
		List<NameValuePair> param = new ArrayList<NameValuePair>();
		User u = UserManager.getInstance().getLoginUser();
		param.add(new BasicNameValuePair("bulanId", arg0[0]));
		param.add(new BasicNameValuePair("commentId", arg0[1]));
		try {
			HttpResponse response = new HttpProxy().post(URL, param);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
				JSONObject root = new JSONObject(rev);
				JSONObject body = root.getJSONObject("body");
				comment = CommentInfo.jsonToCommentInfo(body
						.getJSONObject("commentInfo"));
				return body.getInt("cstatus");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return -1;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		center.commentCallBack(comment,notify);
	}
}
