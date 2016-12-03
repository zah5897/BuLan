package com.mingmay.bulan.ui.fragment.type;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.easeui.widget.EaseExpandGridView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mingmay.bulan.R;
import com.mingmay.bulan.adapter.GridAdapter;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.app.listener.CallBackListener;
import com.mingmay.bulan.base.BaseActivity;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.task.UploadImageTask;
import com.mingmay.bulan.util.AppUtil;
import com.mingmay.bulan.util.FileUtil;
import com.mingmay.bulan.util.ImageLoadUtil;
import com.mingmay.bulan.util.ProgressDialogUtil;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpUtils;

public class ModifyZhuanLanPage extends BaseActivity {
	private EaseExpandGridView userGridview;
	private GridAdapter adapter;
	private static final int REQUEST_CODE_ADD_USER = 0;

	private EditText nameView;
	private String iconPath;
	private ImageView iconView;

	long id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_zhuanlan);
		id = getIntent().getLongExtra("id", 0);

		userGridview = (EaseExpandGridView) findViewById(R.id.gridview);
		nameView = ((EditText) findViewById(R.id.name));
		TextView title = ((TextView) findViewById(R.id.title));
		title.setText("编辑专栏");
		List<User> members = new ArrayList<User>();
		adapter = new GridAdapter(this, R.layout.em_grid, members);
		userGridview.setAdapter(adapter);
		findViewById(R.id.save).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				modify();
			}
		});
		iconView = (ImageView) findViewById(R.id.icon);
		iconView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(ModifyZhuanLanPage.this,
						MultiImageSelectorActivity.class);
				intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE,
						MultiImageSelectorActivity.MODE_SINGLE);
				intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT,
						1);
				startActivityForResult(intent, 1);
			}
		});
		userGridview.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (adapter.isInDeleteMode) {
						adapter.setFlag(false);
						return true;
					}
					break;
				default:
					break;
				}
				return false;
			}
		});
		loadDetial();
	}

	private void loadDetial() {

		ProgressDialogUtil.showProgress(this, "正在加载数据....");
		RequestParams params = new RequestParams();
		params.put("userId", UserManager.getInstance().getLoginUser().ID);
		params.put("wardrobeId", id);
		HttpUtils.post(CCApplication.HTTPSERVER
				+ "/m_wardrobe!getWardrobe.action", params,
				new JsonHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers,
							JSONObject response) {

						JSONObject body = response.optJSONObject("body");
						JSONObject wardrobeInfo = body
								.optJSONObject("wardrobeInfo");

						if (wardrobeInfo != null) {
							String name = wardrobeInfo.optString("name");
							String color = wardrobeInfo.optString("color");
							String wardrobeImage = wardrobeInfo
									.optString("wardrobeImage");

							ImageLoadUtil.load(ModifyZhuanLanPage.this,
									iconView, wardrobeImage);

							nameView.setText(name);
							nameView.setSelection(name.length());

							int isOpen = wardrobeInfo.optInt("isOpen");

							if (isOpen == 1) {
								((CheckBox) findViewById(R.id.isOpen))
										.setChecked(true);
							} else {
								((CheckBox) findViewById(R.id.isOpen))
										.setChecked(false);
							}

							JSONArray users = wardrobeInfo
									.optJSONArray("userInfo");

							if (users != null && users.length() > 0) {
								List<User> userList = new ArrayList<User>();
								for (int i = 0; i < users.length(); i++) {
									JSONObject userObj = users.optJSONObject(i);
									User u = new User();
									u.ID = userObj.optLong("userId");
									u.firstName = userObj.optString("userName");
									u.userImg = userObj.optString("userImg");

									userList.add(u);

								}
								adapter.addAll(userList);
							}
						} else {
							ToastUtil.show("服务器异常");
							finish();
						}

						ProgressDialogUtil.dismiss();
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							String responseString, Throwable throwable) {
						ToastUtil.show("加载数据失败..");
						ProgressDialogUtil.dismiss();
						finish();
					}
				});

	}

	public void back(View view) {
		onBackPressed();
	}

	private void modify() {

		final String name = nameView.getText().toString();
		if (TextUtils.isEmpty(name)) {
			ToastUtil.show("名称不能为空");
			return;
		}
		ProgressDialogUtil.showProgress(this, "正在修改专栏...");

		if (TextUtils.isEmpty(iconPath)) {
			modifyValue(null);
		} else {
			UploadImageTask task = new UploadImageTask();
			task.upload(iconPath, new CallBackListener() {

				@Override
				public void onSuccess(final String tempId) {

					runOnUiThread(new Runnable() {
						public void run() {
							modifyValue(tempId);
						}
					});
				}

				@Override
				public void onFailure(int code) {
					runOnUiThread(new Runnable() {
						public void run() {
							ToastUtil.show("图片上传失败");
							ProgressDialogUtil.dismiss();
						}
					});
				}
			});
		}

	}

	private void modifyValue(String tempId) {

		AppUtil.closeSoftKeyBoard(nameView);
		boolean isOpen = ((CheckBox) findViewById(R.id.isOpen)).isChecked();

		String url = CCApplication.HTTPSERVER
				+ "/m_wardrobe!updateWardrobe.action";
		User u = UserManager.getInstance().getLoginUser();
		RequestParams params = new RequestParams();
		params.add("userId", String.valueOf(u.ID));
		params.add("ccukey", u.ccukey);
		params.add("wardrobeId", String.valueOf(id));
		params.add("wardrobeName", nameView.getText().toString());

		List<User> ids = adapter.getObjects();
		int size = ids.size();
		if (size > 2) {
			String str = null;
			for (int i = 0; i < size; i++) {
				long user_id = ids.get(i).ID;
				if (user_id < 0) {
					continue;
				}
				if (i == 0)
					str = String.valueOf(user_id);
				else
					str += "," + user_id;
			}
			params.add("friendUserIds", str);
		}

		if (tempId != null) {
			params.add("wardrobeImage", tempId);
		}
		params.add("isOpen", isOpen ? "1" : "0");

		HttpUtils.post(url, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				JSONObject body = response.optJSONObject("body");
				int cstatus = body.optInt("cstatus");
				if (cstatus == 0) {
					ToastUtil.show("修改成功");
					setResult(2);
					finish();
				} else {
					ToastUtil.show("修改失败");
				}
				ProgressDialogUtil.dismiss();
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				ToastUtil.show("修改失败");
				ProgressDialogUtil.dismiss();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE_ADD_USER:// 添加群成员
				final String[] newmembers = data
						.getStringArrayExtra("newmembers");

				if(newmembers==null){
					adapter.notifyDataSetChanged();
					return;
				}
				List<User> us = new ArrayList<User>();
				for (String id : newmembers) {

					long i = Long.parseLong(id);
					User u = new User();
					u.ID = i;
					us.add(u);
				}

				adapter.addAll(us);
				break;
			case 1:

				ArrayList<String> tempSelectPath = data
						.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
				if (tempSelectPath != null && tempSelectPath.size() > 0) {
					iconPath = tempSelectPath.get(0);
					chooseSmalPic();
				} else {
					Bitmap bitmap = data.getParcelableExtra("data");
					this.iconView.setImageBitmap(bitmap);
					iconPath = FileUtil.saveBitmap(bitmap).getAbsolutePath();
				}

				break;
			default:
				break;
			}
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

}
