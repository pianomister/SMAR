package de.dhbw.smar.helpers;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpConnectionHelper implements Callable<String>{
	
	private String url;
	private HttpRequest request; 
	private String JSONString;
	
	public HttpConnectionHelper(String url, HttpRequest request) {
		this.url = url;
		this.request = request;
	}
	
	@Override
	public String call() throws Exception {
		// TODO Auto-generated method stub
		this.JSONString = HttpConnection();
		
		return JSONString;
	}

	private String HttpConnection() {
		String response = null;
		
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(this.url);
		ResponseHandler<String> handler = new BasicResponseHandler();
		try {
			response = client.execute(request, handler);
		}
		catch (ClientProtocolException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		client.getConnectionManager().shutdown();
		
		return response;
	}
	
	
}
