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

import de.dhbw.smar.helpers.LoginHelper;
import android.app.Activity;
import android.app.AlertDialog;
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		startingFlag = true;
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		final TextView mTextView = (TextView) findViewById(R.id.tv_stock_insertproduct);

		/* String url ="http://192.168.178.20/SMAR-Web-Administration/api/authenticate";
		
		StatusLine statusLine = null;
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost post = new HttpPost(url);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("hwaddress","hwaddress_bla"));
			nameValuePairs.add(new BasicNameValuePair("user","admin"));
			nameValuePairs.add(new BasicNameValuePair("password","test"));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
		    HttpResponse response = httpclient.execute(post);
		    statusLine = response.getStatusLine();
		    String responseString = EntityUtils.toString(response.getEntity());
	        JSONObject json = new JSONObject(responseString);
	        Log.i(LoginActivity.class.getName(), "JWT:" + json.get("jwt").toString());
		    mTextView.setText("Response ("+statusLine.getStatusCode()+") is: " + responseString);
		} catch(IOException e) {
			 e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} */
		
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
