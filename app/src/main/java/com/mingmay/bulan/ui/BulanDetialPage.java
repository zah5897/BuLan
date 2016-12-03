package com.mingmay.bulan.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.mingmay.bulan.R;
import com.mingmay.bulan.ShareActivity;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.model.BuLanModel;
import com.mingmay.bulan.model.BulanEditModel;
import com.mingmay.bulan.model.CommentInfo;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.task.BuLanDetialTask;
import com.mingmay.bulan.task.CommentTask;
import com.mingmay.bulan.task.PraiseTask;
import com.mingmay.bulan.task.StoreTask;
import com.mingmay.bulan.util.TimeUtil;
import com.mingmay.bulan.util.ToastUtil;

public class BulanDetialPage extends Activity implements OnClickListener {
	private TextView bulanTitle;
	private LinearLayout contentLayout;

	private EditText commentView;
	private BuLanModel model;

	private TextView commentCountView, praseCountView;

	private TextView dateView, authorView;
	boolean hasPrase = false;
	private ProgressBar loading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_bulan_detial);
		dateView = (TextView) findViewById(R.id.date);
		authorView = (TextView) findViewById(R.id.author);
		authorView.setOnClickListener(this);
		model = (BuLanModel) getIntent().getSerializableExtra("bulan");
		if (model == null) {
			long bulanId = getIntent().getLongExtra("bulan_id", -1);
			model = new BuLanModel();
			model.bulanId = bulanId;
		}
		bulanTitle = (TextView) findViewById(R.id.bulan_title);
		contentLayout = (LinearLayout) findViewById(R.id.content_layout);

		commentCountView = (TextView) findViewById(R.id.to_comment);
		praseCountView = (TextView) findViewById(R.id.to_prase);
		commentCountView.setOnClickListener(this);
		findViewById(R.id.to_comment_layout).setOnClickListener(this);
		findViewById(R.id.to_prase_layout).setOnClickListener(this);
		praseCountView.setOnClickListener(this);

		findViewById(R.id.to_store).setOnClickListener(this);

		commentView = (EditText) findViewById(R.id.comment_input);
		commentView.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEND) {
					String commtenStr = v.getText().toString().trim();
					hideKeyboard();
					if (TextUtils.isEmpty(commtenStr)) {
						ToastUtil.show("请输入内容");
						return true;
					}
					CommentTask task = new CommentTask(BulanDetialPage.this);
					task.execute(String.valueOf(model.bulanId), commtenStr);
					return true;
				}
				return false;
			}
		});
		setInfo();
		loading=new ProgressBar(this);
		contentLayout.addView(loading);
		new BuLanDetialTask(this).execute(String.valueOf(model.bulanId));
	}

	public void back(View v) {
		finish();
	}

	public void share(View v) {
		ShareActivity.showShareWindow(this, model.bulanTitle,
				CCApplication.HTTPSERVER
						+ "/popupBulanAction!popupBulan.action?bulanId="
						+ model.bulanId, model.bulanImage);
	}

	private void setInfo() {
		bulanTitle.setText(model.bulanTitle);
		commentCountView.setText(model.commentCount + " 评论");
		praseCountView.setText(model.praiseCount + " 赞");

		if (!TextUtils.isEmpty(model.createDate)) {
			String time = TimeUtil.getTopicTime(model.createDate);
			// dateView.setText(model.createDate.substring(0, 10));
			dateView.setText(time);
		}
		String html = "来自:";
		if (TextUtils.isEmpty(model.firstName)) {
			html += "<a href=#>布栏助手</a>";
			// authorView.setText("作者:布栏助手");
		} else {
			html += "<a href=#>" + model.firstName + "</a>";
			// authorView.setText("作者:" + model.firstName);
		}
		authorView.setText(Html.fromHtml(html));
		setStoreStatus();
	}

	private void setStoreStatus() {
		ImageView store = (ImageView) findViewById(R.id.to_store);
		if (model.isCollection) {
			store.setImageResource(R.drawable.store_press);
		} else {
			store.setImageResource(R.drawable.store_selector);
		}

	}

	private void decodeItem() {
		contentLayout.removeView(loading);
		
		if (model.bulanInfo != null) {
			for (BulanEditModel element : model.bulanInfo) {
				element.addItem(this, contentLayout);
				// contentLayout.addView(element.getElementView(this), -1, -2);
			}
		} else {
			
			TextView errorTip = new TextView(this);
			errorTip.setText("出错了，貌似没有数据啊...");
			errorTip.setGravity(Gravity.CENTER);
			contentLayout.addView(errorTip, -1, -1);
		}
		
	}

	public void commentCallBack(CommentInfo info) {
		if (info != null) {
			commentView.setText("");
			model.commentCount++;
			setInfo();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.author:
			if (model.userId == UserManager.getInstance().getLoginUser().ID) {
				return;
			}
			Intent userInfo = new Intent(this, FriendInfoPage.class);
			User friend = new User();
			friend.ID = model.userId;
			friend.firstName = model.firstName;
			userInfo.putExtra("friend", friend);
			startActivity(userInfo);
			break;
		case R.id.to_comment_layout:
			Intent toComment = new Intent(this, CommentPage.class);
			toComment.putExtra("bulan", model);
			startActivity(toComment);
			break;
		case R.id.to_prase_layout:
			if (hasPrase) {
				ToastUtil.show("您已经点过了...");
				return;
			}
			hasPrase = true;
			PraiseTask pt = new PraiseTask(this);
			pt.execute(String.valueOf(model.bulanId));
			break;

		case R.id.to_store:
			if (model.isCollection) {
				StoreTask task = new StoreTask(this, true);
				task.execute(model.bulanId + "");
			} else {
				StoreTask task = new StoreTask(this);
				task.execute(model.bulanId + "");
			}

			break;
		default:
			break;
		}
	}

	public void hideKeyboard() {
		// 隐藏输入法
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		// 显示或者隐藏输入法
		imm.hideSoftInputFromWindow(commentView.getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	public void praiseCallBack(int result) {
		if (result == 0) {
			model.praiseCount++;
			setInfo();
			ToastUtil.show("点赞成功!");
		} else {
			ToastUtil.show("点赞失败");
		}
	}

	public void storeCallBack(boolean isStore) {
		model.isCollection = isStore;
		if (isStore) {
			ToastUtil.show("收藏成功");
		} else {
			ToastUtil.show("取消收藏成功");
		}
		setStoreStatus();

	}

	public void callBack(BuLanModel model2) {
		if (model2 != null) {
			this.model = model2;
			decodeItem();
			setInfo();
		}

	}
}
