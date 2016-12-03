package com.mingmay.bulan.view;

import com.mingmay.bulan.model.User;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;

public class AtSpan extends SpannableString {

	public AtSpan(CharSequence source, User user) {
		super(source);
		String nick_name = "@" + user.firstName + " ";
		setSpan(new BackgroundColorSpan(Color.GREEN), 0, nick_name.length(),
				Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

	}

}
