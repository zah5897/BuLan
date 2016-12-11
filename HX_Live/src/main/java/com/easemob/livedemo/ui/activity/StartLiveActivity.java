package com.easemob.livedemo.ui.activity;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.easemob.livedemo.R;
import com.easemob.livedemo.data.model.LiveSettings;
import com.easemob.livedemo.ui.widget.BarrageLayout;
import com.easemob.livedemo.ui.widget.LiveLeftGiftView;
import com.easemob.livedemo.ui.widget.PeriscopeLayout;
import com.easemob.livedemo.ui.widget.RoomMessagesView;
import com.easemob.livedemo.utils.Log2FileUtil;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.controller.EaseUI;
import com.ucloud.common.util.DeviceUtils;
import com.ucloud.live.UEasyStreaming;
import com.ucloud.live.UStreamingProfile;
import com.ucloud.live.widget.UAspectFrameLayout;

import java.util.Random;

public class StartLiveActivity extends LiveBaseActivity
        implements UEasyStreaming.UStreamingStateListener {
    private static final String TAG = StartLiveActivity.class.getSimpleName();
    LinearLayout toolbar;
    UAspectFrameLayout mPreviewContainer;
    RelativeLayout startContainer;
    TextView countdownView;
    TextView usernameView;
    Button startBtn;
    ViewStub liveEndLayout;
    ImageView coverImage;
    ImageButton lightSwitch;
    ImageButton voiceSwitch;

    protected UEasyStreaming mEasyStreaming;
    protected String rtmpPushStreamDomain = "publish3.cdn.ucloud.com.cn";
    public static final int MSG_UPDATE_COUNTDOWN = 1;

    public static final int COUNTDOWN_DELAY = 1000;

    public static final int COUNTDOWN_START_INDEX = 3;
    public static final int COUNTDOWN_END_INDEX = 1;
    protected boolean isShutDownCountdown = false;
    private LiveSettings mSettings;
    private UStreamingProfile mStreamingProfile;
    UEasyStreaming.UEncodingType encodingType;

    boolean isStarted;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_COUNTDOWN:
                    handleUpdateCountdown(msg.arg1);
                    break;
            }
        }
    };

    //203138620012364216
    @Override
    protected void onActivityCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_start_live);
        initView();
        liveId = "bulan";
        chatroomId = getIntent().getStringExtra("room_id");
        anchorId = EMClient.getInstance().getCurrentUser();
        usernameView.setText(anchorId);
        initEnv();
    }


    private void initView() {
        toolbar = (LinearLayout) findViewById(R.id.toolbar);
        mPreviewContainer = (UAspectFrameLayout) findViewById(R.id.container);
        startContainer = (RelativeLayout) findViewById(R.id.start_container);
        countdownView = (TextView) findViewById(R.id.countdown_txtv);
        usernameView = (TextView) findViewById(R.id.tv_username);
        startBtn = (Button) findViewById(R.id.btn_start);
        liveEndLayout = (ViewStub) findViewById(R.id.finish_frame);
        coverImage = (ImageView) findViewById(R.id.cover_image);
        lightSwitch = (ImageButton) findViewById(R.id.img_bt_switch_light);
        voiceSwitch = (ImageButton) findViewById(R.id.img_bt_switch_voice);


        leftGiftView = (LiveLeftGiftView) findViewById(R.id.left_gift_view1);
        leftGiftView2 = (LiveLeftGiftView) findViewById(R.id.left_gift_view2);
        messageView = (RoomMessagesView) findViewById(R.id.message_view);
        periscopeLayout = (PeriscopeLayout) findViewById(R.id.periscope_layout);
        bottomBar = findViewById(R.id.bottom_bar);

        barrageLayout = (BarrageLayout) findViewById(R.id.barrage_layout);
        horizontalRecyclerView = (RecyclerView) findViewById(R.id.horizontal_recycle_view);
        audienceNumView = (TextView) findViewById(R.id.audience_num);
        newMsgNotifyImage = (ImageView) findViewById(R.id.new_messages_warn);

        findViewById(R.id.img_bt_switch_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchCamera();
            }
        });

        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLive();
            }
        });
        findViewById(R.id.img_bt_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeLive();
            }
        });

        findViewById(R.id.img_bt_switch_voice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMicrophone();
            }
        });

        findViewById(R.id.img_bt_switch_light).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchLight();
            }
        });

        findViewById(R.id.root_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRootLayoutClick();
            }
        });
        findViewById(R.id.comment_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCommentImageClick();
            }
        });
        findViewById(R.id.present_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPresentImageClick();
            }
        });
        findViewById(R.id.chat_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onChatImageClick();
            }
        });
        findViewById(R.id.screenshot_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onScreenshotImageClick();
            }
        });
    }

    public void initEnv() {
        mSettings = new LiveSettings(this);
        if (mSettings.isOpenLogRecoder()) {
            Log2FileUtil.getInstance().setLogCacheDir(mSettings.getLogCacheDir());
            Log2FileUtil.getInstance().startLog(); //
        }

        //        UStreamingProfile.Stream stream = new UStreamingProfile.Stream(rtmpPushStreamDomain, "ucloud/" + mSettings.getPusblishStreamId());
        //hardcode
        UStreamingProfile.Stream stream =
                new UStreamingProfile.Stream(rtmpPushStreamDomain, "ucloud/" + liveId);

        mStreamingProfile =
                new UStreamingProfile.Builder().setVideoCaptureWidth(mSettings.getVideoCaptureWidth())
                        .setVideoCaptureHeight(mSettings.getVideoCaptureHeight())
                        .setVideoEncodingBitrate(
                                mSettings.getVideoEncodingBitRate()) //UStreamingProfile.VIDEO_BITRATE_NORMAL
                        .setVideoEncodingFrameRate(mSettings.getVideoFrameRate())
                        .setStream(stream)
                        .build();

        encodingType = UEasyStreaming.UEncodingType.MEDIA_X264;
        if (DeviceUtils.hasJellyBeanMr2()) {
            encodingType = UEasyStreaming.UEncodingType.MEDIA_CODEC;
        }
        mEasyStreaming = new UEasyStreaming(this, encodingType);
        mEasyStreaming.setStreamingStateListener(this);
        mEasyStreaming.setAspectWithStreamingProfile(mPreviewContainer, mStreamingProfile);
    }

    @Override
    public void onStateChanged(int type, Object event) {
        switch (type) {
            case UEasyStreaming.State.MEDIA_INFO_SIGNATRUE_FAILED:
                Toast.makeText(this, event.toString(), Toast.LENGTH_LONG).show();
                break;
            case UEasyStreaming.State.START_RECORDING:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (!isFinishing()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    periscopeLayout.addHeart();
                                }
                            });
                            try {
                                Thread.sleep(new Random().nextInt(400) + 200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        mEasyStreaming.stopRecording();
        super.onBackPressed();
    }

    /**
     * 切换摄像头
     */
    void switchCamera() {
        mEasyStreaming.switchCamera();
    }

    /**
     * 开始直播
     */
    void startLive() {
        //demo为了测试方便，只有指定的账号才能开启直播
        startContainer.setVisibility(View.INVISIBLE);
        //Utils.hideKeyboard(titleEdit);
        new Thread() {
            public void run() {
                int i = COUNTDOWN_START_INDEX;
                do {
                    Message msg = Message.obtain();
                    msg.what = MSG_UPDATE_COUNTDOWN;
                    msg.arg1 = i;
                    handler.sendMessage(msg);
                    i--;
                    try {
                        Thread.sleep(COUNTDOWN_DELAY);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (i >= COUNTDOWN_END_INDEX);
            }
        }.start();
    }

    /**
     * 关闭直播显示直播成果
     */
    void closeLive() {
        mEasyStreaming.stopRecording();
        if (!isStarted) {
            finish();
            return;
        }
        showConfirmCloseLayout();
    }

    void toggleMicrophone() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.isMicrophoneMute()) {
            audioManager.setMicrophoneMute(false);
            voiceSwitch.setSelected(false);
        } else {
            audioManager.setMicrophoneMute(true);
            voiceSwitch.setSelected(true);
        }
    }

    private void showConfirmCloseLayout() {
        //显示封面
        coverImage.setVisibility(View.VISIBLE);
        coverImage.setImageResource(R.drawable.ease_ic_launcher);
        View view = liveEndLayout.inflate();
        Button closeConfirmBtn = (Button) view.findViewById(R.id.live_close_confirm);
        TextView usernameView = (TextView) view.findViewById(R.id.tv_username);
        usernameView.setText(EMClient.getInstance().getCurrentUser());
        closeConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void confirmClose() {

    }

    /**
     * 打开或关闭闪关灯
     */
    void switchLight() {
        boolean succeed = mEasyStreaming.toggleFlashMode();
        if (succeed) {
            if (lightSwitch.isSelected()) {
                lightSwitch.setSelected(false);
            } else {
                lightSwitch.setSelected(true);
            }
        }
    }

    @Override
    void onChatImageClick() {
//        ConversationListFragment fragment = ConversationListFragment.newInstance(null, false);
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.message_container, fragment)
//                .commit();
    }

    protected void setListItemClickListener() {
    }

    public void handleUpdateCountdown(final int count) {
        if (countdownView != null) {
            countdownView.setVisibility(View.VISIBLE);
            countdownView.setText(String.format("%d", count));
            ScaleAnimation scaleAnimation =
                    new ScaleAnimation(1.0f, 0f, 1.0f, 0f, Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(COUNTDOWN_DELAY);
            scaleAnimation.setFillAfter(false);
            enterRoom();//同步进行
            scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    countdownView.setVisibility(View.GONE);
                    enterRoom();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            if (!isShutDownCountdown) {
                countdownView.startAnimation(scaleAnimation);
            } else {
                countdownView.setVisibility(View.GONE);
            }
        }
    }



    private void enterRoom(){
        EMClient.getInstance()
                .chatroomManager()
                .joinChatRoom(chatroomId, new EMValueCallBack<EMChatRoom>() {
                    @Override
                    public void onSuccess(EMChatRoom emChatRoom) {
                        chatroom = emChatRoom;
                        addChatRoomChangeListenr();
                        onMessageListInit();
                        animationEndToStartLive();
                    }

                    @Override
                    public void onError(int i, String s) {
                        showToast("加入聊天室失败");


                    }
                });
    }

    private  void animationEndToStartLive(){
        if ( mEasyStreaming != null && !isShutDownCountdown) {
            showToast("直播开始！");
            mEasyStreaming.startRecording();
            isStarted = true;
        }else{
            showToast("当前无法直播");
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        mEasyStreaming.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEasyStreaming.onResume();
        if (isMessageListInited) messageView.refresh();
        EaseUI.getInstance().pushActivity(this);
        // register the event listener when enter the foreground
        EMClient.getInstance().chatManager().addMessageListener(msgListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister this event listener when this activity enters the
        // background
        EMClient.getInstance().chatManager().removeMessageListener(msgListener);

        // 把此activity 从foreground activity 列表里移除
        EaseUI.getInstance().popActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSettings.isOpenLogRecoder()) {
            Log2FileUtil.getInstance().stopLog();
        }
        mEasyStreaming.onDestroy();

        EMClient.getInstance().chatroomManager().leaveChatRoom(chatroomId);

        if (chatRoomChangeListener != null) {
            EMClient.getInstance().chatroomManager().removeChatRoomChangeListener(chatRoomChangeListener);
        }
    }
}
