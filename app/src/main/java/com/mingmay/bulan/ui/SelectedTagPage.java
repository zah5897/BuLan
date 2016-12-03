package com.mingmay.bulan.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.mingmay.bulan.R;
import com.mingmay.bulan.adapter.TagSelectedAdapter;
import com.mingmay.bulan.app.TagManager;
import com.mingmay.bulan.model.Tag;

public class SelectedTagPage extends Activity {
	private TagSelectedAdapter allTagAdapter;
	ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_all_tag_container);
		listView = (ListView) findViewById(R.id.webs);
		if (TagManager.allTags != null) {
			setAdapter();
		}
		findViewById(R.id.back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private void setAdapter() {
		if (allTagAdapter == null) {
			allTagAdapter = new TagSelectedAdapter(this, TagManager.allTags);
			listView.setAdapter(allTagAdapter);
		} else {
			allTagAdapter.notifyDataSetChanged();
		}
	}

	public void addTag(Tag item) {
		Intent data = new Intent();
		data.putExtra("tag", item);
		setResult(1, data);
		finish();
	}

	public void callBackTag(ArrayList<Tag> tags, int from) {
		if (tags == null) {
			return;
		}
		TagManager.allTags = tags;
		setAdapter();
	}

}
