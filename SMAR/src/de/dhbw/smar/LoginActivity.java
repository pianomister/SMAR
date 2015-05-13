package de.dhbw.smar;

import java.util.ArrayList;
import java.util.List;

import de.dhbw.smar.helpers.LoginHelper;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class LoginActivity extends Activity {
	private boolean startingFlag;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		startingFlag = true;
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		List<String> spinnerArray = new ArrayList<String>(); 
		spinnerArray.add("Choose user...");
		spinnerArray.addAll(LoginHelper.getInstance().getUserList());
		
		Spinner spinner = (Spinner) findViewById(R.id.spinner1);
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
}
