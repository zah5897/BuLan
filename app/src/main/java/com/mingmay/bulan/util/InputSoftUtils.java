package com.mingmay.bulan.util;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

public class InputSoftUtils 
{
	public static void hiddenInputSoft(FragmentActivity activity,TextView item)
	{
		InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);  
		imm.hideSoftInputFromWindow(item.getWindowToken(), 0);
		//imm.hideSoftInputFromWindow(text.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
		//得到InputMethodManager的实例 
		if (imm.isActive()) { 
			activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			activity.getWindow().getAttributes().softInputMode=WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED;
		} 
	}
}
