package de.dhbw.smar;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import de.dhbw.smar.helpers.FileHelper;
import de.dhbw.smar.helpers.LoginHelper;
import de.dhbw.smar.helpers.PreferencesHelper;



public class MainActivity extends Activity {
	
	final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (android.os.Build.VERSION.SDK_INT > 9) {
        	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        	StrictMode.setThreadPolicy(policy); 
        }
        
        // Debug: Einloggen!
        // LoginHelper.getInstance().setLogin("test", "test"); // Logge ein
        // Debug Ende
        
        if(!LoginHelper.getInstance().isLoggedIn()) {
        	ProgressDialog pDialog = ProgressDialog.show(this, "Downloading Data..", "Please wait", true, false);
        	// Set LogIn Activity
        	Intent startNewActivityOpen = new Intent(this, LoginActivity.class);
        	startActivityForResult(startNewActivityOpen, 0);
        	
        	pDialog.dismiss();
        }
        
	        setContentView(R.layout.activity_main);
	        /* if (savedInstanceState == null) {
	            getFragmentManager().beginTransaction()
	                    .add(R.id.container, new PlaceholderFragment())
	                    .commit();
	        } */
	        
	        // set initial preference
	        if(PreferencesHelper.getPreferenceInt(this, getString(R.string.prefname_use_internal_storage)) == -1) {
	        	PreferencesHelper.setPreferenceInt(this, getString(R.string.prefname_use_internal_storage), 1);
	        }
	        
	        writeFile();
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
    }
    
    public void onProductLoadClicked(View view) {
    	Intent insertProduct = new Intent(this, InsertProduct.class);
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
    	System.exit(0);
    }
}
