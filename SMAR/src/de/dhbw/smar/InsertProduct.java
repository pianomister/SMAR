package de.dhbw.smar;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
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
import android.widget.Toast;
import de.dhbw.smar.asynctasks.ASyncHttpConnection;
import de.dhbw.smar.helpers.ActivityCodeHelper;
import de.dhbw.smar.helpers.DialogHelper;
import de.dhbw.smar.helpers.HttpConnectionHelper;
import de.dhbw.smar.helpers.PreferencesHelper;

public class InsertProduct extends Activity implements DialogHelper.ShareDialogListener{
	
	int workflow_pos;
	String product_name;
	String current_unit_id;
	String selected_amount; // ausgewählte Anzahl vom User im Dialog
	String selected_unit;
	String current_amount_warehouse;
	String current_amount_shop;
	String current_barcode;
	String current_product_id;
	ArrayList<String> available_units;
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
			current_product_id = b.getString("product_id");
			current_amount_shop = b.getString("amount_shop");
			current_amount_warehouse = b.getString("amount_warehouse");
			current_unit_id = b.getString("unit_id");
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
		updateDatabase();
	}
	
	public void onDialogPositiveClick(DialogFragment dialog,String selected_unit, String amount) {
		//After ok, clicked
		//we got selected_unit and the amount
		//call REST API to update database
		this.selected_unit = selected_unit;
		this.selected_amount = amount;
		
		//call rest api to insert the products
		updateDatabase();
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
	    		searchProductInformation(this, resultBarcode);
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
	
	 private void searchProductInformation(Activity activity, String barcode) {
	    	// get information of the product
	    	// set up asynchronous task 
	    	// set up connection to the server 
	    	// retrieve data from server
	    	// display the information
	    	pDialog = ProgressDialog.show(context, "Please wait", "Receiving product information...", true, false);
			String url = "http://" + PreferencesHelper.getInstance().getServer() + "/getProduct/" + barcode;
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
							
							current_product_id = json.getString("id");
							product_name = json.getString("name");
							current_amount_warehouse = json.getString("amount_warehouse");
							current_amount_shop = json.getString("amount_shop");
							current_unit_id = json.getString("unit_id");
							
							//layout names setzen
							setLayoutNames();

							//Create the Picture to display
							
							// Open Dialog to ask about unit and amount
							getUnitsToProduct();
							
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
	 
	 static DialogHelper newInstance(String current_unit, ArrayList<String> all_units) {
		 // need this, to pass some informationn to the dialog 
		 DialogHelper f = new DialogHelper();

		    // Supply num input as an argument.
		    Bundle args = new Bundle();
		    args.putString("current_unit", current_unit);
		    args.putStringArrayList("all_units", all_units);
		    f.setArguments(args);

		    return f;
		}

	 private void updateDatabase() {
		 // updates the stock of the product
		 // concrete: move amount from Warehouse to Shop
		 	int howMuchToTransfer; 
		 	howMuchToTransfer = Integer.parseInt(selected_unit) * Integer.parseInt(selected_amount);
		 
	    	pDialog = ProgressDialog.show(context, "Please wait", "Updating product information...", true, false);
			String url = "http://" + PreferencesHelper.getInstance().getServer() + "/updateProductStock";
			Log.d("Start updating: ", "server url: " + url);
			hch = new HttpConnectionHelper(url, HttpConnectionHelper.REQUEST_TYPE_POST);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("product_id", this.current_product_id));
			nameValuePairs.add(new BasicNameValuePair("amount", String.valueOf(howMuchToTransfer)));
			hch.setPostPair(nameValuePairs);
			new ASyncHttpConnection() {
				@Override
				public void onPostExecute(String result) {
					pDialog.dismiss();
					// Result ist das JSON Objekt
					Log.d("onPostExecute", result);
					try {
						JSONArray jArray = new JSONArray(hch.getResponseMessage());
						JSONObject json = null;
						for(int i = 0; i < jArray.length(); i++)
						{
							json = jArray.getJSONObject(i);
						}
						if(json.getString("success") == "success") {
						
						//show toast, that it was successful 
						Toast toast = Toast.makeText(context, "Successfully inserted", Toast.LENGTH_SHORT);
						toast.show();
						
						//start from beginning
						startProductSearch();
						}
						else {
							Toast toast = Toast.makeText(context, "There was a problem updating the database. Please try again.", Toast.LENGTH_LONG);
							toast.show();
							
							startProductSearch();
						}
						
					} 
					catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}.execute(hch);
	 }
	 
	 
	 private void getUnitsToProduct() {
		 //ask for available units of a product. 
		 //show them in dialog 
		 
		 //let the user enter and finish
		 searchAvailableUnits();
	 }
	 
	 private void searchAvailableUnits() {
		 //get all available Units to this product
		 pDialog = ProgressDialog.show(context, "Please wait", "Receiving product information...", true, false);
			String url = "http://" + PreferencesHelper.getInstance().getServer() + "/getUnits";
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
							
							JSONArray jArray = new JSONArray(hch.getResponseMessage());
							for(int i = 0; i < jArray.length(); i++) {
								//put all available units into one list
								JSONObject json = jArray.getJSONObject(i);
								available_units.add(json.getString("name"));
							}
							
							
							
							// open dialog and ask user to enter data
							DialogHelper dialog = newInstance(current_unit_id, available_units);
							dialog.show(getFragmentManager(), "tag"); 
						}
						catch (Exception e) 
						{
						}
					}
				}
			}.execute(hch);					
	 }
	 
	 
	 private void setLayoutNames() {
			TextView tv_product = (TextView)findViewById(R.id.tv_product);
			TextView tv_warehouse = (TextView)findViewById(R.id.tv_sales_area);
			TextView tv_shop = (TextView)findViewById(R.id.tv_stock);
		
			tv_product.setText(getResources().getString(R.string.tv_product) + product_name);
			tv_warehouse.setText(getResources().getString(R.string.tv_stock) + current_amount_warehouse);
			tv_shop.setText(getResources().getString(R.string.tv_sales_area) + current_amount_shop);
	 }
	 
	 
}
