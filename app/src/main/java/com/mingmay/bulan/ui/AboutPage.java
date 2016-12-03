package com.mingmay.bulan.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.mingmay.bulan.R;
import com.mingmay.bulan.app.ParamManager;
import com.mingmay.bulan.task.CheckNewVersionTask;
import com.mingmay.bulan.util.ProgressDialogUtil;

public class AboutPage extends Activity implements OnClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_about);
		findViewById(R.id.to_feedback).setOnClickListener(this);
		findViewById(R.id.to_service_clause).setOnClickListener(this);
		findViewById(R.id.to_check_update).setOnClickListener(this);
		TextView versionText=(TextView) findViewById(R.id.version_text);
		versionText.setText("当前版本:v"+ParamManager.versionName);
	}

	public void back(View v) {
		finish();
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.to_score:
			break;
		case R.id.to_feedback:
			Intent i=new Intent(this, Submit.class);
			startActivity(i);
			break;
		case R.id.to_service_clause:
			Intent toServiceClause = new Intent(this, AboutServiceTipPage.class);
			startActivity(toServiceClause);
			break;
		case R.id.to_check_update:
			ProgressDialogUtil.showProgress(this, "正在检查版本...");
			CheckNewVersionTask task = new CheckNewVersionTask(this);
			task.execute();
			break;
		default:
			break;
		}

	}

}
