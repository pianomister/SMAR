package de.dhbw.smar;

import org.json.JSONArray;
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
	private final String logTag = "SearchProduct";
	
	private int ERROR_SVG = 0;
	private int ERROR_UNKNOWN = 1;
	private int ERROR_NOSHELF = 2;

	final Context context = this;
	private ProgressDialog pDialog;
	private HttpConnectionHelper hch;
	String current_product_id;
	int current_amount_warehouse;
	int current_amount_shop;
	String current_product_name;
	String current_unit_id;
	int current_shelf_id;
	int current_section_id;
	int current_shelf_capacity;
	private boolean startedInsertProduct = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_product);
		
		// Start searching the finding a product workflow
		Intent intent = getIntent();
		if(!intent.equals(null)) {
			if(intent.hasExtra("insertAction")) {
				startedInsertProduct = intent.getBooleanExtra("insertAction", false);
			}
			if(intent.hasExtra("started")) {
				String data = intent.getStringExtra("started");
				if (data.equals("main")) 
					startSearchProductWorkflow();
			}
		}
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
			alertDialogBuilder.setTitle(getResources().getString(R.string.ad_title_put_into_shelf));
 
			// set dialog message
			alertDialogBuilder
				.setMessage(getResources().getString(R.string.ad_content_put_into_shelf))
				.setPositiveButton(getResources().getString(R.string.ad_bt_yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// if this button is clicked you switch to 
						// the activity to put products into shelf
						startInsertProductActivity();
					}
				})
				.setNegativeButton(getResources().getString(R.string.ad_bt_no),new DialogInterface.OnClickListener() {
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// after read barcode
		// check it. 
		// then, start getting product based information
    	if(requestCode == ActivityCodeHelper.ACTIVITY_BARCODE_REQUEST) {
	    	Log.d(logTag, "Got Result. Result code: " + String.valueOf(resultCode) + "; Barcode: " + data.getStringExtra("BARCODE"));
	    	if(resultCode == Activity.RESULT_OK && !data.getStringExtra("BARCODE").equals("NULL") && data.getStringExtra("BARCODE").length() > 0) {
		    	String resultBarcode = data.getStringExtra("BARCODE");
		    	Log.d(logTag, "Start searching for product: " + resultBarcode);
	    		searchProductInformation(resultBarcode);
		    } else {
		    	Log.d(logTag, "Cancelled. Back to MainActivity");
		    	Intent intent = this.getIntent();
				this.setResult(RESULT_OK, intent);
				finish();
	    	} 
    	}
    	if(requestCode == ActivityCodeHelper.ACTIVITY_INSERTPRODUCT_REQUEST) {
    		if(resultCode == Activity.RESULT_OK) {
    			startSearchProductWorkflow();
    		} else if(resultCode == Activity.RESULT_CANCELED) {
    			if(data.getBooleanExtra(ActivityCodeHelper.ACTIVITY_INSERTPRODUCT_DESTROY, false)) {
    				cancelApp();
    			} else {
	    			Log.d(logTag, "Cancelled. Back to MainActivity");
			    	Intent intent = this.getIntent();
					this.setResult(RESULT_OK, intent);
					finish();
    			}
    		}
    	}
	}
    
    private void searchProductInformation(final String barcode) {
    	// get information of the product
    	// set up asynchronous task 
    	// set up connection to the server 
    	// retrieve data from server
    	// display the information
    	pDialog = ProgressDialog.show(context, getResources().getString(R.string.pd_title_wait), getResources().getString(R.string.pd_content_receiving_product_infos), true, false);
		String url = "http://" + PreferencesHelper.getInstance().getServer() + "/product/" + barcode;
		Log.d(logTag, "server url: " + url);
		hch = new HttpConnectionHelper(url);
		new ASyncHttpConnection() {
			@Override
			public void onPostExecute(String result) {
				pDialog.dismiss();
				// Result ist das JSON Objekt
				Log.d(logTag, "Response code: " + hch.getResponseCode() + "; Response message: " + hch.getResponseMessage());
				if(!hch.getError() && hch.getResponseCode() == 200) { 
					try {
						Log.d(logTag, "Start decoding json array");
						JSONArray jArray = new JSONArray(result);
						if(jArray.length() > 0) {
							JSONObject json = jArray.getJSONObject(0);
							current_product_id = json.getString("product_id");
							current_product_name = json.getString("name");
							
							JSONObject jsonStock = new JSONObject(json.getString("stock"));
							current_amount_warehouse = jsonStock.getInt("amount_warehouse");
							current_amount_shop = jsonStock.getInt("amount_shop");
							current_unit_id = json.getString("unit");
							
							if(json.getString("shelf").equals("0")) {
								showAlertDialog(ERROR_NOSHELF);
							} else {
								JSONObject jsonShelf = new JSONObject(json.getString("shelf"));
								current_shelf_id = jsonShelf.getInt("shelf_id");
								current_section_id = jsonShelf.getInt("section_id");
								current_shelf_capacity = jsonShelf.getInt("capacity");
								if(startedInsertProduct) {
									startInsertProductActivity();
								} else {
									setLayout();
		
									//Create the Picture to display
									showPicture();
								}
							}
						} else {
							showAlertDialog(404);
						}						
					} catch (Exception e) {
						Log.e(logTag, "Exception while decoding json array");
						e.printStackTrace();
						showAlertDialog(ERROR_UNKNOWN);
					}
						
				} else if(hch.getResponseCode() == 404 && hch.getResponseMessage().equals("[]")){
					showAlertDialog(hch.getResponseCode());
				} else {
					showAlertDialog(ERROR_UNKNOWN);
				}
				
			}
		}.execute(hch);
    }
    
    private void startInsertProductActivity() {
    	Intent intent = new Intent(SearchProduct.this, InsertProduct.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		Bundle b = new Bundle();
		b.putString("product_id", current_product_id);
		b.putInt("amount_warehouse", current_amount_warehouse);
		b.putInt("amount_shop", current_amount_shop);
		b.putString("product_name", current_product_name);
		b.putString("unit", current_unit_id);
		b.putInt("shelf_id", current_shelf_id);
		b.putInt("section_id", current_section_id);
		b.putInt("shelf_capacity", current_shelf_capacity);
		intent.putExtras(b);

		startActivityForResult(intent, ActivityCodeHelper.ACTIVITY_INSERTPRODUCT_REQUEST);
    }
    
    private void showAlertDialog(int errorCode) {
    	if(errorCode == 404) {
    		AlertDialog.Builder alert = new AlertDialog.Builder(context);
    		alert.setTitle("Product not found (" + hch.getResponseCode() + ")");
    		alert.setMessage(getResources().getString(R.string.ad_content_no_information))
    			 .setPositiveButton(getResources().getString(R.string.ad_bt_ok), new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int id) {
    				dialog.dismiss();
    				startSearchProductWorkflow();
    			}
    		});
    		alert.create().show();
    	} else if(errorCode == ERROR_SVG) {
    		AlertDialog.Builder alert = new AlertDialog.Builder(context);
    		alert.setTitle(getResources().getString(R.string.ad_title_svg_error));
    		alert.setMessage(getResources().getString(R.string.ad_content_svg_error))
    			 .setPositiveButton(getResources().getString(R.string.ad_bt_exitapp), new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int id) {
    				dialog.dismiss();
    				cancelApp();
    			}
    		});
    		alert.create().show();
    	} else if(errorCode == ERROR_UNKNOWN) {
    		AlertDialog.Builder alert = new AlertDialog.Builder(context);
    		alert.setTitle(getResources().getString(R.string.ad_title_unfortunely_closed));
    		alert.setMessage(getResources().getString(R.string.ad_content_json))
    			 .setPositiveButton(getResources().getString(R.string.ad_bt_exitapp), new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int id) {
    				dialog.dismiss();
    				cancelApp();
    			}
    		});
    		alert.create().show();
    	} else if(errorCode == ERROR_NOSHELF) {
    		AlertDialog.Builder alert = new AlertDialog.Builder(context);
    		alert.setTitle(getResources().getString(R.string.ad_title_noshelf));
    		alert.setMessage(getResources().getString(R.string.ad_content_noshelf))
    			 .setPositiveButton(getResources().getString(R.string.ad_bt_ok), new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int id) {
    				dialog.dismiss();
    				startSearchProductWorkflow();
    			}
    		});
    		alert.create().show();
    	} else {
    		AlertDialog.Builder alert = new AlertDialog.Builder(context);
    		alert.setTitle(getResources().getString(R.string.ad_title_unfortunely_closed));
    		alert.setMessage(getResources().getString(R.string.ad_content_json))
    			 .setPositiveButton(getResources().getString(R.string.ad_bt_exitapp), new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int id) {
    				dialog.dismiss();
    				cancelApp();
    			}
    		});
    		alert.create().show();
    	}
    }
    
    private void showPicture() {
    	String path = PreferencesHelper.getInstance().getSVGObjectContainer().getSVGObjectPath(current_shelf_id);
    	SVGObject svg = (SVGObject) FileHelper.readSerializable(this, path);
    	if(svg == null) {
    		showAlertDialog(ERROR_SVG);
    	} else {
	    	ImageView iv = (ImageView)findViewById(R.id.iv_searchproduct_product_shelf);
	    	iv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	    	Drawable d = svg.getDrawable(current_section_id);
	        if(d != null)
	        	iv.setImageDrawable(d);
	        else
	        	showAlertDialog(ERROR_SVG);
    	}
    }
    
    @Override
    public void onBackPressed() {
    	showQuestionProductPutIntoShelf();
    }
    
    private void clean_current_store() {
    	current_product_id = "";
    	current_product_name = "";
    	current_amount_shop = 0;
    	current_amount_warehouse = 0;
    	current_unit_id = "";
    	current_shelf_id = 0;
    	current_section_id = 0;
    	current_shelf_capacity = 0;    	
    }
    
    private void setLayout() {
    	TextView tv_product = (TextView)findViewById(R.id.tv_searchproduct_product);
		TextView tv_warehouse = (TextView)findViewById(R.id.tv_searchproduct_sales_area);
		TextView tv_shop = (TextView)findViewById(R.id.tv_searchproduct_stock);
		TextView tv_shelf = (TextView)findViewById(R.id.tv_searchproduct_shelfid);
		
		tv_shelf.setText(getResources().getString(R.string.tv_product_shelfid) + current_shelf_id);
		tv_product.setText(getResources().getString(R.string.tv_product_product) + current_product_name);
		tv_warehouse.setText(getResources().getString(R.string.tv_product_stock) + current_amount_warehouse);
		tv_shop.setText(getResources().getString(R.string.tv_product_sales_area) + current_amount_shop);
    }
    
    public void cancelApp() {
    	Intent intent = this.getIntent();
		this.setResult(RESULT_CANCELED, intent);
		finish();
    }
}
