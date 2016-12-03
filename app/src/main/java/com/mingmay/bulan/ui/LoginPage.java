package com.mingmay.bulan.ui;

import java.util.ArrayList;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.util.NetUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.mingmay.bulan.MainActivity;
import com.mingmay.bulan.R;
import com.mingmay.bulan.adapter.ViewPagerAdapter;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.app.listener.LoginListener;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.service.CCService;
import com.mingmay.bulan.util.ProgressDialogUtil;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpUtils;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class LoginPage extends Activity implements OnClickListener,
		OnPageChangeListener {
	private EditText username, password;

	private ViewPager viewPager;

	// 定义ViewPager适配器
	private ViewPagerAdapter vpAdapter;

	// 定义一个ArrayList来存放View
	private ArrayList<ImageView> views;

	// 引导图片资源
	private static final int[] pics = { R.drawable.frist, R.drawable.second,
			R.drawable.thrid, R.drawable.four, R.drawable.five,
			R.drawable.guide_transparent };

	// 底部小点的图片
	private ImageView[] points;

	// 记录当前选中位置
	private int currentIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		// Intent toChat=getIntent();
		// if(toChat!=null){
		// String msgId = toChat.getStringExtra("msgid");
		// // 发送方
		// String username = toChat.getStringExtra("from");
		//
		// if(msgId!=null){
		// EMMessage message = EMChatManager.getInstance().getMessage(msgId);
		// }
		// // 收到这个广播的时候，message已经在db和内存里了，可以通过id获取mesage对象
		//
		// }
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (UserManager.getInstance().getLoginUser() != null) {
			setContentView(R.layout.activity_splash);
			Intent service = new Intent(this, CCService.class);
			service.setAction(CCService.Action_UPLOAD_ALL_BULAN);
			startService(service);
			loadimg();
			return;
		} else {
			setContentView(R.layout.layout_login);
			SharedPreferences spf = getSharedPreferences("guide",
					Context.MODE_PRIVATE);
			boolean hasShow = spf.getBoolean("has_showd", false);
			if (hasShow) {
				findViewById(R.id.login_layout).setVisibility(View.VISIBLE);
				findViewById(R.id.guide_layout).setVisibility(View.GONE);
				initView();
			} else {
				findViewById(R.id.login_layout).setVisibility(View.GONE);
				findViewById(R.id.guide_layout).setVisibility(View.VISIBLE);
				initGuideView();
			}
		}
	}

	private void loadimg() {

		findViewById(R.id.skip).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				forword();
			}
		});
		String url_imag = CCApplication.HTTPSERVER
				+ "/m_recommend!findStartRecomments.action";
		HttpUtils.post(url_imag, null, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				super.onSuccess(statusCode, headers, response);
				JSONObject body = response.optJSONObject("body");
				if (body != null) {
					JSONArray recommendInfo = body
							.optJSONArray("recommendInfo");
					/**
					 * recommendId: "编号", recommendImg: "图片地址",
					 */

					if (recommendInfo != null && recommendInfo.length() > 0) {
						String recommendImg = recommendInfo.optJSONObject(0)
								.optString("recommendImg");
						ImageView imageView = (ImageView) findViewById(R.id.welcome_image);
						Glide.with(getApplicationContext()).load(recommendImg)
								.error(R.drawable.lanch).into(imageView);
					}
					toMain();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, responseString, throwable);
				toMain();
			}
		});
	}

	private void toMain() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				forword();
			}
		}, 3000);
	}

	private void forword() {
		Intent i = new Intent(LoginPage.this, MainActivity.class);

		String toChatUser = getIntent().getStringExtra(
				EaseConstant.EXTRA_USER_ID);

		if (!TextUtils.isEmpty(toChatUser)) {
			i.putExtra(EaseConstant.EXTRA_USER_ID, toChatUser);
		}

		i.putExtra("need_refresh_userinfo", true);
		startActivity(i);
		finish();
	}

	private void initView() {
		username = (EditText) findViewById(R.id.mobile);
		password = (EditText) findViewById(R.id.password);

		username.setHint(Html
				.fromHtml("<font color='#ffffff'>请输入您注册填写的手机号码</font>"));
		password.setHint(Html
				.fromHtml("<font color='#ffffff'>请输入您的登陆密码</font>"));
		String loginName = getIntent().getStringExtra("login_name");
		if (TextUtils.isEmpty(loginName)) {
			loginName = this.getSharedPreferences("login_config",
					Context.MODE_PRIVATE).getString("username", null);
			if (loginName != null) {
				username.setText(loginName);
			}
		} else {
			username.setText(loginName);
			password.setText("");
		}
		findViewById(R.id.to_login).setOnClickListener(this);
		findViewById(R.id.forget_password).setOnClickListener(this);
		findViewById(R.id.to_regist).setOnClickListener(this);
		findViewById(R.id.login_layout).setOnClickListener(this);
	}

	private void initGuideView() {
		// 实例化ArrayList对象
		views = new ArrayList<ImageView>();

		// 实例化ViewPager
		viewPager = (ViewPager) findViewById(R.id.viewpager);

		// 实例化ViewPager适配器
		vpAdapter = new ViewPagerAdapter(this, views);

		LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);

		// 初始化引导图片列表
		for (int i = 0; i < pics.length; i++) {
			ImageView iv = new ImageView(this);
			iv.setLayoutParams(mParams);
			// iv.setImageResource(pics[i]);
			iv.setScaleType(ScaleType.FIT_XY);
			views.add(iv);
		}

		// 设置数据
		viewPager.setAdapter(vpAdapter);
		// 设置监听
		viewPager.setOnPageChangeListener(this);

		// 初始化底部小点
		initPoint();
	}

	private void initPoint() {
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ll);

		points = new ImageView[pics.length - 1];

		// 循环取得小点图片
		for (int i = 0; i < pics.length - 1; i++) {
			// 得到一个LinearLayout下面的每一个子元素
			points[i] = (ImageView) linearLayout.getChildAt(i);
			// 默认都设为灰色
			points[i].setEnabled(true);
			// 给每个小点设置监听
			points[i].setOnClickListener(this);
			// 设置位置tag，方便取出与当前位置对应
			points[i].setTag(i);
		}

		// 设置当面默认的位置
		currentIndex = 0;
		// 设置为白色，即选中状态
		points[currentIndex].setEnabled(false);
	}

	private void login() {
		String name = username.getText().toString();
		String pwd = password.getText().toString();
		if (TextUtils.isEmpty(name)) {
			ToastUtil.show("账号不能为空！");
			return;
		}
		if (TextUtils.isEmpty(pwd)) {
			ToastUtil.show("密码不能为空！");
			return;
		}
		
		
		if(!NetUtils.isWifiConnection(getApplicationContext())){
			ToastUtil.show("当前网络不可用");
			return;
		}
		UserManager.getInstance().login(name, pwd, new LoginListener() {
			@Override
			public void onSuccess(User user) {
				runOnUiThread(new Runnable() {
					public void run() {
						ProgressDialogUtil.dismiss();
						ToastUtil.show("登录成功");
						Intent i = new Intent(LoginPage.this,
								MainActivity.class);
						LoginPage.this.startActivity(i);
						finish();
					}
				});
			}

			@Override
			public void onFailure(final int code) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ProgressDialogUtil.dismiss();
						switch (code) {
						case 1:
							ToastUtil.show("登陆失败，密码错误");
							break;
						case 2:
							ToastUtil.show("登陆错误,参数异常");
							break;
						case 3:
							ToastUtil.show("账号不存在");
							break;
						case 4:
							ToastUtil.show("账号被锁定");
							break;
						case 5:
							ToastUtil.show("账号连续错误登陆多次");
							break;
						case 6:
							ToastUtil.show("用户被禁用");
							break;
						default:
							ToastUtil.show("系统异常，请稍后");
							break;
						}

					}
				});
			}
		});
		ProgressDialogUtil.showProgress(this, "正在登陆...");
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.to_login:
			login();
			break;
		case R.id.to_regist:
			Intent toRegist = new Intent(this, RegistPage.class);
			startActivity(toRegist);
			break;
		case R.id.forget_password:
			Intent forgetPassword = new Intent(this, ForgetPassword.class);
			startActivity(forgetPassword);
			break;
		case R.id.login_layout:

			View v = getCurrentFocus();
			if (v != null) {
				((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
						.hideSoftInputFromWindow(getCurrentFocus()
								.getWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);
			}

			break;
		default:
			break;
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float positionOffset, int arg2) {
		// TODO Auto-generated method stub
		if (arg0 == 5) {
			Log.d("mm", positionOffset + "");
			views.get(arg0).setAlpha(1 - positionOffset);
		}
	}

	@Override
	public void onPageSelected(int position) {
		// TODO Auto-generated method stub
		setCurDot(position);
		if (position == pics.length - 1) {
			findViewById(R.id.guide_layout).setVisibility(View.GONE);
			// findViewById(R.id.login_layout).setVisibility(View.VISIBLE);
			initView();
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					TranslateAnimation mShowAction = new TranslateAnimation(
							Animation.RELATIVE_TO_SELF, 0.0f,
							Animation.RELATIVE_TO_SELF, 0.0f,
							Animation.RELATIVE_TO_SELF, -1.0f,
							Animation.RELATIVE_TO_SELF, 0.0f);
					mShowAction.setDuration(500);

					findViewById(R.id.login_layout).startAnimation(mShowAction);
					findViewById(R.id.login_layout).setVisibility(View.VISIBLE);

					SharedPreferences spf = getSharedPreferences("guide",
							Context.MODE_PRIVATE);
					spf.edit().putBoolean("has_showd", true).commit();
				}
			}, 200);

		}

	}

	@SuppressWarnings("unused")
	private void setCurView(int position) {
		if (position < 0 || position >= pics.length) {
			return;
		}
		viewPager.setCurrentItem(position);
	}

	private void setCurDot(int positon) {
		if (positon < 0 || positon > pics.length - 2 || currentIndex == positon) {
			return;
		}
		points[positon].setEnabled(false);
		points[currentIndex].setEnabled(true);

		currentIndex = positon;
	}
}
