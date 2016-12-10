package com.mingmay.bulan.app;

import com.mingmay.bulan.service.CCService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NewMessageBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {

		if (intent != null && CCApplication.mianActivity != null) {
			int type = intent.getIntExtra("type", 0);
			switch (type) {
			case 0:
				CCApplication.mianActivity.friendTabMsgTip(); // 联系人更新
				break;
			case 1: // re load unread msg count
				Intent reloadMsg = new Intent(context, CCService.class);
				reloadMsg.setAction(CCService.Action_REFRESH_MSG_CENTER);
				context.startService(reloadMsg);
				break;
			case 2:
				CCApplication.mianActivity.mineTabMsgTip(); // 联系人更新
				break;

			case 3: // 别人把我删除了，我这边需要更新会话界面
				CCApplication.mianActivity.friendTabMsgTip(); // 联系人更新
				break;
			default:
				break;
			}

		}
	}
}
