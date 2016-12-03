package com.mingmay.bulan.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mingmay.bulan.R;
import com.mingmay.bulan.ui.BulanDetialPage;
import com.mingmay.bulan.util.ImageLoadUtil;

public class BulanEditModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BulanEditModel(JSONObject optJSONObject) {
		this.type = optJSONObject.optInt("type");
		this.content = optJSONObject.optString("content");
		this.filename = optJSONObject.optString("filename");
	}

	public BulanEditModel(int type, String content) {
		this.type = type;
		this.content = content;
	}

	public int type;
	public String content;
	public String originalPic;
	public String filename;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public JSONObject toPublishJson() {
		JSONObject obj = new JSONObject();
		try {
			if (type == 1) {
				obj.put("type", type);
				obj.put("content", filename);

			} else {
				obj.put("type", type);
				obj.put("content", content);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}

	@Override
	public String toString() {
		return "{\"type\":" + type + ",\"content\":" + content
				+ ",\"filename\":" + filename + "}";
	}

	public View getElementView(BulanDetialPage bulanDetialPage) {
		if (type == 1) {
			// 图片
			ImageView image = new ImageView(bulanDetialPage);
			image.setAdjustViewBounds(true);
			return image;
		} else {
			// 文本
			TextView tx = new TextView(bulanDetialPage);
			tx.setText(content);
			return tx;
		}
	}

	public View addItem(BulanDetialPage bulanDetialPage, LinearLayout container) {
		if (type == 1) {
			// 图片
			ImageView image = new ImageView(bulanDetialPage);
			image.setAdjustViewBounds(true);
			// image.setImageResource(R.drawable.default_image);
			// ImageLoader.getInstance().displayImage(content, image);

			ImageLoadUtil.load(bulanDetialPage, image, content);
			container.addView(image, -1, -2);
			return image;
		} else {
			// 文本
			TextView tx = new TextView(bulanDetialPage);
			tx.setTextSize(18);
			tx.setTextColor(bulanDetialPage.getResources().getColor(R.color.bulan_content_color));
			tx.setText(content);
			container.addView(tx, -1, -2);
			return tx;
		}
	}
}
