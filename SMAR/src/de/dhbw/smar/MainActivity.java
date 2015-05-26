package de.dhbw.smar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import de.dhbw.smar.asynctasks.ASyncHttpConnection;
import de.dhbw.smar.helpers.ActivityCodeHelper;
import de.dhbw.smar.helpers.HttpConnectionHelper;
import de.dhbw.smar.helpers.LoginHelper;
import de.dhbw.smar.helpers.PreferencesHelper;

/**
 * 
 * MainActivity - Launcher Activity
 * 1. Look up hwaddress (wifi device)
 * 2. Load initial configuration or
 *    initialize activity to set up initial configuration
 * 3. Check server connection
 * 4. Initialize activity for login
 * 5. Show menu for further actions and create other activities
 * 
 * @author Sebastian Kowalski
 *
 */
public class MainActivity extends Activity {
	// Logging Tag, Context
	private final String logTag = "MainActivity";
	private final Context context = this;
	
	// Variables
	private boolean initConfig = false;
	HttpConnectionHelper hch;
	ProgressDialog pDialog;

	// Activity lifecycle methods with logging and actions
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.d(logTag, "Start onCreate");
    	super.onCreate(savedInstanceState);
    	
    	WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    	WifiInfo info = manager.getConnectionInfo();
    	String hwaddress = info.getMacAddress();
    	if(hwaddress == null)
    		hwaddress = "0";
    	Log.d(logTag, "Saving hwaddress (" + hwaddress + ") in LoginHelper.");
    	LoginHelper.getInstance().setHwaddress(hwaddress);
        
        Log.d(logTag, "Check for initial configuration");
        // check for initial configuration, if not set start activity
        if(PreferencesHelper.getPreferenceInt(this, PreferencesHelper.PREFKEY_INIT_CONFIG) != 1) {
        	Log.d(logTag, "No initial configuration found - create InitConfActivity");
        	Intent startNewActivityOpen = new Intent(context, InitConfActivity.class);
        	startActivityForResult(startNewActivityOpen, ActivityCodeHelper.ACTIVITY_INITCONFIG_REQUEST);
        } else {
        	Log.d(logTag, "Initial configuration found - load preferences");
        	PreferencesHelper.getInstance().loadPreferences(context);
        	initConfig = true;
        }
        
        Log.d(logTag, "Finish onCreate");
    }
    
    @Override
    protected void onStart() {
    	boolean initConfigLoaded = false;
    	
    	Log.d(logTag, "Start onStart");
    	super.onStart();
    	
    	if(initConfig) {
    		Log.d(logTag, "loading initial configuration");
    		PreferencesHelper.getInstance().loadPreferences(context);
    		initConfigLoaded = true;
    	} else {
    		Log.e(logTag, "could not load initial configuration");
    		Toast.makeText(context, 
	    	        "Could not load initial configuration!",
	    	        Toast.LENGTH_LONG).show();
    	}
    	
    	if(initConfigLoaded) {
	    	Log.d(logTag, "checking server connection");
	    	pDialog = ProgressDialog.show(context, "Please wait", "Checking server connection...", true, false);
			String url = "http://" + PreferencesHelper.getInstance().getServer() + "/checkConnection";
			Log.d(logTag, "server url: " + url);
			hch = new HttpConnectionHelper(url);
			new ASyncHttpConnection() {
				@Override
				public void onPostExecute(String result) {
					pDialog.dismiss();
					if(!hch.getError() && hch.getResponseCode() == 200)
						initializeSMAR();
					else {
						createConnectionErrorDialog();
					}
				}
				
			}.execute(hch);
    	}
    }
    
    @Override
    protected void onPause() {
    	Log.d(logTag, "Start onPause");
    	super.onPause();
    }
    
    @Override
    protected void onResume() {
    	Log.d(logTag, "Start onResume");
    	super.onResume();
    }
    
    @Override
    protected void onStop() {
    	Log.d(logTag, "Start onStop");
    	super.onStop();
    }
    
    @Override
    protected void onDestroy() {
    	Log.d(logTag, "Start onDestroy");
    	
    	Log.d(logTag, "Save preferences");
    	PreferencesHelper.getInstance().savePreferences(this);
    	
    	Log.d(logTag, "Debugging: reset initial configuration");
    	PreferencesHelper.resetPreferences(context);
    	
    	Log.d(logTag, "Start super.onDestroy");
    	super.onDestroy();
    }
    
    // Method executed, if all tests has been successfully
    // 1. Start Login-Activity
    // 2. Show menu
    protected void initializeSMAR() {
    	// Skip LogIn for debugging.
    	// Attention: Server connection will fail!
    	// TODO remove
    	// LoginHelper.getInstance().setLoggedIn(true);
    	
		if(LoginHelper.getInstance().isLoggedIn()) {
			setContentView(R.layout.activity_main);
		} else {
			Intent startNewActivityOpen = new Intent(this, LoginActivity.class);
        	startActivityForResult(startNewActivityOpen, ActivityCodeHelper.ACTIVITY_LOGIN_REQUEST);
		}
	}
    
    // Methods executed, if a menu button has been selected
    public void onProductSearchClicked(View view) {
    	Intent searchProduct = new Intent(this, SearchProduct.class);
    	startActivity(searchProduct);
    }
    
    public void onProductLoadClicked(View view) {
    	Intent insertProduct = new Intent(this, InsertProduct.class);
    	startActivity(insertProduct);
    }
    
    public void onProductStockClicked(View view) {
    	showNoActionDialog();
    }
    
    public void onSettingsClicked(View view) {
    	// TODO: Write settings activity
    	// Intent startNewActivityOpen = new Intent(this, SettingsActivity.class);
    	// startActivityForResult(startNewActivityOpen, 0);
    	showNoActionDialog();
    }
    
    public void onLogoutClicked(View view) {
    	LoginHelper.getInstance().setLogout();
    	super.onBackPressed();
    }
    
    // Method fired by result of another activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If cancelled initial configuration or login -> get out of here
    	if(resultCode == Activity.RESULT_CANCELED && 
    			(requestCode == ActivityCodeHelper.ACTIVITY_INITCONFIG_REQUEST || 
    			 requestCode == ActivityCodeHelper.ACTIVITY_LOGIN_REQUEST)) {
    		Log.d(logTag, "Login or initial configuration canceled. Exec onBackPressed()");
    		onBackPressed();
    	}
    	
    	if(resultCode == Activity.RESULT_OK) {
    		if(requestCode == ActivityCodeHelper.ACTIVITY_INITCONFIG_REQUEST) {
    			if(data.getBooleanExtra(ActivityCodeHelper.ACTIVITY_INITCONFIG_DATA_SET, false))
    				initConfig = true;
    		}
    	}
    }
    
    // Methods creating error dialogs
    private void createConnectionErrorDialog() {
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
    }
        
    private void showNoActionDialog() {
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);
 
			// set title
			alertDialogBuilder.setTitle("No Action...");
 
			// set dialog message
			alertDialogBuilder
				.setMessage("There is no action here...\nPlease stand by!")
				.setCancelable(false)
				.setNeutralButton("Okay",new DialogInterface.OnClickListener() {
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