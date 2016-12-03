package com.mingmay.bulan.task;

import android.os.AsyncTask;

import com.mingmay.bulan.app.CCApplication;

public class UploadVideoTask extends AsyncTask<String, String, String> {

	
	@Override
	protected String doInBackground(String... arg0) {
		String URL = CCApplication.HTTPSERVER + "/m_file! addVideo.action";
//		String localPath = params[0];
//		String remoteName = params[1];
//		if (!FileUtil.isExist(localPath)) {
//			FileUtil.toFile(data, localPath);
//		}
//		progress = 0;
//		try {
//			MultipartEntity param = new MultipartEntity();
//			param.addPart("file", new FileBody(new File(localPath)));
//			param.addPart("imgName", new StringBody(remoteName));
//			param.addPart("tempId", new StringBody(tempId));
//			HttpResponse response = new HttpProxy().post(URL, param);
//			int code = response.getStatusLine().getStatusCode();
//			progress = 100;
//
//			DataBaseManager.getInstance().deleteFileUpload(tempId, remoteName);
//            CCApplication.uploadImageTask.remove(tempId);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return "";
		
	}

}
