package de.dhbw.smar;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import de.dhbw.smar.asynctasks.ASyncHttpConnection;
import de.dhbw.smar.helpers.ActivityCodeHelper;
import de.dhbw.smar.helpers.FileHelper;
import de.dhbw.smar.helpers.HttpConnectionHelper;
import de.dhbw.smar.helpers.PreferencesHelper;
import de.dhbw.smar.svg.SVGObject;

public class SearchProduct extends Activity {

	final Context context = this;
	private ProgressDialog pDialog;
	private HttpConnectionHelper hch;
	String current_product_id;
	String current_amount_warehouse;
	String current_amount_shop;
	String current_product_name;
	String current_unit_id;
	int current_shelf_id;
	int current_section_id;
	int onback_pressed_event = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_product);
		
		// Start searching the finding a product workflow
		//startSearchProductWorkflow();
		
		//startBarcodeScanner
		startSearchProductWorkflow();
		
		//start searching for Product Information and display them
    	
		
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search_product, menu);
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
	
	private void startSearchProductWorkflow(){
		// clean the view
		clean_current_store();
		setLayout();
		// read barcode
    	Intent startNewActivityOpen = new Intent(getBaseContext(), BarcodeScannerActivity.class);
    	startActivityForResult(startNewActivityOpen, ActivityCodeHelper.ACTIVITY_BARCODE_REQUEST);
	}
	
    private void showQuestionProductPutIntoShelf() {
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);
 
			// set title
			alertDialogBuilder.setTitle("Product into shelf");
 
			// set dialog message
			alertDialogBuilder
				.setMessage("Do you want to put this article into the shelf?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// if this button is clicked you switch to 
						// the activity to put products into shelf
						Log.d("Haloop", "bis hierhin");
						Intent intent = new Intent(SearchProduct.this, InsertProduct.class);
						
						Bundle b = new Bundle();
						b.putInt("workflow_position", 1);
						b.putString("product_id", current_product_id);
						b.putString("amount_warehouse", current_amount_warehouse);
						b.putString("amount_shop", current_amount_shop);
						b.putString("product_name", current_product_name);
						b.putString("unit_id", current_unit_id);
						intent.putExtras(b);
						Log.d("Haloop", "extras put");
						startActivity(intent);
						finish();
					}
				})
				.setNegativeButton("No",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, just close
						// the dialog box and go back to main activity
						startSearchProductWorkflow();
					}
				});
 
				// create alert dialog and show it
				alertDialogBuilder.create().show();
    }

    
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	// after read barcode
	// check it. 
	// then, start getting product based information
	{    
    	Log.d("Taaag", String.valueOf(resultCode));
    	Log.d("Result: ", data.getStringExtra("BARCODE"));
    	if (resultCode == Activity.RESULT_OK && !data.getStringExtra("BARCODE").equals(null)) 
	    {
    		Log.d("Barcode", data.getStringExtra("BARCODE"));
	    	String resultBarcode = data.getStringExtra("BARCODE");
	    	Log.d("Barcode2" , resultBarcode);
	    	if(!resultBarcode.equals("")){
	    		Log.d("Started", "Starte Produktsuche");
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
    				startSearchProductWorkflow();
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
						
						current_product_id = json.getString("product_id");
						current_product_name = json.getString("name");
						current_amount_warehouse = json.getString("amount_warehouse");
						current_amount_shop = json.getString("amount_shop");
						current_unit_id = json.getString("unit_id");
						current_shelf_id = json.getInt("shelf_id");
						current_section_id = json.getInt("section_id");
						
						setLayout();
						

						Log.d("onpostexecute", "bis vor das show");
						
						//Create the Picture to display
						showPicture();
						onback_pressed_event = 1;
						//Ask if you want to put this item into shelf
						//Start then the InsertProduct Activity
						//showQuestionProductPutIntoShelf();
						
					}
					catch (JSONException e)
					{
						AlertDialog.Builder alert = new AlertDialog.Builder(context);
			    		alert.setTitle("Failure");
			    		alert.setMessage("Couldn't find information to this product. Check code and talk to admin. Click \"ok\" to scan next prodcut")
			    			 .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			    			public void onClick(DialogInterface dialog, int id) {
			    				dialog.dismiss();
			    				startSearchProductWorkflow();
			    			}
			    		});
			    		alert.create().show();
						e.getStackTrace();
					}
						
				}
				else {
					AlertDialog.Builder alert = new AlertDialog.Builder(context);
		    		alert.setTitle("Failure");
		    		alert.setMessage("Couldn't connect to server to receice information. Check code and talk to admin. Click \"ok\" to scan next prodcut")
		    			 .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		    			public void onClick(DialogInterface dialog, int id) {
		    				dialog.dismiss();
		    				startSearchProductWorkflow();
		    			}
		    		});
		    		alert.create().show();
				}
				
			}
		}.execute(hch);
    }
    
    private void showPicture() {
    	String path = PreferencesHelper.getInstance().getSVGObjectContainer().getSVGObjectPath(current_shelf_id);
    	SVGObject svg = (SVGObject) FileHelper.readSerializable(this, path);

    	
    	ImageView iv = (ImageView)findViewById(R.id.iv_product_shelf);
    	iv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    	Drawable d = svg.getDrawable(current_section_id);
        if(d != null)
        	iv.setImageDrawable(d);
    }
    
    @Override
    public void onBackPressed() {
    	if(onback_pressed_event  == 1) {
    		showQuestionProductPutIntoShelf();
    	}
    	else if (onback_pressed_event == 0) {
    		// go back to main menu
    		Intent intent = new Intent(context, MainActivity.class);
    		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // to clear the rest of activity stack
    		startActivity(intent);
    	}
    }
    
    private void clean_current_store() {
    	current_product_id = "";
    	current_product_name = "";
    	current_amount_shop = "";
    	current_amount_warehouse = "";
    	current_unit_id = "";
    	current_shelf_id = 0;
    	current_section_id = 0;
    	onback_pressed_event = 0;
    	
    }
    
    private void setLayout() {
    	TextView tv_product = (TextView)findViewById(R.id.tv_product);
		TextView tv_warehouse = (TextView)findViewById(R.id.tv_sales_area);
		TextView tv_shop = (TextView)findViewById(R.id.tv_stock);
		
		tv_product.setText(getResources().getString(R.string.tv_product) + current_product_name);
		tv_warehouse.setText(getResources().getString(R.string.tv_stock) + current_amount_warehouse);
		tv_shop.setText(getResources().getString(R.string.tv_sales_area) + current_amount_shop);
    }
}
