package de.dhbw.smar.asynctasks;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import de.dhbw.smar.helpers.HttpConnectionHelper;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class ASyncHttpConnection extends AsyncTask<HttpConnectionHelper, Void, String> {
	private String logTag = "ASyncHttpConnection";
	
	@Override
	protected String doInBackground(HttpConnectionHelper... hch) {
		String responseString;
		try {
			HttpResponse response;
			HttpClient httpclient = new DefaultHttpClient();
			if(hch[0].getRequestType() == HttpConnectionHelper.REQUEST_TYPE_POST) {
				HttpPost request = new HttpPost(hch[0].getUrl());
				response = httpclient.execute(request);
			} else {
				HttpGet request = new HttpGet(hch[0].getUrl());
				response = httpclient.execute(request);
			}
		    StatusLine statusLine = response.getStatusLine();
		    responseString = EntityUtils.toString(response.getEntity());
	        JSONObject json = new JSONObject(responseString);
	        hch[0].setResponseCode(statusLine.getStatusCode());
	        hch[0].setResponseMessage(responseString);
	        Log.d(logTag, "Check Connection: ready:" + json.get("ready").toString());
		    Log.d(logTag, "Response ("+statusLine.getStatusCode()+") is: " + responseString);
		} catch(Exception e) {
			responseString = e.getMessage();
			hch[0].setError(true);
		}
		return responseString;
	}

}
