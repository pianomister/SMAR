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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		startingFlag = true;
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		Log.d(logTag, "receiving device users");
    	pDialog = ProgressDialog.show(context, "Please wait", "Checking server connection...", true, false);
		String url = "http://" + PreferencesHelper.getInstance().getServer() + "/listDeviceUsers";
		Log.d(logTag, "server url: " + url);
		hch = new HttpConnectionHelper(url);
		new ASyncHttpConnection() {
			@Override
			public void onPostExecute(String result) {
				pDialog.dismiss();
				/* if(!hch.getError())
					initializeSMAR();
				else {
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
							context);
			 
					// set title
					alertDialogBuilder.setTitle("Could not establish server connection...");
		 
					// set dialog message
					alertDialogBuilder
						.setMessage("Is this device connected?\n"
								+ "Are the settings correct?\n"
								+ "Is the server online and ready?\n\n"
								+ "Please choose an action...")
						.setCancelable(false)
						.setNegativeButton("Exit App", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								onBackPressed();
							}
						})
						.setPositiveButton("Initial Configuration", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								Intent startNewActivityOpen = new Intent(context, InitConfActivity.class);
					        	startActivityForResult(startNewActivityOpen, ActivityCodeHelper.ACTIVITY_INITCONFIG_REQUEST);
							}
						});
		 
						// create alert dialog and show it
						alertDialogBuilder.create().show();
				} */
			}
			
		}.execute(hch);
		
		List<String> spinnerArray = new ArrayList<String>(); 
		spinnerArray.add("Choose user...");
		spinnerArray.addAll(LoginHelper.getInstance().getUserList());
		
		Spinner spinner = (Spinner) findViewById(R.id.spinner_unit);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
			    this, android.R.layout.simple_spinner_item, spinnerArray); 		
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		spinner.setAdapter(adapter);
		
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		    	Toast.makeText(parentView.getContext(), 
		    	        "OnItemSelectedListener : " + parentView.getItemAtPosition(position).toString(),
		    	        Toast.LENGTH_SHORT).show();
		        if(!startingFlag) {
		        	Intent startNewActivityOpen = new Intent(getBaseContext(), BarcodeScannerActivity.class);
		        	startActivityForResult(startNewActivityOpen, 0);
		        } else {
		        	startingFlag = false;
		        }
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		        // your code here
		    }

		});
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
}
