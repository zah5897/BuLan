package com.mingmay.bulan.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

import org.apache.http.HttpResponse;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.zxing.WriterException;
import com.mingmay.bulan.R;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.Gender;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.util.BitmapUtil;
import com.mingmay.bulan.util.FileUtil;
import com.mingmay.bulan.util.ImageLoadUtil;
import com.mingmay.bulan.util.ProgressDialogUtil;
import com.mingmay.bulan.util.PropertyUtil;
import com.mingmay.bulan.util.QRCodeUtil;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpProxy;
import com.mingmay.bulan.view.CircularImageView;

public class MyInfoPage extends Activity implements OnClickListener {
	CircularImageView asyImg;

	private Bitmap qrBitmap;
	private ImageView qrView;

	private EditText nickName, cityName, info;
	private ToggleButton genderView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_my_info);
		findViewById(R.id.save).setOnClickListener(this);
		findViewById(R.id.back).setOnClickListener(this);
		nickName = (EditText) findViewById(R.id.nick_name);
		cityName = (EditText) findViewById(R.id.city);
		info = (EditText) findViewById(R.id.info);
		genderView = (ToggleButton) findViewById(R.id.gender);
		qrView = (ImageView) findViewById(R.id.qr);
		setUserInfo();
		createQr();
	}

	private void setUserInfo() {
		User loginUser = UserManager.getInstance().getLoginUser();
		asyImg = ((CircularImageView) findViewById(R.id.head_icon));
		ImageLoadUtil.load(this, asyImg, loginUser.userImg);

		genderView.setChecked(loginUser.gender == Gender.Female);
		asyImg.setOnClickListener(this);
		nickName.setText(loginUser.firstName);
		cityName.setText(loginUser.address);
		info.setText(loginUser.signature);
		((TextView) findViewById(R.id.bulan_card)).setText(String
				.valueOf(loginUser.loginName));
	}

	@Override
	protected void onDestroy() {
		if (qrBitmap != null && !qrBitmap.isRecycled()) {
			qrBitmap.recycle();
			qrBitmap = null;
		}
		super.onDestroy();
	}

	private void createQr() {
		User loginUser = UserManager.getInstance().getLoginUser();
		File f = new File(Environment.getExternalStorageDirectory()
				.getAbsoluteFile() + "/bulan", loginUser.ID + "");
		if (f.exists()) {
			Bitmap temp = BitmapUtil.getBitmap(f.getAbsolutePath());
			if (temp != null) {
				qrBitmap = temp;
				handler.sendEmptyMessage(100);
				return;
			}
		}

		new Thread() {
			public void run() {
				User loginUser = UserManager.getInstance().getLoginUser();
				QRCodeUtil qrUtil = new QRCodeUtil();
				try {
					String url = "http://www.mingmay.com?user_id="
							+ loginUser.ID;
					qrBitmap = qrUtil.Create2DCode(url, getResources()
							.getDisplayMetrics().widthPixels / 2);
					saveBitmap(qrBitmap);
					handler.sendEmptyMessage(100);
				} catch (WriterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
	}

	public void saveBitmap(Bitmap qr) {
		User loginUser = UserManager.getInstance().getLoginUser();
		File f = new File(Environment.getExternalStorageDirectory()
				.getAbsoluteFile() + "/bulan", loginUser.ID + "");
		if (!f.getParentFile().isDirectory()) {
			f.getParentFile().mkdirs();
		}
		if (f.exists()) {
			f.delete();
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			qr.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void setQr() {
		if (qrBitmap != null) {
			qrView.setImageBitmap(qrBitmap);
			findViewById(R.id.loading).setVisibility(View.GONE);
		} else {
			createQr();
		}
	}

	public void callBack(User user) {

		setUserInfo();
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.head_icon:
			Intent intent = new Intent(this, MultiImageSelectorActivity.class);
			intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE,
					MultiImageSelectorActivity.MODE_SINGLE);
			intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 1);
			startActivityForResult(intent, 1);
			break;
		case R.id.back:
			finish();
			break;
		case R.id.save:
			ProgressDialogUtil.showProgress(this, "正在修改...");
			try {
				MultipartEntity param = new MultipartEntity();
				User loginUser = UserManager.getInstance().getLoginUser();
				param.addPart("userId",
						new StringBody(String.valueOf(loginUser.ID)));
				param.addPart("ccukey", new StringBody(loginUser.ccukey));
				param.addPart("gender", new StringBody(
						genderView.isChecked() ? "0" : "1"));
				param.addPart("signature", new StringBody(info.getText()
						.toString(), Charset.forName("UTF-8")));
				param.addPart("address", new StringBody(cityName.getText()
						.toString(), Charset.forName("UTF-8")));
				param.addPart("firstName", new StringBody(nickName.getText()
						.toString(), Charset.forName("UTF-8")));
				if (!TextUtils.isEmpty(iconPath) && FileUtil.isExist(iconPath)) {
					param.addPart("file", new FileBody(new File(iconPath)));
				}

				modify(param);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data == null) {
			return;
		}
		ArrayList<String> tempSelectPath = data
				.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
		if (tempSelectPath != null && tempSelectPath.size() > 0) {
			iconPath = tempSelectPath.get(0);
			chooseSmalPic();
		} else {
			Bitmap bitmap = data.getParcelableExtra("data");

			iconPath = FileUtil.saveBitmap(bitmap).getAbsolutePath();
			ImageLoadUtil.load(this, asyImg, new File(iconPath));
		}
	}

	private void chooseSmalPic() {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(Uri.fromFile(new File(iconPath)), "image/*");
		intent.putExtra("crop", "true");
		// 裁剪框的比例，1：1
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// 裁剪后输出图片的尺寸大小
		intent.putExtra("outputX", 250);
		intent.putExtra("outputY", 250);

		intent.putExtra("outputFormat", "JPEG");// 图片格式
		intent.putExtra("noFaceDetection", true);// 取消人脸识别
		intent.putExtra("return-data", true);
		// 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CUT
		startActivityForResult(intent, 1);
	}

	private String iconPath;

	private void modify(final MultipartEntity param) {
		new Thread() {
			@Override
			public void run() {
				String URL = CCApplication.HTTPSERVER
						+ "/m_user!updateUser.action";
				try {
					HttpResponse response = new HttpProxy().post(URL, param);
					int code = response.getStatusLine().getStatusCode();
					if (code == 200) {
						String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
						JSONObject body = new JSONObject(rev)
								.getJSONObject("body");
						int cstatus = body.getInt("cstatus");
						if (cstatus == 0) {
							JSONObject userJson = body
									.getJSONObject("userInfo");

							User newUser = User.jsonToUser(userJson);
							UserManager.getInstance().updateUser(newUser);
							PropertyUtil.putValue("user_info",
									userJson.toString());
							Message msg = handler.obtainMessage(0);
							handler.sendMessage(msg);
							CCApplication.needRefreshUserInfo = true;
							return;
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
				Message msg = handler.obtainMessage(-1);
				handler.sendMessage(msg);
			}
		}.start();
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0) {
				ProgressDialogUtil.dismiss();
				ToastUtil.show("修改用户信息成功!");
				finish();
			} else if (msg.what == -1) {
				ProgressDialogUtil.dismiss();
				ToastUtil.show("修改失败");
			} else if (msg.what == 100) {
				setQr();
			}
		}
	};
}
