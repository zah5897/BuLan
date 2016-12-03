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
import com.mingmay.bulan.model.BuLanModel;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.ui.fragment.TopicFragment;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpProxy;

public class TopicTask extends AsyncTask<String, String, Integer> {
	public static final int PAGE_SIZE=20;
	TopicFragment topicPage;
	private int from;
	public TopicTask(TopicFragment topicPage) {
		this.topicPage = topicPage;
		from=0;//from home.
	} 
	
	private ArrayList<BuLanModel> bulans;
	@Override
	protected Integer doInBackground(String... arg0) {
		// TODO Auto-generated method stub

		String URL = CCApplication.HTTPSERVER;
		List<NameValuePair> param = new ArrayList<NameValuePair>();
		User u=UserManager.getInstance().getLoginUser();
		param.add(new BasicNameValuePair("curPage",arg0[0]));
		param.add(new BasicNameValuePair("userId",String.valueOf(u.ID)));
		param.add(new BasicNameValuePair("pageSize",String.valueOf(PAGE_SIZE)));
		
		if(from==0){
			URL+="/m_bp!findHomeBulans.action";
		}else if(from==1){
			URL+="/m_bp!findFriendsBulans.action";
		}

		
		try {
			HttpResponse response = new HttpProxy().post(URL, param);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
				JSONObject body=new JSONObject(rev).getJSONObject("body");
				JSONArray bulanArray=body.getJSONArray("bulanInfo");
				if(bulanArray!=null){
					int len=bulanArray.length();
					if(len>0){
						bulans=new ArrayList<BuLanModel>();
						for(int i=0;i<len;i++){
							bulans.add(BuLanModel.jsonToModel(bulanArray.getJSONObject(i)));
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
		if(result!=0){
			ToastUtil.show(  "获取数据异常");
		} 
		topicPage.callBackBuLan(bulans);
		super.onPostExecute(result);
	}
}
