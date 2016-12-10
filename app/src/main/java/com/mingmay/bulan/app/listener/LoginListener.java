package com.mingmay.bulan.app.listener;

import com.mingmay.bulan.model.User;

public interface LoginListener {
	void onSuccess(User user);

	void onFailure(int code);
}
