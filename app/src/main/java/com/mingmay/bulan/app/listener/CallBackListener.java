package com.mingmay.bulan.app.listener;


public interface CallBackListener {
	public void onSuccess(String tempId);

	public void onFailure(int code);
}
