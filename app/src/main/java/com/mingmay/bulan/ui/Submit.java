package com.mingmay.bulan.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.mingmay.bulan.R;
import com.mingmay.bulan.task.ReportTask;
import com.mingmay.bulan.util.ToastUtil;

public class Submit extends Activity {
	EditText input;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_submit);
		input = (EditText) findViewById(R.id.input);
	}

	public void back(View v) {
		finish();
	}

	public void submit(View v) {
		String content = input.getText().toString();
		if (TextUtils.isEmpty(content)) {
			ToastUtil.show("请输入内容...");
			return;
		}
		ReportTask task = new ReportTask(this);
		task.execute(content);
	}
}
