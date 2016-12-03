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
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.ui.BulanDetialPage;
import com.mingmay.bulan.ui.CommentDetialPage;
import com.mingmay.bulan.ui.CommentPage;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpProxy;

public class CommentTask extends AsyncTask<String, String, Integer> {
	BulanDetialPage page;
	CommentPage commentPage;
	CommentDetialPage commDetialPage;

	public CommentTask(BulanDetialPage page) {
		this.page = page;
	}

	public CommentTask(CommentPage page) {
		this.commentPage = page;
	}

	public CommentTask(CommentDetialPage page) {
		this.commDetialPage = page;
	}

	private CommentInfo comment;

	@Override
	protected Integer doInBackground(String... arg0) {
		String URL = CCApplication.HTTPSERVER
				+ "/m_comment!createComment.action";
		List<NameValuePair> param = new ArrayList<NameValuePair>();
		User u = UserManager.getInstance().getLoginUser();
		param.add(new BasicNameValuePair("bulanId", arg0[0]));
		param.add(new BasicNameValuePair("userId", String.valueOf(u.ID)));
		param.add(new BasicNameValuePair("ccukey", u.ccukey));
		param.add(new BasicNameValuePair("commentText", arg0[1]));

		if (commDetialPage != null) {
			param.add(new BasicNameValuePair("commentUserId", arg0[2]));
			param.add(new BasicNameValuePair("commentMainId", arg0[3]));
		}
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
		if (result == 0) {
			ToastUtil.show("评论成功");
			if (commentPage != null) {
				commentPage.commentCallBack(comment);
			} else if (page != null) {
				page.commentCallBack(comment);
			} else {
				commDetialPage.commentCallBack(comment);
			}
		} else {
			ToastUtil.show("评论失败");
		}
		super.onPostExecute(result);
	}
}
