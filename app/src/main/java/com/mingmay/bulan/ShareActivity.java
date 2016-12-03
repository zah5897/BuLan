package com.mingmay.bulan;

import java.io.File;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.framework.utils.UIHandler;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.util.FileUtil;
import com.mingmay.bulan.util.ImageCache;
import com.mingmay.bulan.util.MD5Util;

public class ShareActivity {
	static ImageCache imageCache;
	public static String shareName = "";

	public static void showShareWindow(Activity context, String shareContent,
			String url, String iconUrl) {
		shareName = shareContent;
		final AlertDialog dialog = new AlertDialog.Builder(context).create();
		Window window = dialog.getWindow();
		window.setGravity(Gravity.BOTTOM | Gravity.LEFT); // 此处可以设置dialog显示的位置
		window.setWindowAnimations(R.style.bottomDialogStyle); // 添加动画

		// dialog.setView(view,0,0,0,0);
		dialog.show();
		dialog.setContentView(R.layout.dialog_share);
		LayoutParams windowParams = window.getAttributes();
		if (windowParams == null) {
			windowParams = new LayoutParams();
		}
		WindowManager windowManager = context.getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		dialog.getWindow().setLayout(display.getWidth(),
				LayoutParams.WRAP_CONTENT);

		dialog.findViewById(R.id.s_weixin_friend)
				.setOnClickListener(
						new MenuOnClick(context, shareName, shareContent, url,
								iconUrl));
		dialog.findViewById(R.id.s_weixin_friend0)
				.setOnClickListener(
						new MenuOnClick(context, shareName, shareContent, url,
								iconUrl));
		dialog.findViewById(R.id.s_sina)
				.setOnClickListener(
						new MenuOnClick(context, shareName, shareContent, url,
								iconUrl));
		dialog.findViewById(R.id.cancel).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
	}

	public static void shareToWeixinFriend(Activity context, String title,
			String content, String url, String iconUrl) {
		Platform plat = null;
		ShareParams sp = new ShareParams();
		sp.setTitle(title);
		sp.setText(content);

		sp.setShareType(Platform.SHARE_WEBPAGE);
		sp.setUrl(url);
		imageCache = ImageCache.getInstance();
		if (!TextUtils.isEmpty(iconUrl)) {
			String imageUrlMD5 = MD5Util.toMd5(iconUrl.getBytes());
			Bitmap bitmap = imageCache.getMemoryCache(imageUrlMD5);
			if (bitmap != null) {
				sp.setImageData(bitmap);
			} else {
				File cacheFile = FileUtil.createAppCacheDir();
				File imageFile = new File(cacheFile, imageUrlMD5);
				bitmap = imageCache.decodeSimpleBitMapFromResource(
						imageFile.getAbsolutePath(),
						(int) (50 * CCApplication.density));
				if (bitmap != null) {
					imageCache.addBitmapToMemoryCache(imageUrlMD5, bitmap);
					sp.setImageData(bitmap);
				} else {
					sp.setImageData(BitmapFactory.decodeResource(
							context.getResources(), R.drawable.ic_launcher));
				}
			}
		} else {
			sp.setImageData(BitmapFactory.decodeResource(
					context.getResources(), R.drawable.ic_launcher));
		}
		plat = ShareSDK.getPlatform("Wechat");
		plat.setPlatformActionListener(new ShareListener(context));
		plat.share(sp);
	}

