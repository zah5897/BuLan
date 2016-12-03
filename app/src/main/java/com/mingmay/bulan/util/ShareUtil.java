package com.mingmay.bulan.util;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class ShareUtil {

	public static void share(Context context, String summary, File imagePath) {
		Intent intent=new Intent(Intent.ACTION_SEND); 
		intent.setType("image/*");
		intent.putExtra(Intent.EXTRA_SUBJECT, "分享"); 
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imagePath));
		intent.putExtra(Intent.EXTRA_TEXT, summary);  
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		context.startActivity(Intent.createChooser(intent, "Mbox分享")); 
	}
	public static void share(Context context, String summary) {
		Intent intent=new Intent(Intent.ACTION_SEND); 
		intent.setType("text/plain"); 
		intent.putExtra(Intent.EXTRA_SUBJECT, "分享"); 
		intent.putExtra(Intent.EXTRA_TEXT, summary);  
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		context.startActivity(Intent.createChooser(intent, "Mbox分享")); 
	}
}
