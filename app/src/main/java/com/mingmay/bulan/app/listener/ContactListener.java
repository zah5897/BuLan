package com.mingmay.bulan.app.listener;

import java.util.List;

import com.mingmay.bulan.model.User;

public interface ContactListener {
	public void onSuccess(List<User> users);

	public void onFailure(int code);
}
