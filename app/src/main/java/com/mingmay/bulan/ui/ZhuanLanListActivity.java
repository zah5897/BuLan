package com.mingmay.bulan.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

//import com.easemob.livedemo.ui.activity.LiveDetailsActivity;
//import com.easemob.livedemo.ui.activity.StartLiveActivity;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hyphenate.easeui.EaseConstant;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mingmay.bulan.R;
import com.mingmay.bulan.adapter.BuLanAdapter;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.UserManager;
import com.mingmay.bulan.base.BaseActivity;
import com.mingmay.bulan.model.BuLanModel;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.ui.fragment.type.ModifyZhuanLanPage;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpUtils;

public class ZhuanLanListActivity extends BaseActivity {
    private PullToRefreshListView mPullRefreshListView;
    private ListView mListview;

    private BuLanAdapter adapter;

    private int pageIndex;

    private long id;

    private boolean canShowManager;

    private String nickName;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
//	private GoogleApiClient client;

    private boolean can_show_live;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id = getIntent().getLongExtra("id", 0);
        can_show_live = getIntent().getBooleanExtra("can_show_live", false);
        nickName = getIntent().getStringExtra("user_nickname");
        canShowManager = getIntent().getBooleanExtra("canShowManager", false);
        if (id <= 0) {
            finish();
        }
        setContentView(R.layout.activity_zhuanlan_list);
        initRefreshView();

        ((TextView) findViewById(R.id.title)).setText(nickName + "的专栏");
//		if (!canShowManager) {
//			findViewById(R.id.manager).setVisibility(View.GONE);
//		}

//		findViewById(R.id.manager).setVisibility(View.GONE);
        findViewById(R.id.manager).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (canShowManager) {
                    Intent edit = new Intent(ZhuanLanListActivity.this, ModifyZhuanLanPage.class);
                    edit.putExtra("id", id);
                    startActivity(edit);
                } else {
                    Intent i = new Intent(ZhuanLanListActivity.this, FollowZhuanLanActivity.class);
                    i.putExtra("id", id);
                    i.putExtra("user_nickname", nickName);
                    startActivityForResult(i, 1);
                }


            }
        });
        if (can_show_live) {
            findViewById(R.id.live).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Intent intent = new Intent(getBaseContext(), StartLiveActivity.class);
//                    String roomId = "";
//                    if (adapter.getCount() > 0) {
//                        roomId = adapter.getItem(0).bulanKey;
//                    } else {
//                        ToastUtil.show("请稍后..");
//                        return;
//                    }
//                    intent.putExtra("room_id", roomId);
//                    startActivity(intent);
                }
            });
        } else {
            findViewById(R.id.live).setVisibility(View.GONE);
        }


        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                mPullRefreshListView.setRefreshing();
            }
        }, 1000);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void back(View c) {
        finish();
    }

    private void initRefreshView() {
        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);

        mPullRefreshListView.setMode(Mode.BOTH);
        OnRefreshListener2<ListView> onr = new OnRefreshListener2<ListView>() {
            public void onPullDownToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                loadData(false);
            }

            public void onPullUpToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                loadData(true);
            }

        };
        mPullRefreshListView.setOnRefreshListener(onr);
        mListview = mPullRefreshListView.getRefreshableView();
        mListview.setCacheColorHint(getResources().getColor(R.color.transparent));
        adapter = new BuLanAdapter(this, new ArrayList<BuLanModel>());
        mListview.setAdapter(adapter);
        mListview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

                BuLanModel bm = adapter.getItem(arg2 - 1);
                if (bm.bulanId == -1) {
                    Intent toChat = new Intent(ZhuanLanListActivity.this,
                            ChatActivity.class);
                    toChat.putExtra(EaseConstant.EXTRA_USER_ID, bm.bulanKey);
                    toChat.putExtra("chatType", EaseConstant.CHATTYPE_CHATROOM);
                    startActivity(toChat);

//					Intent toLive=new Intent(getBaseContext(), LiveDetailsActivity.class);
//					toLive.putExtra("room_id",bm.bulanKey);
//                    startActivity(toLive);

                } else {
                    Intent detial = new Intent(ZhuanLanListActivity.this,
                            BulanDetialPage.class);
                    detial.putExtra("bulan", bm);
                    startActivity(detial);
                }
            }
        });
    }

    private void loadData(boolean isLoadMore) {

        if (isLoadMore) {
            pageIndex++;
        } else {
            pageIndex = 1;
        }

        String url = CCApplication.HTTPSERVER
                + "/m_bp!findBulansByWardrobe.action";
        User u = UserManager.getInstance().getLoginUser();
        RequestParams params = new RequestParams();
        params.add("userId", String.valueOf(u.ID));
        params.add("ccukey", u.ccukey);
        params.add("curPage", String.valueOf(pageIndex));
        params.add("pageSize", "20");
        params.add("wardrobeId", String.valueOf(id));
        HttpUtils.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  JSONObject response) {

                JSONObject body = response.optJSONObject("body");
                JSONArray tagArray = body.optJSONArray("bulanInfo");
                JSONObject room = body.optJSONObject("roomInfo");

                if (pageIndex == 1) {
                    adapter.clear();
                }

                if (tagArray != null) {
                    int len = tagArray.length();
                    if (len > 0) {
                        List<BuLanModel> bulans = new ArrayList<BuLanModel>();
                        for (int i = 0; i < len; i++) {
                            bulans.add(BuLanModel.jsonToModel(tagArray
                                    .optJSONObject(i)));
                        }
                        adapter.add(bulans);
                    }
                }

                if (room != null) {
                    BuLanModel bmRoom = createRoom(room);
                    if (bmRoom != null) {
                        adapter.addTop(bmRoom);
                    }
                }
                mPullRefreshListView.onRefreshComplete();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  String responseString, Throwable throwable) {
                // TODO Auto-generated method stub
                super.onFailure(statusCode, headers, responseString, throwable);
                mPullRefreshListView.onRefreshComplete();
            }
        });
    }

    private BuLanModel createRoom(JSONObject room) {
        if (room != null) {
            BuLanModel bm = new BuLanModel();
            bm.bulanId = -1;
            bm.bulanTitle = room.optString("name");
//			bm.bulanTitle = room.optString("description");
            bm.browseCount = room.optInt("affiliations_count");
            String room_id = room.optString("id");
            bm.bulanKey = room_id;
            return bm;
        }
        return null;
    }


}
