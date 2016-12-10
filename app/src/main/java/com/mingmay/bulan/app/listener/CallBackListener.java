package com.mingmay.bulan.app.listener;


public interface CallBackListener {
	void onSuccess(String tempId);

	void onFailure(int code);
}
