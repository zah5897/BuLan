/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mingmay.bulan.ui.group;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.ui.EaseGroupRemoveListener;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseAlertDialog;
import com.hyphenate.easeui.widget.EaseAlertDialog.AlertDialogUser;
import com.hyphenate.easeui.widget.EaseExpandGridView;
import com.hyphenate.easeui.widget.EaseSwitchButton;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mingmay.bulan.R;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.SettingManager;
import com.mingmay.bulan.base.BaseActivity;
import com.mingmay.bulan.ui.ChatActivity;
import com.mingmay.bulan.ui.friend.ContactPage;
import com.mingmay.bulan.util.ToastUtil;
import com.mingmay.bulan.util.http.HttpUtils;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GroupDetailsActivity extends BaseActivity implements
        OnClickListener {
    private static final String TAG = "GroupDetailsActivity";
    private static final int REQUEST_CODE_ADD_USER = 0;
    private static final int REQUEST_CODE_EXIT = 1;
    private static final int REQUEST_CODE_EXIT_DELETE = 2;
    private static final int REQUEST_CODE_EDIT_GROUPNAME = 5;

    private EaseExpandGridView userGridview;
    private String groupId;
    private ProgressBar loadingPB;
    private Button exitBtn;
    private Button deleteBtn;
    private EMGroup group;
    private GridAdapter adapter;
    private ProgressDialog progressDialog;

    private RelativeLayout rl_switch_block_groupmsg;

    public static GroupDetailsActivity instance;

    String st = "";
    // 清空所有聊天记录
    private RelativeLayout clearAllHistory;
    private RelativeLayout blacklistLayout;
    private RelativeLayout changeGroupNameLayout;
    private RelativeLayout idLayout;
    private TextView idText;
    private EaseSwitchButton switchButton;
    private GroupRemoveListener groupRemoveListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 获取传过来的groupid
        groupId = getIntent().getStringExtra("groupId");
        group = EMClient.getInstance().groupManager().getGroup(groupId);

        // we are not supposed to show the group if we don't find the group
        if (group == null) {
            finish();
            return;
        }

        setContentView(R.layout.em_activity_group_details);
        instance = this;
        st = getResources().getString(R.string.people);
        clearAllHistory = (RelativeLayout) findViewById(R.id.clear_all_history);
        userGridview = (EaseExpandGridView) findViewById(R.id.gridview);
        loadingPB = (ProgressBar) findViewById(R.id.progressBar);
        exitBtn = (Button) findViewById(R.id.btn_exit_grp);
        deleteBtn = (Button) findViewById(R.id.btn_exitdel_grp);
        blacklistLayout = (RelativeLayout) findViewById(R.id.rl_blacklist);
        changeGroupNameLayout = (RelativeLayout) findViewById(R.id.rl_change_group_name);
        idLayout = (RelativeLayout) findViewById(R.id.rl_group_id);
        idLayout.setVisibility(View.VISIBLE);
        idText = (TextView) findViewById(R.id.tv_group_id_value);

        rl_switch_block_groupmsg = (RelativeLayout) findViewById(R.id.rl_switch_block_groupmsg);
        switchButton = (EaseSwitchButton) findViewById(R.id.switch_btn);

        if (SettingManager.getInstance().isDisNoice(group.getGroupId())) {
            switchButton.openSwitch();
        } else {
            switchButton.closeSwitch();
        }

        rl_switch_block_groupmsg.setOnClickListener(this);

        idText.setText(groupId);
        if (group.getOwner() == null
                || "".equals(group.getOwner())
                || !group.getOwner().equals(
                EMClient.getInstance().getCurrentUser())) {
            exitBtn.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.GONE);
            blacklistLayout.setVisibility(View.GONE);
            changeGroupNameLayout.setVisibility(View.GONE);

        } else {
            findViewById(R.id.rl_group_owner).setVisibility(View.VISIBLE);
            String nickName = EaseUserUtils.getUserInfo(group.getOwner())
                    .getNick();
            ((TextView) findViewById(R.id.tv_group_owner_value))
                    .setText(nickName);
        }
        // 如果自己是群主，显示解散按钮
        if (EMClient.getInstance().getCurrentUser().equals(group.getOwner())) {
            exitBtn.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.VISIBLE);
        }

        groupRemoveListener = new GroupRemoveListener();
        EMClient.getInstance().groupManager()
                .addGroupChangeListener(groupRemoveListener);

        ((TextView) findViewById(R.id.group_name)).setText(group.getGroupName()
                + "(" + group.getAffiliationsCount() + st);

        List<String> members = new ArrayList<String>();
        members.addAll(group.getMembers());


        adapter = new GridAdapter(this, R.layout.em_grid, members);

        if (group.getOwner().equals(
                EMClient.getInstance().getCurrentUser())) {
            adapter.showAddAndDel();
        }
        userGridview.setAdapter(adapter);


        // 保证每次进详情看到的都是最新的group
        updateGroup();

        // 设置OnTouchListener
        userGridview.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (adapter.isInDeleteMode) {
                            adapter.showAddAndDel();
                            return true;
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        clearAllHistory.setOnClickListener(this);
        blacklistLayout.setOnClickListener(this);
        changeGroupNameLayout.setOnClickListener(this);

        userGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                                    if ("-1".equals(adapter.getItem(i))) {
                                                        adapter.dismissAddAndDel();
                                                        adapter.isInDeleteMode = true;
                                                        adapter.notifyDataSetChanged();
                                                    } else if ("0".equals(adapter.getItem(i))) {
                                                        Intent toAddMember = new Intent(
                                                                GroupDetailsActivity.this,
                                                                ContactPage.class);
                                                        toAddMember.putExtra("groupId", groupId);
                                                        toAddMember.putExtra("need_back", true);
                                                        startActivityForResult(toAddMember,
                                                                REQUEST_CODE_ADD_USER);
                                                    } else {
                                                        // 如果是删除自己，return
                                                        String item=adapter.getItem(i);
                                                        if (EMClient.getInstance().getCurrentUser()
                                                                .equals(item)) {
                                                            return;
                                                        }

                                                        if (adapter.isInDeleteMode) {
                                                            deleteMembersFromGroup(adapter.getItem(i));
                                                        }
                                                    }
                                                }
                                            }

        );
    }

    protected void deleteMembersFromGroup(final String username) {

        Dialog dialog = new AlertDialog.Builder(this).setMessage("确定删除该用户？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                delete(username);
            }
        }).setNegativeButton("取消", null).create();
        dialog.show();
    }


    private void delete(final String username) {

        final ProgressDialog deleteDialog = new ProgressDialog(
                GroupDetailsActivity.this);
        deleteDialog.setMessage("正在删除该成员...");
        deleteDialog.setCanceledOnTouchOutside(false);
        deleteDialog.show();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    // 删除被选中的成员
                    EMClient.getInstance()
                            .groupManager()
                            .removeUserFromGroup(groupId,
                                    username);
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            deleteDialog.dismiss();
                            ((TextView) findViewById(R.id.group_name)).setText(group
                                    .getGroupName()
                                    + "("
                                    + group.getAffiliationsCount()
                                    + st);
                            adapter.removeMember(username);
                        }
                    });
                } catch (final Exception e) {
                    deleteDialog.dismiss();
                }

            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String st1 = getResources().getString(R.string.being_added);
        String st2 = getResources().getString(R.string.is_quit_the_group_chat);
        String st3 = getResources().getString(R.string.chatting_is_dissolution);
        String st4 = getResources().getString(R.string.are_empty_group_of_news);
        String st5 = getResources()
                .getString(R.string.is_modify_the_group_name);
        final String st6 = getResources().getString(
                R.string.Modify_the_group_name_successful);
        final String st7 = getResources().getString(
                R.string.change_the_group_name_failed_please);

        if (resultCode == RESULT_OK) {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(GroupDetailsActivity.this);
                progressDialog.setMessage(st1);
                progressDialog.setCanceledOnTouchOutside(false);
            }
            switch (requestCode) {
                case REQUEST_CODE_ADD_USER:// 添加群成员
                    final String[] newmembers = data
                            .getStringArrayExtra("newmembers");
                    if (newmembers == null) {
                        return;
                    }
                    List<String> toAddMembers = new ArrayList<>();
                    for (String member : newmembers) {
                        if (!adapter.contain(member)) {
                            toAddMembers.add(member);
                        }
                    }
                    if (toAddMembers.size() > 0) {
                        progressDialog.setMessage(st1);
                        progressDialog.show();
                        String[] newMems = new String[toAddMembers.size()];
                        toAddMembers.toArray(newMems);
                        addMembersToGroup(newMems);
                    }
                    break;
                case REQUEST_CODE_EXIT: // 退出群
                    progressDialog.setMessage(st2);
                    progressDialog.show();
                    exitGrop();
                    break;
                case REQUEST_CODE_EXIT_DELETE: // 解散群

                    progressDialog.setMessage(st3);
                    progressDialog.show();
                    deleteGrop();
                    break;

                case REQUEST_CODE_EDIT_GROUPNAME: // 修改群名称
                    final String returnData = data.getStringExtra("data");
                    if (!TextUtils.isEmpty(returnData)) {
                        progressDialog.setMessage(st5);
                        progressDialog.show();

                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    EMClient.getInstance().groupManager()
                                            .changeGroupName(groupId, returnData);
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            ((TextView) findViewById(R.id.group_name)).setText(returnData
                                                    + "("
                                                    + group.getAffiliationsCount()
                                                    + st);
                                            progressDialog.dismiss();
                                            Toast.makeText(getApplicationContext(),
                                                    st6, Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            progressDialog.dismiss();
                                            Toast.makeText(getApplicationContext(),
                                                    st7, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }).start();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    protected void addUserToBlackList(final String username) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage(getString(R.string.Are_moving_to_blacklist));
        pd.show();
        new Thread(new Runnable() {
            public void run() {
                try {
                    EMClient.getInstance().groupManager()
                            .blockUser(groupId, username);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            refreshMembers();
                            pd.dismiss();
                            ToastUtil.show(getResources().getString(R.string.Move_into_blacklist_success));
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            Toast.makeText(getApplicationContext(),
                                    R.string.failed_to_move_into, 0).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void refreshMembers() {
        adapter.clear();
        adapter.addAll(group.getMembers());
        if (group.getOwner().equals(
                EMClient.getInstance().getCurrentUser())) {
            adapter.showAddAndDel();
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 点击退出群组按钮
     *
     * @param view
     */
    public void exitGroup(View view) {

        Dialog d = new AlertDialog.Builder(this).setMessage("确定退出该群？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.dismiss();
                        if (progressDialog == null) {
                            progressDialog = new ProgressDialog(
                                    GroupDetailsActivity.this);
                            progressDialog.setMessage("正在退出该群...");
                            progressDialog.setCanceledOnTouchOutside(false);
                        }
                        exitGrop();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        arg0.dismiss();
                    }
                }).create();
        d.show();

    }

    /**
     * 点击解散群组按钮
     *
     * @param view
     */
    public void exitDeleteGroup(View view) {
        Dialog d = new AlertDialog.Builder(this)
                .setMessage("确定解散该群？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.dismiss();
                        if (progressDialog == null) {
                            progressDialog = new ProgressDialog(
                                    GroupDetailsActivity.this);
                            progressDialog.setMessage("正在解散该群...");
                            progressDialog.setCanceledOnTouchOutside(false);
                        }
                        deleteGrop();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.dismiss();

                    }
                }).create();
        d.show();

    }

    /**
     * 清空群聊天记录
     */
    private void clearGroupHistory() {

        EMClient.getInstance().chatManager()
                .deleteConversation(group.getGroupId(), true);
        ToastUtil.show("消息已经清空");
    }

    /**
     * 退出群组
     */
    private void exitGrop() {
        String st1 = getResources().getString(
                R.string.Exit_the_group_chat_failure);
        new Thread(new Runnable() {
            public void run() {
                try {
                    EMClient.getInstance().groupManager().leaveGroup(groupId);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            EMClient.getInstance().chatManager().deleteConversation(groupId,true);
                            setResult(RESULT_OK);
                            finish();
                            if (ChatActivity.activityInstance != null)
                                ChatActivity.activityInstance.finish();
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            ToastUtil.show("退出失败");
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * 解散群组
     */
    private void deleteGrop() {
        final String st5 = getResources().getString(
                R.string.Dissolve_group_chat_tofail);
        new Thread(new Runnable() {
            public void run() {

                try {

                    EMClient.getInstance().groupManager().destroyGroup(groupId);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            setResult(RESULT_OK);
                            finish();
                            if (ChatActivity.activityInstance != null)
                                ChatActivity.activityInstance.finish();
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),
                                    st5 + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * 增加群成员
     *
     * @param newmembers
     */
    private void addMembersToGroup(final String[] newmembers) {
        RequestParams param = new RequestParams();

        String str = null;
        for (int i = 0; i < newmembers.length; i++) {
            if (i == 0) str = newmembers[i];
            else str += "," + newmembers[i];
        }
        param.put("invitationUserIds", str);
        param.put("groupId", groupId);
        final String toAddAdapter = str;
        HttpUtils.post(CCApplication.HTTPSERVER + "/m_group!invitationGroup.action", param, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                JSONObject body = response.optJSONObject("body");
                int cstatus = body.optInt("cstatus");
                if (cstatus == 0) {
                    ToastUtil.show("添加成功");
                    adapter.addMembers(toAddAdapter);
                } else {
                    ToastUtil.show("添加失败");
                }
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                ToastUtil.show("添加失败");
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_switch_block_groupmsg: // 屏蔽或取消屏蔽群组
                toggleBlockGroup();
                break;

            case R.id.clear_all_history: // 清空聊天记录
                String st9 = getResources().getString(R.string.sure_to_empty_this);
                new EaseAlertDialog(GroupDetailsActivity.this, null, st9, null,
                        new AlertDialogUser() {

                            @Override
                            public void onResult(boolean confirmed, Bundle bundle) {
                                if (confirmed) {
                                    clearGroupHistory();
                                }
                            }
                        }, true).show();

                break;

            case R.id.rl_blacklist: // 黑名单列表
                // startActivity(new Intent(GroupDetailsActivity.this,
                // GroupBlacklistActivity.class).putExtra("groupId", groupId));
                break;

            case R.id.rl_change_group_name:
                startActivityForResult(
                        new Intent(this, EditActivity.class).putExtra("data",
                                group.getGroupName()), REQUEST_CODE_EDIT_GROUPNAME);
                break;

            default:
                break;
        }

    }

    private void toggleBlockGroup() {
        if (SettingManager.getInstance().isDisNoice(group.getGroupId())) {
            SettingManager.getInstance()
                    .removeDisNoiceGroup(group.getGroupId());
            switchButton.closeSwitch();
        } else {
            switchButton.openSwitch();
            SettingManager.getInstance().addDisNoiceGroup(group.getGroupId());
        }


    }

    /**
     * 群组成员gridadapter
     *
     * @author admin_new
     */
    private class GridAdapter extends BaseAdapter {

        private int res;
        public boolean isInDeleteMode = false;
        private List<String> objects;

        private Context context;

        public GridAdapter(Context context, int textViewResourceId,
                           List<String> objects) {
            this.context = context;
            this.objects = objects;
            res = textViewResourceId;
            isInDeleteMode = false;
        }

        public void showAddAndDel() {

            if (group.getOwner()
                    .equals(EMClient.getInstance().getCurrentUser())) {
                if (!objects.contains("0")) {
                    objects.add("0");
                }

                if (!objects.contains("-1")) {
                    objects.add("-1");
                }


                isInDeleteMode = false;
            }
            notifyDataSetChanged();
        }

        public void dismissAddAndDel() {
            if (objects.contains("-1")) {
                objects.remove("-1");
            }
            if (objects.contains("0")) {
                objects.remove("0");
            }
            isInDeleteMode = true;
            notifyDataSetChanged();
        }

        @Override
        public View getView(final int position, View convertView,
                            final ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(res,
                        null);
                holder.imageView = (ImageView) convertView
                        .findViewById(R.id.iv_avatar);
                holder.textView = (TextView) convertView
                        .findViewById(R.id.tv_name);
                holder.badgeDeleteView = (ImageView) convertView
                        .findViewById(R.id.badge_delete);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final String username = getItem(position);
            if (isInDeleteMode) {
                holder.badgeDeleteView.setVisibility(View.VISIBLE);
                holder.badgeDeleteView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteMembersFromGroup(username);
                    }
                });
            } else {
                holder.badgeDeleteView.setVisibility(View.GONE);
                holder.badgeDeleteView.setOnClickListener(null);
            }

            // 最后一个item，减人按钮
            if ("-1".equals(username)) {
                holder.textView.setText("删除");
                // 设置成删除按钮
                holder.imageView
                        .setImageResource(R.drawable.em_smiley_minus_btn);
            } else if ("0".equals(username)) { // 添加群组成员按钮
                holder.textView.setText("添加");
                holder.imageView.setImageResource(R.drawable.em_smiley_add_btn);
            } else { // 普通item，显示群组成员
                EaseUser user = EaseUserUtils.getUserInfo(username);
                if (user != null) {
                    String avatar = user.getAvatar();

                    EaseUserUtils.setUserNick(username, holder.textView);
                    Glide.with(context).load(avatar).override(60, 60).error(R.drawable.headlogo)
                            .into(holder.imageView);
                }
            }
            return convertView;
        }

        @Override
        public int getCount() {
            return objects.size();
        }

        @Override
        public String getItem(int i) {
            return objects.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        public void addMembers(String members) {
            String member[] = members.split(",");
            for (String m : member) {
                int o = objects.indexOf("0");
                if (o > 0) {
                    objects.add(o - 1, m);
                } else {
                    objects.add(0, m);
                }
            }
            notifyDataSetChanged();
        }

        public void clear() {
            objects.clear();
        }

        public void addAll(List<String> members) {
            objects.addAll(members);
        }

        public boolean contain(String member) {
            return objects.contains(member);
        }

        public void removeMember(String username) {
            objects.remove(username);
            notifyDataSetChanged();
        }
    }

    protected void updateGroup() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    final EMGroup returnGroup = EMClient.getInstance()
                            .groupManager().getGroupFromServer(groupId);
                    // 更新本地数据

                    runOnUiThread(new Runnable() {
                        public void run() {
                            ((TextView) findViewById(R.id.group_name))
                                    .setText(group.getGroupName() + "("
                                            + group.getAffiliationsCount()
                                            + ")");
                            loadingPB.setVisibility(View.INVISIBLE);
                            refreshMembers();
                            if (EMClient.getInstance().getCurrentUser()
                                    .equals(group.getOwner())) {
                                // 显示解散按钮
                                exitBtn.setVisibility(View.GONE);
                                deleteBtn.setVisibility(View.VISIBLE);
                            } else {
                                // 显示退出按钮
                                exitBtn.setVisibility(View.VISIBLE);
                                deleteBtn.setVisibility(View.GONE);
                            }

                        }
                    });

                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            loadingPB.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        }).start();
    }

    public void back(View view) {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    private static class ViewHolder {
        ImageView imageView;
        TextView textView;
        ImageView badgeDeleteView;
    }

    /**
     * 监测群组解散或者被T事件
     */
    private class GroupRemoveListener extends EaseGroupRemoveListener {

        @Override
        public void onInvitationAccepted(String s, String s1, String s2) {

        }

        @Override
        public void onUserRemoved(final String groupId, String groupName) {
            finish();
        }

        @Override
        public void onGroupDestroyed(String s, String s1) {
            finish();
        }


    }

}
