package de.dhbw.smar;

import java.io.File;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.IntentIntegrator;
import com.google.zxing.integration.IntentResult;

import de.dhbw.smar.helper.LoginHelper;
import de.dhbw.smar.helpers.FileHelper;
import de.dhbw.smar.helpers.PreferencesHelper;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Debug: Einloggen!
        LoginHelper.getInstance().setLogin("test", "test"); // Logge ein
        // Debug Ende
        
        if(!LoginHelper.getInstance().isLoggedIn()) {
        	// Set LogIn Activity
        	Intent startNewActivityOpen = new Intent(MainActivity.this, LoginActivity.class);
        	startActivityForResult(startNewActivityOpen, 0);
        } else {
        
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
    
    public void onScanClicked(View view) {
    	Intent myIntent = new Intent(this, QR_Result.class);
    	startActivity(myIntent);
    	
    }
    
    public void onLogoutClicked(View view) {
    	System.exit(0);
    }
    

    

    
}
