package com.mingmay.bulan.ui;

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.mingmay.bulan.R;
import com.mingmay.bulan.adapter.SaveBuLanAdapter;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.DataBaseManager;
import com.mingmay.bulan.model.BuLanSaveModel;
import com.mingmay.bulan.ui.fragment.TextCreatorPage;
import com.mingmay.bulan.util.ToastUtil;

public class BulanSaves extends Activity {
	private ListView list;
	private SaveBuLanAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_cache_box);
		list = (ListView) findViewById(R.id.listview);
		((TextView) findViewById(R.id.title)).setText("草稿箱");
		loadSave();

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				BuLanSaveModel model = adapter.getItem(position);
				Intent i = new Intent(BulanSaves.this, TextCreatorPage.class);
				i.putExtra("localId", model.id);
				startActivity(i);
			}
		});
		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				final BuLanSaveModel model = adapter.getItem(arg2);
				Dialog d = new android.app.AlertDialog.Builder(BulanSaves.this)
						.setTitle("提示")
						.setMessage("删除草稿？")
						.setPositiveButton("删除",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										// TODO Auto-generated method stub
										DataBaseManager.getInstance()
												.deleteBulanSave(model.id);
										adapter.remove(model);
										ToastUtil.show("删除成功!");
									}
								}).setNegativeButton("取消", null).create();
				d.show();
				return true;
			}
		});
	}

	@Override
	protected void onResume() {
		if (CCApplication.caogaoxiangNeedRefresh) {
			loadSave();
			CCApplication.caogaoxiangNeedRefresh = false;
		}
		super.onResume();
	}

	public void back(View v) {
		finish();
	}

	public void loadSave() {
		List<BuLanSaveModel> saves = DataBaseManager.getInstance(this)
				.loadSaves();
		if (saves != null) {
			adapter = new SaveBuLanAdapter(this, saves);
			list.setAdapter(adapter);
		}
	}
}
