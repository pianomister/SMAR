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
import de.dhbw.smar.asynctasks.ASyncHttpConnection;
import de.dhbw.smar.helpers.ActivityCodeHelper;
import de.dhbw.smar.helpers.DialogHelper;
import de.dhbw.smar.helpers.HttpConnectionHelper;
import de.dhbw.smar.helpers.PreferencesHelper;
import de.dhbw.smar.helpers.UnitHelper;

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
	ArrayList<String> available_units = new ArrayList<String>();
	final Context context = this;
	private ProgressDialog pDialog;
	private HttpConnectionHelper hch;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_insert_product);
		
		// In the Bundle are some params
		// They indicate where to start this activity
		Log.d("InsertProduct", "before getting bundles");
		Bundle b = getIntent().getExtras();
		Log.d("InsertProduct", "after getting bundles");
		if(b != null) {
			Log.d("InsertProduct", "Bundle is not null");
			Log.d("insert", b.getString("amount_shop"));
			Log.d("InsertProduct", String.valueOf(b.getInt("workflow_position")));
			
			workflow_pos = b.getInt("workflow_position");
			current_product_id = b.getString("product_id");
			current_amount_shop = b.getString("amount_shop");
			current_amount_warehouse = b.getString("amount_warehouse");
			current_unit_id = b.getString("unit_id");
			product_name = b.getString("product_name");
			
			setLayoutNames();
		} else {
			Log.d("InsertProduct", "Bundle is null");
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
		Log.d("InsertProduct", "before showing dialoghelper");
		getUnitsToProduct();
		Log.d("InsertProduct", current_unit_id);

	//	DialogFragment dialog = newInstance(current_unit_id, available_units);
	//	dialog.show(getFragmentManager(), "DialogHelper");
	}
	
	public void onDialogPositiveClick(DialogFragment dialog, String amount, int index, String valueOfSpinner) {
		//After ok, clicked
		//we got selected_unit and the amount
		//call REST API to update database


		

		
		//this.selected_unit = selected_unit;
		this.selected_amount = amount;
		//Log.d("DialogHelper", "Fresh data, unit: " + this.selected_unit);
		Log.d("DialogHelper", "Fresh data, amount : " + this.selected_amount);
		Log.d("DialogHelper", "Fresh index, amount : " + index);
		if(index == 0) {
			this.selected_unit = "1";
		} else {
			this.selected_unit = valueOfSpinner.split(";")[1];
		}
		
		
		Log.d("DialogHelper", this.selected_unit);
		Log.d("DialogHelper", this.selected_amount);
		
		Log.d("DialogHelper", "OnClickPositive");
		
		//call rest api to insert the products
		updateDatabase();
		//Use REST API to refresh Database 
				//updateDatabase();
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
		if (resultCode == RESULT_OK && !data.getStringExtra("BARCODE").equals("NULL")) 
	    {
			String resultBarcode = data.getStringExtra("BARCODE");
	    	
    		this.current_barcode = resultBarcode;
    		searchProductInformation(this, resultBarcode);
	    	
	    }
    	else {
	   		 Intent intent = new Intent(this, MainActivity.class);
	   		 intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	   		 startActivity(intent);
    		/*
    		AlertDialog.Builder alert = new AlertDialog.Builder(context);
    		alert.setTitle("Failure");
    		alert.setMessage("Couldn't read this code. Check code and try again")
    			 .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int id) {
    				dialog.dismiss();
    				startProductSearch();
    			}
    		});
    		alert.create().show(); */
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
							Log.d("start json", result + "teeest");
							//JSONArray jArray = new JSONArray(hch.getResponseMessage());
							result = result.substring(1, result.length() -1);
							JSONObject json = new JSONObject(result);
							
							Log.d("read json", "reading json...");
							
							current_product_id = json.getString("product_id");
							product_name = json.getString("name");
							current_amount_warehouse = json.getString("amount_warehouse");
							current_amount_shop = json.getString("amount_shop");
							current_unit_id = json.getString("unit_id");
							
							//layout names setzen
							Log.d("setLayout", "layout wird jetzt im nächsten schritt gesetzt");
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
		 Log.d("DialogHelper", "started");
		 DialogHelper f = new DialogHelper();
		 	
		    // Supply num input as an argument.
		    Bundle args = new Bundle();
		    Log.d("creating dialog", current_unit);
		    if(all_units.isEmpty()) {
		    	Log.d("creating dialog", "all units is empty");
		    }
		    Log.d("DialogHelper", "setting parameters");
		    args.putString("current_unit", current_unit);
		    args.putStringArrayList("all_units", all_units);
		   
		    Log.d("DialogHelper", "parameters set");
		    f.setArguments(args);

		    return f;
		}

	 private void updateDatabase() {
		 // updates the stock of the product
		 // concrete: move amount from Warehouse to Shop
		 	Log.d("DialogHelper", "started update");
		 	int howMuchToTransfer; 
		 	howMuchToTransfer = Integer.parseInt(selected_unit) * Integer.parseInt(selected_amount);
		 	
		 	Log.d("updateDatabase", "Transfer: " + howMuchToTransfer);
		 	
		 	Log.d("DialogHelper", String.valueOf(howMuchToTransfer));
		 	
		 	final int new_amount_shop = Integer.parseInt(this.current_amount_shop) + howMuchToTransfer;
		 	final int new_amount_warehouse = Integer.parseInt(this.current_amount_warehouse) - howMuchToTransfer;
		 	
	    	pDialog = ProgressDialog.show(context, "Please wait", "Updating product information...", true, false);
			String url = "http://" + PreferencesHelper.getInstance().getServer() + "/updateProductStock";
			Log.d("Start updating: ", "server url: " + url);
			hch = new HttpConnectionHelper(url, HttpConnectionHelper.REQUEST_TYPE_POST);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
			nameValuePairs.add(new BasicNameValuePair("product_id", this.current_product_id));
			nameValuePairs.add(new BasicNameValuePair("new_amount_shop", String.valueOf(new_amount_shop)));
			nameValuePairs.add(new BasicNameValuePair("new_amount_warehouse", String.valueOf(new_amount_warehouse)));
			hch.setPostPair(nameValuePairs);
			new ASyncHttpConnection() {
				@Override
				public void onPostExecute(String result) {
					pDialog.dismiss();
					// Result ist das JSON Objekt
					Log.d("updateDatabase", result);
					try {
						JSONObject json = new JSONObject(hch.getResponseMessage());
						Log.d("updateDatabase", "Value of single json object: " + json.getString("success"));

						if(json.getString("success").equals("success")) {
							
							//update view on screen
							current_amount_shop = String.valueOf(new_amount_shop);
							current_amount_warehouse = String.valueOf(new_amount_warehouse);
							setLayoutNames();
							
							//show toast, that it was successful 
							AlertDialog.Builder alert = new AlertDialog.Builder(context);
							alert.setTitle("Successful update");
							alert.setMessage("You have inserted successfully. Click \"ok\" to scan next.");
							alert.setNeutralButton("ok", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									//start from beginning
									startProductSearch();
			                   }
							});
							alert.create().show();}
						else {
							//show toast, that it was not successful 
							AlertDialog.Builder alert = new AlertDialog.Builder(context);
							alert.setTitle("Update failed.");
							alert.setMessage("Please try again or ask the admin for more support. Click \"ok\" to scan next.");
							alert.setNeutralButton("ok", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									//start from beginning
									startProductSearch();
			                   }
							});
							alert.create().show();
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
					Log.d("getting_units", "start on postexecute");
					pDialog.dismiss();
					// Result ist das JSON Objekt
					Log.d("onPostExecute", result);
					Log.d("onPostExecute", String.valueOf(hch.getResponseCode()));
					if(!hch.getError() && hch.getResponseCode() == 200) { 
						try {
							
							Log.d("start json", result);
							
							JSONArray jArray = new JSONArray(result);
							if(jArray.length() == 0)
								Log.d("json", "JSONArray is empty");
							
							for(int i = 0; i < jArray.length(); i++) {
								//put all available units into one list
								Log.d("json", "started looping");
								JSONObject json = jArray.getJSONObject(i);
								available_units.add(json.getString("name")+";"+json.getString("capacity"));
								Log.d("units", json.getString("name")+";"+json.getString("capacity"));
							}
							
						
							
							
							// open dialog and ask user to enter data
							Log.d("json", "start opening dialog");
							DialogHelper dialog = newInstance(current_unit_id, available_units);
							dialog.show(getFragmentManager(), "tag"); 
						}
						catch (Exception e) 
						{
							e.printStackTrace();
						}
					}
				}
			}.execute(hch);					
	 }
	 
	 
	 private void setLayoutNames() {
		 	Log.d("start setting layout", "start");
		 	Log.d("layout: product_name", product_name);
		 	Log.d("layout: current_amount_warehouse", current_amount_warehouse);
		 	Log.d("layout: current_amount_shop", current_amount_shop);
			TextView tv_product = (TextView)findViewById(R.id.tv_insertproduct_product);
			TextView tv_warehouse = (TextView)findViewById(R.id.tv_insertproduct_sales_area);
			TextView tv_shop = (TextView)findViewById(R.id.tv_insertproduct_stock);
		
			tv_product.setText(getResources().getString(R.string.tv_product_product) + product_name);
			tv_warehouse.setText(getResources().getString(R.string.tv_product_stock) + current_amount_warehouse);
			tv_shop.setText(getResources().getString(R.string.tv_product_sales_area) + current_amount_shop);
			
			Log.d("layout finished", "finished");
	 }
	 
	 @Override
	 public void onBackPressed() {
		 Intent intent = new Intent(this, MainActivity.class);
		 intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		 startActivity(intent);
	 }
	 
}
