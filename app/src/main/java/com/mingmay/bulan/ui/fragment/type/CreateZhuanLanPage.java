package com.mingmay.bulan.ui.fragment.type;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

import org.apache.http.Header;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.easeui.widget.EaseExpandGridView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mingmay.bulan.R;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.app.listener.CallBackListener;
import com.mingmay.bulan.base.BaseActivity;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.task.UploadImageTask;
import com.mingmay.bulan.ui.friend.ContactPage;
import com.mingmay.bulan.util.FileUtil;
import com.mingmay.bulan.util.ImageLoadUtil;
import com.mingmay.bulan.util.ProgressDialogUtil;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpUtils;

public class CreateZhuanLanPage extends BaseActivity {
	private EaseExpandGridView userGridview;
	private GridAdapter adapter;
	private static final int REQUEST_CODE_ADD_USER = 0;

	private EditText nameView;
	private String iconPath;
	private ImageView iconView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_zhuanlan);
		userGridview = (EaseExpandGridView) findViewById(R.id.gridview);
		nameView = ((EditText) findViewById(R.id.name));

		List<String> members = new ArrayList<String>();

		adapter = new GridAdapter(this, R.layout.em_grid, members);
		userGridview.setAdapter(adapter);

		findViewById(R.id.save).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				create();
			}
		});
		iconView = (ImageView) findViewById(R.id.icon);
		iconView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(CreateZhuanLanPage.this,
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
						adapter.isInDeleteMode = false;
						adapter.notifyDataSetChanged();
						return true;
					}
					break;
				default:
					break;
				}
				return false;
			}
		});
	}

	public void back(View view) {
		onBackPressed();
	}

	private void create() {

		if (TextUtils.isEmpty(iconPath)) {
			ToastUtil.show("图标不能为空");
			return;
		}

		final String name = nameView.getText().toString();
		if (TextUtils.isEmpty(name)) {
			ToastUtil.show("名称不能为空");
			return;
		}

		ProgressDialogUtil.showProgress(this, "正在创建专栏...");
		UploadImageTask task = new UploadImageTask();
		task.upload(iconPath, new CallBackListener() {

			@Override
			public void onSuccess(final String tempId) {

				runOnUiThread(new Runnable() {
					public void run() {
						boolean isOpen = ((CheckBox) findViewById(R.id.isOpen))
								.isChecked();

						String url = CCApplication.HTTPSERVER
								+ "/m_wardrobe!addWardrobe.action";
						User u = UserManager.getInstance().getLoginUser();
						RequestParams params = new RequestParams();
						params.add("userId", String.valueOf(u.ID));
						params.add("ccukey", u.ccukey);
						params.add("wardrobeName", name);

						List<String> ids = adapter.getObjects();
						int size = ids.size();
						if (size > 0) {
							String str = null;
							for (int i = 0; i < size; i++) {
								if (i == 0)
									str = ids.get(i);
								else
									str += "," + ids.get(i);
							}
							params.add("friendUserIds", str);
						}

						params.add("wardrobeImage", tempId);
						params.add("isOpen", isOpen ? "1" : "0");

						HttpUtils.post(url, params,
								new JsonHttpResponseHandler() {
									@Override
									public void onSuccess(int statusCode,
											Header[] headers,
											JSONObject response) {
										JSONObject body = response
												.optJSONObject("body");
										int cstatus = body.optInt("cstatus");
										if (cstatus == 0) {
											ToastUtil.show("创建成功");
											setResult(2);
											finish();
										} else {
											ToastUtil.show("创建失败");
										}
										ProgressDialogUtil.dismiss();
									}

									@Override
									public void onFailure(int statusCode,
											Header[] headers,
											String responseString,
											Throwable throwable) {
										ToastUtil.show("创建失败");
										ProgressDialogUtil.dismiss();
									}
								});
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE_ADD_USER:// 添加群成员
				if(adapter!=null&&adapter.getCount()>0){
					final String[] newmembers = data
							.getStringArrayExtra("newmembers");
					if(newmembers!=null){
						adapter.addAll(newmembers);
					}

				}
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

	private class GridAdapter extends ArrayAdapter<String> {

		private int res;
		public boolean isInDeleteMode;
		private List<String> objects;

		public GridAdapter(Context context, int textViewResourceId,
				List<String> objects) {
			super(context, textViewResourceId, objects);
			this.objects = objects;
			res = textViewResourceId;
			isInDeleteMode = false;
		}


		public List<String> getObjects() {
			// TODO Auto-generated method stub
			return objects;
		}

		@Override
		public View getView(final int position, View convertView,
				final ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(getContext()).inflate(res,
						null);
				holder.imageView = (ImageView) convertView
						.findViewById(R.id.iv_avatar);
				holder.textView = (TextView) convertView
						.findViewById(R.id.tv_name);
				holder.badgeDeleteView = (ImageView) convertView
						.findViewById(R.id.badge_delete);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final LinearLayout button = (LinearLayout) convertView
					.findViewById(R.id.button_avatar);
			// 最后一个item，减人按钮
			if (position == getCount() - 1) {
				holder.textView.setText("");
				// 设置成删除按钮
				holder.imageView
						.setImageResource(R.drawable.em_smiley_minus_btn);
				// button.setCompoundDrawablesWithIntrinsicBounds(0,
				// R.drawable.smiley_minus_btn, 0, 0);
				// 如果不是创建者或者没有相应权限，不提供加减人按钮
				// 显示删除按钮
				if (isInDeleteMode) {
					// 正处于删除模式下，隐藏删除按钮
					convertView.setVisibility(View.INVISIBLE);
				} else {
					// 正常模式
					convertView.setVisibility(View.VISIBLE);
					convertView.findViewById(R.id.badge_delete).setVisibility(
							View.INVISIBLE);
				}
				final String st10 = getResources().getString(
						R.string.The_delete_button_is_clicked);
				button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (objects.size() > 0) {
							isInDeleteMode = true;
						}
						notifyDataSetChanged();
					}
				});

			} else if (position == getCount() - 2) { // 添加群组成员按钮
				holder.textView.setText("");
				holder.imageView.setImageResource(R.drawable.em_smiley_add_btn);
				// button.setCompoundDrawablesWithIntrinsicBounds(0,
				// R.drawable.smiley_add_btn, 0, 0);
				// 如果不是创建者或者没有相应权限

				// 正处于删除模式下,隐藏添加按钮
				if (isInDeleteMode) {
					convertView.setVisibility(View.INVISIBLE);
				} else {
					convertView.setVisibility(View.VISIBLE);
					convertView.findViewById(R.id.badge_delete).setVisibility(
							View.INVISIBLE);
				}
				final String st11 = getResources().getString(
						R.string.Add_a_button_was_clicked);
				button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent toAddMember = new Intent(
								CreateZhuanLanPage.this, ContactPage.class);
						toAddMember.putExtra("need_back", true);
						startActivityForResult(toAddMember,
								REQUEST_CODE_ADD_USER);
					}
				});
			} else { // 普通item，显示群组成员
				final String username = getItem(position);
				convertView.setVisibility(View.VISIBLE);
				button.setVisibility(View.VISIBLE);
				// Drawable avatar =
				// getResources().getDrawable(R.drawable.default_avatar);
				// avatar.setBounds(0, 0, referenceWidth, referenceHeight);
				// button.setCompoundDrawables(null, avatar, null, null);

				User user = UserManager.getInstance().getUser(username);

				if (user != null) {
					holder.textView.setText(user.firstName);
					ImageLoadUtil.load(CreateZhuanLanPage.this,
							holder.imageView, user.userImg);
				} else {
					holder.textView.setText(username);
					holder.imageView.setImageResource(R.drawable.headlogo);
				}

				// EaseUserUtils.setUserNick(username, holder.textView);
				// EaseUserUtils.setUserAvatar(getContext(), username,
				// holder.imageView);
				if (isInDeleteMode) {
					// 如果是删除模式下，显示减人图标
					convertView.findViewById(R.id.badge_delete).setVisibility(
							View.VISIBLE);
				} else {
					convertView.findViewById(R.id.badge_delete).setVisibility(
							View.INVISIBLE);
				}
				 

				button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (isInDeleteMode) {
							// 如果是删除自己，return
							objects.remove(position);
							if (objects.size() == 0) {
								isInDeleteMode = false;
							}
							notifyDataSetChanged();
						} else {
							// 正常情况下点击user，可以进入用户详情或者聊天页面等等
							// startActivity(new
							// Intent(GroupDetailsActivity.this,
							// ChatActivity.class).putExtra("userId",
							// user.getUsername()));

						}
					}
				});

			}
			return convertView;
		}

		@Override
		public int getCount() {
			return super.getCount() + 2;
		}

		class ViewHolder {
			ImageView imageView;
			TextView textView;
			ImageView badgeDeleteView;
		}
	}

}
