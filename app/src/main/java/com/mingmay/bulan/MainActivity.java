package com.mingmay.bulan;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.hyphenate.EMConnectionListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.EaseConstant;
import com.mingmay.bulan.app.AppContent;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.NewMessageBroadcastReceiver;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.service.CCService;
import com.mingmay.bulan.task.CheckNewVersionTask;
import com.mingmay.bulan.ui.ChatActivity;
import com.mingmay.bulan.ui.LoginPage;
import com.mingmay.bulan.ui.fragment.FriendFragment;
import com.mingmay.bulan.ui.fragment.MineFragment;
import com.mingmay.bulan.ui.fragment.TextCreatorPage;
import com.mingmay.bulan.ui.fragment.TopicFragment;
import com.mingmay.bulan.ui.fragment.TypeFragment;
import com.mingmay.bulan.util.PropertyUtil;
import com.mingmay.bulan.util.ToastUtil;

public class MainActivity extends FragmentActivity implements OnClickListener {
    private FragmentManager fragmentManager;

    private TopicFragment topicFragment;
    private TypeFragment typeFragment;
    public FriendFragment friendFragment;
    private MineFragment mineFragment;

    private int currentPageIndex = 0;
    public ImageView unReadMsgTip, noReadTip;

    NewMessageBroadcastReceiver msgReceiver;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CCApplication) getApplication()).mianActivity = this;
        setContentView(R.layout.layout_main);
        DisplayMetrics d = getResources().getDisplayMetrics();
        CCApplication.screenWidth = d.widthPixels;
        CCApplication.screenHeight = d.heightPixels;
        CCApplication.density = d.density;
        AppContent.getInstance(this.getApplicationContext());
        fragmentManager = getSupportFragmentManager();
        setListener();
        selectInstanceByIndex(R.id.topic);
        unReadMsgTip = (ImageView) findViewById(R.id.un_read_msg_tip);
        noReadTip = (ImageView) findViewById(R.id.no_read_msg_tip);

        String toChatUser = getIntent().getStringExtra(
                EaseConstant.EXTRA_USER_ID);
        if (!TextUtils.isEmpty(toChatUser)) {
            Intent i = new Intent(this, ChatActivity.class);
            i.putExtra(EaseConstant.EXTRA_USER_ID, toChatUser);
            startActivity(i);
        }

        notifyReceiver();
        startService(new Intent(this, CCService.class)
                .setAction(CCService.Action_INIT));
        startService(new Intent(this, CCService.class)
                .setAction(CCService.Action_UPLOAD_ALL_BULAN));
        EMClient.getInstance()
                .addConnectionListener(myConnectionListener);
        int index = getIntent().getIntExtra("index", -1);
        if (index > 0) {
            selectInstanceByIndex(index);
        }
        new CheckNewVersionTask(this).execute();
    }

    private RadioButton topic, type, creator, friends, mine;

    private void setListener() {
        topic = (RadioButton) findViewById(R.id.topic);
        type = (RadioButton) findViewById(R.id.type);
        creator = (RadioButton) findViewById(R.id.creator);
        friends = (RadioButton) findViewById(R.id.friends);
        mine = (RadioButton) findViewById(R.id.mine);

        topic.setOnClickListener(this);
        type.setOnClickListener(this);
        creator.setOnClickListener(this);
        friends.setOnClickListener(this);
        mine.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (R.id.creator == v.getId()) {
            Intent toCreate = new Intent(this, TextCreatorPage.class);
            long lastLocalId = PropertyUtil.getLongValue("last_edit_local_id");
            toCreate.putExtra("localId", lastLocalId);
            startActivity(toCreate);
            creator.setChecked(false);
            return;
        }
        selectInstanceByIndex(v.getId());
    }

    private void notifyReceiver() {
        msgReceiver = new NewMessageBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter("new.msg.tip");
        registerReceiver(msgReceiver, intentFilter);
    }

    long lastTime;

    @Override
    public void onBackPressed() {

        if (System.currentTimeMillis() - lastTime <= 1000) {
            unregisterReceiver(msgReceiver);
            super.onBackPressed();
        } else {
            ToastUtil.show("再按一次退出布栏");
            lastTime = System.currentTimeMillis();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CCApplication.mianActivity = null;
    }

    public void friendTabMsgTip() {
        int unreadCount = EMClient.getInstance().chatManager()
                .getUnreadMsgsCount();
        int totalCount = CCApplication.friendCount + unreadCount;
        if (totalCount > 0) {
            unReadMsgTip.setVisibility(View.VISIBLE);
        } else {
            unReadMsgTip.setVisibility(View.GONE);
        }
        if (friendFragment != null) {
            friendFragment.checkRefresh();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        friendTabMsgTip();
        mineTabMsgTip();

        if (friendFragment != null && currentPageIndex == R.id.friends) {
            friendFragment.setUpView();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getBooleanExtra("relogin", false)) {
            CCApplication.mianActivity = null;
            Intent toLogin = new Intent(MainActivity.this, LoginPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }

        ((RadioButton) findViewById(R.id.topic)).setChecked(true);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (currentPageIndex == R.id.type) {
            typeFragment.onActivityResult(requestCode, resultCode, data);
        } else if (currentPageIndex == R.id.mine) {
            mineFragment.onActivityResult(requestCode, resultCode, data);
        } else if (currentPageIndex == R.id.friends) {
            friendFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void resetTab() {
        topic.setChecked(false);
        type.setChecked(false);
        creator.setChecked(false);
        friends.setChecked(false);
        mine.setChecked(false);
    }

    public void selectInstanceByIndex(int index) {
        if (currentPageIndex == index) {
            return;
        }
        resetTab();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideFragments(transaction);
        switch (index) {
            case R.id.topic:
                topic.setChecked(true);
                if (topicFragment == null) {
                    topicFragment = new TopicFragment();
                    transaction.add(R.id.content, topicFragment);
                } else {
                    transaction.show(topicFragment);
                }

                break;
            case R.id.type:
                type.setChecked(true);
                if (typeFragment == null) {
                    typeFragment = new TypeFragment();
                    transaction.add(R.id.content, typeFragment);

                } else {
                    transaction.show(typeFragment);
                }

                break;

            case R.id.friends:
                friends.setChecked(true);
                if (friendFragment == null) {
                    friendFragment = new FriendFragment();
                    transaction.add(R.id.content, friendFragment);
                } else {
                    transaction.show(friendFragment);
                }
                friendFragment.checkRefresh();

                break;
            case R.id.mine:
                mine.setChecked(true);
                if (mineFragment == null) {
                    mineFragment = new MineFragment();
                    transaction.add(R.id.content, mineFragment);
                } else {
                    transaction.show(mineFragment);
                }

                break;
        }
        transaction.commit();
        currentPageIndex = index;
    }

    private void hideFragments(FragmentTransaction transaction) {
        if (topicFragment != null) {
            transaction.hide(topicFragment);
        }
        if (typeFragment != null) {
            transaction.hide(typeFragment);
        }
        if (friendFragment != null) {
            transaction.hide(friendFragment);
        }

        if (mineFragment != null) {
            transaction.hide(mineFragment);
        }
    }

    // public void callback(int count) {
    // if (count > 0) {
    // tip.setVisibility(View.VISIBLE);
    // } else {
    // tip.setVisibility(View.GONE);
    // }
    // }

    // 实现ConnectionListener接口
    private EMConnectionListener myConnectionListener = new EMConnectionListener() {
        @Override
        public void onConnected() {
            EMClient.getInstance().chatManager().loadAllConversations();
        }

        @Override
        public void onDisconnected(int i) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 显示帐号在其他设备登陆
                    EMClient.getInstance().removeConnectionListener(myConnectionListener);

                    UserManager.getInstance().logout();
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.putExtra("relogin", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                }
            });
        }

    };

    public void mineTabMsgTip() {

        if (CCApplication.getTotalCount() > 0) {
            noReadTip.setVisibility(View.VISIBLE);
        } else {
            noReadTip.setVisibility(View.GONE);
        }
        if (mineFragment != null) {
            mineFragment.refreshCount();
        }
    }

    public void resreshMine() {
        if (mineFragment != null) {
            mineFragment.setUserInfo();
        }
    }
}
