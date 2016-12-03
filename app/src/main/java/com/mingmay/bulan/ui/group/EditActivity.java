package com.mingmay.bulan.ui.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.mingmay.bulan.R;
import com.mingmay.bulan.base.BaseActivity;

public class EditActivity extends BaseActivity {
	private EditText editText;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.em_activity_edit);

		editText = (EditText) findViewById(R.id.edittext);
		String title = getIntent().getStringExtra("title");
		String data = getIntent().getStringExtra("data");
		if (data != null) {
			editText.setText(data);
			editText.setSelection(editText.length());
		}
	}

	public void save(View view) {
		setResult(RESULT_OK,
				new Intent().putExtra("data", editText.getText().toString()));
		finish();
	}

	public void back(View view) {
		finish();
	}
}
