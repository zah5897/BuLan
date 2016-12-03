//package com.mingmay.bulan.app;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.support.v4.content.LocalBroadcastManager;
//
//import com.easemob.EMCallBack;
//import com.easemob.EMConnectionListener;
//import com.easemob.EMError;
//import com.easemob.EMEventListener;
//import com.easemob.EMNotifierEvent;
//import com.easemob.chat.CmdMessageBody;
//import com.easemob.chat.EMChat;
//import com.easemob.chat.EMChatManager;
//import com.easemob.chat.EMChatOptions;
//import com.easemob.chat.EMContact;
//import com.easemob.chat.EMConversation;
//import com.easemob.chat.EMGroupManager;
//import com.easemob.chat.EMMessage;
//import com.easemob.chat.EMMessage.ChatType;
//import com.easemob.chat.EMMessage.Type;
//import com.easemob.easeui.EaseConstant;
//import com.easemob.easeui.controller.EaseUI;
//import com.easemob.easeui.controller.EaseUI.EaseSettingsProvider;
//import com.easemob.easeui.controller.EaseUI.EaseUserProfileProvider;
//import com.easemob.easeui.domain.EaseUser;
//import com.easemob.easeui.model.EaseNotifier;
//import com.easemob.easeui.model.EaseNotifier.EaseNotificationInfoProvider;
//import com.easemob.easeui.utils.EaseCommonUtils;
//import com.easemob.exceptions.EaseMobException;
//import com.easemob.util.EMLog;
//import com.mingmay.bulan.model.User;
//import com.mingmay.bulan.ui.ChatActivity;
//import com.mingmay.bulan.ui.LoginPage;
//
//public class BulanHelper {
//	/**
//	 * 数据同步listener
//	 */
//	static public interface DataSyncListener {
//		/**
//		 * 同步完毕
//		 * 
//		 * @param success
//		 *            true：成功同步到数据，false失败
//		 */
//		public void onSyncComplete(boolean success);
//	}
//
//	protected static final String TAG = "DemoHelper";
//
//	private EaseUI easeUI;
//
//	/**
//	 * EMEventListener
//	 */
//	protected EMEventListener eventListener = null;
//
//	private Map<String, EaseUser> contactList;
//
//	private static BulanHelper instance = null;
//
//	private BulanModel demoModel = null;
//
//	/**
//	 * HuanXin sync groups status listener
//	 */
//	private List<DataSyncListener> syncGroupsListeners;
//	/**
//	 * HuanXin sync contacts status listener
//	 */
//	private List<DataSyncListener> syncContactsListeners;
//	/**
//	 * HuanXin sync blacklist status listener
//	 */
//	private List<DataSyncListener> syncBlackListListeners;
//
//	private boolean isSyncingGroupsWithServer = false;
//	private boolean isSyncingContactsWithServer = false;
//	private boolean isSyncingBlackListWithServer = false;
//	private boolean isGroupsSyncedWithServer = false;
//	private boolean isContactsSyncedWithServer = false;
//	private boolean isBlackListSyncedWithServer = false;
//
//	private boolean alreadyNotified = false;
//
//	public boolean isVoiceCalling;
//	public boolean isVideoCalling;
//
//	private String username;
//
//	private Context appContext;
//
//	private EMConnectionListener connectionListener;
//
//	private LocalBroadcastManager broadcastManager;
//
//	private boolean isGroupAndContactListenerRegisted;
//
//	private BulanHelper() {
//	}
//
//	public synchronized static BulanHelper getInstance() {
//		if (instance == null) {
//			instance = new BulanHelper();
//		}
//		return instance;
//	}
//
//	/**
//	 * init helper
//	 * 
//	 * @param context
//	 *            application context
//	 */
//	public void init(Context context) {
//		if (EaseUI.getInstance().init(context)) {
//			appContext = context;
//			// 设为调试模式，打成正式包时，最好设为false，以免消耗额外的资源
//			EMChat.getInstance().setDebugMode(true);
//			// get easeui instance
//			easeUI = EaseUI.getInstance();
//			// 调用easeui的api设置providers
//			setEaseUIProviders();
//			demoModel = new BulanModel(context);
//			// 设置chat options
//			setChatoptions();
//			// 初始化PreferenceManager
//			// PreferenceManager.init(context);
//			// 初始化用户管理类
//			// getUserProfileManager().init(context);
//
//			// 设置全局监听
//			setGlobalListeners();
//			broadcastManager = LocalBroadcastManager.getInstance(appContext);
//		}
//	}
//
//	private void setChatoptions() {
//		// easeui库默认设置了一些options，可以覆盖
//		EMChatOptions options = EMChatManager.getInstance().getChatOptions();
//		options.allowChatroomOwnerLeave(true);
//	}
//
//	protected void setEaseUIProviders() {
//		// 需要easeui库显示用户头像和昵称设置此provider
//		easeUI.setUserProfileProvider(new EaseUserProfileProvider() {
//
//			@Override
//			public EaseUser getUser(String username) {
//				return getUserInfo(username);
//			}
//		});
//
//		// 不设置，则使用easeui默认的
//		easeUI.setSettingsProvider(new EaseSettingsProvider() {
//
//			@Override
//			public boolean isSpeakerOpened() {
//				return demoModel.getSettingMsgSpeaker();
//			}
//
//			@Override
//			public boolean isMsgVibrateAllowed(EMMessage message) {
//				return demoModel.getSettingMsgVibrate();
//			}
//
//			@Override
//			public boolean isMsgSoundAllowed(EMMessage message) {
//				return demoModel.getSettingMsgSound();
//			}
//
//			@Override
//			public boolean isMsgNotifyAllowed(EMMessage message) {
//				if (message == null) {
//					return demoModel.getSettingMsgNotification();
//				}
//				if (!demoModel.getSettingMsgNotification()) {
//					return false;
//				} else {
//					// 如果允许新消息提示
//					// 屏蔽的用户和群组不提示用户
//					String chatUsename = null;
//					List<String> notNotifyIds = null;
//					// 获取设置的不提示新消息的用户或者群组ids
//					if (message.getChatType() == ChatType.Chat) {
//						chatUsename = message.getFrom();
//					} else {
//						chatUsename = message.getTo();
//					}
//
//					if (notNotifyIds == null
//							|| !notNotifyIds.contains(chatUsename)) {
//						return true;
//					} else {
//						return false;
//					}
//				}
//			}
//		});
//		// 设置表情provider
//		// easeUI.setEmojiconInfoProvider(new EaseEmojiconInfoProvider() {
//		//
//		// @Override
//		// public EaseEmojicon getEmojiconInfo(String emojiconIdentityCode) {
//		// EaseEmojiconGroupEntity data = EmojiconExampleGroupData.getData();
//		// for(EaseEmojicon emojicon : data.getEmojiconList()){
//		// if(emojicon.getIdentityCode().equals(emojiconIdentityCode)){
//		// return emojicon;
//		// }
//		// }
//		// return null;
//		// }
//		//
//		// @Override
//		// public Map<String, Object> getTextEmojiconMapping() {
//		// //返回文字表情emoji文本和图片(resource id或者本地路径)的映射map
//		// return null;
//		// }
//		// });
//
//		// 不设置，则使用easeui默认的
//		easeUI.getNotifier().setNotificationInfoProvider(
//				new EaseNotificationInfoProvider() {
//
//					@Override
//					public String getTitle(EMMessage message) {
//						// 修改标题,这里使用默认
//						return null;
//					}
//
//					@Override
//					public int getSmallIcon(EMMessage message) {
//						// 设置小图标，这里为默认
//						return 0;
//					}
//
//					@Override
//					public String getDisplayedText(EMMessage message) {
//						// 设置状态栏的消息提示，可以根据message的类型做相应提示
//						String ticker = EaseCommonUtils.getMessageDigest(
//								message, appContext);
//						if (message.getType() == Type.TXT) {
//							ticker = ticker.replaceAll("\\[.{2,3}\\]", "[表情]");
//						}
//						EaseUser user = getUserInfo(message.getFrom());
//						if (user != null) {
//							return getUserInfo(message.getFrom()).getNick()
//									+ ": " + ticker;
//						} else {
//							return message.getFrom() + ": " + ticker;
//						}
//					}
//
//					@Override
//					public String getLatestText(EMMessage message,
//							int fromUsersNum, int messageNum) {
//						return null;
//						// return fromUsersNum + "个基友，发来了" + messageNum + "条消息";
//					}
//
//					@Override
//					public Intent getLaunchIntent(EMMessage message) {
//						// 设置点击通知栏跳转事件
//						Intent intent = new Intent(appContext,
//								ChatActivity.class);
//						// 有电话时优先跳转到通话页面
//
//						ChatType chatType = message.getChatType();
//						if (chatType == ChatType.Chat) { // 单聊信息
//							intent.putExtra("userId", message.getFrom());
//							intent.putExtra("chatType",
//									EaseConstant.CHATTYPE_SINGLE);
//						} else { // 群聊信息
//							// message.getTo()为群聊id
//							intent.putExtra("userId", message.getTo());
//							if (chatType == ChatType.GroupChat) {
//								intent.putExtra("chatType",
//										EaseConstant.CHATTYPE_GROUP);
//							} else {
//								intent.putExtra("chatType",
//										EaseConstant.CHATTYPE_CHATROOM);
//							}
//
//						}
//
//						return intent;
//					}
//				});
//	}
//
//	/**
//	 * 设置全局事件监听
//	 */
//	protected void setGlobalListeners() {
//		syncGroupsListeners = new ArrayList<DataSyncListener>();
//		syncContactsListeners = new ArrayList<DataSyncListener>();
//		syncBlackListListeners = new ArrayList<DataSyncListener>();
//
//		// create the global connection listener
//		connectionListener = new EMConnectionListener() {
//			@Override
//			public void onDisconnected(int error) {
//				if (error == EMError.USER_REMOVED) {
//					onCurrentAccountRemoved();
//				} else if (error == EMError.CONNECTION_CONFLICT) {
//					onConnectionConflict();
//				}
//			}
//
//			@Override
//			public void onConnected() {
//
//				// in case group and contact were already synced, we supposed to
//				// notify sdk we are ready to receive the events
//				if (isGroupsSyncedWithServer && isContactsSyncedWithServer) {
//					new Thread() {
//						@Override
//						public void run() {
//							getInstance().notifyForRecevingEvents();
//						}
//					}.start();
//				} else {
//					if (!isGroupsSyncedWithServer) {
//						asyncFetchGroupsFromServer(null);
//					}
//				}
//			}
//		};
//
//		// 注册连接监听
//		EMChatManager.getInstance().addConnectionListener(connectionListener);
//		// 注册群组和联系人监听
//		// 注册消息事件监听
//		registerEventListener();
//
//	}
//
//	/**
//	 * 注册群组和联系人监听，由于logout的时候会被sdk清除掉，再次登录的时候需要再注册一下
//	 */
//
//	/**
//	 * 保存并提示消息的邀请消息
//	 * 
//	 * @param msg
//	 */
//	// private void notifyNewIviteMessage(InviteMessage msg) {
//	// if (inviteMessgeDao == null) {
//	// inviteMessgeDao = new InviteMessgeDao(appContext);
//	// }
//	// inviteMessgeDao.saveMessage(msg);
//	// // 保存未读数，这里没有精确计算
//	// inviteMessgeDao.saveUnreadMessageCount(1);
//	// // 提示有新消息
//	// getNotifier().viberateAndPlayTone(null);
//	// }
//
//	/**
//	 * 账号在别的设备登录
//	 */
//	protected void onConnectionConflict() {
//		UserManager.getInstance().logout();
//		Intent intent = new Intent(appContext, LoginPage.class);
//		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		intent.putExtra("ACCOUNT_CONFLICT", true);
//		appContext.startActivity(intent);
//	}
//
//	/**
//	 * 账号被移除
//	 */
//	protected void onCurrentAccountRemoved() {
//		UserManager.getInstance().logout();
//		Intent intent = new Intent(appContext, LoginPage.class);
//		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		intent.putExtra("ACCOUNT_REMOVED", true);
//		appContext.startActivity(intent);
//	}
//
//	private EaseUser getUserInfo(String username) {
//		// 获取user信息，demo是从内存的好友列表里获取，
//		// 实际开发中，可能还需要从服务器获取用户信息,
//		// 从服务器获取的数据，最好缓存起来，避免频繁的网络请求
//
//		User user = UserManager.getInstance().getUser(username);
//		EaseUser easeUser = null;
//		if (user == null) {
//			easeUser = new EaseUser(username);
//			easeUser.setAvatar("");
//		} else {
//			easeUser = new EaseUser(username);
//			easeUser.setNick(user.firstName);
//			easeUser.setAvatar(user.userImg);
//		}
//		return easeUser;
//
//	}
//
//	/**
//	 * 全局事件监听 因为可能会有UI页面先处理到这个消息，所以一般如果UI页面已经处理，这里就不需要再次处理 activityList.size()
//	 * <= 0 意味着所有页面都已经在后台运行，或者已经离开Activity Stack
//	 */
//	protected void registerEventListener() {
//		eventListener = new EMEventListener() {
//			//private BroadcastReceiver broadCastReceiver = null;
//
//			@Override
//			public void onEvent(EMNotifierEvent event) {
//				EMMessage message = null;
//				if (event.getData() instanceof EMMessage) {
//					message = (EMMessage) event.getData();
//					EMLog.d(TAG, "receive the event : " + event.getEvent()
//							+ ",id : " + message.getMsgId());
//				}
//
//				switch (event.getEvent()) {
//				case EventNewMessage:
//					// 应用在后台，不需要刷新UI,通知栏提示新消息
//					if (!easeUI.hasForegroundActivies()) {
//						getNotifier().onNewMsg(message);
//					}
//					break;
//				case EventOfflineMessage:
//					if (!easeUI.hasForegroundActivies()) {
//						EMLog.d(TAG, "received offline messages");
//						List<EMMessage> messages = (List<EMMessage>) event
//								.getData();
//						getNotifier().onNewMesg(messages);
//					}
//					break;
//				// below is just giving a example to show a cmd toast, the app
//				// should not follow this
//				// so be careful of this
//				case EventNewCMDMessage: {
//
//					//EMLog.d(TAG, "收到透传消息");
//					// 获取消息body
//					CmdMessageBody cmdMsgBody = (CmdMessageBody) message
//							.getBody();
//					final String action = cmdMsgBody.action;// 获取自定义action
//					String type = null;
//					String text="0";
//					try {
//						 type=message.getStringAttribute("type");
//						 text=message.getStringAttribute("text");
//					} catch (EaseMobException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					
//					
//					
//						if(action.equals("REVOKE_FLAG")){
//							try {
//								String msgId = message.getStringAttribute("msgId");
//								EMConversation conversation = EMChatManager.getInstance().getConversation(message.getFrom());
//								//--删除消息来表示撤回--
//								conversation.removeMessage(msgId);
//								// 如果需要，可以插入一条“XXX回撤一条消息”
//								
//								Intent i = new Intent();
//								i.putExtra("from", message.getFrom());
//								i.setAction("new.msg.del_back");
//								appContext.sendBroadcast(i);
//							} catch (EaseMobException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//			 
//							return;
//						}
//					
//					// 获取扩展属性 此处省略
//					// message.getStringAttribute("");
////					EMLog.d(TAG, String.format("透传消息：action:%s,message:%s",
////							action, message.toString()));
////					final String str = appContext
////							.getString(R.string.receive_the_passthrough);
//
////					final String CMD_TOAST_BROADCAST = "easemob.demo.cmd.toast";
////					IntentFilter cmdFilter = new IntentFilter(
////							CMD_TOAST_BROADCAST);
//
////					if (broadCastReceiver == null) {
////						broadCastReceiver = new BroadcastReceiver() {
////
////							@Override
////							public void onReceive(Context context, Intent intent) {
////								// TODO Auto-generated method stub
////								String cmd_value = intent
////										.getStringExtra("cmd_value");
////								Toast.makeText(appContext, cmd_value,
////										Toast.LENGTH_SHORT).show();
////							}
////						};
////
////						// 注册广播接收者
////						appContext.registerReceiver(broadCastReceiver,
////								cmdFilter);
////					}
//
////					Intent broadcastIntent = new Intent(CMD_TOAST_BROADCAST);
////					broadcastIntent.putExtra("cmd_value", str + action);
////					appContext.sendBroadcast(broadcastIntent, null);
//					
//					
//					if("1".equals(type)){
//						Intent i = new Intent();
//						i.setAction("new.msg.tip");
//						i.putExtra("type", 2);
//						i.putExtra("count", Integer.parseInt(text));
//						appContext.sendBroadcast(i);
//					}else if("2".equals(type)){
//						EMChatManager.getInstance().deleteConversation(text, true);
//						Intent i = new Intent();
//						i.setAction("del.friend");
//						i.putExtra("type", 2);
//						i.putExtra("text", text);
//						appContext.sendBroadcast(i);
//					}
//					break;
//				}
//				case EventDeliveryAck:
//					message.setDelivered(true);
//					break;
//				case EventReadAck:
//					message.setAcked(true);
//					break;
//				// add other events in case you are interested in
//				default:
//					break;
//				}
//
//			}
//		};
//
//		EMChatManager.getInstance().registerEventListener(eventListener);
//	}
//
//	/**
//	 * 是否登录成功过
//	 * 
//	 * @return
//	 */
//	public boolean isLoggedIn() {
//		return EMChat.getInstance().isLoggedIn();
//	}
//
//	/**
//	 * 退出登录
//	 * 
//	 * @param unbindDeviceToken
//	 *            是否解绑设备token(使用GCM才有)
//	 * @param callback
//	 *            callback
//	 */
//	public void logout(boolean unbindDeviceToken, final EMCallBack callback) {
//		endCall();
//		EMChatManager.getInstance().logout(unbindDeviceToken, new EMCallBack() {
//
//			@Override
//			public void onSuccess() {
//				reset();
//				if (callback != null) {
//					callback.onSuccess();
//				}
//
//			}
//
//			@Override
//			public void onProgress(int progress, String status) {
//				if (callback != null) {
//					callback.onProgress(progress, status);
//				}
//			}
//
//			@Override
//			public void onError(int code, String error) {
//				if (callback != null) {
//					callback.onError(code, error);
//				}
//			}
//		});
//	}
//
//	/**
//	 * 获取消息通知类
//	 * 
//	 * @return
//	 */
//	public EaseNotifier getNotifier() {
//		return easeUI.getNotifier();
//	}
//
//	public BulanModel getModel() {
//		return (BulanModel) demoModel;
//	}
//
//	/**
//	 * 设置好友user list到内存中
//	 * 
//	 * @param contactList
//	 */
//	public void setContactList(Map<String, EaseUser> contactList) {
//		this.contactList = contactList;
//	}
//
//	void endCall() {
//		try {
//			EMChatManager.getInstance().endCall();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public void addSyncGroupListener(DataSyncListener listener) {
//		if (listener == null) {
//			return;
//		}
//		if (!syncGroupsListeners.contains(listener)) {
//			syncGroupsListeners.add(listener);
//		}
//	}
//
//	public void removeSyncGroupListener(DataSyncListener listener) {
//		if (listener == null) {
//			return;
//		}
//		if (syncGroupsListeners.contains(listener)) {
//			syncGroupsListeners.remove(listener);
//		}
//	}
//
//	public void addSyncContactListener(DataSyncListener listener) {
//		if (listener == null) {
//			return;
//		}
//		if (!syncContactsListeners.contains(listener)) {
//			syncContactsListeners.add(listener);
//		}
//	}
//
//	public void removeSyncContactListener(DataSyncListener listener) {
//		if (listener == null) {
//			return;
//		}
//		if (syncContactsListeners.contains(listener)) {
//			syncContactsListeners.remove(listener);
//		}
//	}
//
//	public void addSyncBlackListListener(DataSyncListener listener) {
//		if (listener == null) {
//			return;
//		}
//		if (!syncBlackListListeners.contains(listener)) {
//			syncBlackListListeners.add(listener);
//		}
//	}
//
//	public void removeSyncBlackListListener(DataSyncListener listener) {
//		if (listener == null) {
//			return;
//		}
//		if (syncBlackListListeners.contains(listener)) {
//			syncBlackListListeners.remove(listener);
//		}
//	}
//
//	/**
//	 * 同步操作，从服务器获取群组列表 该方法会记录更新状态，可以通过isSyncingGroupsFromServer获取是否正在更新
//	 * 和isGroupsSyncedWithServer获取是否更新已经完成
//	 * 
//	 * @throws EaseMobException
//	 */
//	public synchronized void asyncFetchGroupsFromServer(
//			final EMCallBack callback) {
//		if (isSyncingGroupsWithServer) {
//			return;
//		}
//
//		isSyncingGroupsWithServer = true;
//
//		new Thread() {
//			@Override
//			public void run() {
//				try {
//					EMGroupManager.getInstance().getGroupsFromServer();
//
//					// in case that logout already before server returns, we
//					// should return immediately
//					if (!EMChat.getInstance().isLoggedIn()) {
//						return;
//					}
//
//					isGroupsSyncedWithServer = true;
//					isSyncingGroupsWithServer = false;
//
//					// 通知listener同步群组完毕
//					noitifyGroupSyncListeners(true);
//					if (isContactsSyncedWithServer()) {
//						notifyForRecevingEvents();
//					}
//					if (callback != null) {
//						callback.onSuccess();
//					}
//				} catch (EaseMobException e) {
//					isGroupsSyncedWithServer = false;
//					isSyncingGroupsWithServer = false;
//					noitifyGroupSyncListeners(false);
//					if (callback != null) {
//						callback.onError(e.getErrorCode(), e.toString());
//					}
//				}
//
//			}
//		}.start();
//	}
//
//	public void noitifyGroupSyncListeners(boolean success) {
//		for (DataSyncListener listener : syncGroupsListeners) {
//			listener.onSyncComplete(success);
//		}
//	}
//
//	public void notifyContactsSyncListener(boolean success) {
//		for (DataSyncListener listener : syncContactsListeners) {
//			listener.onSyncComplete(success);
//		}
//	}
//
//	public void notifyBlackListSyncListener(boolean success) {
//		for (DataSyncListener listener : syncBlackListListeners) {
//			listener.onSyncComplete(success);
//		}
//	}
//
//	public boolean isSyncingGroupsWithServer() {
//		return isSyncingGroupsWithServer;
//	}
//
//	public boolean isSyncingContactsWithServer() {
//		return isSyncingContactsWithServer;
//	}
//
//	public boolean isSyncingBlackListWithServer() {
//		return isSyncingBlackListWithServer;
//	}
//
//	public boolean isGroupsSyncedWithServer() {
//		return isGroupsSyncedWithServer;
//	}
//
//	public boolean isContactsSyncedWithServer() {
//		return isContactsSyncedWithServer;
//	}
//
//	public boolean isBlackListSyncedWithServer() {
//		return isBlackListSyncedWithServer;
//	}
//
//	public synchronized void notifyForRecevingEvents() {
//		if (alreadyNotified) {
//			return;
//		}
//
//		// 通知sdk，UI 已经初始化完毕，注册了相应的receiver和listener, 可以接受broadcast了
//		EMChat.getInstance().setAppInited();
//		alreadyNotified = true;
//	}
//
//	synchronized void reset() {
//		isSyncingGroupsWithServer = false;
//		isSyncingContactsWithServer = false;
//		isSyncingBlackListWithServer = false;
//
//		isGroupsSyncedWithServer = false;
//		isContactsSyncedWithServer = false;
//		isBlackListSyncedWithServer = false;
//
//		alreadyNotified = false;
//		isGroupAndContactListenerRegisted = false;
//
//		setContactList(null);
//	}
//
//	public void pushActivity(Activity activity) {
//		easeUI.pushActivity(activity);
//	}
//
//	public void popActivity(Activity activity) {
//		easeUI.popActivity(activity);
//	}
//
//}
