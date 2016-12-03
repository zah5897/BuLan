package com.mingmay.bulan.ui.fragment.publish;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mingmay.bulan.R;
import com.mingmay.bulan.adapter.SimpleZhuanLanAdapter;
import com.mingmay.bulan.adapter.TagPublishAdapter;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.DataBaseManager;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.BuLanModel;
import com.mingmay.bulan.model.BuLanSaveModel;
import com.mingmay.bulan.model.Group;
import com.mingmay.bulan.model.Tag;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.service.CCService;
import com.mingmay.bulan.util.FileUtil;
import com.mingmay.bulan.util.ImageLoadUtil;
import com.mingmay.bulan.util.PropertyUtil;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpUtils;
import com.mingmay.bulan.view.MyListView;

public class BuLanPublishActivity extends Activity {
	private ImageView icon;
	private String iconPath;

	private TextView titleView, publisher;

	private TagPublishAdapter allTagAdapter;
	private MyListView allTagsListView, zhuanlan_list;

	private String title;

	private TextView publishTip;

	CheckBox openOrOnlyShare;

	private long local_edit_id;

	private String[] images;

	private PullToRefreshScrollView pullToRefreshScrollView;
	private int pageIndex = 1;

	private SimpleZhuanLanAdapter zhuanlanAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_bulan_publish);
		images = getIntent().getStringArrayExtra("images");
		if (images != null) {
			iconPath = images[0];
		}
		local_edit_id = getIntent().getLongExtra("local_id", 0);
		title = getIntent().getStringExtra("title");
		initPullRefreshView();
		initview();
		loadZhuanLan();
	}

	private void initPullRefreshView() {
		pullToRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.pull_refresh_scrollview);
		pullToRefreshScrollView.setMode(Mode.PULL_UP_TO_REFRESH);
		OnRefreshListener<ScrollView> onr = new OnRefreshListener<ScrollView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				if (openOrOnlyShare.isChecked()) {
					return;
				}
				loadZhuanLan();
			}
		};
		pullToRefreshScrollView.setOnRefreshListener(onr);
	}

	private void initview() {
		findViewById(R.id.back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onBackPressed();
			}
		});
		findViewById(R.id.publish).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (TextUtils.isEmpty(iconPath)) {
					ToastUtil.show("请选择icon...");
					Animation cycleAnim = android.view.animation.AnimationUtils
							.loadAnimation(BuLanPublishActivity.this,
									R.anim.shake);
					icon.startAnimation(cycleAnim);
					return;
				}

				String isOpen = "0";
				String tagStr = "";
				if (!openOrOnlyShare.isChecked()) {
					isOpen = "1"; // 公开
					tagStr = getTagStr();
				}

				DataBaseManager.getInstance().updateBulanInfo(local_edit_id,
						iconPath, isOpen, tagStr,
						BuLanSaveModel.STATE_HAS_TO_COMMIT);
				// if (images != null) {
				ToastUtil.show("后台提交发布中...");
				Intent toServiceUpload = new Intent(BuLanPublishActivity.this,
						CCService.class);
				toServiceUpload.putExtra("local_id", local_edit_id);
				toServiceUpload.setAction(CCService.Action_UPLOAD_BULAN);
				startService(toServiceUpload);
				setResult(100);
				finish();
				PropertyUtil.putValue("last_edit_local_id", 0);
			}
		});
		icon = (ImageView) findViewById(R.id.summary_icon);
		icon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				openGallery();
			}
		});
		setIcon();

		titleView = (TextView) findViewById(R.id.summary_str);
		publisher = (TextView) findViewById(R.id.summary_name);

		titleView.setText(title);
		publisher.setText(UserManager.getInstance().getLoginUser().firstName);

		allTagsListView = (MyListView) findViewById(R.id.tags);
		zhuanlan_list = (MyListView) findViewById(R.id.zhuanlan_list);

		publishTip = (TextView) findViewById(R.id.publish_tip);
		openOrOnlyShare = (CheckBox) findViewById(R.id.isOpen);
		openOrOnlyShare
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {

						if (isChecked) {
							findViewById(R.id.botton_layout).setVisibility(
									View.GONE);

							publishTip.setText("仅保存到我的布栏");
						} else {
							publishTip.setText("选择发布到栏目");
							findViewById(R.id.botton_layout).setVisibility(
									View.VISIBLE);
						}

					}
				});
		findViewById(R.id.to_expend).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (allTagsListView.getVisibility() == View.GONE) {
					allTagsListView.setVisibility(View.VISIBLE);
				} else {
					allTagsListView.setVisibility(View.GONE);
				}

			}
		});
	}

	private void showAllTagToSelect(JSONArray array) {
		if (array != null) {
			List<Tag> tags = new ArrayList<Tag>();

			int len = array.length();
			for (int i = 0; i < len; i++) {
				tags.add(Tag.jsonToTag(array.optJSONObject(i)));
			}
			allTagAdapter = new TagPublishAdapter(BuLanPublishActivity.this,
					tags);
			allTagsListView.setAdapter(allTagAdapter);
		}
	}

	public Tag hasSelected;

	public void setTag(Tag tag) {
		this.hasSelected = tag;
	}

	private String getTagStr() {

		String ids = null;
		if (zhuanlanAdapter != null && zhuanlanAdapter.getSelected().size() > 0) {
			List<Group> selected = zhuanlanAdapter.getSelected();
			for (int i = 0; i < selected.size(); i++) {
				if (i == 0) {
					ids = String.valueOf(selected.get(i).id);
				} else {
					ids += "," + String.valueOf(selected.get(i).id);
				}
			}
		}

		if (ids == null) {
			if (hasSelected == null) {
				return "";
			} else {
				return String.valueOf(hasSelected.id);
			}
		} else {
			if (hasSelected != null) {
				ids += "," + String.valueOf(hasSelected.id);
			}
			return ids;
		}
	}

	private void loadZhuanLan() {
		String url = CCApplication.HTTPSERVER
				+ "/m_wardrobe!findSelectWardobes.action";
		User u = UserManager.getInstance().getLoginUser();
		RequestParams params = new RequestParams();
		params.add("userId", String.valueOf(u.ID));
		params.add("ccukey", u.ccukey);
		params.add("curPage", String.valueOf(pageIndex));
		params.add("pageSize", "20");
		HttpUtils.post(url, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {

				JSONObject body = response.optJSONObject("body");
				JSONArray tags = body.optJSONArray("systemtag");
				showAllTagToSelect(tags);
				JSONArray tagArray = body.optJSONArray("zhuanlan");
				int len = tagArray.length();
				if (len > 0) {
					List<Group> groups = new ArrayList<Group>();
					for (int i = 0; i < len; i++) {
						JSONObject obj = tagArray.optJSONObject(i);
						Group g = Group.parse(obj);
						groups.add(g);
					}
					if (pageIndex == 1) {
						zhuanlanAdapter = new SimpleZhuanLanAdapter(
								BuLanPublishActivity.this, groups);
						zhuanlan_list.setAdapter(zhuanlanAdapter);
					} else {
						zhuanlanAdapter.add(groups);
					}
					pageIndex++;
				}
				pullToRefreshScrollView.onRefreshComplete();
				findViewById(R.id.botton_layout).setVisibility(View.VISIBLE);
				findViewById(R.id.loading).setVisibility(View.GONE);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				super.onFailure(statusCode, headers, responseString, throwable);
				pullToRefreshScrollView.onRefreshComplete();
				findViewById(R.id.loading).setVisibility(View.GONE);
			}
		});
	}

	private void openGallery() {
		Intent intent = new Intent(this, MultiImageSelectorActivity.class);
		intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE,
				MultiImageSelectorActivity.MODE_SINGLE);
		intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 1);
		startActivityForResult(intent, 2);
	}

	private void setIcon() {
		if (!TextUtils.isEmpty(iconPath)) {
			if (new File(iconPath).exists()) {
				ImageLoadUtil.load(this, icon, new File(iconPath), new int[] {
						100, 100 });
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (resultCode == RESULT_OK) { // 清空消息
			if (data != null) {
				ArrayList<String> tempSelectPath = data
						.getStringArrayListExtra("select_result");
				if (tempSelectPath != null) {
					for (String path : tempSelectPath) {
						iconPath = path;
						chooseSmalPic();
						return;
					}
				} else {
					Bitmap bitmap = data.getParcelableExtra("data");
					this.icon.setImageBitmap(bitmap);
					iconPath = FileUtil.saveBuLanIconBitmap(bitmap)
							.getAbsolutePath();
				}
			}
		}
	}

	public static final int REQUEST_CROP_IMAGE = 3;

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
		startActivityForResult(intent, REQUEST_CROP_IMAGE);
	}

	public void publishSuccess(BuLanModel publishModel) {
		// TODO Auto-generated method stub
		setResult(100);
		finish();
	}
}
