package de.dhbw.smar;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;



public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
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
    	
        String filename = "test.txt";
        String string = "Schreib mal rein hier :)";
        FileOutputStream outputStream;

        try {
          outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
          outputStream.write(string.getBytes());
          outputStream.close();	
        } catch (Exception e) {
          e.printStackTrace();
        }

        try {
        	FileInputStream fis = openFileInput(filename);
        	
        	StringBuilder builder = new StringBuilder();
        	int ch;
        	while((ch = fis.read()) != -1){
        	    builder.append((char)ch);
        	}

        	System.out.println(builder.toString());

        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        
    }
}
