package com.mingmay.bulan.ui.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMConversation.EMConversationType;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.widget.EaseConversationList;
import com.mingmay.bulan.R;
import com.mingmay.bulan.ui.ChatActivity;
import com.mingmay.bulan.ui.friend.ContactPage;

public class FriendFragment extends Fragment {
	protected List<EMConversation> conversationList = new ArrayList<EMConversation>();
	protected EaseConversationList conversationListView;
	protected boolean isConflict;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// layout_tab_friends
		return inflater.inflate(R.layout.layout_tab_friends, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null
				&& savedInstanceState.getBoolean("isConflict", false)) {
			return;
		}
		initView();
		setUpView();

		IntentFilter intentFilter = new IntentFilter("del.friend");
		intentFilter.addAction("refresh.friend");
		intentFilter.addAction("new.msg.del_back");
		// intentFilter.setPriority(3);
		getActivity().registerReceiver(delFriendBroadcast, intentFilter);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		getActivity().unregisterReceiver(delFriendBroadcast);
	}

	protected void initView() {

		// 会话列表控件
		conversationListView = (EaseConversationList) getView().findViewById(
				R.id.list);
		// 搜索框
		// 搜索框中清除button
	}

	public void setUpView() {
		conversationList.clear();
		conversationList.addAll(loadConversationList());
		conversationListView.init(conversationList);
		conversationListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				EMConversation conversation = conversationListView
						.getItem(position);

				if (conversation.getType() == EMConversationType.ChatRoom) {
					startActivity(new Intent(getActivity(), ChatActivity.class)
							.putExtra(EaseConstant.EXTRA_USER_ID,
									conversation.getUserName()));
				} else {
					Intent toChat = new Intent(getActivity(),
							ChatActivity.class);
					toChat.putExtra(EaseConstant.EXTRA_USER_ID,
							conversation.getUserName());
					if (conversation.getType() == EMConversationType.GroupChat) {
						toChat.putExtra("chatType", EaseConstant.CHATTYPE_GROUP);
					}
					startActivity(toChat);

				}

			}
		});

		conversationListView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				hideSoftKeyboard();
				return false;
			}
		});

		getView().findViewById(R.id.contact_layout).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent contact = new Intent(getActivity(),
								ContactPage.class);
						startActivityForResult(contact, 0);
					}
				});
	}

 

	/**
	 * 刷新页面
	 */
	public void refresh() {
		conversationList.clear();
		conversationList.addAll(loadConversationList());
		if (conversationListView != null) {
			conversationListView.refresh();
		}
	}

	/**
	 * 获取会话列表
	 * 
	 * @param context
	 * @return +
	 */
	protected List<EMConversation> loadConversationList() {
		// 获取所有会话，包括陌生人
		Map<String, EMConversation> conversations = EMClient.getInstance().chatManager().getAllConversations();
		// 过滤掉messages size为0的conversation
		/**
		 * 如果在排序过程中有新消息收到，lastMsgTime会发生变化 影响排序过程，Collection.sort会产生异常
		 * 保证Conversation在Sort过程中最后一条消息的时间不变 避免并发问题
		 */
		List<Pair<Long, EMConversation>> sortList = new ArrayList<Pair<Long, EMConversation>>();
		synchronized (conversations) {
			for (EMConversation conversation : conversations.values()) {
				if (conversation.getAllMessages().size() != 0) {
					// if(conversation.getType() !=
					// EMConversationType.ChatRoom){
					sortList.add(new Pair<Long, EMConversation>(conversation
							.getLastMessage().getMsgTime(), conversation));
					// }
				}
			}
		}
		try {
			// Internal is TimSort algorithm, has bug
			sortConversationByLastChatTime(sortList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<EMConversation> list = new ArrayList<EMConversation>();
		for (Pair<Long, EMConversation> sortItem : sortList) {

			EMConversation conversation = sortItem.second;
			if (conversation.getType() == EMConversationType.ChatRoom) {
				EMClient.getInstance().chatManager().deleteConversation(
						conversation.getUserName(), false);
				conversation.markAllMessagesAsRead();
				break;
			}
			list.add(conversation);
		}
		return list;
	}

	/**
	 * 根据最后一条消息的时间排序
	 * 
	 * @param usernames
	 */
	private void sortConversationByLastChatTime(
			List<Pair<Long, EMConversation>> conversationList) {
		Collections.sort(conversationList,
				new Comparator<Pair<Long, EMConversation>>() {
					@Override
					public int compare(final Pair<Long, EMConversation> con1,
							final Pair<Long, EMConversation> con2) {

						if (con1.first == con2.first) {
							return 0;
						} else if (con2.first > con1.first) {
							return 1;
						} else {
							return -1;
						}
					}

				});
	}

	protected void hideSoftKeyboard() {
		if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {

		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden && !isConflict) {
			refresh();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		conversationListView.refresh();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (isConflict) {
			outState.putBoolean("isConflict", true);
		}
	}

	public interface EaseConversationListItemClickListener {
		/**
		 * 会话listview item点击事件
		 * 
		 * @param conversation
		 *            被点击item所对应的会话
		 */
		void onListItemClicked(EMConversation conversation);
	}


	public void checkRefresh() {
		refresh();
	}

	private BroadcastReceiver delFriendBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				refresh();
			}
		}
	};

}
