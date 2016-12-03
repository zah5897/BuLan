package com.mingmay.bulan.util;

import java.io.File;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class URIUtil {
	public static String toPath(Context context, Uri selectedImage) {
		// String[] filePathColumn = { MediaStore.Images.Media.DATA };
		Cursor cursor = context.getContentResolver().query(selectedImage, null,
				null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex("_data");
			String picturePath = cursor.getString(columnIndex);
			cursor.close();
			cursor = null;
			return picturePath;

		} else {
			File file = new File(selectedImage.getPath());
			if (file.exists()) {
				return file.getAbsolutePath();
			}
		}
		return null;
	}
	@SuppressLint("NewApi")
	public static String  uriToPath(Context context, Uri selectedImage) {
		String filePath = null;
		if(DocumentsContract.isDocumentUri(context, selectedImage)){
		    String wholeID = DocumentsContract.getDocumentId(selectedImage);
		    String id = wholeID.split(":")[1];
		    String[] column = { MediaStore.Images.Media.DATA };
		    String sel = MediaStore.Images.Media._ID +"=?";
		    Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column,
		            sel, new String[] { id }, null);
		    int columnIndex = cursor.getColumnIndex(column[0]);
		    if (cursor.moveToFirst()) {
		        filePath = cursor.getString(columnIndex);
		    }
		    cursor.close();
		}else{
		    String[] projection = { MediaStore.Images.Media.DATA };
		    Cursor cursor = context.getContentResolver().query(selectedImage, projection, null, null, null);
		    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		    cursor.moveToFirst();
		    filePath = cursor.getString(column_index);
		}
		return filePath;

	}
}
