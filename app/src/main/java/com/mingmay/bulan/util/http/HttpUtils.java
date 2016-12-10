package com.mingmay.bulan.util.http;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.HttpGet;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mingmay.bulan.app.CCApplication;
import com.mingmay.bulan.app.ParamManager;
import com.mingmay.bulan.app.err.ErrorCode;
import com.mingmay.bulan.util.ToastUtil;

public class HttpUtils {
	private static WeakReference<AsyncHttpClient> httpClientRef = new WeakReference<AsyncHttpClient>(
			null);

	private static WeakReference<AsyncHttpClient> getHttpClientRef() {
		return httpClientRef;
	}

	public synchronized static AsyncHttpClient getAsyncHttpClient() {
		AsyncHttpClient httpClient = httpClientRef.get();
		if (httpClient == null) {
			httpClient = new AsyncHttpClient();
			httpClient.setTimeout(20 * 1000);
			httpClient.setMaxRetriesAndTimeout(0, 500);
			httpClientRef = new WeakReference<AsyncHttpClient>(httpClient);
		}
		return httpClient;
	}

	public static HttpResponse get(String URL) throws
			IOException {
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
		HttpConnectionParams.setSoTimeout(httpParams, 20000);
		DefaultHttpClient client = new DefaultHttpClient(httpParams);
		HttpGet httpGet = new HttpGet(URL);
		return client.execute(httpGet);
	}

	public static void post(String url, RequestParams params,
			final JsonHttpResponseHandler asyncHttpResponseHandler) {
		if (params == null) {
			params = new RequestParams();
		}
		for (NameValuePair pair : ParamManager.headers) {
			params.add(pair.getName(), pair.getValue());
		}
		if (isNetworkConnected()) {
			getAsyncHttpClient().post(url, params,
					new JsonHttpResponseHandler() {
						@Override
						public void onSuccess(int statusCode, Header[] headers,
								JSONObject response) {
							super.onSuccess(statusCode, headers, response);
							// boolean isLogin =
							// UserManager.getInstance().checkLogin(response);
							// if (isLogin) {
							asyncHttpResponseHandler.onSuccess(statusCode,
									headers, response);
							// }
						}

						@Override
						public void onSuccess(int statusCode, Header[] headers,
								JSONArray response) {
							asyncHttpResponseHandler.onSuccess(statusCode,
									headers, response);
						}

						@Override
						public void onFailure(int statusCode, Header[] headers,
								String responseString, Throwable throwable) {
							asyncHttpResponseHandler.onFailure(statusCode,
									headers, responseString, throwable);
						}

						@Override
						public void onFailure(int statusCode, Header[] headers,
								Throwable throwable, JSONArray errorResponse) {
							// TODO Auto-generated method stub
							asyncHttpResponseHandler.onFailure(statusCode,
									headers, "", throwable);
						}

						@Override
						public void onFailure(int statusCode, Header[] headers,
								Throwable throwable, JSONObject errorResponse) {
							// TODO Auto-generated method stub
							asyncHttpResponseHandler.onFailure(statusCode,
									headers, "", throwable);
						}

						@Override
						public void onFinish() {
							super.onFinish();
						}
					});
		} else {
			String responseString = "网络不可用";
			asyncHttpResponseHandler.onFailure(ErrorCode.NONETWORK_ERROR, null,
					responseString, null);
			ToastUtil.show(responseString);
		}

	}

	// public static List<?> arrayToModel(JSONArray array, Class clazz) {
	// int len = array.length();
	// List<Object> list = new ArrayList<Object>();
	// Gson g = new Gson();
	// for (int i = 0; i < len; i++) {
	// list.add(g.fromJson(array.optJSONObject(i).toString(), clazz));
	// }
	// return list;
	// }

	public static boolean isNetworkConnected() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) CCApplication.app
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (mNetworkInfo != null) {
			return mNetworkInfo.isAvailable();
		}
		return false;
	}
	// private JsonHttpResponseHandler defaultJsonHttpResponseHandler=new
	// JsonHttpResponseHandler(){
	// @Override
	// public void onSuccess(int statusCode, Header[] headers, JSONObject
	// response) {
	// super.onSuccess(statusCode, headers, response);
	// }
	// @Override
	// public void onFailure(int statusCode, Header[] headers, String
	// responseString, Throwable throwable) {
	// super.onFailure(statusCode, headers, responseString, throwable);
	// }
	// };
	//
	// private TextHttpResponseHandler defaultTextHttpResponseHandler =new
	// TextHttpResponseHandler() {
	// @Override
	// public void onFailure(int statusCode, Header[] headers, String
	// responseString, Throwable throwable) {
	//
	// }
	// @Override
	// public void onSuccess(int statusCode, Header[] headers, String
	// responseString) {
	//
	// }
	// };

}