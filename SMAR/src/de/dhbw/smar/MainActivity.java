package de.dhbw.smar;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import de.dhbw.smar.asynctasks.ASyncHttpConnection;
import de.dhbw.smar.helpers.ActivityCodeHelper;
import de.dhbw.smar.helpers.FileHelper;
import de.dhbw.smar.helpers.HttpConnectionHelper;
import de.dhbw.smar.helpers.LoginHelper;
import de.dhbw.smar.helpers.PreferencesHelper;
import de.dhbw.smar.svg.SVGObject;



public class MainActivity extends Activity {
	
	private final String logTag = "MainActivity";
	private final Context context = this;
	private boolean initConfig = false;
	HttpConnectionHelper hch;
	ProgressDialog pDialog;

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
        
        //TODO remove: test for SVG
        String stringSVG = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 20010904//EN\" \"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\"><svg width=\"600\" height=\"180\" viewBox=\"0 0 600 180\" style=\"width:100%;height: auto;\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"><title>Shelf 'Softgetränke' (ID: 2, last updated: '22.04.2015 02:01:35')</title><defs><style type=\"text/css\">		<![CDATA[		text {fill: #333;font-family:Roboto;font-size: 8px;}		rect {fill:#ccc; stroke:#777; stroke-width: 2px;}		.section {fill:#ddd; stroke:#555; stroke-width: 1px; opacity:.8;}		.selected {fill:#16a082;}		]]></style></defs><rect id=\"shelf2\" x=\"0\" y=\"0\" width=\"600\" height=\"180\"/><rect id=\"section1\" x=\"110\" y=\"0\" width=\"110\" height=\"60\" class=\"section\"/><text x=\"115\" y=\"15\">1: Likör</text><rect id=\"section2\" x=\"110\" y=\"60\" width=\"110\" height=\"120\" class=\"section\"/><text x=\"115\" y=\"75\">2: Aufstrich</text><rect id=\"section4\" x=\"0\" y=\"0\" width=\"110\" height=\"180\" class=\"section\"/><text x=\"5\" y=\"15\">4: Test-Sektion</text><rect id=\"section8\" x=\"220\" y=\"0\" width=\"110\" height=\"180\" class=\"section\"/><text x=\"225\" y=\"15\">8: Wein</text></svg>";
        SVGObject objectSVG = new SVGObject(stringSVG);
        
        ImageView svgImage = (ImageView) findViewById(R.id.svgTest);
        ImageView bla = new ImageView(this);
        Drawable d = objectSVG.getDrawable();
        if(d != null)
        	bla.setImageDrawable(d);
        	//svgImage.setImageDrawable(d); // fehlers, fehlers everywhere (nullPointerException) -> svgImage not found?
        
        /*
        if (android.os.Build.VERSION.SDK_INT > 9) {
        	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        	StrictMode.setThreadPolicy(policy); 
        }
        
        // Debug: Einloggen!
        LoginHelper.getInstance().setLogin("test", "test"); // Logge ein
        // Debug Ende
        
       if(!LoginHelper.getInstance().isLoggedIn()) {
        	ProgressDialog pDialog = ProgressDialog.show(this, "Downloading Data..", "Please wait", true, false);
        	// Set LogIn Activity
        	Intent startNewActivityOpen = new Intent(this, LoginActivity.class);
        	startActivityForResult(startNewActivityOpen, ActivityCodeHelper.ACTIVITY_LOGIN_REQUEST);
        	
        	pDialog.dismiss();
        }
        
	        setContentView(R.layout.activity_main);
	        if (savedInstanceState == null) {
	            getFragmentManager().beginTransaction()
	                    .add(R.id.container, new PlaceholderFragment())
	                    .commit();
	        }
	        
	        // set initial preference
	        if(PreferencesHelper.getPreferenceInt(this, getString(R.string.prefname_use_internal_storage)) == -1) {
	        	PreferencesHelper.setPreferenceInt(this, getString(R.string.prefname_use_internal_storage), 1);
	        }
	        
	        writeFile();
	        */
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
				}
				
			}.execute(hch);
    	}
    }
    
    protected void initializeSMAR() {
    	
    	//TODO remove
    	//LoginHelper.getInstance().setLoggedIn(true);
    	
		if(LoginHelper.getInstance().isLoggedIn()) {
			setContentView(R.layout.activity_main);
		} else {
			Intent startNewActivityOpen = new Intent(this, LoginActivity.class);
        	startActivityForResult(startNewActivityOpen, ActivityCodeHelper.ACTIVITY_LOGIN_REQUEST);
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

    //Das ist ein Testkommentar.s
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
    
    public void writeFile() {
    	
        String sFilename = "test.txt";
        String sFilename2 = "test.txt";
        String sString = "Schreib mal rein hier :)";
        String sString2 = "Versuch zwei";
        
        FileHelper.writeFile(this, sFilename, sString);
        File result = FileHelper.readFile(this, sFilename);
        System.out.println("TEST1: " + FileHelper.getFileContents(result));
        
        FileHelper.writeFile(this, sFilename2, sString2);
        File result2 = FileHelper.readFile(this, sFilename2);
        System.out.println("TEST2: " + FileHelper.getFileContents(result2));
        
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
    
    public void onProductSearchClicked(View view) {
    	//showNoActionDialog();
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
    	Intent startNewActivityOpen = new Intent(this, SettingsActivity.class);
    	startActivityForResult(startNewActivityOpen, 0);
    }
    
    public void onLogoutClicked(View view) {
    	LoginHelper.getInstance().setLogout();
    	super.onBackPressed();
    }
    
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
    	
//    	// Results from Login-Activity
//        if(requestCode == ActivityCodeHelper.ACTIVITY_LOGIN_REQUEST) {
//        		if(resultCode == Activity.RESULT_CANCELED) {
//        			onBackPressed();
//        		} else if(resultCode == Activity.RESULT_OK) {
//        			// Perform a query to the contact's content provider for the contact's name
//                    Cursor cursor = getContentResolver().query(data.getData(),
//                    new String[] {Contacts.DISPLAY_NAME}, null, null, null);
//                    if (cursor.moveToFirst()) { // True if the cursor is not empty
//                        int columnIndex = cursor.getColumnIndex(Contacts.DISPLAY_NAME);
//                        String name = cursor.getString(columnIndex);
//                        // Do something with the selected contact's name...
//                    }
//        		}
//        }
    }
}
