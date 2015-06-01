package de.dhbw.smar;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import de.dhbw.smar.asynctasks.ASyncHttpConnection;
import de.dhbw.smar.container.OrderContainer;
import de.dhbw.smar.container.OrderItem;
import de.dhbw.smar.helpers.ActivityCodeHelper;
import de.dhbw.smar.helpers.HttpConnectionHelper;
import de.dhbw.smar.helpers.PreferencesHelper;

@SuppressLint("InflateParams")
public class ReceivingProducts extends Activity {
	//  implements DialogHelper.ShareDialogListener
	private final String logTag = "ReceivingProductActivity";
	
	private final int ERROR_UNKNOWN = 0;
	private final int ERROR_PRODUCT = 1;
	
	private OrderContainer orderContainer;
	private OrderItem tempOrderItem;

	private int process_pos; // 0 = scan Lieferschein, 1 = scan products, 2 = finished work
	private HttpConnectionHelper hch;
	final Context context = this;
	private ProgressDialog pDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_receiving_products);
		// get currently available units and store them in a list
		
		//startBarcodeScanner
		Intent intent = getIntent();
		if(!intent.equals(null)) {
			if(intent.hasExtra("started")) {
			String data = intent.getStringExtra("started");
				if (data.equals("main")) {
					intent.removeExtra("started");
					process_pos = 0;
					this.lookUpOrder();
				}
			}
		}
	}
	
	public void lookUpOrder() {
		// start barcode scanner
		// setze flag, damit nicht immer wieder Lieferschein gescanned werden
    	Intent startNewActivityOpen = new Intent(getBaseContext(), BarcodeScannerActivity.class);
    	startActivityForResult(startNewActivityOpen, ActivityCodeHelper.ACTIVITY_BARCODE_REQUEST);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(requestCode == ActivityCodeHelper.ACTIVITY_BARCODE_REQUEST) {
	    	Log.d(logTag, "Got Result. Result code: " + String.valueOf(resultCode) + "; Barcode: " + data.getStringExtra("BARCODE"));
	    	if(resultCode == Activity.RESULT_OK && !data.getStringExtra("BARCODE").equals("NULL") && data.getStringExtra("BARCODE").length() > 0) {
		    	String resultBarcode = data.getStringExtra("BARCODE");
		    	if(this.process_pos == 0) {
			    	Log.d(logTag, "Start searching for order: " + resultBarcode);
			    	getOrder(resultBarcode);
		    	} else if(this.process_pos == 1) {
		    		Log.d(logTag, "Start searching for product: " + resultBarcode);
		    		getProductInformation(resultBarcode);
		    	}
		    } else {
		    	Log.d(logTag, "Cancelled. Back to MainActivity");
		    	Intent intent = this.getIntent();
				this.setResult(RESULT_OK, intent);
				finish();
	    	} 
    	}
		
	}

	
	protected void getOrder(String orderBarcode) {
		pDialog = ProgressDialog.show(context, getResources().getString(R.string.pd_title_wait), getResources().getString(R.string.pd_content_receiving_receive_infos), true, false);
		String url = "http://" + PreferencesHelper.getInstance().getServer() + "/order/" + orderBarcode;
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
						int order_id;
						String order_name;
						String order_date;
						String order_barcode;
						JSONArray jsonOrderItems;
						
						JSONObject jsonOrder = new JSONObject(hch.getResponseMessage());
						order_id = jsonOrder.getInt("order_id");
						order_name = jsonOrder.getString("name");
						order_date = jsonOrder.getString("date");
						order_barcode = jsonOrder.getString("barcode");
						
						orderContainer = new OrderContainer(order_id, order_name, order_date, order_barcode);
						
						jsonOrderItems = jsonOrder.getJSONArray("items");
						for(int i = 0; i < jsonOrderItems.length(); i++) {
							JSONObject jsonOrderItem = jsonOrderItems.getJSONObject(i);
							OrderItem oi = new OrderItem(
									jsonOrderItem.getInt("product_id"), 
									jsonOrderItem.getString("product_name"),
									jsonOrderItem.getInt("unit_id"), 
									jsonOrderItem.getString("unit_name"), 
									jsonOrderItem.getInt("unit_capacity"), 
									jsonOrderItem.getInt("amount"));
							orderContainer.addItem(oi);
						}
							
						Log.d(logTag, "Show OrderTable");
						setLayout();
						
					} catch (Exception e) {
						Log.e(logTag, "Exception while decoding json array");
						e.printStackTrace();
						showAlertDialog(ERROR_UNKNOWN);
					}
						
				} else if(hch.getResponseCode() == 404){
					try{
						JSONObject jsonError = new JSONObject(hch.getResponseMessage());
						if(jsonError.getString("reason").equals("no_order"))
							showAlertDialog(hch.getResponseCode());
						else
							showAlertDialog(ERROR_UNKNOWN);
					} catch(Exception e) {
						showAlertDialog(ERROR_UNKNOWN);
					}
				} else {
					showAlertDialog(ERROR_UNKNOWN);
				}
				
			}
		}.execute(hch);
	}

	private void setLayout() {
		Log.d(logTag, "Set headline");
		((TextView)findViewById(R.id.tv_receiveproduct_ordername)).setText(getResources().getString(R.string.tv_receiveproduct_ordername) + orderContainer.getName());
		((TextView)findViewById(R.id.tv_receiveproduct_orderbarcode)).setText(orderContainer.getBarcode());
		((TextView)findViewById(R.id.tv_receiveproduct_orderdate)).setText(getResources().getString(R.string.tv_receiveproduct_orderdate) + orderContainer.getDate());
		
		Log.d(logTag, "Set table");
		TableLayout tl = (TableLayout)findViewById(R.id.table_receiving_products);
		tl.removeAllViews();
		Log.d(logTag, "Set table heading");
		TableRow tr = new TableRow(this);
		tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT / 5, TableRow.LayoutParams.WRAP_CONTENT));
		
		TextView tv1 = new TextView(this);
		tv1.setText(getResources().getString(R.string.tv_name));
		tv1.setTextSize(22);
		tr.addView(tv1);
		
		TextView tv2 = new TextView(this);
		tv2.setText(getResources().getString(R.string.tv_unit));
		tv2.setTextSize(22);
		tr.addView(tv2);
		
		TextView tv3 = new TextView(this);
		tv3.setText(getResources().getString(R.string.tv_amount));
		tv3.setTextSize(22);
		tr.addView(tv3);
		
		tl.addView(tr, new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
		
		Log.d(logTag, "Set table body");
		Log.d(logTag, "Get Items from OrderContainer as array list and add items to table");
		ArrayList<OrderItem> orderItems = orderContainer.getItems();
		for(OrderItem oi: orderItems) {
			tr = new TableRow(this);
			tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT / 3, TableRow.LayoutParams.WRAP_CONTENT));
			
			tv1 = new TextView(this);
			tv1.setText(oi.getProduct_name());
			tv1.setTextColor(Color.WHITE);
			tv1.setTextSize(22);
			tr.addView(tv1);
			
			tv2 = new TextView(this);
			tv2.setText(oi.getUnit_name());
			tv2.setTextColor(Color.WHITE);
			tv2.setTextSize(22);
			tr.addView(tv2);
			
			tv3 = new TextView(this);
			tv3.setText(oi.getAmount() + "(" + oi.getDelivered_amount() + ")");
			tv3.setTextColor(Color.WHITE);
			tv3.setTextSize(22);
			tr.addView(tv3);
			
			tl.addView(tr, new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
		}
		Log.d(logTag, "Table finished");
	}
	
	public void go_on_scanning(View view) {
		process_pos = 1;
		Intent startNewActivityOpen = new Intent(getBaseContext(), BarcodeScannerActivity.class);
    	startActivityForResult(startNewActivityOpen, ActivityCodeHelper.ACTIVITY_BARCODE_REQUEST);
	}
	
	public void finish_scanning(View view) {
		updateDatabase();
	}
	
	 private void getProductInformation(final String barcode) {
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
							int product_id;
							String product_name;
							int unit_id;
							String unit_name;
							int unit_capacity;
							int amount = 0;
							
							Log.d(logTag, "Start decoding json array");
							JSONArray jArray = new JSONArray(result);
							if(jArray.length() > 0) {
								JSONObject json = jArray.getJSONObject(0);
								product_id = json.getInt("product_id");
								product_name = json.getString("name");
								if(json.getString("unit").equals("0")) {
									unit_id = 1;
									unit_name = getResources().getString(R.string.var_unit_single);
									unit_capacity = 1;
								} else {
									JSONObject jsonUnit = json.getJSONObject("unit");
									unit_id = jsonUnit.getInt("unit_id");
									unit_name = jsonUnit.getString("name");
									unit_capacity = jsonUnit.getInt("capacity");
								}
								
								tempOrderItem = new OrderItem(product_id, product_name, unit_id, unit_name, unit_capacity, amount);
								showAmountChooser();
							} else {
								showAlertDialog(ERROR_PRODUCT);
							}						
						} catch (Exception e) {
							Log.e(logTag, "Exception while decoding json array");
							e.printStackTrace();
							showAlertDialog(ERROR_UNKNOWN);
						}
							
					} else if(hch.getResponseCode() == 404 && hch.getResponseMessage().equals("[]")){
						showAlertDialog(ERROR_PRODUCT);
					} else {
						showAlertDialog(ERROR_UNKNOWN);
					}
					
				}
			}.execute(hch);
	    }
	 
	private void showAmountChooser() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	LayoutInflater inflater = this.getLayoutInflater();
        View v = inflater.inflate(R.layout.unit_amount_layout, null); 
        
    	builder.setTitle(v.getResources().getString(R.string.ad_title_amountchooser));
        
        //get textviews and amount field
        final EditText etAmount = (EditText)v.findViewById(R.id.et_ad_amount);
        final TextView tvProduct = (TextView)v.findViewById(R.id.tv_ad_product);
        final TextView tvUnit = (TextView)v.findViewById(R.id.tv_ad_unit);
        
        tvProduct.setText(v.getResources().getString(R.string.tv_product_product) + tempOrderItem.getProduct_name());
        tvUnit.setText(v.getResources().getString(R.string.tv_product_current_unit) + tempOrderItem.getUnit_name() + " (" + tempOrderItem.getUnit_capacity() + ")");
        
        builder.setView(v)
               .setPositiveButton(v.getResources().getString(R.string.ad_bt_add), new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                	  dialog.dismiss();
                	  tempOrderItem.setDelivered_amount(Integer.valueOf(etAmount.getText().toString()));
                	  checkDelivery();
                   }
               })
               .setNegativeButton(v.getResources().getString(R.string.ad_bt_cancel), new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       dialog.dismiss();
                   }
               });      
         builder.create().show();
	}
	
	private void checkDelivery() {
		int product_id = tempOrderItem.getProduct_id();
		int unit_id = tempOrderItem.getUnit_id();
		if(orderContainer.isInOrder(product_id, unit_id)) {
			Log.d(logTag, "Product is already in Order!");
			if(orderContainer.getDeliveredAmount(product_id, unit_id) != 0) {
				Log.d(logTag, "Product is already in order and has been received... show dialog!");
				AlertDialog.Builder alert = new AlertDialog.Builder(context);
	    		alert.setTitle(getResources().getString(R.string.ad_title_overwrite));
	    		alert.setMessage(getResources().getString(R.string.ad_content_overwrite))
	    			 .setPositiveButton(getResources().getString(R.string.ad_bt_add), new DialogInterface.OnClickListener() {
		    			public void onClick(DialogInterface dialog, int id) {
		    				dialog.dismiss();
		    				addDelivery(false);
		    			}
	    			 })
	    			 .setNeutralButton(getResources().getString(R.string.ad_bt_cancel), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							tempOrderItem = null;
						}
					})
	    			 .setNegativeButton(getResources().getString(R.string.ad_bt_overwrite), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							addDelivery(true);
						}
					});
	    		alert.create().show();
			} else {
				Log.d(logTag, "Product delivey amount is zero");
				addDelivery(true);
			}
		} else {
			Log.d(logTag, "Product is not in order");
			addDelivery(true);
		}
	}
	
	private void addDelivery(boolean overwrite) {
		int product_id = tempOrderItem.getProduct_id();
		int unit_id = tempOrderItem.getUnit_id();
		int delivered_amount = tempOrderItem.getDelivered_amount();
		if(overwrite) {
			if(orderContainer.isInOrder(product_id, unit_id)) {
				orderContainer.setDeliveredAmount(delivered_amount, product_id, unit_id);
			} else {
				orderContainer.addItem(tempOrderItem);
			}
		} else {
			int old_delivered_amount = orderContainer.getDeliveredAmount(product_id, unit_id);
			int new_delivered_amount = old_delivered_amount + delivered_amount;
			orderContainer.setDeliveredAmount(new_delivered_amount, product_id, unit_id);
		}
		
		tempOrderItem = null;
		Toast.makeText(context, getResources().getString(R.string.toast_delivery_product_added), Toast.LENGTH_LONG).show();
		setLayout();
	}
	
	private void updateDatabase() {
		try {
			pDialog = ProgressDialog.show(context, getResources().getString(R.string.pd_title_wait), getResources().getString(R.string.pd_content_updating), true, false);
			String url = "http://" + PreferencesHelper.getInstance().getServer() + "/delivery/create";
			hch = new HttpConnectionHelper(url,  HttpConnectionHelper.REQUEST_TYPE_POST);
			
			// First basic stuff
			JSONObject jsonDelivery = new JSONObject();
			jsonDelivery.put("order_id", orderContainer.getOrder_id());
			
			// Delivery items to JSONArray
			JSONArray jsonItems = new JSONArray();
			ArrayList<OrderItem> orderItemsDelivery = orderContainer.getItems();
			for(OrderItem oiDelivery: orderItemsDelivery) {
				JSONObject jsonOIDelivery = new JSONObject();
				jsonOIDelivery.put("product_id", oiDelivery.getProduct_id());
				jsonOIDelivery.put("unit_id", oiDelivery.getUnit_id());
				jsonOIDelivery.put("amount", oiDelivery.getDelivered_amount());
				jsonItems.put(jsonOIDelivery);
				jsonOIDelivery = null;
			}
			jsonDelivery.put("delivery_items", jsonItems);
			
			Log.d(logTag, "delivery json: " + jsonDelivery.toString());
			
	
	//		Put data into $_POST variable
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("json_delivery", jsonDelivery.toString()));
			hch.setPostPair(nameValuePairs);
	//		inform user and start
			
			new ASyncHttpConnection() {
				@Override
				public void onPostExecute(String result) {
					Log.d(logTag, "Response code: " + hch.getResponseCode() + "; Response message: " + hch.getResponseMessage());
					if(!hch.getError() && hch.getResponseCode() == 200) { 
						pDialog.dismiss();
						Toast toast = Toast.makeText(context, getResources().getString(R.string.toast_delivery_successfully), Toast.LENGTH_LONG);
						toast.show();
						
						closeActivity();
					} else {
						showAlertDialog(ERROR_UNKNOWN);
					}
				}
			}.execute(hch);
		} catch(Exception e) {
			Log.e(logTag, "Exception while encoding json array");
			e.printStackTrace();
			showAlertDialog(ERROR_UNKNOWN);
		}
	}
	
	private void closeActivity() {
		orderContainer = null;
		process_pos = 0;
		tempOrderItem = null;
		
		Intent intent = this.getIntent();
		this.setResult(RESULT_OK, intent);
		finish();
	}
	
	@Override
	public void onBackPressed() {
		AlertDialog.Builder alert = new AlertDialog.Builder(context);
		alert.setTitle(getResources().getString(R.string.ad_title_receiving_cancel));
		alert.setMessage(getResources().getString(R.string.ad_content_receiving_cancel))
			 .setPositiveButton(getResources().getString(R.string.ad_bt_yes), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
					closeActivity();
				}
			 })
			 .setNegativeButton(getResources().getString(R.string.ad_bt_no), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		alert.create().show();
	}

	/* 	
	private void searchAvailableUnits() {
		 //get all available Units to this product
		 pDialog = ProgressDialog.show(context, getResources().getString(R.string.pd_title_wait), getResources().getString(R.string.pd_content_available_units), true, false);
			String url = "http://" + PreferencesHelper.getInstance().getServer() + "/units";
			Log.d("Start connectinting to: ", "server url: " + url);
			Log.d("API CALL", "starting search for available units");
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
							Log.d("API CALL", "Got some units...");
							Log.d("start json", result);
							
							JSONArray jArray = new JSONArray(result);
							if(jArray.length() == 0) {
								Log.d("API CALL", "JSONArray for units is empty");
							} else {
								for(int i = 0; i < jArray.length(); i++) {
									//put all available units into one list
									Log.d("json", "started looping");
									JSONObject json = jArray.getJSONObject(i);
									available_units.add(json.getString("unit_id")+": "+json.getString("name"));
									Log.d("API CALLL","getting units: " +  json.getString("unit_id")+": "+json.getString("name"));
								}
							}
							
							
						}
						catch (Exception e) 
						{
							e.printStackTrace();
						}
					}
					else {
						AlertDialog.Builder alert = new AlertDialog.Builder(context);
						alert.setTitle(getResources().getString(R.string.ad_title_no_information));
						alert.setMessage(getResources().getString(R.string.ad_content_no_information));
						alert.setPositiveButton(getResources().getString(R.string.ad_bt_ok), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								process_pos = 1;
								scanProduct();
							}
						});
						alert.create().show();
					}
				}
			}.execute(hch);					
	 }
	 
	 */
	
	private void showAlertDialog(int errorCode) {
    	if(errorCode == 404) {
    		AlertDialog.Builder alert = new AlertDialog.Builder(context);
    		alert.setTitle("Order not found (" + hch.getResponseCode() + ")");
    		alert.setMessage(getResources().getString(R.string.ad_content_no_order))
    			 .setPositiveButton(getResources().getString(R.string.ad_bt_ok), new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int id) {
    				dialog.dismiss();
    				lookUpOrder();
    			}
    		});
    		alert.create().show();
    	} else if(errorCode == ERROR_PRODUCT) {
    		AlertDialog.Builder alert = new AlertDialog.Builder(context);
    		alert.setTitle("Product not found (" + hch.getResponseCode() + ")");
    		alert.setMessage(getResources().getString(R.string.ad_content_no_information))
    			 .setPositiveButton(getResources().getString(R.string.ad_bt_ok), new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int id) {
    				dialog.dismiss();
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
	
	public void cancelApp() {
    	Intent intent = this.getIntent();
		this.setResult(RESULT_CANCELED, intent);
		finish();
    }
}

















