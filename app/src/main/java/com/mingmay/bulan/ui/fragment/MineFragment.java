package com.mingmay.bulan.ui.fragment;

import java.io.File;
import java.util.ArrayList;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

import org.apache.http.HttpResponse;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.hyphenate.easeui.ui.EaseShowBigImageActivity;
import com.mingmay.bulan.R;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.ui.AboutPage;
import com.mingmay.bulan.ui.BulanSaves;
import com.mingmay.bulan.ui.MessageCenter;
import com.mingmay.bulan.ui.MyBuLanPage;
import com.mingmay.bulan.ui.MyInfoPage;
import com.mingmay.bulan.ui.MyStorePage;
import com.mingmay.bulan.ui.SettingPage;
import com.mingmay.bulan.util.FileUtil;
import com.mingmay.bulan.util.ImageLoadUtil;
import com.mingmay.bulan.util.PropertyUtil;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpProxy;
import com.mingmay.bulan.view.CircularImageView;

public class MineFragment extends Fragment implements OnClickListener {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater
				.inflate(R.layout.layout_mine_fragment, container, false);
	}

	private TextView name, city, info;
	private CircularImageView image;
	private ImageView topBg;

	private TextView tip, guanzhu_tip;

	@Override
	public void onResume() {
		if (CCApplication.needRefreshUserInfo) {
			CCApplication.needRefreshUserInfo = false;
			setUserInfo();
		}
		refreshCount();
		super.onResume();
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void setUserInfo() {

		User loginUser = UserManager.getInstance().getLoginUser();
		if (loginUser.userImg != null) {
			ImageLoadUtil.load(this, image, loginUser.userImg,
					R.drawable.headlogo, R.drawable.headlogo);
		}
		name.setText(loginUser.firstName);
		city.setText(loginUser.address);
		info.setText(loginUser.signature);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		name = (TextView) getView().findViewById(R.id.name);
		city = (TextView) getView().findViewById(R.id.city);
		info = (TextView) getView().findViewById(R.id.single);
		tip = (TextView) getView().findViewById(R.id.tip);
		guanzhu_tip = (TextView) getView().findViewById(R.id.guanzhu_tip);
		topBg = (ImageView) getView().findViewById(R.id.top_img);
		topBg.getLayoutParams().height = CCApplication.screenWidth / 2;
		topBg.setScaleType(ScaleType.FIT_XY);
		if (!TextUtils
				.isEmpty(UserManager.getInstance().getLoginUser().userCover)) {
			ImageLoadUtil.load(this, topBg, UserManager.getInstance()
					.getLoginUser().userCover);
			// topBg.setImageUrl(CCApplication.loginUser.userCover);
		}
		topBg.setOnClickListener(this);

		image = (CircularImageView) getView().findViewById(R.id.image);
		setUserInfo();

		image.setOnClickListener(this);
		getView().findViewById(R.id.my_store_layout).setOnClickListener(this);
		getView().findViewById(R.id.my_bulan_layout).setOnClickListener(this);
		getView().findViewById(R.id.my_info_layout).setOnClickListener(this);
		getView().findViewById(R.id.setting_layout).setOnClickListener(this);
		getView().findViewById(R.id.about_layout).setOnClickListener(this);
		getView().findViewById(R.id.guanzhu_layout).setOnClickListener(this);
		getView().findViewById(R.id.msg_center_layout).setOnClickListener(this);
		getView().findViewById(R.id.caogaoxiang_layout)
				.setOnClickListener(this);
		refreshCount();

	}

	public String avacterPath;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data == null) {
			return;
		}

		ArrayList<String> tempSelectPath = data
				.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
		if (tempSelectPath != null && tempSelectPath.size() > 0) {
			avacterPath = tempSelectPath.get(0);
			chooseSmalPic();
		} else {
			Bitmap bitmap = data.getParcelableExtra("data");
			this.topBg.setImageBitmap(bitmap);
			avacterPath = FileUtil.saveBitmap(bitmap).getAbsolutePath();
			upload(avacterPath);
		}

	}

	public static final int REQUEST_CROP_IMAGE = 1;

	private void chooseSmalPic() {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(Uri.fromFile(new File(avacterPath)), "image/*");
		intent.putExtra("crop", "true");
		// 裁剪框的比例，1：1
		intent.putExtra("aspectX", 2);
		intent.putExtra("aspectY", 1);
		// 裁剪后输出图片的尺寸大小
		intent.putExtra("outputX", 500);
		intent.putExtra("outputY", 250);

		intent.putExtra("outputFormat", "JPEG");// 图片格式
		intent.putExtra("noFaceDetection", true);// 取消人脸识别
		intent.putExtra("return-data", true);
		// 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CUT
		startActivityForResult(intent, REQUEST_CROP_IMAGE);
	}

	private void upload(final String path) {
		new Thread() {
			@Override
			public void run() {
				String URL = CCApplication.HTTPSERVER
						+ "/m_user!setUserCover.action";
				User loginUser = UserManager.getInstance().getLoginUser();
				try {
					MultipartEntity param = new MultipartEntity();
					param.addPart("userId",
							new StringBody(String.valueOf(loginUser.ID)));
					param.addPart("ccukey", new StringBody(loginUser.ccukey));
					param.addPart("file", new FileBody(new File(path)));
					HttpResponse response = new HttpProxy().post(URL, param);
					int code = response.getStatusLine().getStatusCode();
					if (code == 200) {
						String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
						JSONObject body = new JSONObject(rev)
								.getJSONObject("body");

						JSONObject userObj = body.optJSONObject("userInfo");

						User newUser = User.jsonToUser(userObj);
						UserManager.getInstance().updateUser(newUser);
						PropertyUtil.putValue("user_info", userObj.toString());

						handler.sendEmptyMessage(0);
						return;
					}
				} catch (Exception e) {
				}
				handler.sendEmptyMessage(-1);
			}

		}.start();
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0) {
				ToastUtil.show("上传成功");
				if (avacterPath != null) {

					ImageLoadUtil.load(getActivity(), topBg, new File(
							avacterPath));
				}

			} else if (msg.what == -1) {
				ToastUtil.show("上传失败");
			}
		}
	};

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.my_store_layout:
			Intent toStore = new Intent(getActivity(), MyStorePage.class);
			startActivity(toStore);
			break;
		case R.id.my_bulan_layout:
			Intent toMyBulan = new Intent(getActivity(), MyBuLanPage.class);
			startActivity(toMyBulan);
			break;
		case R.id.my_info_layout:
			Intent toMyInfo = new Intent(getActivity(), MyInfoPage.class);
			startActivity(toMyInfo);
			break;
		case R.id.setting_layout:
			Intent toSetting = new Intent(getActivity(), SettingPage.class);
			startActivity(toSetting);
			break;
		case R.id.about_layout:
			Intent toAbout = new Intent(getActivity(), AboutPage.class);
			startActivity(toAbout);
			break;
		case R.id.guanzhu_layout:
			Intent msIntent = new Intent(getActivity(), MessageCenter.class);
			msIntent.putExtra("type", 0);
			startActivity(msIntent);
			break;
		case R.id.msg_center_layout:
			msIntent = new Intent(getActivity(), MessageCenter.class);
			msIntent.putExtra("type", 1);
			startActivity(msIntent);
			break;
		case R.id.caogaoxiang_layout:
			Intent caogaox = new Intent(getActivity(), BulanSaves.class);
			startActivity(caogaox);
			break;
		case R.id.top_img:
			Intent intent = new Intent(getActivity(),
					MultiImageSelectorActivity.class);
			intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE,
					MultiImageSelectorActivity.MODE_SINGLE);
			intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 1);
			getActivity().startActivityForResult(intent, 1);
			break;
		case R.id.image:
			showBigImage();
			break;
		default:
			break;
		}
	}

	private void showBigImage() {
		Intent intent = new Intent(getActivity(),
				EaseShowBigImageActivity.class);
		intent.putExtra("remotepath",
				UserManager.getInstance().getLoginUser().userImg);
		startActivity(intent);
	}

	public void refreshCount() {
		if (CCApplication.unreadMsgCount > 0) {
			if (tip == null) {
				tip = (TextView) getView().findViewById(R.id.tip);
			}
			tip.setVisibility(View.VISIBLE);
			if (CCApplication.unreadMsgCount > 99) {
				tip.setText(String.valueOf(99));
			} else {
				tip.setText(String.valueOf(CCApplication.unreadMsgCount));
			}
		} else {
			tip.setVisibility(View.GONE);
		}


		if (guanzhu_tip == null) {
			guanzhu_tip = (TextView) getView().findViewById(R.id.guanzhu_tip);
		}
		if (CCApplication.guanzhu_MsgCount > 0) {
			guanzhu_tip.setVisibility(View.VISIBLE);
			if (CCApplication.guanzhu_MsgCount > 99) {
				guanzhu_tip.setText(String.valueOf(99));
			} else {
				guanzhu_tip.setText(String.valueOf(CCApplication.guanzhu_MsgCount));
			}
		} else {
			guanzhu_tip.setVisibility(View.GONE);
		}
	}
}
