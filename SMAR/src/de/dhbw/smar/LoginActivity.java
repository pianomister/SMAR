package de.dhbw.smar;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
import android.widget.Toast;

/**
 * Activity handling the login
 * 
 * @author Sebastian Kowalski
 *
 */
public class LoginActivity extends Activity {
	// Context, Logging tag
	private final Context context = this;
	private final String logTag = "LoginActivity";
	
	// Variables
	private ProgressDialog pDialog;
	private HttpConnectionHelper hch;
	
	// Constants
	private static int ERROR_SERVER = 0;
	private static int ERROR_CAMERA = 1;
	private static int ERROR_PERMISSION = 2;
	private static int ERROR_CRED_DEVICE = 3;
	private static int ERROR_CRED_PASSWORD = 4;
	
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
	
	public void cancelLogin() {
		Intent intent = this.getIntent();
		this.setResult(RESULT_CANCELED, intent);
		super.onBackPressed();
	}
	
	public void finishLogin() {
		Intent intent = this.getIntent();
		this.setResult(RESULT_OK, intent);
		finish();
	}
	
	// Method executed by back button
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
	
	// Method executed by result from BarcodeScanner
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{    
	    if (resultCode == RESULT_OK) 
	    {
	    	String resultBarcode = data.getStringExtra("BARCODE");
	    	Log.d(logTag, "result barcode: " + resultBarcode);
	    	// TODO: resultBarcode == null ist nicht korrekt!
	    	if(resultBarcode == null || resultBarcode.equalsIgnoreCase("NULL"))
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
	    				if(!hch.getError()) {
	    					try {
		    					JSONArray jArray = new JSONArray(hch.getResponseMessage());
								JSONObject json = jArray.getJSONObject(0);
								Log.d(logTag, "Response (" + hch.getResponseCode() + "): " + hch.getResponseMessage());
		    					if(hch.getResponseCode() == 200) {
		    						LoginHelper lh = LoginHelper.getInstance();
		    						lh.setJwt(json.getString("jwt"));
		    						lh.setLoggedIn(true);
		    						finishLogin();
		    					} else if(hch.getResponseCode() == 403) {
		    						String reason = json.getString("reason");
		    						if(reason.equals("password")) {
		    							createError(ERROR_CRED_PASSWORD);
		    						} else if(reason.equals("device")) {
		    							createError(ERROR_CRED_DEVICE);
		    						} else {
		    							createError(ERROR_SERVER);
		    						}
		    					} else if(hch.getResponseCode() == 401) {
		    						createError(ERROR_PERMISSION);
		    					} else {
		    						createError(ERROR_SERVER);
		    					}
	    					} catch(Exception e) {
								createError(ERROR_SERVER);
								Log.e(logTag, e.getMessage());
	    					}
	    				} else {
	    					Log.d(logTag, "Error (" + hch.getResponseCode() + "): " + result);
	    					createError(ERROR_SERVER);
	    				}
	    			}
	    			
	    		}.execute(hch);
	    	}
	    } else if(resultCode == RESULT_CANCELED) {
	        createError(ERROR_CAMERA);
	    }
	}
	
	// Creates and Shows an error dialog
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
}
