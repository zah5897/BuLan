package com.mingmay.bulan.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.CommentInfo;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.ui.CommentDetialPage;
import com.mingmay.bulan.ui.CommentPage;
import com.mingmay.bulan.util.http.HttpProxy;

public class LoadCommentTask extends AsyncTask<String, String, Integer> {
	public static final int PAGE_SIZE = 20;
	CommentPage page;
	CommentDetialPage detialPage;
	public LoadCommentTask(CommentPage page) {
		this.page = page;
	}
	public LoadCommentTask(CommentDetialPage page) {
		this.detialPage = page;
	}
	private ArrayList<CommentInfo> comments;

	@Override
	protected Integer doInBackground(String... arg0) {
		// TODO Auto-generated method stub

		String URL = CCApplication.HTTPSERVER + "/m_comment!getComments.action";

		List<NameValuePair> param = new ArrayList<NameValuePair>();
		User u = UserManager.getInstance().getLoginUser();
		param.add(new BasicNameValuePair("bulanId", arg0[0]));
		param.add(new BasicNameValuePair("curPage", arg0[1]));
		param.add(new BasicNameValuePair("userId", String.valueOf(u.ID)));
		param.add(new BasicNameValuePair("pageSize", PAGE_SIZE + ""));
		if(detialPage!=null){
			param.add(new BasicNameValuePair("commentMainId",arg0[2]));
		}
		try {
			HttpResponse response = new HttpProxy().post(URL, param);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
				JSONObject body = new JSONObject(rev).getJSONObject("body");
				JSONArray commentInfos = body.getJSONArray("commentInfo");
				if (commentInfos != null) {
					int len = commentInfos.length();
					if (len > 0) {
						comments = new ArrayList<CommentInfo>();
						for (int i = 0; i < len; i++) {
							comments.add(CommentInfo
									.jsonToCommentInfo(commentInfos
											.getJSONObject(i)));
						}
					}
				}
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
		if(page!=null){
			page.callBack(comments);
		}else{
			detialPage.callBack(comments);
		}
		super.onPostExecute(result);
	}
}
