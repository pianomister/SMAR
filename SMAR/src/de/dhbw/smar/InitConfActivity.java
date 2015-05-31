package de.dhbw.smar;

import de.dhbw.smar.helpers.ActivityCodeHelper;
import de.dhbw.smar.helpers.PreferencesHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.CheckBox;

public class InitConfActivity extends Activity {
	private final Context context = this;
	private final String logTag = "InitConfActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_init_conf);
	}
	
	public void onSaveConfigurationPressed(View view) {
		Log.d(logTag, "Save Configuration Button pressed");
		String serverIP = ((EditText) findViewById(R.id.initConfig_serverIP)).getText().toString();
		Log.d(logTag, "serverIP: " + serverIP);
		// Workaround for Vuzix M100 - Missing dot in keyboard...
		if(serverIP.contains("*")){
			serverIP = serverIP.replace("*", ".");
			Log.d(logTag, "Modified serverIP: " + serverIP);
		}
		int useInternalStorage = 1;
		if(((CheckBox) findViewById(R.id.initConfig_storage)).isChecked()) {
			useInternalStorage = 0;
		}
		Log.d(logTag, "serverIP: " + serverIP);
		PreferencesHelper.setPreference(this, PreferencesHelper.PREFKEY_SERVER_IP, serverIP + "/index.php");
		Log.d(logTag, "use internal storage: " + useInternalStorage);
		PreferencesHelper.setPreferenceInt(this, PreferencesHelper.PREFKEY_USE_INTERNAL_STORAGE, useInternalStorage);
		PreferencesHelper.setPreferenceInt(this, PreferencesHelper.PREFKEY_INIT_CONFIG, 1);
		
		Log.d(logTag, "ByeBye!");
		Intent intent = this.getIntent();
		this.setResult(RESULT_OK, intent);
		intent.putExtra(ActivityCodeHelper.ACTIVITY_INITCONFIG_DATA_SET, true);
		finish();
	}
	
	@Override
	public void onBackPressed() {
		Log.d(logTag, "onBackPressed()");
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);
 
		// set title
		alertDialogBuilder.setTitle(getResources().getString(R.string.ad_title_exitapp));
 
			// set dialog message
		alertDialogBuilder
			.setMessage(getResources().getString(R.string.ad_content_exitapp))
			.setCancelable(false)
			.setPositiveButton(getResources().getString(R.string.ad_bt_yes),
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					cancelInitConfig();
				}
			})
			.setNegativeButton(getResources().getString(R.string.ad_bt_no),
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					// if this button is clicked, just close
					// the dialog box and do nothing
						dialog.cancel();
					}
				});
 
				// create alert dialog and show it
			alertDialogBuilder.create().show();
	}
	
	public void cancelInitConfig() {
		Log.d(logTag, "initial configuration canceled");
		Intent intent = this.getIntent();
		this.setResult(RESULT_CANCELED, intent);
		super.onBackPressed();
	}
}
