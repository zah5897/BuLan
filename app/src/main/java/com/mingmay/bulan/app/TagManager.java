package com.mingmay.bulan.app;

import java.util.List;

import com.mingmay.bulan.model.Tag;

public class TagManager {
	private static TagManager tagManager = new TagManager();
	public static List<Tag> allTags;
	public static List<Tag> myTags;

	private TagManager() {
	}

	public static TagManager getInstance() {
		return tagManager;
	}
}
