package de.dhbw.smar;

import java.io.File;

import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import de.dhbw.smar.helpers.ActivityCodeHelper;
import de.dhbw.smar.helpers.FileHelper;
import de.dhbw.smar.helpers.LoginHelper;
import de.dhbw.smar.helpers.PreferencesHelper;



public class MainActivity_old1 extends Activity {
	
	private final String logTag = "MainActivity";
	private final Context context = this;
	private boolean initConfig = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.d(logTag, "Start onCreate");
    	super.onCreate(savedInstanceState);
        
        Log.d(logTag, "Check for initial configuration");
        // check for initial configuration, if not set start activity
        if(PreferencesHelper.getPreferenceInt(this, PreferencesHelper.PREFKEY_INIT_CONFIG) != 1) {
        	Log.d(logTag, "No initial configuration found - create InitConfActivity");
        	Intent startNewActivityOpen = new Intent(this, InitConfActivity.class);
        	startActivityForResult(startNewActivityOpen, ActivityCodeHelper.ACTIVITY_INITCONFIG_REQUEST);
        } else {
        	Log.d(logTag, "Initial configuration found - load preferences");
        	PreferencesHelper.getInstance().loadPreferences(context);
        	initConfig = true;
        }
        
        Log.d(logTag, "Finish onCreate");
        
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
    	Log.d(logTag, "Start onStart");
    	super.onStart();
    	
    	if(initConfig) {
    		Log.d(logTag, "loading initial configuration");
    		PreferencesHelper.getInstance().loadPreferences(context);
    	} else {
    		Log.e(logTag, "could not load initial configuration");
    		Toast.makeText(context, 
	    	        "Could not load initial configuration!",
	    	        Toast.LENGTH_SHORT).show();
    	}
    	
    	Log.d(logTag, "checking server connection");
    	HttpClient client = new DefaultHttpClient();
		try {
			String url = "http://" + PreferencesHelper.getInstance().getServer() + "/checkConnection";
			String SetServerString = "";
			// Create Request to server and get response
			HttpGet httpget = new HttpGet(url);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			SetServerString = client.execute(httpget, responseHandler);
			
			// Show response on activity
			// content.setText(SetServerString);
		} catch(Exception ex) {
			// content.setText("Fail!");
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
