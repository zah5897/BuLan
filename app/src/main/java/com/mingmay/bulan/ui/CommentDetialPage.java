package com.mingmay.bulan.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.mingmay.bulan.R;
import com.mingmay.bulan.model.BuLanModel;
import com.mingmay.bulan.model.CommentInfo;
import com.mingmay.bulan.task.CommentTask;
import com.mingmay.bulan.task.LoadCommentTask;
import com.mingmay.bulan.util.ImageLoadUtil;
import com.mingmay.bulan.util.ToastUtil;

public class CommentDetialPage extends Activity {
	private EditText commentView;
	private CommentInfo model;
	private BuLanModel bulan;
	private ListView listview;
	private View footView;
	private boolean isLoading = false;
	private boolean hasMore = true;

	private SimpleCommentAdapter adapter;
	private int pageIndex = 1;

	private CommentInfo currentComm;

	private TextView toReplay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_bulan_comment);
		model = (CommentInfo) getIntent().getSerializableExtra("comment");
		bulan = (BuLanModel) getIntent().getSerializableExtra("bulan");
		initView();
		loadComments();
		setCommentParentInfo();
	}

	private void initView() {
		toReplay = (TextView) findViewById(R.id.to_comm);
		toReplay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				toReplay.setText("");
				currentComm = null;
			}
		});
		findViewById(R.id.comment_parent).setVisibility(View.VISIBLE);
		((TextView) findViewById(R.id.name)).setText(model.createUserName);
		((TextView) findViewById(R.id.time)).setText((bulan.createDate));
		((TextView) findViewById(R.id.comment_content))
				.setText(model.commentText);

		listview = (ListView) findViewById(R.id.comment_list);
		commentView = (EditText) findViewById(R.id.comment_input);
		commentView.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
				if (actionId == EditorInfo.IME_ACTION_SEND) {
					hideKeyboard();
					String commtenStr = v.getText().toString().trim();
					if (TextUtils.isEmpty(commtenStr)) {
						ToastUtil.show("请输入内容");
						return true;
					}
					CommentTask task = new CommentTask(CommentDetialPage.this);
					if (currentComm != null) {
						task.execute(String.valueOf(bulan.bulanId), commtenStr,
								currentComm.createUserId + "", model.commentId
										+ "");
					} else {
						task.execute(String.valueOf(bulan.bulanId), commtenStr,
								"", model.commentId + "");
					}

					return true;
				}
				return false;
			}
		});
		footView = getLayoutInflater().inflate(R.layout.foot_view, null);
		listview.addFooterView(footView);
		listview.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// 当不滚动时
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					// 判断是否滚动到底部
					if (view.getLastVisiblePosition() == view.getCount() - 1) {
						// 加载更多功能的代码
						if (isLoading || !hasMore) {
							return;
						}
						listview.addFooterView(footView);
						pageIndex++;
						loadComments();
					}
				}
			}

			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {

			}
		});
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				currentComm = adapter.getItem(arg2);
				toReplay.setText("@" + currentComm.createUserName);

			}
		});
	}

	private void setCommentParentInfo() {
		ImageLoadUtil.load(this, (ImageView) findViewById(R.id.comment_icon),
				model.createUserImg);
	}

	public void loadComments() {
		isLoading = true;
		LoadCommentTask t = new LoadCommentTask(this);
		t.execute(String.valueOf(bulan.bulanId), pageIndex + "",
				model.commentId + "");
	}

	public void hideKeyboard() {
		// 隐藏输入法
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		// 显示或者隐藏输入法
		imm.hideSoftInputFromWindow(commentView.getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	public void back(View v) {
		finish();
	}

	public void commentCallBack(CommentInfo info) {
		if (info == null) {
			return;
		}
		commentView.setText("");
		toReplay.setText("");
		currentComm = null;
		if (adapter == null) {
			List<CommentInfo> comments = new ArrayList<CommentInfo>();
			comments.add(info);
			adapter = new SimpleCommentAdapter(comments);
			listview.setAdapter(adapter);
		} else {
			adapter.add(info);
		}
	}

	public void callBack(ArrayList<CommentInfo> comments) {
		isLoading = false;
		findViewById(R.id.comm_loading).setVisibility(View.GONE);
		if (comments == null && adapter == null) {
			ToastUtil.show("没有评论..");
			hasMore = false;
		} else if (adapter == null) {
			adapter = new SimpleCommentAdapter(comments);
			listview.setAdapter(adapter);
		} else if (comments == null) {
			hasMore = false;
			ToastUtil.show("没有更多数据了");
		} else {
			adapter.add(comments);
		}
		if (listview.getFooterViewsCount() > 0) { // 如果有底部视图
			listview.removeFooterView(footView);
		}
	}

	class SimpleCommentAdapter extends BaseAdapter {
		List<CommentInfo> comments;

		public SimpleCommentAdapter(List<CommentInfo> comments) {
			this.comments = comments;
		}

		public void add(ArrayList<CommentInfo> comments2) {
			// TODO Auto-generated method stub
			this.comments.addAll(comments2);
			notifyDataSetChanged();
		}

		public void add(CommentInfo info) {
			// TODO Auto-generated method stub
			this.comments.add(0, info);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return comments.size();
		}

		@Override
		public CommentInfo getItem(int arg0) {
			// TODO Auto-generated method stub
			return comments.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub

			TextView commView = new TextView(CommentDetialPage.this);
			CommentInfo info = getItem(arg0);
			String text = "<font color='#FF8E32'>" + info.createUserName
					+ "</font>";

			if (info.commentUserId > 0) {
				text += ":@<font color='#FF8E32'>" + info.commentUserName
						+ "</font>";
			}
			text += ":" + info.commentText;
			commView.setText(Html.fromHtml(text));
			commView.setTextSize(14);
			commView.setPadding(20, 5, 5, 5);
			return commView;
		}

	}
}
