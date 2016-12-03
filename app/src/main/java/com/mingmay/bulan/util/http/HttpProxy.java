package com.mingmay.bulan.util.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.mingmay.bulan.app.ParamManager;

public class HttpProxy {

	public static final int CONNECTION_TIMEOUT = 10000;

	public HttpResponse post(String URL, List<NameValuePair> params)
			throws ClientProtocolException, IOException {
		params.addAll(ParamManager.headers);
		HttpPost httpPost = new HttpPost(URL);
		// httpPost.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
		// CONNECTION_TIMEOUT);
		HttpResponse httpResponse = null;
		httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
		httpResponse = new DefaultHttpClient().execute(httpPost);
		return httpResponse;
	}

	public void asyPost(String url, RequestParams param,
			ResponseHandlerInterface responseHandler) {
		AsyncHttpClient client = new AsyncHttpClient();

		if (param == null) {
			param = new RequestParams();
		}
		for (NameValuePair nv : ParamManager.headers) {
			param.put(nv.getName(), nv.getName());
		}
		client.post(url, param, responseHandler);
	}

	public HttpResponse post(String URL, MultipartEntity params)
			throws ClientProtocolException, IOException {
		for (NameValuePair pair : ParamManager.headers) {
			params.addPart(pair.getName(), new StringBody(pair.getValue()));
		}

		HttpPost httpPost = new HttpPost(URL);
		// httpPost.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
		// CONNECTION_TIMEOUT);
		HttpResponse httpResponse = null;
		httpPost.setEntity(params);
		httpResponse = new DefaultHttpClient().execute(httpPost);
		return httpResponse;
	}

	public static byte[] downloadImage(String imageUrl) throws Exception {
		return urlToByte(imageUrl);
	}

	public static String urlToString(String urlStr) throws Exception {
		try {
			return new String(urlToByte(urlStr), "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static byte[] urlToByte(String urlStr) throws Exception {
		HttpURLConnection connection = null;
		try {
			URL url = new URL(urlStr);

			connection = (HttpURLConnection) url.openConnection();

			InputStream in = connection.getInputStream();
			ByteArrayOutputStream bout = new ByteArrayOutputStream(
					connection.getContentLength());
			int len = 0;
			byte[] buffer = new byte[1024];
			while ((len = in.read(buffer)) != -1) {
				bout.write(buffer, 0, len);
			}

			return bout.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}

		}
	}
}
