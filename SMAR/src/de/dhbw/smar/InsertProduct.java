package de.dhbw.smar;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import de.dhbw.smar.asynctasks.ASyncHttpConnection;
import de.dhbw.smar.helpers.ActivityCodeHelper;
import de.dhbw.smar.helpers.DialogHelper;
import de.dhbw.smar.helpers.HttpConnectionHelper;
import de.dhbw.smar.helpers.PreferencesHelper;

public class InsertProduct extends Activity implements DialogHelper.ShareDialogListener{
	
	int workflow_pos;
	String product_name;
	String current_unit;
	String current_stock;
	String current_sales;
	String current_amount;
	String current_barcode;
	String product_id;
	final Context context = this;
	private ProgressDialog pDialog;
	private HttpConnectionHelper hch;
	
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
	
	public void onDialogPositiveClick(DialogFragment dialog,String selected_unit, String amount) {
		//After ok, clicked
		//we got selected_unit and the amount
		//call REST API to update database
		this.current_amount = amount;
		this.current_unit = selected_unit;

		
		
		
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
		//at first, read barcode
    	Intent startNewActivityOpen = new Intent(getBaseContext(), BarcodeScannerActivity.class);
    	startActivityForResult(startNewActivityOpen, ActivityCodeHelper.ACTIVITY_BARCODE_REQUEST);
		// Use REST API to get information about the article 
		
		//show dialog 
		startAfterProductSearch();
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {   
    	//read barcode 
		//get information to this product
		if (resultCode == RESULT_OK) 
	    {
	    	String resultBarcode = data.getStringExtra("BARCODE");
	    	if(!resultBarcode.equals(null)) {
	    		this.current_barcode = resultBarcode;
	    		searchProductInformation(resultBarcode);
	    	}
	    }
    	else {
    		AlertDialog.Builder alert = new AlertDialog.Builder(context);
    		alert.setTitle("Failure");
    		alert.setMessage("Couldn't read this code. Check code and try again")
    			 .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int id) {
    				dialog.dismiss();
    				startProductSearch();
    			}
    		});
    		alert.create().show();
    	}
		
	}
	
	 private void searchProductInformation(String productNumber) {
	    	// get information of the product
	    	// set up asynchronous task 
	    	// set up connection to the server 
	    	// retrieve data from server
	    	// display the information
	    	pDialog = ProgressDialog.show(context, "Please wait", "Receiving product information...", true, false);
			String url = "http://" + PreferencesHelper.getInstance().getServer() + "/getProduct/" + productNumber;
			Log.d("Start connectinting to: ", "server url: " + url);
			hch = new HttpConnectionHelper(url);
			new ASyncHttpConnection() {
				@Override
				public void onPostExecute(String result) {
					pDialog.dismiss();
					// Result ist das JSON Objekt
					Log.d("onPostExecute", result);
					if(!hch.getError() && hch.getResponseCode() == 200) { 
						try {
							Log.d("start json", "result");
							//JSONArray jArray = new JSONArray(hch.getResponseMessage());
							result = result.substring(1, result.length() -1);
							JSONObject json = new JSONObject(result);
							
							
							String product_name = json.getString("name");
							String amount_warehouse = json.getString("amount_warehouse");
							String amount_shop = json.getString("amount_shop");
							
							TextView tv_product = (TextView)findViewById(R.id.tv_product);
							TextView tv_warehouse = (TextView)findViewById(R.id.tv_sales_area);
							TextView tv_shop = (TextView)findViewById(R.id.tv_stock);
							
							tv_product.setText(getResources().getString(R.string.tv_product) + product_name);
							tv_warehouse.setText(getResources().getString(R.string.tv_stock) + amount_warehouse);
							tv_shop.setText(getResources().getString(R.string.tv_sales_area) + amount_shop);
							
							//Create the Picture to display
							
							// Open Dialog to ask about unit and amount
							// Afterwards call REST API to update the data on database
							
						}
						catch (JSONException e)
						{
							e.getStackTrace();
						}
							
					}
					
				}
			}.execute(hch);
	    }
	 
	 static DialogHelper newInstance(String current_unit, String[] all_units) {
		 // need this, to pass some informationn to the dialog 
		 DialogHelper f = new DialogHelper();

		    // Supply num input as an argument.
		    Bundle args = new Bundle();
		    args.putString("current_unit", current_unit);
		    args.putStringArray("all_units", all_units);
		    f.setArguments(args);

		    return f;
		}

	 private void updateDatabase() {
		 // updates the stock of the product
		 // concrete: move amount from Warehouse to Shop
	    	pDialog = ProgressDialog.show(context, "Please wait", "Updating product information...", true, false);
			String url = "http://" + PreferencesHelper.getInstance().getServer();
	 }
	 
	
}
