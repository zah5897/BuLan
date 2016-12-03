//###
package com.mingmay.bulan.app.err;

//###
import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
public class CrashReportDialog extends Activity {
	private static final int CRASH_DIALOG_LEFT_ICON = android.R.drawable.ic_dialog_alert;
	private String mReportFileName = null;
	private String[] mReportEmail = null;
	private Bundle mCrashResources;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mReportFileName = getIntent().getStringExtra(
				ErrorReporter.EXTRA_REPORT_FILE_NAME);
		mReportEmail = getIntent().getStringArrayExtra(
				ErrorReporter.EXTRA_REPORT_EMAIL);
		if (TextUtils.isEmpty(mReportFileName)
				|| TextUtils.isEmpty(mReportEmail[0])) {
			finish();
		}
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		final CrashApplication application = CrashApplication.getInstance(this);
		mCrashResources = application.getCrashResources();

		final LinearLayout root = new LinearLayout(this);
		root.setOrientation(LinearLayout.VERTICAL);
		root.setPadding(10, 10, 10, 10);
		root.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));

		final ScrollView scroll = new ScrollView(this);
		root.addView(scroll, new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));

		final TextView text = new TextView(this);

		text.setText(mCrashResources
				.getString(CrashApplication.RES_DIALOG_TEXT));
		scroll.addView(text, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		final LinearLayout buttons = new LinearLayout(this);
		buttons.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		buttons.setPadding(buttons.getPaddingLeft(), 10,
				buttons.getPaddingRight(), buttons.getPaddingBottom());

		final Button yes = new Button(this);
		yes.setText(mCrashResources
				.getString(CrashApplication.RES_BUTTON_REPORT));
		yes.setOnClickListener(new View.OnClickListener() {

			public void onClick(final View v) {
				// Start email to send report
				sendEmail();
				exit();
			}

		});
		buttons.addView(yes, new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
		final Button no = new Button(this);
		no.setText(mCrashResources
				.getString(CrashApplication.RES_BUTTON_CANCEL));
		no.setOnClickListener(new View.OnClickListener() {

			public void onClick(final View v) {
				final File file = new File(mReportFileName);
				if (file.exists()) {
					file.delete();
				}
				exit();
			}

		});
		buttons.addView(no, new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));

		final String restartButtonText = mCrashResources
				.getString(CrashApplication.RES_BUTTON_RESTART);
		if (null != restartButtonText && restartButtonText.length() > 0) {
			final Button restart = new Button(this);
			restart.setText(restartButtonText);
			restart.setOnClickListener(new View.OnClickListener() {

				public void onClick(final View v) {
					application.onRestart();
					exit();
				}

			});
			buttons.addView(restart, new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
		}

		root.addView(buttons, new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		setContentView(root);

		setTitle(mCrashResources.getString(CrashApplication.RES_DIALOG_TITLE));

		final int resLeftIcon = mCrashResources
				.getInt(CrashApplication.RES_DIALOG_ICON);
		if (resLeftIcon != 0) {
			getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
					resLeftIcon);
		} else {
			getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
					CRASH_DIALOG_LEFT_ICON);
		}
	}

	private void sendEmail() {
		try {
			final Intent intent = new Intent(Intent.ACTION_SEND);
			// final String[] tos = new String[] {mReportEmail};
			intent.putExtra(Intent.EXTRA_EMAIL, mReportEmail);
			intent.putExtra(Intent.EXTRA_SUBJECT, mCrashResources
					.getString(CrashApplication.RES_EMAIL_SUBJECT));
			intent.putExtra(Intent.EXTRA_TEXT,
					mCrashResources.getString(CrashApplication.RES_EMAIL_TEXT));
			intent.putExtra(Intent.EXTRA_STREAM,
					Uri.parse("file://" + mReportFileName));
			intent.setType("text/plain");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void exit() {
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(10);
	}

}
