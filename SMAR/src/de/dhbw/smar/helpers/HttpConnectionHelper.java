package de.dhbw.smar.helpers;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.NameValuePair;

public class HttpConnectionHelper{
	
	public static int REQUEST_TYPE_GET = 0;
	public static int REQUEST_TYPE_POST = 1;
	
	private String url;
	private int requestType = REQUEST_TYPE_GET;
	private int responseCode;
	private String responseMessage;
	private boolean error = false;
	private List<NameValuePair> postPair;
	private boolean standardMode = true;
	
	public List<NameValuePair> getPostPair() {
		return postPair;
	}

	public void setPostPair(List<NameValuePair> postPair) {
		this.postPair = postPair;
	}

	public HttpConnectionHelper(String url) {
		this.url = url;
	}
	
	public HttpConnectionHelper(String url, boolean standardMode) {
		this.url = url;
		this.standardMode = standardMode;
	}
	
	public HttpConnectionHelper(String url, int requestType) {
		this.url = url;
		this.requestType = requestType;
	}
	
	public HttpConnectionHelper(String url, int requestType, boolean standardMode) {
		this.url = url;
		this.requestType = requestType;
		this.standardMode = standardMode;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public String getUrl() {
		return url;
	}

	public int getRequestType() {
		return requestType;
	}
	
	public void setError(boolean error) {
		this.error = error;
	}
	
	public boolean getError() {
		return this.error;
	}

	public boolean isStandardMode() {
		return standardMode;
	}

	public void setStandardMode(boolean standardMode) {
		this.standardMode = standardMode;
	}
}
