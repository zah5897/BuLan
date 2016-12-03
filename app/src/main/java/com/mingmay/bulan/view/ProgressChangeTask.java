package com.mingmay.bulan.view;

import android.os.AsyncTask;
import android.widget.ProgressBar;

public class ProgressChangeTask extends AsyncTask<Void, Void, Void> {

	private static final int DURATION_MS = 200;
	private static final int SLEEP_MS = 40;
	private static float SPEED = -1;
	private static int CURRENT_PROGRESS;
	private ProgressBar mProgressBar;
	private int newProgress;

	public ProgressChangeTask(ProgressBar mProgressBar, int newProgress) {
		this.mProgressBar = mProgressBar;
		this.newProgress = newProgress;

	}
	
	@Override
	protected void onPreExecute() {
		CURRENT_PROGRESS = mProgressBar.getProgress();
		SPEED = ((float) (newProgress - CURRENT_PROGRESS)) / DURATION_MS
				* SLEEP_MS;
		 
	}

	@Override
	protected Void doInBackground(Void... params) {
		 
		while (SPEED>0 ? newProgress>CURRENT_PROGRESS : newProgress<CURRENT_PROGRESS) {
			try {
				Thread.sleep(SLEEP_MS);
				CURRENT_PROGRESS += SPEED;
				publishProgress();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		mProgressBar.setProgress(CURRENT_PROGRESS);
	}

}
