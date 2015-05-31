package de.dhbw.smar;

import java.util.Locale;

import de.dhbw.smar.helpers.ActivityCodeHelper;
import de.dhbw.smar.helpers.PreferencesHelper;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class SettingsActivity extends Activity {
	String logTag = "SettingsActivity";
	String chosenLocale = "en";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		boolean useIntStorage = false;
		if(PreferencesHelper.getInstance().getStorage() == 0)
			useIntStorage = true;
		((CheckBox) findViewById(R.id.settings_storage)).setChecked(useIntStorage);
		
		((EditText) findViewById(R.id.settings_serverIP))
			.setText(PreferencesHelper.getInstance().getServer());
		
		Spinner spinner = (Spinner) findViewById(R.id.spinner_settings_localization);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.spinner_localization, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		
		if(PreferencesHelper.getInstance().getLocale().equals("de"))
			spinner.setSelection(1);
		
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		    	switch(position) {
		    	case 0:
		    		chosenLocale = "en";
		    		break;
		    	case 1:
		    		chosenLocale = "de";
		    		break;
		    	default:
		    		chosenLocale = "en";
		    	}
		    	Log.d(logTag, "New Locale: " + chosenLocale);
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		        // Nothing to do...
		    }
		});
	}
	
	public void onSaveConfigurationPressed(View view) {
		Log.d(logTag, "Save Configuration Button pressed");
		String serverIP = ((EditText) findViewById(R.id.settings_serverIP)).getText().toString();
		int useInternalStorage = 1;
		if(((CheckBox) findViewById(R.id.settings_storage)).isChecked()) {
			useInternalStorage = 0;
		}
		Log.d(logTag, "serverIP: " + serverIP);
		PreferencesHelper.getInstance().setServer(serverIP);
		Log.d(logTag, "use internal storage: " + useInternalStorage);
		PreferencesHelper.getInstance().setStorage(useInternalStorage);
		PreferencesHelper.getInstance().setLocale(chosenLocale);
		
		setLocaleNow(chosenLocale);
		
		Log.d(logTag, "ByeBye!");
		Intent intent = this.getIntent();
		this.setResult(RESULT_OK, intent);
		intent.putExtra(ActivityCodeHelper.ACTIVITY_SETTINGS_RESET, false);
		finish();
	}
	
	public void setLocaleNow(String lang) {
		Log.d(logTag, "Activate new locale: " + lang);
		Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
	}
	
	public void onResetPressed(View view) {
		Log.d(logTag, "Reset pressed! Bye bye");
		Intent intent = this.getIntent();
		this.setResult(RESULT_OK, intent);
		intent.putExtra(ActivityCodeHelper.ACTIVITY_SETTINGS_RESET, true);
		finish();
	}
}
