package com.mingmay.bulan.app.listener;

import com.mingmay.bulan.model.User;

public interface LoginListener {
	public void onSuccess(User user);

	public void onFailure(int code);
}
