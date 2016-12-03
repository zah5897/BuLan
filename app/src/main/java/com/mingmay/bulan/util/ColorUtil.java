package com.mingmay.bulan.util;

import java.util.Random;

import android.graphics.Color;

public class ColorUtil {

	public static int getRandomColor() {
		Random ran = new Random();
		int r = ran.nextInt(100) + 155;
		int g = ran.nextInt(120) + 135;
		int b = ran.nextInt(110) + 145;
		return Color.argb(1, r, g, b);
	}

}
