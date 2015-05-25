package de.dhbw.smar;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import de.dhbw.smar.helpers.DialogHelper;

public class InsertProduct extends Activity implements DialogHelper.ShareDialogListener{
	
	int workflow_pos;
	String product_name;
	String current_unit;
	String current_stock;
	String current_sales;
	String current_amount;
	final Context context = this;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_insert_product);
		
		// In the Bundle are some params
		// They indicate where to start this activity
		
		Bundle b = getIntent().getExtras();
		if(b != null) {
			workflow_pos = b.getInt("workflow_position");
		} else {
			workflow_pos = 0;
		}
		
		if(workflow_pos == 0 ){
			startProductSearch();
		} else if (workflow_pos == 1) {
			startAfterProductSearch();
		}
		
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.insert_product, menu);
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

	public void startAfterProductSearch() {
		// Open dialog
		// User can enter which unit
		// User can enter the amount of the unit	
		DialogFragment dialog = new DialogHelper();
		dialog.show(getFragmentManager(), "DialogHelper");
		
		//Use REST API to refresh Database 

	}
	
	public void onDialogPositiveClick(DialogFragment dialog, String amount) {
		String vorsilbe;
		this.current_amount = amount;
		TextView tv = (TextView) findViewById(R.id.tv_current_unit);
		vorsilbe = tv.getText().toString().split(":")[0];
		tv.setText(vorsilbe + ": " + this.current_amount);
		
	}
	
	public void onDialogNegativeClick(DialogFragment dialog) {
		dialog.dismiss();
	}
	
	public void restartInsertProduct(View view) {
		workflow_pos = 0;
		startProductSearch();
	}
	
	private void startProductSearch() {
		//start bar code scanner
		
		// Use REST API to get information about the article 
		
		//show dialog 
		startAfterProductSearch();
		
	}
	
}
