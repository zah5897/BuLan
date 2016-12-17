package com.mingmay.bulan.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;

import com.hyphenate.easeui.controller.EaseUI;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.DataBaseManager;
import com.mingmay.bulan.app.DemoHelper;
import com.mingmay.bulan.app.NewMessageBroadcastReceiver;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.app.listener.ContactListener;
import com.mingmay.bulan.model.BuLanSaveModel;
import com.mingmay.bulan.model.BulanEditModel;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.task.LoadAllTagTask;
import com.mingmay.bulan.task.PublishBULAN;
import com.mingmay.bulan.util.http.HttpProxy;
import com.mingmay.bulan.util.http.HttpUtils;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class CCService extends Service {
    public static final String Action_UPLOAD_BULAN = "CC_ACTION_UPLOAD_BULAN";
    public static final String Action_UPLOAD_ALL_BULAN = "CC_ACTION_UPLOAD_ALL_BULAN";
    public static final String Action_INIT = "CC_ACTION_INIT";
    public static final String Action_REFRESH_MSG_CENTER = "CC_ACTION_REFRESH_MSF_CENTER";

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (Action_UPLOAD_BULAN.equals(action)) {
                long localId = intent.getLongExtra("local_id", 0);
                submitBulan(localId);
            } else if (Action_UPLOAD_ALL_BULAN.equals(action)) {
                List<BuLanSaveModel> list = DataBaseManager.getInstance(
                        getBaseContext()).getBulanSaveList();
                if (list != null) {
                    for (BuLanSaveModel model : list) {
                        if (model.state == BuLanSaveModel.STATE_HAS_TO_COMMIT) {
                            submitBulan(model.id);
                        }
                    }
                }
            } else if (Action_INIT.equals(action)) {
                loadUnReadMsgCount();
                LoadAllTagTask t = new LoadAllTagTask(this);
                t.execute();

                UserManager.getInstance().loadContact(1, new ContactListener() {

                    @Override
                    public void onSuccess(List<User> users) {

                        Intent i = new Intent(getApplicationContext(),
                                NewMessageBroadcastReceiver.class);
                        i.putExtra("type", 1);
                        sendBroadcast(i);
                    }

                    @Override
                    public void onFailure(int code) {
                        // TODO Auto-generated method stub

                    }
                });

            } else if (Action_REFRESH_MSG_CENTER.equals(action)) {
                loadUnReadMsgCount();
            }
        }
        //初始化环信的IM系统
        EaseUI.getInstance().init(this, null);
        DemoHelper.getInstance().init(getApplicationContext());
//		if(!TextUtils.isEmpty(EMChatManager.getInstance().getCurrentUser())){
//			EMChatManager.getInstance().loadAllConversations();// 加载本地会话历史
//		}

        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    private void submitBulan(final long local_id) {
        if (local_id <= 0) {
            return;
        }
        new Thread() {
            public void run() {

                BuLanSaveModel bulan = DataBaseManager.getInstance()
                        .getBulanSave(local_id);
                ArrayList<BulanEditModel> bms = bulan.getModels();
                try {

                    uploadIconFile(bulan);
                    uploadFile(bms);
                    String content = bulan.getPublishBulanJson();
                    PublishBULAN t = new PublishBULAN();
                    t.publishBulanWithImage(bulan.iconServerPath, bulan.title,
                            bulan.isOpen, bulan.tag, local_id, content);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }.start();

    }

    private void uploadFile(ArrayList<BulanEditModel> bms)
            throws IOException, JSONException {
        String URL = CCApplication.HTTPSERVER + "/m_file!addNewFile.action";
        int i = 0;
        for (BulanEditModel bm : bms) {
            if (bm.type == 1 && TextUtils.isEmpty(bm.getFilename())) {
                MultipartEntity param = new MultipartEntity();
                param.addPart("file", new FileBody(new File(bm.content)));
                param.addPart("tempId", new StringBody(String.valueOf(i)));

                HttpResponse response = new HttpProxy().post(URL, param);
                int code = response.getStatusLine().getStatusCode();
                String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
                JSONObject obj = new JSONObject(rev);
                JSONObject body = obj.optJSONObject("body");
                // "body":{"filename":"559cfabb-22c2-4e71-9590-0aa12d2de3a4.jpg","tempId":"11","cstatus":"0"}}
                if (body != null) {
                    int cstatus = body.optInt("cstatus");
                    if (cstatus == 0) {
                        String filename = body.optString("filename");
                        bm.setFilename(filename);
                    }

                }
            }
            i++;
        }
        for (BulanEditModel bm : bms) {
            if (bm.type == 1) {
                if (TextUtils.isEmpty(bm.getFilename())) {
                    uploadFile(bms);
                    return;
                }
            }
        }
    }

    private boolean uploadIconFile(BuLanSaveModel bsm)
            throws IOException, JSONException {
        if (TextUtils.isEmpty(bsm.iconPath)) {
            return false;
        }
        File f = new File(bsm.iconPath);
        if (!f.exists()) {
            return false;
        }
        String URL = CCApplication.HTTPSERVER + "/m_file!addNewFile.action";
        MultipartEntity param = new MultipartEntity();
        param.addPart("file", new FileBody(new File(bsm.iconPath)));
        param.addPart("tempId", new StringBody("0"));

        HttpResponse response = new HttpProxy().post(URL, param);
        int code = response.getStatusLine().getStatusCode();
        String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
        JSONObject obj = new JSONObject(rev);
        JSONObject body = obj.optJSONObject("body");
        // "body":{"filename":"559cfabb-22c2-4e71-9590-0aa12d2de3a4.jpg","tempId":"11","cstatus":"0"}}
        if (body != null) {
            int cstatus = body.optInt("cstatus");
            if (cstatus == 0) {
                String filename = body.optString("filename");
                bsm.iconServerPath = filename;
                return true;
            }
        }
        return false;
    }

    private void loadUnReadMsgCount() {

        String url = CCApplication.HTTPSERVER
                + "/m_notice!getNotreadCount.action";
        RequestParams params = new RequestParams();
        params.put("userId",
                String.valueOf(UserManager.getInstance().getLoginUser().ID));
        HttpUtils.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  JSONObject response) {
                JSONObject object = response.optJSONObject("body");
                if (object != null) {
                    int unReadCount = object.optInt("count");
                    int wardrobeCount = object.optInt("wardrobeCount");
                    int friendCount = object.optInt("friendCount");

                    Intent i = new Intent();
                    i.setAction("new.msg.tip");
                    i.putExtra("type", 2);
                    CCApplication.unreadMsgCount = unReadCount;
                    CCApplication.guanzhu_MsgCount = wardrobeCount;
                    CCApplication.friendCount = friendCount;
                    sendBroadcast(i);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });

    }

}
