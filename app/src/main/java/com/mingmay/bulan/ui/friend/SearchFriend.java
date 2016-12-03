package com.mingmay.bulan.ui.friend;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.mingmay.bulan.R;
import com.mingmay.bulan.adapter.FriendAdapter;
import com.mingmay.bulan.model.User;
import com.mingmay.bulan.task.SearchFriendTask;
import com.mingmay.bulan.ui.FriendInfoPage;

public class SearchFriend extends Activity {
	private EditText input;
	private ListView listview;
	private FriendAdapter adapter;
	int cursor = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_search_friend);
		findViewById(R.id.left_btn).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		listview = (ListView) findViewById(R.id.friend_listview);
		input = (EditText) findViewById(R.id.search);
		input.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		input.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {

				if (arg1 == EditorInfo.IME_ACTION_SEARCH) {
					String key = arg0.getText().toString();
					if (TextUtils.isEmpty(key)) {
						input.setText("");
					} else {
						SearchFriendTask task = new SearchFriendTask(
								SearchFriend.this);
						task.execute(key, String.valueOf(cursor));
						findViewById(R.id.loading).setVisibility(View.VISIBLE);
					}
					return true;
				}

				return false;
			}
		});
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Intent i = new Intent(SearchFriend.this, FriendInfoPage.class);
				i.putExtra("friend", adapter.getItem(arg2));
				startActivity(i);
			}
		});
	}

	public void callBack(ArrayList<User> users) {
		findViewById(R.id.loading).setVisibility(View.GONE);
		if (users != null) {
			adapter = new FriendAdapter(this, users);
			listview.setAdapter(adapter);
		}
	}
}
