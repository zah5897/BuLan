package com.mingmay.bulan.app.listener;

import java.util.List;

import com.mingmay.bulan.model.User;

public interface ContactListener {
	void onSuccess(List<User> users);

	void onFailure(int code);
}