	private static void shareToWeixinFriend0(Activity context, String title,
			String content, String url, String iconUrl) {

		Platform plat = null;
		ShareParams sp = new ShareParams();
		sp.setTitle(title);
		sp.setText(content);

		sp.setShareType(Platform.SHARE_WEBPAGE);
		sp.setUrl(url);
		// 布栏图片
		imageCache = ImageCache.getInstance();
		if (!TextUtils.isEmpty(iconUrl)) {
			String imageUrlMD5 = MD5Util.toMd5(iconUrl.getBytes());
			Bitmap bitmap = imageCache.getMemoryCache(imageUrlMD5);
			if (bitmap != null) {
				sp.setImageData(bitmap);
			} else {
				File cacheFile = FileUtil.createAppCacheDir();
				File imageFile = new File(cacheFile, imageUrlMD5);
				bitmap = imageCache.decodeSimpleBitMapFromResource(
						imageFile.getAbsolutePath(),
						(int) (50 * CCApplication.density));
				if (bitmap != null) {
					imageCache.addBitmapToMemoryCache(imageUrlMD5, bitmap);
					sp.setImageData(bitmap);
				} else {
					sp.setImageData(BitmapFactory.decodeResource(
							context.getResources(), R.drawable.ic_launcher));
				}
			}
		} else {
			sp.setImageData(BitmapFactory.decodeResource(
					context.getResources(), R.drawable.ic_launcher));
		}

		plat = ShareSDK.getPlatform("WechatMoments");
		plat.setPlatformActionListener(new ShareListener(context));
		plat.share(sp);
	}

	/** 将action转换为String */
	public static String actionToString(int action) {
		switch (action) {
		case Platform.ACTION_AUTHORIZING:
			return "ACTION_AUTHORIZING";
		case Platform.ACTION_GETTING_FRIEND_LIST:
			return "ACTION_GETTING_FRIEND_LIST";
		case Platform.ACTION_FOLLOWING_USER:
			return "ACTION_FOLLOWING_USER";
		case Platform.ACTION_SENDING_DIRECT_MESSAGE:
			return "ACTION_SENDING_DIRECT_MESSAGE";
		case Platform.ACTION_TIMELINE:
			return "ACTION_TIMELINE";
		case Platform.ACTION_USER_INFOR:
			return "ACTION_USER_INFOR";
		case Platform.ACTION_SHARE:
			return "ACTION_SHARE";
		default: {
			return "UNKNOWN";
		}
		}
	}

	/**
	 * ShareSDK集成方法有两种</br>
	 * 1、第一种是引用方式，例如引用onekeyshare项目，onekeyshare项目再引用mainlibs库</br>
	 * 2、第二种是把onekeyshare和mainlibs集成到项目中，本例子就是用第二种方式</br> 请看“ShareSDK
	 * 使用说明文档”，SDK下载目录中 </br> 或者看网络集成文档
	 * http://wiki.mob.com/Android_%E5%BF%AB%E9%
	 * 80%9F%E9%9B%86%E6%88%90%E6%8C%87%E5%8D%97
	 * 3、混淆时，把sample或者本例子的混淆代码copy过去，在proguard-project.txt文件中
	 * 
	 * 
	 * 平台配置信息有三种方式： 1、在我们后台配置各个微博平台的key
	 * 2、在代码中配置各个微博平台的key，http://mob.com/androidDoc
	 * /cn/sharesdk/framework/ShareSDK.html
	 * 3、在配置文件中配置，本例子里面的assets/ShareSDK.conf,
	 */
	private static void showShare(Activity context, boolean silent,
			String platform, boolean captureView, String content, String url,
			String iconUrl) {
		final OnekeyShare oks = new OnekeyShare();

		oks.setNotification(R.drawable.ic_launcher,
				context.getString(R.string.app_name));
		// oks.setAddress("12345678901");
		oks.setTitle("布栏");
		String customText = content;
		oks.setText(customText);
		oks.setUrl(url);
		oks.setSilent(silent);
		oks.setShareFromQQAuthSupport(true);
		if (platform != null) {
			oks.setPlatform(platform);
		}

		// 令编辑页面显示为Dialog模式
		oks.setDialogMode();

		// 在自动授权时可以禁用SSO方式
		// if(!shareFromQQLogin)
		// oks.disableSSOWhenAuthorize();

		// 去除注释，则快捷分享的操作结果将通过OneKeyShareCallback回调
		// oks.setCallback(new OneKeyShareCallback());

		// 去自定义不同平台的字段内容
		oks.setShareContentCustomizeCallback(new ShareContentCustomizeDemo());

		// 去除注释，演示在九宫格设置自定义的图标
		// Bitmap logo = BitmapFactory.decodeResource(menu.getResources(),
		// R.drawable.ic_launcher);
		// String label = menu.getResources().getString(R.string.app_name);
		// OnClickListener listener = new OnClickListener() {
		// public void onClick(View v) {
		// String text = "Customer Logo -- ShareSDK " +
		// ShareSDK.getSDKVersionName();
		// Toast.makeText(menu.getContext(), text, Toast.LENGTH_SHORT).show();
		// oks.finish();
		// }
		// };
		// oks.setCustomerLogo(logo, label, listener);

		// 去除注释，则快捷分享九宫格中将隐藏新浪微博和腾讯微博
		// oks.addHiddenPlatform(SinaWeibo.NAME);
		// oks.addHiddenPlatform(TencentWeibo.NAME);

		// 为EditPage设置一个背景的View
		// oks.setEditPageBackground(findViewById(R.id.back));

		// 设置kakaoTalk分享链接时，点击分享信息时，如果应用不存在，跳转到应用的下载地址
		oks.setInstallUrl("http://www.mob.com");
		// 设置kakaoTalk分享链接时，点击分享信息时，如果应用存在，打开相应的app
		oks.setExecuteUrl("kakaoTalkTest://starActivity");

		oks.show(context);
	}

