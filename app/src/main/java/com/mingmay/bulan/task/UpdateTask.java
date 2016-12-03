package com.mingmay.bulan.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

public class UpdateTask extends AsyncTask<String, Integer, Integer> {
	private Context main;
	ProgressDialog progress;
	private long length;
	private boolean isSetMax = false;
	private boolean isCanceled = false;
	private File temp;

	public UpdateTask(Context main) {
		this.main = main;
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		progress = new ProgressDialog(main);
		progress.setTitle("下载中...");
		progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progress.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				isCanceled = true;
			}
		});
		progress.show();
		super.onPreExecute();
	}

	@Override
	protected Integer doInBackground(String... params) {
		try {

			HttpURLConnection connection = null;

			URL url = new URL(params[0]);

			connection = (HttpURLConnection) url.openConnection();
			length = connection.getContentLength();
			InputStream in = connection.getInputStream();
			temp = createTempApk();
			OutputStream out = new FileOutputStream(temp);
			byte[] buffer = new byte[1024];
			int count = 0;
			while ((count = in.read(buffer)) != -1) {
				if (isCanceled) {
					if (temp.exists())
						temp.delete();
					return 0;
				}
				out.write(buffer, 0, count);
				publishProgress(count);
			}
			out.flush();
			out.close();
			in.close();
			renameTempApk(temp);
			return 0;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			if (temp.exists())
				temp.delete();
			return -1;
		}

	}

	public void renameTempApk(File tmp) {
		File parent = new File(Environment.getExternalStorageDirectory()
				.getAbsoluteFile() + "/bulan/temp/");
		parent.mkdirs();
		File f = new File(parent.getAbsoluteFile() + "/bulan.apk");
		tmp.renameTo(f);
	}

	public File createTempApk() throws IOException {
		File parent = new File(Environment.getExternalStorageDirectory()
				.getAbsoluteFile() + "/bulan/temp/");
		parent.mkdirs();
		File f = new File(parent.getAbsoluteFile() + "/bulan.apk.tmp");
		if (f.exists()) {
			f.delete();
			f.createNewFile();
		}
		return f;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
		if (!isSetMax) {
			isSetMax = true;
			progress.setMax((int) length / 1024);
		}
		progress.incrementProgressBy(values[0] / 1024);

	}

	public File checkApkExist() {
		File f = new File(Environment.getExternalStorageDirectory()
				.getAbsoluteFile() + "/bulan/temp/bulan.apk");
		if (f.exists()) {
			return f;
		} else {
			return null;
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		if (progress != null) {
			progress.dismiss();
		}
		if (result != null && result == 0) {
			File f = checkApkExist();
			if (f != null) {
				Intent intent = new Intent();
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setAction(android.content.Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(f),
						"application/vnd.android.package-archive");
				main.startActivity(intent);
			} else {
				Toast.makeText(main, "更新失败...", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(main, "更新失败...", Toast.LENGTH_SHORT).show();
		}

	}

}
