package com.mingmay.bulan.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.mingmay.bulan.R;
import com.mingmay.bulan.adapter.CommentAdapter;
import com.mingmay.bulan.model.BuLanModel;
import com.mingmay.bulan.model.CommentInfo;
import com.mingmay.bulan.task.CommentTask;
import com.mingmay.bulan.task.LoadCommentTask;
import com.mingmay.bulan.util.ToastUtil;

public class CommentPage extends Activity {
	private TextView bulanTitle;

	private EditText commentView;
	private BuLanModel model;

	private ListView listview;
	private int pageIndex = 1;
	private View footView;
	private boolean isLoading = false;
	private boolean hasMore = true;

	private CommentAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_bulan_comment);
		model = (BuLanModel) getIntent().getSerializableExtra("bulan");
		bulanTitle = (TextView) findViewById(R.id.title);
		((TextView) findViewById(R.id.name)).setText(model.bulanTitle);
		((TextView) findViewById(R.id.time)).setText((model.createDate));
		((TextView) findViewById(R.id.comment_content))
				.setText(model.commentCount + "");
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
					CommentTask task = new CommentTask(CommentPage.this);
					task.execute(String.valueOf(model.bulanId), commtenStr);
					return true;
				}
				return false;
			}
		});
		bulanTitle.setText(model.bulanTitle);
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
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent toDetial = new Intent(CommentPage.this,
						CommentDetialPage.class);
				toDetial.putExtra("comment", adapter.getItem(position));
				toDetial.putExtra("bulan", model);
				startActivity(toDetial);
			}
		});
		loadComments();
	}

	public void hideKeyboard() {
		// 隐藏输入法
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		// 显示或者隐藏输入法
		imm.hideSoftInputFromWindow(commentView.getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	public void loadComments() {
		isLoading = true;
		LoadCommentTask t = new LoadCommentTask(this);
		t.execute(String.valueOf(model.bulanId), pageIndex + "");
	}

	public void back(View v) {
		finish();
	}

	public void commentCallBack(CommentInfo info) {
		if (adapter == null) {
			List<CommentInfo> comments = new ArrayList<CommentInfo>();
			comments.add(info);
			adapter = new CommentAdapter(this, comments);
			listview.setAdapter(adapter);
		} else {
			adapter.add(info);
		}
		commentView.setText("");
	}

	public void callBack(ArrayList<CommentInfo> comments) {
		isLoading = false;
		findViewById(R.id.comm_loading).setVisibility(View.GONE);
		if (comments == null && adapter == null) {
			ToastUtil.show("没有评论..");
			hasMore = false;
		} else if (adapter == null) {
			adapter = new CommentAdapter(this, comments);
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
}
