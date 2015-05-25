package de.dhbw.smar;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.dhbw.smar.asynctasks.ASyncHttpConnection;
import de.dhbw.smar.helpers.ActivityCodeHelper;
import de.dhbw.smar.helpers.HttpConnectionHelper;
import de.dhbw.smar.helpers.LoginHelper;
import de.dhbw.smar.helpers.PreferencesHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {
	private boolean startingFlag;
	private final Context context = this;
	private final String logTag = "LoginActivity";
	private ProgressDialog pDialog;
	private HttpConnectionHelper hch;
	
	private static int ERROR_SERVER = 0;
	private static int ERROR_CAMERA = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		Log.d(logTag, "receiving device users");
    	pDialog = ProgressDialog.show(context, "Please wait", "Receiving user list...", true, false);
		String url = "http://" + PreferencesHelper.getInstance().getServer() + "/listDeviceUsers";
		Log.d(logTag, "server url: " + url);
		hch = new HttpConnectionHelper(url);
		new ASyncHttpConnection() {
			@Override
			public void onPostExecute(String result) {
				pDialog.dismiss();
				if(!hch.getError() && hch.getResponseCode() == 200) {
					Log.d(logTag, "Response (" + hch.getResponseCode() + "): " + hch.getResponseMessage());
					List<String> spinnerArray = new ArrayList<String>(); 
					spinnerArray.add("Choose user...");
					try {
						JSONArray jArray = new JSONArray(hch.getResponseMessage());
						for(int i = 0; i < jArray.length(); i++) {
							JSONObject json = jArray.getJSONObject(i);
							Log.d(logTag, "Got following username: "+ json.getString("username"));
							spinnerArray.add(json.getString("username"));
						}
						initializeLogin(spinnerArray);
					} catch (JSONException e) {
						Log.e(logTag, e.getMessage());
						createError(ERROR_SERVER);
					}
					// initializeSMAR();
				} else {
					Log.d(logTag, "Error (" + hch.getResponseCode() + "): " + result);
					createError(ERROR_SERVER);
				}
			}
			
		}.execute(hch);
	}
	
	public void initializeLogin(List<String> spinnerArray) {
		Spinner spinner = (Spinner) findViewById(R.id.spinner_unit);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
			    this, android.R.layout.simple_spinner_item, spinnerArray); 		
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		spinner.setAdapter(adapter);
		
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		        if(position != 0) {
		        	LoginHelper.getInstance().setUsername(parentView.getItemAtPosition(position).toString());
		        	Intent startNewActivityOpen = new Intent(getBaseContext(), BarcodeScannerActivity.class);
		        	startActivityForResult(startNewActivityOpen, ActivityCodeHelper.ACTIVITY_BARCODE_REQUEST);
		        }
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		        // Nothing to do...
		    }
		});
	}
	
	public void createError(int error) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
		context);

		if(error == ERROR_CAMERA) {
			// set title
			alertDialogBuilder.setTitle("Could not load camera...");
			
			// set dialog message
			alertDialogBuilder
				.setMessage("Camera error...\n\n"
						+ "Please restart your device and try again.")
				.setCancelable(false)
				.setNegativeButton("Exit App", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						onBackPressed();
					}
				});
		} else {
			// set title
			alertDialogBuilder.setTitle("Could not load users...");
			
			// set dialog message
			alertDialogBuilder
				.setMessage("Unknown error...\n\n"
						+ "Please try again later or contact system administrator...")
				.setCancelable(false)
				.setNegativeButton("Exit App", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						onBackPressed();
					}
				});
		}
		
		// create alert dialog and show it
		alertDialogBuilder.create().show();
	}
	
	public void cancelLogin() {
		Intent intent = this.getIntent();
		this.setResult(RESULT_CANCELED, intent);
		super.onBackPressed();
	}
	
	@Override
	public void onBackPressed() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);
 
		// set title
		alertDialogBuilder.setTitle("Exit Application?");
 
			// set dialog message
		alertDialogBuilder
			.setMessage("Are you sure you want to exit SMAR?")
			.setCancelable(false)
			.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					cancelLogin();
				}
			})
			.setNegativeButton("No",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					// if this button is clicked, just close
					// the dialog box and do nothing
						dialog.cancel();
					}
				});
 
				// create alert dialog and show it
			alertDialogBuilder.create().show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{    
	    if (resultCode == RESULT_OK) 
	    {
	    	String resultBarcode = data.getStringExtra("BARCODE");
	    	// TODO: resultBarcode == null ist nicht korrekt!
	    	if(resultBarcode == null)
	    		Toast.makeText(this, "No barcode/QR-code scanned", Toast.LENGTH_SHORT).show();
	    	else {
	    		LoginHelper.getInstance().setPassword(resultBarcode);
	    		pDialog = ProgressDialog.show(context, "Please wait", "Logging in...", true, false);
	    		String url = "http://" + PreferencesHelper.getInstance().getServer() + "/authenticate";
	    		Log.d(logTag, "server url: " + url);
	    		hch = new HttpConnectionHelper(url, HttpConnectionHelper.REQUEST_TYPE_POST);
	    		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
	            nameValuePairs.add(new BasicNameValuePair("hwaddress", LoginHelper.getInstance().getHwaddress()));
	            nameValuePairs.add(new BasicNameValuePair("user", LoginHelper.getInstance().getUsername()));
	            nameValuePairs.add(new BasicNameValuePair("password", LoginHelper.getInstance().getPassword()));
	            hch.setPostPair(nameValuePairs);
	    		new ASyncHttpConnection() {
	    			@Override
	    			public void onPostExecute(String result) {
	    				pDialog.dismiss();
	    				// TODO!!!
	    				/* if(!hch.getError() && hch.getResponseCode() == 200) {
	    					Log.d(logTag, "Response (" + hch.getResponseCode() + "): " + hch.getResponseMessage());
	    					List<String> spinnerArray = new ArrayList<String>(); 
	    					spinnerArray.add("Choose user...");
	    					try {
	    						JSONArray jArray = new JSONArray(hch.getResponseMessage());
	    						for(int i = 0; i < jArray.length(); i++) {
	    							JSONObject json = jArray.getJSONObject(i);
	    							Log.d(logTag, "Got following username: "+ json.getString("username"));
	    							spinnerArray.add(json.getString("username"));
	    						}
	    						initializeLogin(spinnerArray);
	    					} catch (JSONException e) {
	    						Log.e(logTag, e.getMessage());
	    						createError(ERROR_SERVER);
	    					}
	    					// initializeSMAR();
	    				} else { */
	    					Log.d(logTag, "Error (" + hch.getResponseCode() + "): " + result);
	    					/* createError(ERROR_SERVER);
	    				} */
	    			}
	    			
	    		}.execute(hch);
	    	}
	    } else if(resultCode == RESULT_CANCELED) {
	        createError(ERROR_CAMERA);
	    }
	}
}
