package com.mingmay.bulan.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.TextView;

public class TagView extends TextView {
	public TagView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public TagView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TagView(Context context) {
		super(context);
		init();
	}
	
	private void init(){
		setTextSize(14);
		setTextColor(Color.WHITE);
		setPadding(15, 8, 15, 8);
	}
	
}
