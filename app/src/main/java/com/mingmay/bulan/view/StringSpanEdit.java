package com.mingmay.bulan.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.widget.EditText;

public class StringSpanEdit extends EditText {

	public StringSpanEdit(Context context) {
		super(context);
	}

	public StringSpanEdit(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * ���ı��������ͼƬ
	 */
	public void insertImgForText(Bitmap bitmap, String imgPath) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		int sw, sh;
		int vw = getWidth();
		int vh;

		vh = (int) (vw / (w * 1.0f / h));
		sw = vw;
		sh = vh;

		Drawable drawable = new BitmapDrawable(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
		SpannableString ss = new SpannableString(imgPath);
		ss.setSpan(imageSpan, 0, imgPath.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		append("\n");
		append(ss);
		append("\n");
		setSelection(this.getText().toString().length());
		// setSpanContent(this.getText().toString());
	}

	// /**
	// * Ϊ�ı������ô�ͼ�Ļ��ŵ�����
	// * @param content Ҫ���õ�����
	// */
	// public void setSpanContent(String content){
	// String patternStr = Environment.getExternalStorageDirectory()
	// + "/" +Cr.IMG_DIR + "/.+?\\.\\w{3}";
	// Pattern pattern = Pattern.compile(patternStr);
	// Matcher m = pattern.matcher(content);
	// SpannableString ss = new SpannableString(content);
	// while(m.find()){
	// Bitmap bmp = BitmapFactory.decodeFile(m.group());
	// Bitmap bitmap = BitmapTools.getScaleBitmap(bmp, 0.2f, 0.2f);
	// if(bmp != null){
	// bmp.recycle();
	// }
	// ImageSpan imgSpan = new ImageSpan(bitmap, ImageSpan.ALIGN_BASELINE);
	// ss.setSpan(imgSpan, m.start(), m.end(),
	// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	// }
	// this.setText(ss);
	// }
}
