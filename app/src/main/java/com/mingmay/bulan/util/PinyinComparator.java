package com.mingmay.bulan.util;

import java.util.Comparator;

import com.mingmay.bulan.model.User;

/**
 * 
 * @author xiaanming
 * 
 */
public class PinyinComparator implements Comparator<User> {

	public int compare(User o1, User o2) {
		if (o1.firstChar.equals("@") || o2.firstChar.equals("#")) {
			return -1;
		} else if (o1.firstChar.equals("#") || o2.firstChar.equals("@")) {
			return 1;
		} else {
			return o1.firstChar.compareTo(o2.firstChar);
		}
	}

}
