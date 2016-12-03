package com.mingmay.bulan.ui.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseEmojiconGroupEntity;
import com.hyphenate.easeui.model.EaseDefaultEmojiconDatas;
import com.hyphenate.easeui.utils.EaseSmileUtils;
import com.hyphenate.easeui.widget.emojicon.EaseEmojiconMenu;
import com.hyphenate.easeui.widget.emojicon.EaseEmojiconMenuBase.EaseEmojiconMenuListener;
import com.mingmay.bulan.R;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.DataBaseManager;
import com.mingmay.bulan.model.BuLanSaveModel;
import com.mingmay.bulan.model.BulanEditModel;
import com.mingmay.bulan.ui.fragment.publish.BuLanPublishActivity;
import com.mingmay.bulan.util.AppUtil;
import com.mingmay.bulan.util.BitmapUtil;
import com.mingmay.bulan.util.CommonUtils;
import com.mingmay.bulan.util.ImageLoadUtil;
import com.mingmay.bulan.util.PropertyUtil;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.view.OnSizeChangedListener;
import com.mingmay.bulan.view.ResizeRelativeLayout;

public class TextCreatorPage extends Activity implements OnClickListener,
		OnSizeChangedListener {
	private EditText titleEdit;
	private LinearLayout mainContainer;
	ResizeRelativeLayout layout;
	int bottomState = 0;// 默认 ,1 content被点击,2底部二级操作menu点击close状态记录
	private View editMenu, mainMenu;
	private View faceContainer;
	private File cameraFile;
	public static final int REQUEST_CODE_CAMERA = 18;

	public static final int MODE_MULTI = 1;
	private static final int REQUEST_IMAGE = 2;

	private ArrayList<View> views = new ArrayList<View>();

	private EditText currentEditView;

	private long localID;

	private BuLanSaveModel old;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		layout = (ResizeRelativeLayout) getLayoutInflater().inflate(
				R.layout.layout_publish_text, null);
		setContentView(layout);
		localID = getIntent().getLongExtra("localId", 0);
		if (localID > 0) {
			old = DataBaseManager.getInstance(this).getBulanSave(localID);
			if (old == null) {
				localID = 0;
				PropertyUtil.putValue("last_edit_local_id", 0);
			}
		}
		layout.setOnSizeChangedListener(this);
		initView();
		intEmoji();
	}

	private void removeView(View view) {
		views.remove(view);
		mainContainer.removeView(view);
	}

	@SuppressLint("ClickableViewAccessibility")
	private void initView() {
		editMenu = findViewById(R.id.edit_menu);
		mainMenu = findViewById(R.id.main_menu);

		findViewById(R.id.back).setOnClickListener(this);
		findViewById(R.id.new_task).setOnClickListener(this);
		findViewById(R.id.pause_task).setOnClickListener(this);
		findViewById(R.id.publish_task).setOnClickListener(this);

		findViewById(R.id.open_camera).setOnClickListener(this);
		findViewById(R.id.select_pic).setOnClickListener(this);
		findViewById(R.id.select_emoji).setOnClickListener(this);
		mainContainer = (LinearLayout) findViewById(R.id.mainContainer);
		faceContainer = findViewById(R.id.ll_face_container);
		titleEdit = (EditText) findViewById(R.id.title);
		titleEdit.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				bottomState = 0;
				reView();
				return false;
			}
		});

		newTask(false);
		if (old != null) {
			reloadOldEdit();
		}
	}

	private void reloadOldEdit() {

		String title = old.title;

		if (!TextUtils.isEmpty(title)) {
			titleEdit.setText(title);
			titleEdit.setSelection(title.length());
		}
		ArrayList<BulanEditModel> element = old.getModels();

		if (element.size() > 0) {
			for (BulanEditModel bem : element) {
				if (bem.type == 1) { // image
					setPicture(bem.content);
				} else { // text
					reloadEdit(bem);
				}
			}
		}
	}

	private void reloadEdit(BulanEditModel model) {
		if (TextUtils.isEmpty(model.content)) {
			return;
		}
		Spannable emojiContent = EaseSmileUtils.getSmiledText(getBaseContext(),
				model.content);
		if (views.size() == 0) {
			createEndEdit(emojiContent);
		} else {
			View last = views.get(views.size() - 1);
			if (last instanceof EditText) {
				if (model.content != null) {
					((EditText) last).append(emojiContent);
				}
			} else {
				createEndEdit(emojiContent);
			}
		}
	}

	@SuppressLint("RtlHardcoded")
	private void newTask(boolean newTask) {
		if (newTask) {
			mainContainer.removeAllViews();
			views.clear();
			AppUtil.closeSoftKeyBoard(currentEditView);
			reView();
			localID = 0;
			old = null;
		}
		titleEdit.setText("");
		titleEdit.setHint(Html.fromHtml("<font color='#E4E4E4'>请输入标题</font>"));
		EditText contentTip = new EditText(this);
		contentTip.setHint(Html.fromHtml("<font color='#E4E4E4'>内容</font>"));
		contentTip.setGravity(Gravity.TOP | Gravity.LEFT);
		currentEditView = contentTip;
		addEditView(contentTip, new LinearLayout.LayoutParams(-1, -1));

	}

	@SuppressLint("ClickableViewAccessibility")
	private OnTouchListener editOnTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent arg1) {
			currentEditView = (EditText) view;
			bottomState = 1;
			change();
			return false;
		}
	};

	@SuppressLint("NewApi")
	private void addEditView(EditText editText, LinearLayout.LayoutParams param) {
		editText.setOnTouchListener(editOnTouchListener);
		editText.setBackground(null);
		setKeyListener(editText);
		mainContainer.addView(editText, param);
		views.add(editText);
	}

	private void addImageView(ImageView imageview,
			LinearLayout.LayoutParams param) {
		mainContainer.addView(imageview, param);
		views.add(imageview);
	}

	public void selectPicFromCamera() {
		if (!CommonUtils.isExitsSdcard()) {
			ToastUtil.show("sd卡不存在");
			return;
		}

		cameraFile = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
		cameraFile.getParentFile().mkdirs();
		startActivityForResult(
				new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(
						MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile)),
				REQUEST_CODE_CAMERA);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) { // 清空消息
			if (requestCode == REQUEST_CODE_CAMERA) { // 发送照片
				if (cameraFile != null && cameraFile.exists())
					setPicture(cameraFile.getAbsolutePath());
				reView();
			} else {
				if (data != null) {
					ArrayList<String> tempSelectPath = data
							.getStringArrayListExtra("select_result");
					if (tempSelectPath != null) {
						for (String path : tempSelectPath) {
							setPicture(path);
						}
					}
				}
			}
		} else if (resultCode == 100) {
			newTask(true);
		}
	}

	private void setPicture(final String filePath) {
		if (!TextUtils.isEmpty(filePath)) {
			int[] wh = BitmapUtil.getDecodeBitmap(filePath);
			float percent = wh[0] * 1.0f / wh[1];
			int realH = (int) (CCApplication.screenWidth - 40 / percent);

			createAndAddImageView(filePath, new int[] {
					CCApplication.screenWidth - 40, realH });
			createEndEdit(null);
		}
	}

	private void createAndAddImageView(String filePath, int[] wh) {

		ImageView img = new ImageView(this);
		img.setAdjustViewBounds(true);
		ImageLoadUtil.load(this, img, new File(filePath), wh);
		LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(-1, -2);
		if (views.size() > 0) {
			View v = views.get(views.size() - 1);
			if (v instanceof EditText) {
				v.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
			}
		}
		param.setMargins(10, 5, 10, 5);
		img.setTag(filePath);
		addImageView(img, param);
	}

	private void createEndEdit(Spannable text) {
		EditText end = new EditText(this);
		end.setGravity(Gravity.TOP | Gravity.LEFT);
		addEditView(end, new LayoutParams(-1, -2));
		if (text != null) {
			end.setText(text);
		}
		end.requestFocus();
	}

	private boolean needSave() {
		String title = titleEdit.getText().toString();
		if (TextUtils.isEmpty(title.trim())) {
			if (views.size() == 1) {
				View fristView = views.get(0);
				if (fristView instanceof EditText) {
					String conString = ((EditText) fristView).getText()
							.toString();
					if (TextUtils.isEmpty(conString)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private long save() {
		if (!needSave()) {
			ToastUtil.show("您还没编辑任何内容呢!");
			return 0;
		}
		if (views.size() > 0) {
			JSONArray taskJson = new JSONArray();
			for (View view : views) {
				if (view instanceof EditText) {
					String text = ((EditText) view).getText().toString();
					JSONObject obj = new JSONObject();
					try {
						obj.put("type", 2);
						obj.put("content", text);
						taskJson.put(obj);
					} catch (JSONException e) {
						e.printStackTrace();
					}

				} else {
					String filePath = (String) view.getTag();
					JSONObject obj = new JSONObject();
					try {
						obj.put("type", 1);
						obj.put("content", filePath);
						obj.put("filename", "");
						taskJson.put(obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			String title = titleEdit.getText().toString();
			if (localID > 0) {
				DataBaseManager.getInstance(this).updateBulan(
						String.valueOf(localID), title, geyIconPath(), "0",
						"-", taskJson.toString());
			} else {
				localID = DataBaseManager.getInstance(this).savePublishBuLan(
						title, geyIconPath(), "0", "-", taskJson.toString());
			}
			PropertyUtil.putValue("last_edit_local_id", localID);
			return localID;
		} else {
			return -1;
		}
	}

	private String geyIconPath() {
		String[] images = getImagePath();
		if (images != null && images.length > 0) {
			return images[0];
		} else {
			return "";
		}
	}

	private String[] getImagePath() {
		ArrayList<String> imagePath = new ArrayList<String>();
		for (View view : views) {
			if (view instanceof ImageView) {
				imagePath.add(view.getTag().toString());
			}
		}
		String[] result = new String[imagePath.size()];
		imagePath.toArray(result);
		return result;
	}

	@Override
	public void onBackPressed() {
		save();
		CCApplication.caogaoxiangNeedRefresh = true;
		super.onBackPressed();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			onBackPressed();
			break;
		case R.id.new_task:
			save();
			ToastUtil.show("旧的编辑已经帮您保存草稿箱");
			newTask(true);
			break;
		case R.id.pause_task:
			save();
			ToastUtil.show("保存成功");
			break;
		case R.id.publish_task:

			String title = titleEdit.getText().toString().trim();
			if (TextUtils.isEmpty(title)) {
				ToastUtil.show("标题不能为空");
				return;
			}

			long id = save();
			if (id > 0) {
				Intent toPublish = null;
				toPublish = new Intent(TextCreatorPage.this,
						BuLanPublishActivity.class);
				toPublish.putExtra("title", titleEdit.getText().toString());
				String[] images = getImagePath();
				if (images != null && images.length > 0) {
					toPublish.putExtra("images", images);
				}
				toPublish.putExtra("local_id", id);
				startActivityForResult(toPublish, 0);
			}
			break;
		case R.id.open_camera:
			keep(false);
			selectPicFromCamera();
			break;
		case R.id.select_pic:
			keep(false);
			openGallery();
			break;
		case R.id.select_emoji:
			keep(true);
			break;
		default:
			break;
		}
	}

	private void openGallery() {
		int selectedMode = MODE_MULTI;
		boolean showCamera = false;
		int maxNum = 1;

		Intent intent = new Intent(this, MultiImageSelectorActivity.class);
		// 是否显示拍摄图片
		intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA,
				showCamera);
		// 最大可选择图片数量
		intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, maxNum);
		// 选择模式
		intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE,
				selectedMode);
		// 默认选择
		// if (mSelectPath != null && mSelectPath.size() > 0) {
		// intent.putExtra(
		// MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST,
		// mSelectPath);
		// }
		startActivityForResult(intent, REQUEST_IMAGE);
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (h > oldh) {
			// close keyboard
			if (bottomState != 2) {
				reView();
			} else {
				change();
			}

		} else {
			// open keyboard
			change();
		}
	}

	private void reView() {
		isEmoji = false;
		mainMenu.setVisibility(View.VISIBLE);
		editMenu.setVisibility(View.GONE);
		faceContainer.setVisibility(View.GONE);
	}

	private void change() {
		mainMenu.setVisibility(View.GONE);
		editMenu.setVisibility(View.VISIBLE);
		if (isEmoji) {
			faceContainer.setVisibility(View.VISIBLE);
		} else {
			faceContainer.setVisibility(View.GONE);
		}

	}

	boolean isEmoji;

	private void keep(boolean isEmoji) {
		this.isEmoji = isEmoji;
		bottomState = 2;
		if (currentEditView != null) {
			AppUtil.closeSoftKeyBoard(currentEditView);
		}
		if (isEmoji) {
			faceContainer.setVisibility(View.VISIBLE);
		} else {
			faceContainer.setVisibility(View.GONE);
		}

	}

	private void setKeyListener(EditText v) {
		if (v instanceof EditText) {
			((EditText) v).setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(View view, int arg1, KeyEvent event) {

					if (event != null) {
						if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
							EditText text = (EditText) view;
							if (event.getAction() == KeyEvent.ACTION_DOWN) {
								int selectedStart = text.getSelectionStart();
								if (selectedStart == 0) {
									text.setTag(true);
								} else {
									text.setTag(false);
								}
								return false;
							} else {
								if ((Boolean) text.getTag()) {
									int index = views.indexOf(text);
									if (index > 0) {
										int pre = index - 1;
										View temp = views.get(pre);
										if (temp instanceof ImageView) {
											removeView(temp);
											int ppp = pre - 1;
											if (ppp >= 0) {
												View pppView = views.get(ppp);
												if (pppView instanceof EditText) {
													String value = ((EditText) pppView)
															.getText()
															.toString();
													removeView(pppView);
													text.setText(value
															+ text.getText()
																	.toString());
													text.setSelection(text
															.length());
												}
											}
										} else {
											String str = ((EditText) temp)
													.getText().toString();
											removeView(temp);
											text.setText(str
													+ text.getText().toString());
											text.setSelection(str.length());
										}
									}
								}
								return true;
							}
						}
					}
					return false;
				}
			});
		}
	}

	List<EaseEmojiconGroupEntity> emojiconGroupList;
	private EaseEmojiconMenu emojiconMenu;

	private void intEmoji() {
		if (emojiconMenu == null) {
			emojiconMenu = (EaseEmojiconMenu) findViewById(R.id.emojicon);
			emojiconMenu
					.setEmojiconMenuListener(new EaseEmojiconMenuListener() {

						@Override
						public void onExpressionClicked(EaseEmojicon emojicon) {
							if (emojicon.getType() != EaseEmojicon.Type.BIG_EXPRESSION) {
								if (emojicon.getEmojiText() != null) {
									if (currentEditView == null) {
										return;
									}
									Editable editable = currentEditView
											.getText();
									int index = currentEditView
											.getSelectionEnd();

									Spannable emoji = EaseSmileUtils
											.getSmiledText(getBaseContext(),
													emojicon.getEmojiText());
									if (index < currentEditView.length()) {
										editable.insert(index, emoji);
									} else {
										editable.append(emoji);
									}
									currentEditView.setSelection(index
											+ emoji.length());
								}
							}
						}

						@Override
						public void onDeleteImageClicked() {

							if (currentEditView == null) {
								return;
							}
							if (!TextUtils.isEmpty(currentEditView.getText())) {
								KeyEvent event = new KeyEvent(0, 0, 0,
										KeyEvent.KEYCODE_DEL, 0, 0, 0, 0,
										KeyEvent.KEYCODE_ENDCALL);
								currentEditView.dispatchKeyEvent(event);
							}

						}
					});
		}
		if (emojiconGroupList == null) {
			emojiconGroupList = new ArrayList<EaseEmojiconGroupEntity>();
			emojiconGroupList.add(new EaseEmojiconGroupEntity(R.drawable.ee_1,
					Arrays.asList(EaseDefaultEmojiconDatas.getData())));
		}
		((EaseEmojiconMenu) emojiconMenu).init(emojiconGroupList);
	}
}