	private static class MenuOnClick implements OnClickListener {

		private Activity context;

		private String title;

		private String content;

		private String url;
		private String iconUrl;

		public MenuOnClick(Activity context, String title, String content,
				String url, String iconUrl) {
			super();
			this.title = title;
			this.context = context;
			this.content = content;
			this.url = url;
			this.iconUrl = iconUrl;
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.s_weixin_friend:
				shareToWeixinFriend(context, title, content, url, iconUrl);
				break;
			case R.id.s_weixin_friend0:
				shareToWeixinFriend0(context, title, content, url, iconUrl);
				break;
			case R.id.s_sina:
				showShare(context, false, "SinaWeibo", false, content, url,
						iconUrl);
				break;
			case R.id.cancel:
				break;

			default:
				break;
			}
		}

	}

	private static class ShareListener implements PlatformActionListener,
			Callback {

		private Activity context;

		public ShareListener(Activity context) {
			super();
			this.context = context;
		}

		public void onComplete(Platform plat, int action,
				HashMap<String, Object> res) {
			Message msg = new Message();
			msg.arg1 = 1;
			msg.arg2 = action;
			msg.obj = plat;
			UIHandler.sendMessage(msg, this);
		}

		public void onCancel(Platform plat, int action) {
			Message msg = new Message();
			msg.arg1 = 3;
			msg.arg2 = action;
			msg.obj = plat;
			UIHandler.sendMessage(msg, this);
		}

		public void onError(Platform plat, int action, Throwable t) {
			t.printStackTrace();
			Message msg = new Message();
			msg.arg1 = 2;
			msg.arg2 = action;
			msg.obj = t;
			UIHandler.sendMessage(msg, this);
		}

		public boolean handleMessage(Message msg) {
			String text = actionToString(msg.arg2);
			switch (msg.arg1) {
			case 1: {
				// 成功
				Platform plat = (Platform) msg.obj;
				text = "分享成功";
			}
				break;
			case 2: {
				// 失败
				if ("WechatClientNotExistException".equals(msg.obj.getClass()
						.getSimpleName())) {
					text = "分享失败";
				} else if ("WechatTimelineNotSupportedException".equals(msg.obj
						.getClass().getSimpleName())) {
					text = "分享失败";
				} else {
					text = "分享失败";
				}
			}
				break;
			case 3: {
				// 取消
				Platform plat = (Platform) msg.obj;
				text = plat.getName() + " canceled at " + text;
			}
				break;
			}

			Toast.makeText(context, text, Toast.LENGTH_LONG).show();
			return false;
		}
	}
}
