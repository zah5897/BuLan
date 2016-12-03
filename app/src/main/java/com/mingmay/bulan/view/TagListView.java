package com.mingmay.bulan.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import com.mingmay.bulan.R;
import com.mingmay.bulan.app.TagManager;
import com.mingmay.bulan.model.Tag;
import com.mingmay.bulan.ui.SelectedTagPage;
import com.mingmay.bulan.ui.fragment.TextCreatorPage;

/**
 * @author lhxia
 * 
 */
public class TagListView extends FlowLayout {

	private List<Tag> mTags = new ArrayList<Tag>();

	private List<TagView> childs = new ArrayList<TagView>();

	public SelectedTagPage selectedTagPage;
	public TextCreatorPage createPage;

	/**
	 * @param context
	 */
	public TagListView(Context context) {
		super(context);
		init();
	}

	/**
	 * @param context
	 * @param attributeSet
	 */
	public TagListView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		init();
	}

	/**
	 * @param context
	 * @param attributeSet
	 * @param defStyle
	 */
	public TagListView(Context context, AttributeSet attributeSet, int defStyle) {
		super(context, attributeSet, defStyle);
		init();
	}

	private void init() {
		refreshTags();
	}

	public void setTags(SelectedTagPage selectedTagPage, List<Tag> mTags) {
		this.selectedTagPage = selectedTagPage;
		if (mTags == null) {
			mTags = new ArrayList<Tag>();
		}
		this.mTags = mTags;
		refreshTags();
	}

	public void setTags(TextCreatorPage createpage, List<Tag> mTags) {
		this.createPage = createpage;
		if (mTags == null) {
			mTags = new ArrayList<Tag>();
		}
		this.mTags = mTags;
		refreshTags();
	}

	private void refreshTags() {
		if (mTags == null) {
			mTags = new ArrayList<Tag>();
		}
		int visibleCount = mTags.size();
		int delta = visibleCount - childs.size();
		for (int i = 0; i < delta; i++) {

			TagView tv = new TagView(getContext());
			tv.setGravity(Gravity.CENTER);
			addView(tv);
			childs.add(tv);

		}

		for (int i = visibleCount; i < childs.size(); i++) {
			childs.get(i).setVisibility(View.GONE);
		}

		for (int i = 0; i < visibleCount; i++) {
			TagView view = childs.get(i);
			view.setVisibility(View.VISIBLE);
			final Tag tag = mTags.get(i);
			view.setBackgroundResource(tag.bgResId);
			view.setText(tag.name);
			if (TagManager.myTags!=null&&TagManager.myTags.contains(tag)) {
				view.setTextColor(getResources().getColor(R.color.tag_selected));
			} else {
				view.setTextColor(getResources().getColor(R.color.white));
				view.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						if (createPage != null) {
							//createPage.addTag(tag);
						} else {
							selectedTagPage.addTag(tag);
						}
					}
				});
			}
		}
	}
}