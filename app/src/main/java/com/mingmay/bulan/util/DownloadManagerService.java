package com.mingmay.bulan.util;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.webkit.MimeTypeMap;

public class DownloadManagerService {

	@SuppressWarnings("deprecation")
	public void downloadImage(Context context,String url,String fileName) {
		DownloadManager	downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);  
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context); 
		Uri resource = Uri.parse(url);
		DownloadManager.Request request = new DownloadManager.Request(resource);
		request.setAllowedNetworkTypes(Request.NETWORK_MOBILE
				| Request.NETWORK_WIFI);
		request.setAllowedOverRoaming(false);
		// 设置文件类型
		MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
		String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap
				.getFileExtensionFromUrl(url));
		request.setMimeType(mimeString);
		// 在通知栏中显示
		request.setShowRunningNotification(true);
		request.setVisibleInDownloadsUi(true);
		request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		// sdcard的目录下的download文件夹
		request.setDestinationInExternalFilesDir(context,"download/",fileName);
		request.setTitle("Mbox图片下载");
		long id = downloadManager.enqueue(request);
		// 保存id
		prefs.edit().putLong("downloadId", id).commit();
	}

}
