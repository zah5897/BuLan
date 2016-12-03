package com.mingmay.bulan.task;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpProxy;

public class ModifyUserTask extends AsyncTask<String, String, Integer> {

	public ModifyUserTask() {
	}

	private User afterModify;

	@Override
	protected Integer doInBackground(String... arg0) {
		// TODO Auto-generated method stub
		int result = -1;
		try {
			String URL = CCApplication.HTTPSERVER + "/m_user!updateUser.action";
			MultipartEntity param = new MultipartEntity();
			User u = UserManager.getInstance().getLoginUser();
			param.addPart("userId", new StringBody(String.valueOf(u.ID)));
			param.addPart("ccukey", new StringBody(u.ccukey));
			param.addPart("firstName",
					new StringBody(arg0[0], Charset.forName("UTF-8")));
			param.addPart("gender",
					new StringBody(arg0[1], Charset.forName("UTF-8")));
			param.addPart("emailAddress",
					new StringBody(arg0[2], Charset.forName("UTF-8")));
			param.addPart("address",
					new StringBody(arg0[3], Charset.forName("UTF-8")));
			param.addPart("signature",
					new StringBody(arg0[4], Charset.forName("UTF-8")));
			param.addPart("file", new FileBody(new File(arg0[5])));
			HttpResponse response = new HttpProxy().post(URL, param);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
				JSONObject obj = new JSONObject(rev);
				JSONObject body = obj.getJSONObject("body");
				result = body.getInt("cstatus");
				afterModify = User.jsonToUser(body.getJSONObject("userInfo"));
			}
		} catch (Exception e) {
			return result;
		}
		return result;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		if (result == -1) {
			ToastUtil.show("修改失败");
		} else if (result == 2) {
			ToastUtil.show("参数异常");
		} else if (result == 3) {
			ToastUtil.show("邮箱重复,请确认");
		} else {
			ToastUtil.show("修改成功");
			UserManager.getInstance().updateUser(afterModify);
		}
	}
}
