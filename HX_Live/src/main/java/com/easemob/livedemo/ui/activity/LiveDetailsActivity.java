package com.easemob.livedemo.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.livedemo.R;
import com.easemob.livedemo.data.model.LiveRoom;
import com.easemob.livedemo.ui.widget.BarrageLayout;
import com.easemob.livedemo.ui.widget.LiveLeftGiftView;
import com.easemob.livedemo.ui.widget.PeriscopeLayout;
import com.easemob.livedemo.ui.widget.RoomMessagesView;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.controller.EaseUI;
import com.ucloud.common.logger.L;
import com.ucloud.player.widget.v2.UVideoView;

import java.util.Random;


public class LiveDetailsActivity extends LiveBaseActivity implements UVideoView.Callback {

    String rtmpPlayStreamUrl = "rtmp://vlive3.rtmp.cdn.ucloud.com.cn/ucloud/";
    private UVideoView mVideoView;

    RelativeLayout loadingLayout;
    ProgressBar progressBar;
    TextView loadingText;
    ImageView coverView;
    TextView usernameView;


    private void initView() {
        loadingLayout = (RelativeLayout) findViewById(R.id.loading_layout);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        loadingText = (TextView) findViewById(R.id.loading_text);
        coverView = (ImageView) findViewById(R.id.cover_image);
        usernameView = (TextView) findViewById(R.id.tv_username);
        findViewById(R.id.img_bt_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        leftGiftView = (LiveLeftGiftView) findViewById(R.id.left_gift_view1);
        leftGiftView2 = (LiveLeftGiftView) findViewById(R.id.left_gift_view2);
        messageView = (RoomMessagesView) findViewById(R.id.message_view);
        periscopeLayout = (PeriscopeLayout) findViewById(R.id.periscope_layout);
        bottomBar = findViewById(R.id.bottom_bar);

        barrageLayout = (BarrageLayout) findViewById(R.id.barrage_layout);
        horizontalRecyclerView = (RecyclerView) findViewById(R.id.horizontal_recycle_view);
        audienceNumView = (TextView) findViewById(R.id.audience_num);
        newMsgNotifyImage = (ImageView) findViewById(R.id.new_messages_warn);
    }

    @Override
    protected void onActivityCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_live_details);
        initView();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

        LiveRoom liveRoom = getIntent().getParcelableExtra("liveroom");
        liveId = liveRoom.getId();
        chatroomId = liveRoom.getChatroomId();
        int coverRes = liveRoom.getCover();
        coverView.setImageResource(coverRes);

        anchorId = liveRoom.getAnchorId();
        usernameView.setText(anchorId);

        mVideoView = (UVideoView) findViewById(R.id.videoview);

        mVideoView.setPlayType(UVideoView.PlayType.LIVE);
        mVideoView.setPlayMode(UVideoView.PlayMode.NORMAL);
        mVideoView.setRatio(UVideoView.VIDEO_RATIO_FILL_PARENT);
        mVideoView.setDecoder(UVideoView.DECODER_VOD_SW);

        mVideoView.registerCallback(this);
        mVideoView.setVideoPath(rtmpPlayStreamUrl + liveId);
//      mVideoView.setVideoPath(rtmpPlayStreamUrl);

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isMessageListInited)
            messageView.refresh();
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
        EMClient.getInstance().chatroomManager().leaveChatRoom(chatroomId);

        if (chatRoomChangeListener != null) {
            EMClient.getInstance().chatroomManager().removeChatRoomChangeListener(chatRoomChangeListener);
        }
        if (mVideoView != null) {
            mVideoView.setVolume(0, 0);
            mVideoView.stopPlayback();
            mVideoView.release(true);
        }
    }

    @Override
    public void onEvent(int what, String message) {
        L.d(TAG, "what:" + what + ", message:" + message);
        switch (what) {
            case UVideoView.Callback.EVENT_PLAY_START:
                loadingLayout.setVisibility(View.INVISIBLE);
                EMClient.getInstance().chatroomManager().joinChatRoom(chatroomId, new EMValueCallBack<EMChatRoom>() {
                    @Override
                    public void onSuccess(EMChatRoom emChatRoom) {
                        chatroom = emChatRoom;
                        addChatRoomChangeListenr();
                        onMessageListInit();
                    }

                    @Override
                    public void onError(int i, String s) {

                    }
                });

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
            case UVideoView.Callback.EVENT_PLAY_PAUSE:
                break;
            case UVideoView.Callback.EVENT_PLAY_STOP:
                break;
            case UVideoView.Callback.EVENT_PLAY_COMPLETION:
                Toast.makeText(this, "直播已结束", Toast.LENGTH_LONG).show();
                finish();
                break;
            case UVideoView.Callback.EVENT_PLAY_DESTORY:
                Toast.makeText(this, "DESTORY", Toast.LENGTH_SHORT).show();
                break;
            case UVideoView.Callback.EVENT_PLAY_ERROR:
                loadingText.setText("主播尚未开播");
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "主播尚未开播", Toast.LENGTH_LONG).show();
                break;
            case UVideoView.Callback.EVENT_PLAY_RESUME:
                break;
            case UVideoView.Callback.EVENT_PLAY_INFO_BUFFERING_START:
//                Toast.makeText(VideoActivity.this, "unstable network", Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
