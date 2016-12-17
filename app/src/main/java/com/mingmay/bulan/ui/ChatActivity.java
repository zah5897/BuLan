package com.mingmay.bulan.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.ui.EaseBaseActivity;
import com.hyphenate.easeui.ui.EaseChatFragment;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.mingmay.bulan.MainActivity;
import com.mingmay.bulan.R;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.ui.chat.ChatFragment;
import com.mingmay.bulan.ui.group.GroupDetailsActivity;
import com.mingmay.bulan.ui.group.NewGroupActivity;
import com.mingmay.bulan.ui.group.RoomDetailsActivity;

import java.util.List;

@SuppressLint("NewApi")
public class ChatActivity extends EaseBaseActivity {
    public static ChatActivity activityInstance;
    private EaseChatFragment chatFragment;
    String toChatUsername;

    private int chatType;

    public static final int AT_RESULT_CODE = 1;
    public int currentInputIndex;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_chat);
        activityInstance = this;
        // 聊天人或群id
        toChatUsername = getIntent().getExtras().getString(
                EaseConstant.EXTRA_USER_ID);
        chatType = getIntent().getIntExtra("chatType",
                EaseConstant.CHATTYPE_SINGLE);
        CCApplication.app.currentChatUser = toChatUsername;
        chatFragment = new ChatFragment();
        chatFragment.setRightListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (chatType == EaseConstant.CHATTYPE_CHATROOM) {
                    Intent i = new Intent(ChatActivity.this,
                            RoomDetailsActivity.class);
                    i.putExtra("roomId", toChatUsername);
                    startActivityForResult(i, 1);
                } else if (chatType == EaseConstant.CHATTYPE_GROUP) {
                    Intent i = new Intent(ChatActivity.this,
                            GroupDetailsActivity.class);
                    i.putExtra("groupId", toChatUsername);
                    startActivityForResult(i, 1);
                } else {
                    Intent i = new Intent(ChatActivity.this,
                            NewGroupActivity.class);
                    i.putExtra("user_id", toChatUsername);
                    startActivity(i);
                    finish();
                }
            }
        });
        // 传入参数
        chatFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, chatFragment).commit();

        IntentFilter intentFilter = new IntentFilter("del.friend");
        intentFilter.addAction("refresh.friend");
        intentFilter.addAction("new.msg.del_back");
        // intentFilter.setPriority(3);
        registerReceiver(delFriendBroadcast, intentFilter);
//        showLiveTip();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (CCApplication.mianActivity == null) {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.putExtra("index", R.id.friends);
            startActivity(i);
        }
        unregisterReceiver(delFriendBroadcast);
        CCApplication.app.currentChatUser = null;
        activityInstance = null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // 点击notification bar进入聊天页面，保证只有一个聊天页面
        String username = intent.getStringExtra("userId");
        if (toChatUsername.equals(username))
            super.onNewIntent(intent);
        else {
            finish();
            startActivity(intent);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(chatType==EaseConstant.CHATTYPE_CHATROOM){
            EMClient.getInstance().chatManager().addMessageListener(msgListener);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        EMClient.getInstance().chatManager().removeMessageListener(msgListener);
    }

    @Override
    public void onBackPressed() {
        chatFragment.onBackPressed();
    }

    public String getToChatUsername() {
        if (chatType == EaseConstant.CHATTYPE_SINGLE) {
            return EaseUserUtils.getUserInfo(toChatUsername).getNick();
        } else {
            return toChatUsername;
        }

    }

    private BroadcastReceiver delFriendBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if ("del.friend".equals(intent.getAction())) {
                    String text = intent.getStringExtra("text");
                    if (toChatUsername.equals(text)) {
                        finish();
                    }
                } else if ("refresh.friend".equals(intent.getAction())) {
                    // chatFragment.refreshUI();
                } else if ("new.msg.del_back".equals(intent.getAction())) {
                    String from = intent.getStringExtra("from");
                    if (toChatUsername.equals(from)) {
                        // chatFragment.refreshUI();
                    }
                }

            }
        }
    };

    EMMessageListener msgListener = new EMMessageListener() {
        @Override
        public void onMessageReceived(List<EMMessage> messages) {
        }

        @Override
        public void onCmdMessageReceived(List<EMMessage> messages) {
            final EMMessage message = messages.get(messages.size() - 1);

            String action = ((EMCmdMessageBody) message.getBody()).action();
            if ("action_live".equals(action)) {
                if(donotTipLive){
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long time = message.getMsgTime();
                        long now = System.currentTimeMillis();
                        if (now - time < 5000) {
                            showLiveTip();
                        }
                    }
                });
            }
        }

        @Override
        public void onMessageReadAckReceived(List<EMMessage> messages) {
        }

        @Override
        public void onMessageDeliveryAckReceived(List<EMMessage> message) {
        }

        @Override
        public void onMessageChanged(EMMessage message, Object change) {
        }
    };

    Dialog liveTip;
    boolean  donotTipLive=false;
    private void showLiveTip() {
        if (liveTip != null && liveTip.isShowing()) {
            return;
        }
        liveTip = new android.app.AlertDialog.Builder(this).setTitle("提示").setMessage("该聊天室正在直播，是否观看").setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                donotTipLive=true;
                dialogInterface.dismiss();
            }
        }).setPositiveButton("去看看", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                EMClient.getInstance().chatManager().removeMessageListener(msgListener);
               /// Intent intent = new Intent(getBaseContext(), LiveDetailsActivity.class);
               // intent.putExtra("room_id", toChatUsername);
               // startActivity(intent);
                donotTipLive=false;
            }
        }).create();
        liveTip.show();
    }
}
