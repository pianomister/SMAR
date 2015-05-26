package de.dhbw.smar.asynctasks;

import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import de.dhbw.smar.helpers.HttpConnectionHelper;
import de.dhbw.smar.helpers.LoginHelper;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class ASyncHttpConnection extends AsyncTask<HttpConnectionHelper, Void, String> {
	private String logTag = "ASyncHttpConnection";
	
	@Override
	protected String doInBackground(HttpConnectionHelper... hch) {
		Log.d(logTag, "Starting ASyncHttpConnection...");
		String responseString;
		try {
			HttpResponse response;
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			// The default value is zero, that means the timeout is not used. 
			int timeoutConnection = 10000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT) 
			// in milliseconds which is the timeout for waiting for data.
			int timeoutSocket = 10000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			HttpClient httpclient = new DefaultHttpClient(httpParameters);
			if(hch[0].getRequestType() == HttpConnectionHelper.REQUEST_TYPE_POST) {
				Log.d(logTag, "POST-Request");
				Log.d(logTag, "URL: " + hch[0].getUrl());
				HttpPost request = new HttpPost(hch[0].getUrl());
				List<NameValuePair> postPair = hch[0].getPostPair();
				if(hch[0].isStandardMode())
					postPair.add(new BasicNameValuePair("jwt", LoginHelper.getInstance().getJwt()));
		        request.setEntity(new UrlEncodedFormEntity(postPair));
				response = httpclient.execute(request);
			} else {
				Log.d(logTag, "POST-Request");
				Log.d(logTag, "URL: " + hch[0].getUrl());
				String url = hch[0].getUrl();
				if(hch[0].isStandardMode()) {
					url = url + "/" + LoginHelper.getInstance().getJwt();
				}
				HttpGet request = new HttpGet(url);
				response = httpclient.execute(request);
			}
		    StatusLine statusLine = response.getStatusLine();
		    hch[0].setResponseCode(statusLine.getStatusCode());
		    responseString = EntityUtils.toString(response.getEntity());
	        hch[0].setResponseMessage(responseString);
	        Log.d(logTag, "Check Connection: " + responseString);
		    Log.d(logTag, "Response ("+statusLine.getStatusCode()+") is: " + responseString);
		} catch(Exception e) {
			responseString = e.getMessage();
			hch[0].setError(true);
		}
		return responseString;
	}

}
