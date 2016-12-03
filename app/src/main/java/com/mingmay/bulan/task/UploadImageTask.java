package com.mingmay.bulan.task;

import java.io.File;

import org.apache.http.HttpResponse;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.listener.CallBackListener;
import com.mingmay.bulan.util.http.HttpProxy;

public class UploadImageTask {

	public void upload(final String filePath, final CallBackListener listener) {
		new Thread() {
			@Override
			public void run() {
				String URL = CCApplication.HTTPSERVER
						+ "/m_file!addNewFile.action";
				MultipartEntity param = new MultipartEntity();
				try {
					param.addPart("file", new FileBody(new File(filePath)));
					param.addPart("tempId", new StringBody("0"));

					HttpResponse response = new HttpProxy().post(URL, param);
					int code = response.getStatusLine().getStatusCode();
					String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
					JSONObject obj = new JSONObject(rev);
					JSONObject body = obj.optJSONObject("body");
					// "body":{"filename":"559cfabb-22c2-4e71-9590-0aa12d2de3a4.jpg","tempId":"11","cstatus":"0"}}
					if (body != null) {
						int cstatus = body.optInt("cstatus");
						if (cstatus == 0) {
							String filename = body.optString("filename");
							listener.onSuccess(filename);
							return;
						}
					}
				} catch (Exception e) {

				}
				listener.onFailure(-1);
			}
		}.start();

	}
}
