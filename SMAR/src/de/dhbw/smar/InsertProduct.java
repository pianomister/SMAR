package de.dhbw.smar;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import de.dhbw.smar.asynctasks.ASyncHttpConnection;
import de.dhbw.smar.helpers.ActivityCodeHelper;
import de.dhbw.smar.helpers.FileHelper;
import de.dhbw.smar.helpers.HttpConnectionHelper;
import de.dhbw.smar.helpers.PreferencesHelper;
import de.dhbw.smar.svg.SVGObject;

public class InsertProduct extends Activity {
	
	private final String logTag = "InsertProductActivity";
	
	private final int ERROR_UNKNOWN = 0;
	private final int ERROR_QUERY = 1;
	private final int ERROR_PARAMETERS = 2;
	private final int ERROR_PERMISSION = 3;
	private final int ERROR_CAPACITY = 4;
	private final int ERROR_SVG = 5;
	private final int ERROR_OUTOFSTOCK = 6;
	
	String product_name;
	String current_unit;
	int current_amount_warehouse;
	int current_amount_shop;
	String current_product_id;
	int current_shelf_id;
	int current_section_id;
	int current_shelf_capacity;
	int current_unit_id;
	String current_unit_name;
	int current_unit_capacity;
	final Context context = this;
	private ProgressDialog pDialog;
	private HttpConnectionHelper hch;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_insert_product);
		
		// In the Bundle are some params
		// They indicate where to start this activity
		Log.d(logTag, "before getting bundles");
		Bundle b = getIntent().getExtras();
		Log.d(logTag, "after getting bundles");
		
		/*
		 * b.putString("product_id", current_product_id);
		b.putString("amount_warehouse", current_amount_warehouse);
		b.putString("amount_shop", current_amount_shop);
		b.putString("product_name", current_product_name);
		b.putString("unit", current_unit_id);
		b.putInt("shelf_id", current_shelf_id);
		b.putInt("section_id", current_section_id);
		b.putInt("shelf_capacity", current_shelf_capacity);
		 * 
		 */
		
		Log.d(logTag, "Given Bundle: " + b.toString());

		try {
			current_product_id = b.getString("product_id");
			current_amount_warehouse = b.getInt("amount_warehouse");
			current_amount_shop = b.getInt("amount_shop");
			product_name = b.getString("product_name");
			current_unit = b.getString("unit");
			if(current_unit.equals("0")) {
				current_unit_id = 1;
				current_unit_name = getResources().getString(R.string.var_unit_single);
				current_unit_capacity = 1;
			} else {
				JSONObject jsonUnit = new JSONObject(current_unit);
				current_unit_id = jsonUnit.getInt("unit_id");
				current_unit_name = jsonUnit.getString("name");
				current_unit_capacity = jsonUnit.getInt("capacity");
			}
			current_shelf_id = b.getInt("shelf_id");
			current_section_id = b.getInt("section_id");
			current_shelf_capacity = b.getInt("shelf_capacity");
			
			setLayoutNames();
			showPicture();
		} catch(Exception e) {
			// TODO: ERROR
			Log.e(logTag, "Error while decoding JSON Object..");
			e.printStackTrace();
		}
	}
	
    private void showPicture() {
    	String path = PreferencesHelper.getInstance().getSVGObjectContainer().getSVGObjectPath(current_shelf_id);
    	SVGObject svg = (SVGObject) FileHelper.readSerializable(this, path);
    	if(svg == null) {
    		showAlertDialog(ERROR_SVG);
    	} else {
	    	ImageView iv = (ImageView)findViewById(R.id.iv_insertproduct_product_shelf);
	    	iv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	    	Drawable d = svg.getDrawable(current_section_id);
	        if(d != null)
	        	iv.setImageDrawable(d);
	        else
	        	showAlertDialog(ERROR_SVG);
    	}
    }

	public void insertProductPressed(View view) {
		// updates the stock of the product
		// concrete: move amount from Warehouse to Shop
	 	Log.d(logTag, "Insert button pressed");
	 	int insertAmount = Integer.parseInt(((EditText)findViewById(R.id.et_insertproduct_amount)).getText().toString());
	 	
	 	if((current_amount_shop + (insertAmount*current_unit_capacity)) < current_shelf_capacity) {
	 		
	 		if((insertAmount*current_unit_capacity) <= current_amount_warehouse) { 
	 			
		    	pDialog = ProgressDialog.show(context, getResources().getString(R.string.pd_title_wait), getResources().getString(R.string.pd_content_updating), true, false);
				String url = "http://" + PreferencesHelper.getInstance().getServer() + "/stock/update";
				Log.d(logTag, "server url: " + url);
				hch = new HttpConnectionHelper(url, HttpConnectionHelper.REQUEST_TYPE_POST);
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
				nameValuePairs.add(new BasicNameValuePair("product_id", this.current_product_id));
				nameValuePairs.add(new BasicNameValuePair("insertAmount", String.valueOf(insertAmount)));
				nameValuePairs.add(new BasicNameValuePair("unit_capacity", String.valueOf(current_unit_capacity)));
				hch.setPostPair(nameValuePairs);
				new ASyncHttpConnection() {
					@Override
					public void onPostExecute(String result) {
						pDialog.dismiss();
						// Result ist das JSON Objekt
						Log.d(logTag, "Result code: " + hch.getResponseCode() + "; Result message: " + hch.getResponseMessage());
						if(hch.getResponseCode() == 200) {
							Toast.makeText(context, 
				    				getResources().getString(R.string.t_dbupdated),
					    	        Toast.LENGTH_LONG).show();
							finishInsert();
						} else if(hch.getResponseCode() == 500) {
							if(hch.getResponseMessage().equals("query"))
								showAlertDialog(ERROR_QUERY);
							else if(hch.getResponseMessage().equals("parameters"))
								showAlertDialog(ERROR_PARAMETERS);
							else
								showAlertDialog(ERROR_UNKNOWN);
						} else if(hch.getResponseCode() == 403) {
							showAlertDialog(ERROR_PERMISSION);
						} else {
							showAlertDialog(ERROR_UNKNOWN);
						}
					}
				}.execute(hch);
	 		} else {
	 			showAlertDialog(ERROR_OUTOFSTOCK);
	 			Log.e(logTag, "Amount bigger than stock");
	 		}
	 	} else {
	 		showAlertDialog(ERROR_CAPACITY);
	 		Log.e(logTag, "Amount bigger than capacity");
	 	}
	 }
	 
	 private void showAlertDialog(int errorCode) {
		 AlertDialog.Builder alert;
		 switch(errorCode) {
		 case ERROR_QUERY:
			 alert = new AlertDialog.Builder(context);
				alert.setTitle(getResources().getString(R.string.ad_title_query));
				alert.setMessage(getResources().getString(R.string.ad_content_query))
					 .setPositiveButton(getResources().getString(R.string.ad_bt_exitapp), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						cancelApp();
					}
				});
				
			 break;
		 case ERROR_PARAMETERS:
			 alert = new AlertDialog.Builder(context);
				alert.setTitle(getResources().getString(R.string.ad_title_parameters));
				alert.setMessage(getResources().getString(R.string.ad_content_parameters))
					 .setPositiveButton(getResources().getString(R.string.ad_bt_exitapp), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						cancelApp();
					}
				});
			 break;
		 case ERROR_PERMISSION:
			 alert = new AlertDialog.Builder(context);
				alert.setTitle(getResources().getString(R.string.ad_title_permission));
				alert.setMessage(getResources().getString(R.string.ad_content_permission))
					 .setPositiveButton(getResources().getString(R.string.ad_bt_exitapp), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						cancelApp();
					}
				});
			 break;
		 case ERROR_CAPACITY:
			 alert = new AlertDialog.Builder(context);
				alert.setTitle(getResources().getString(R.string.ad_title_capacity));
				alert.setMessage(getResources().getString(R.string.ad_content_capacity))
					 .setPositiveButton(getResources().getString(R.string.ad_bt_ok), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						((EditText)findViewById(R.id.et_insertproduct_amount)).setText("0");
					}
				});
			 break;
		 case ERROR_OUTOFSTOCK:
			 alert = new AlertDialog.Builder(context);
				alert.setTitle(getResources().getString(R.string.ad_title_outofstock));
				alert.setMessage(getResources().getString(R.string.ad_content_outofstock))
					 .setPositiveButton(getResources().getString(R.string.ad_bt_ok), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						((EditText)findViewById(R.id.et_insertproduct_amount)).setText("0");
					}
				});
			 break;
		 case ERROR_SVG:
			 alert = new AlertDialog.Builder(context);
	    		alert.setTitle(getResources().getString(R.string.ad_title_svg_error));
	    		alert.setMessage(getResources().getString(R.string.ad_content_svg_error))
	    			 .setPositiveButton(getResources().getString(R.string.ad_bt_exitapp), new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int id) {
	    				dialog.dismiss();
	    				cancelApp();
	    			}
	    		});
		 case ERROR_UNKNOWN:
		 default:
			alert = new AlertDialog.Builder(context);
			alert.setTitle(getResources().getString(R.string.ad_title_unfortunely_closed));
			alert.setMessage(getResources().getString(R.string.ad_content_json))
				 .setPositiveButton(getResources().getString(R.string.ad_bt_exitapp), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
					cancelApp();
				}
			});
			break;
		 }
		 alert.create().show();
	 }
	 
	 private void finishInsert() {
		Intent intent = this.getIntent();
		this.setResult(RESULT_OK, intent);
		finish();
	 }
	 
	 private void setLayoutNames() {
			TextView tv_product = (TextView)findViewById(R.id.tv_insertproduct_product);
			TextView tv_warehouse = (TextView)findViewById(R.id.tv_insertproduct_sales_area);
			TextView tv_shop = (TextView)findViewById(R.id.tv_insertproduct_stock);
			TextView tv_unit = (TextView)findViewById(R.id.tv_insertproduct_current_unit);
		
			tv_unit.setText(getResources().getString(R.string.tv_product_current_unit) + current_unit_name);
			tv_product.setText(getResources().getString(R.string.tv_product_product) + product_name);
			tv_warehouse.setText(getResources().getString(R.string.tv_product_stock) + current_amount_warehouse);
			tv_shop.setText(getResources().getString(R.string.tv_product_sales_area) + current_amount_shop);
			
			Log.d(logTag, "layout finished");
	 }
	 
	 private void cancelApp() {
		Intent intent = this.getIntent();
		intent.putExtra(ActivityCodeHelper.ACTIVITY_INSERTPRODUCT_DESTROY, true);
		this.setResult(RESULT_CANCELED, intent);
		finish();
	 }
	 
	 @Override
	 public void onBackPressed() {
		Intent intent = this.getIntent();
		this.setResult(RESULT_CANCELED, intent);
		finish();
	 }
	 
}
