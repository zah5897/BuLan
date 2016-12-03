package com.mingmay.bulan.task;

import java.io.File;
import java.nio.charset.Charset;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.DataBaseManager;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.BuLanModel;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.ui.fragment.publish.BuLanPublishActivity;
import com.mingmay.bulan.util.http.HttpProxy;

public class PublishBULAN {

	private BuLanPublishActivity createFragment;
	private DataBaseManager dbManager;

	private String uuid;

	public BuLanModel publishModel;

	public int dbID;

	public PublishBULAN(BuLanPublishActivity createFragment) {
		this.createFragment = createFragment;
		dbManager = DataBaseManager.getInstance(createFragment);
	}

	public PublishBULAN() {
		dbManager = DataBaseManager.getInstance();
	}

	public int publishBulan(String iconPath, String title, String isOpen,
			String bulanTags, String localId) {
		String URL = CCApplication.HTTPSERVER + "/m_bp!createBulan.action";
		try {

			User u = UserManager.getInstance().getLoginUser();
			MultipartEntity param = new MultipartEntity();
			param.addPart("file", new FileBody(new File(iconPath)));
			param.addPart("bulanTitle",
					new StringBody(title, Charset.forName("UTF-8")));
			param.addPart("userId", new StringBody(String.valueOf(u.ID)));
			param.addPart("ccukey", new StringBody(u.ccukey));
			uuid = UUID.randomUUID().toString();
			param.addPart("tempId", new StringBody(uuid));
			String local_id = localId;
			String bulanTxt = toBuLanStr(local_id);
			param.addPart("bulanContents",
					new StringBody(bulanTxt, Charset.forName("UTF-8")));
			param.addPart("isOpen", new StringBody(isOpen));
			param.addPart("bulanTags",
					new StringBody(bulanTags, Charset.forName("UTF-8")));
			HttpResponse response = new HttpProxy().post(URL, param);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
				JSONObject obj = new JSONObject(rev);
				JSONObject body = obj.getJSONObject("body");
				JSONObject bulanObj = body.getJSONObject("bulanInfo");
				BuLanModel bulanModel = BuLanModel.jsonToModel(bulanObj);
				publishModel = bulanModel;
				dbManager.deleteBulanSave(Long.parseLong(local_id));
				return 0;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public int publishBulanWithImage(String iconPath, String title,
			String isOpen, String bulanTags, long localId, String content) {
		String URL = CCApplication.HTTPSERVER + "/m_bp!createBulan.action";
		try {

			User u = UserManager.getInstance().getLoginUser();
			MultipartEntity param = new MultipartEntity();
			param.addPart("bulanImage", new StringBody(iconPath));
			param.addPart("bulanTitle",
					new StringBody(title, Charset.forName("UTF-8")));
			param.addPart("userId", new StringBody(String.valueOf(u.ID)));
			param.addPart("ccukey", new StringBody(u.ccukey));
			uuid = UUID.randomUUID().toString();
			param.addPart("tempId", new StringBody(uuid));
			param.addPart("bulanContents",
					new StringBody(content, Charset.forName("UTF-8")));
			param.addPart("toFriendCircle", new StringBody(isOpen));
			param.addPart("wardrobeIds",
					new StringBody(bulanTags, Charset.forName("UTF-8")));
			HttpResponse response = new HttpProxy().post(URL, param);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
				JSONObject obj = new JSONObject(rev);
				JSONObject body = obj.getJSONObject("body");
				JSONObject bulanObj = body.getJSONObject("bulanInfo");
				BuLanModel bulanModel = BuLanModel.jsonToModel(bulanObj);
				publishModel = bulanModel;
				dbManager.deleteBulanSave(localId);
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	private String toBuLanStr(String local_id) throws JSONException {
		return dbManager.getBulanSaveContent(Long.parseLong(local_id));
	}

}
