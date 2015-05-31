package de.dhbw.smar;

import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import de.dhbw.smar.asynctasks.ASyncHttpConnection;
import de.dhbw.smar.helpers.ActivityCodeHelper;
import de.dhbw.smar.helpers.HttpConnectionHelper;
import de.dhbw.smar.helpers.LoginHelper;
import de.dhbw.smar.helpers.PreferencesHelper;
import de.dhbw.smar.svg.SVGDownload;

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
	// Reset executed?
	private boolean resetButton = false;
	
	// Logging Tag, Context
	private final String logTag = "MainActivity";
	private final Context context = this;
	
	// is this the first initialization in this session?
	private boolean firstInitialize = true;
	
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
        	Log.d(logTag, "Set default language to english");
        	PreferencesHelper.setPreference(context, PreferencesHelper.PREFKEY_LOCALE, "en");
        	PreferencesHelper.getInstance().setLocale("en");
        	Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = new Locale("en");
            res.updateConfiguration(conf, dm);
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
    				getResources().getString(R.string.t_initconfigerror),
	    	        Toast.LENGTH_LONG).show();
    	}
    	
    	if(initConfigLoaded) {
	    	Log.d(logTag, "checking server connection");
	    	pDialog = ProgressDialog.show(context, 
	    			getResources().getString(R.string.pd_title_wait), 
	    			getResources().getString(R.string.pd_content_checkconnection), true, false);
			String url = "http://" + PreferencesHelper.getInstance().getServer() + "/connection/check";
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
    	
    	if(resetButton) {
    		Log.d(logTag, "Debugging: reset initial configuration");
        	PreferencesHelper.resetPreferences(context);
    	} else {
	    	Log.d(logTag, "Save preferences");
	    	PreferencesHelper.getInstance().savePreferences(this);
    	}
    	
    	Log.d(logTag, "Start super.onDestroy");
    	super.onDestroy();
    }
    
    @Override
	public void onBackPressed() {
    	LoginHelper.getInstance().setLogout();
    	super.onBackPressed();
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
			// Logged in. Load main menu
			setContentView(R.layout.activity_main);
			
			// do first initialization stuff, should not be done each time the menu is called!
			if(firstInitialize) {
				firstInitialize = false;
				new SVGDownload(this).checkSVGRepository();
			}
		} else {
			Intent startNewActivityOpen = new Intent(this, LoginActivity.class);
        	startActivityForResult(startNewActivityOpen, ActivityCodeHelper.ACTIVITY_LOGIN_REQUEST);
		}
	}
    
    // Methods executed, if a menu button has been selected
    public void onProductSearchClicked(View view) {
    	Intent searchProduct = new Intent(this, SearchProduct.class);
    	searchProduct.putExtra("started", "main");
    	startActivity(searchProduct);
    }
    
    public void onProductLoadClicked(View view) {
    	Intent insertProduct = new Intent(this, InsertProduct.class);
    	insertProduct.putExtra("started", "main");
    	startActivity(insertProduct);
    }
    
    public void onProductStockClicked(View view) {
    	Intent ReceivingProducts = new Intent(this, ReceivingProducts.class);
    	ReceivingProducts.putExtra("started", "main");
    	startActivity(ReceivingProducts);
    }
    
    public void onSettingsClicked(View view) {
    	Intent settingsActivity = new Intent(context, SettingsActivity.class);
    	startActivityForResult(settingsActivity, ActivityCodeHelper.ACTIVITY_SETTINGS_REQUEST);
    }
    
    public void onLogoutClicked(View view) {
    	onBackPressed();
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
    		
    		if(requestCode == ActivityCodeHelper.ACTIVITY_SETTINGS_REQUEST) {
    			if(data.getBooleanExtra(ActivityCodeHelper.ACTIVITY_SETTINGS_RESET, false)) {
    				resetButton = true;
    				onBackPressed();
    			} else {
    				PreferencesHelper.getInstance().savePreferences(context);
    			}
    		}
    	}
    }
    
    // Methods creating error dialogs
    private void createConnectionErrorDialog() {
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);
 
		// set title
		alertDialogBuilder.setTitle(getResources().getString(R.string.ad_title_connectionerror));

		// set dialog message
		alertDialogBuilder
			.setMessage(getResources().getString(R.string.ad_content_connectionerror))
			.setCancelable(false)
			.setNegativeButton(getResources().getString(R.string.ad_bt_exitapp), 
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					onBackPressed();
				}
			})
			.setPositiveButton(getResources().getString(R.string.ad_bt_initconfig), 
					new DialogInterface.OnClickListener() {
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
