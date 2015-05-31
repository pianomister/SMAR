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
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import de.dhbw.smar.asynctasks.ASyncHttpConnection;
import de.dhbw.smar.helpers.ActivityCodeHelper;
import de.dhbw.smar.helpers.DialogHelper;
import de.dhbw.smar.helpers.HttpConnectionHelper;
import de.dhbw.smar.helpers.PreferencesHelper;
import de.dhbw.smar.helpers.ReceivingListHelper;
import de.dhbw.smar.helpers.product;

public class ReceivingProducts extends Activity implements DialogHelper.ShareDialogListener{

	private int process_pos; // 0 = scan Lieferschein, 1 = scan products, 2 = finished work
	private String current_barcode_receiving_note;
	private HttpConnectionHelper hch;
	final Context context = this;
	private ProgressDialog pDialog;
	private String current_product_barcode;
	private String current_product_id;
	private ArrayList<product> ListOfReceivingUnits = ReceivingListHelper.getInstance().get();
	private ArrayList<product> ListOfScannedProducts = new ArrayList<product>();
	private ArrayList<String> available_units = new ArrayList<String>();
	private String current_product_amount_warehouse;
	private int current_order_id = 0;
	
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
					searchAvailableUnits();
					this.startProductStore();
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.receiving_products, menu);
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
	
	public void startProductStore() {
		// start barcode scanner
		// setze flag, damit nicht immer wieder Lieferschein gescanned werden
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
			
			if(this.process_pos == 1) {
				// scan here products
				this.current_product_barcode = resultBarcode;
				Log.d("API CALL", "Start getting product infos after setlayout");
				startGetProductInfos();
			}
			else if(this.process_pos == 0) {
				this.process_pos = 1;
				this.current_barcode_receiving_note = resultBarcode;
				//start rest api to get the infos about receiving note 
				Log.d("API CALL", "Start getting infos to Lieferschein");
				startGetReceivingInfos();
			}
	    	
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
    				
    			}
    		});
    		alert.create().show(); */
    	}
		
	}

	
	protected void startGetReceivingInfos() {
		pDialog = ProgressDialog.show(context, getResources().getString(R.string.pd_title_wait), getResources().getString(R.string.pd_content_receiving_receive_infos), true, false);
		String url = "http://" + PreferencesHelper.getInstance().getServer() + "/order/" + current_barcode_receiving_note;
		Log.d("Start connectinting to: ", "server url: " + url);
		hch = new HttpConnectionHelper(url);
		new ASyncHttpConnection() {
			@Override
			public void onPostExecute(String result) {
				pDialog.dismiss();
				// Result ist das JSON Objekt
				Log.d("API CALL", result);
				if(!hch.getError() && hch.getResponseCode() == 200) { 
					try {
						
						JSONArray jArray = new JSONArray(hch.getResponseMessage());
						if(jArray.length() > 0) {
							for(int i = 0; i < jArray.length(); i++) {
								JSONObject json = jArray.getJSONObject(i);
								product p = new product();
								p.setName(json.getString("product_name"));
								p.setReceiving_name(json.getString("receiving_name"));
								p.setAmount(json.getString("amount"));
								p.setUnit(json.getString("unit"));
								p.setReceiving_date(json.getString("receiving_date"));
								p.setId(json.getInt("product_id"));
								current_order_id = json.getInt("order_id");
								ReceivingListHelper.getInstance().get().add(p);
							}
							
							
							CharSequence text = getResources().getString(R.string.toast_got_receiving_note);
							int duration = Toast.LENGTH_LONG;
	
							Toast toast = Toast.makeText(context, text, duration);
							toast.show();
							Log.d("read json", "reading json...");
							
	
							//layout names setzen
							Log.d("setLayout", "layout wird jetzt im nächsten schritt gesetzt");
							setLayout();
							scanProduct();
						}
						else {
							// show alert, that nothing found to this barcode
							AlertDialog.Builder p = new AlertDialog.Builder(context);
							p.setTitle(getResources().getString(R.string.ad_title_no_information));
							p.setMessage(getResources().getString(R.string.ad_content_no_information));
							p.setNeutralButton(getResources().getString(R.string.ad_bt_ok), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,int id) {
									process_pos = 0;
									startProductStore();
								}
							});
							p.create().show();
						}
					}
					catch (JSONException e)
					{
						e.getStackTrace();
					}
						
				}
				else {
					// wrong barcode 
					// show alert, that wrong barcode and return to scanning
					AlertDialog.Builder p = new AlertDialog.Builder(context);
					p.setTitle(getResources().getString(R.string.ad_title_no_information));
					p.setMessage(getResources().getString(R.string.ad_content_no_information));
					p.setNeutralButton(getResources().getString(R.string.ad_bt_ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							process_pos = 0;
							startProductStore();
						}
					});
					p.create().show();
				}
				
			}
		}.execute(hch);
	}
	
	private void setLayout() {
		Log.d("API CALL", "starting setting layout");
//		Log.d("API CALL", String.valueOf(ListOfReceivingUnits.size()));
		
		TableLayout tl = (TableLayout)findViewById(R.id.table_receiving_products);
		tl.removeAllViews();
		
		int flag = 1;
		try {
		Log.d("API CALL", "starting setting layout");
		for(int i = 0; i < ReceivingListHelper.getInstance().get().size(); i++) {
			Log.d("API CALL", "inside loop");

			
			Log.d("API CALL", "before first item");
			// set headings
			if(flag == 1) {
				TableRow tr = new TableRow(this);
				tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT / 5, TableRow.LayoutParams.WRAP_CONTENT));
				
				TextView tv1 = new TextView(this);
				tv1.setText(getResources().getString(R.string.tv_name));
				tv1.setTextSize(15);
				tr.addView(tv1);
				
				TextView tv2 = new TextView(this);
				
				tv2.setText(getResources().getString(R.string.tv_unit));
				tv2.setTextSize(15);
				tr.addView(tv2);
				
				TextView tv3 = new TextView(this);
				tv3.setText(getResources().getString(R.string.tv_amount));
				tv3.setTextSize(15);
				tr.addView(tv3);
				
				/*
				if(process_pos == 2) {
				TextView tv4 = new TextView(this);
					tv4.setText("(" + getResources().getString(R.string.tv_new_unit) + ",  " + getResources().getString(R.string.tv_new_amount) + ")");
					tv4.setTextSize(15);
					tv4.setTextColor(Color.GREEN);
					tr.addView(tv4);
					
					
					TextView tv5 = new TextView(this);
					tv5.setText(getResources().getString(R.string.tv_new_amount));
					tv5.setTextSize(15);
					tv5.setTextColor(Color.GREEN);
					tr.addView(tv5); 
				}*/
				Log.d("API CALL", "after last item in heading");
				
				tl.addView(tr, new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                flag=0;
			}
			
			// set values
			Log.d("API CALL", "outside if statement");
			String current_name = ReceivingListHelper.getInstance().get().get(i).getName();
			Log.d("API CALL", "try get first name " + current_name );
			String current_unit = ReceivingListHelper.getInstance().get().get(i).getUnit();
			String current_amount = ReceivingListHelper.getInstance().get().get(i).getAmount();
			String scanned_unit = "";
			String scanned_amount = "";
			
			
			if(process_pos == 2) {
				scanned_unit = ReceivingListHelper.getInstance().get().get(i).getUnit_to_add();
				scanned_amount= String.valueOf(ReceivingListHelper.getInstance().get().get(i).getAmount_to_add());
			}
			
			TableRow tr = new TableRow(this);
			tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT / 3, TableRow.LayoutParams.WRAP_CONTENT));
			
			Log.d("API CALL", "got all values");
			TextView tv1 = new TextView(this);
			tv1.setText(current_name);
			tv1.setTextColor(Color.WHITE);
			tv1.setTextSize(15);
			tr.addView(tv1);
			
			if(current_unit.equals(null) || current_unit.equals("")) 
				current_unit = "0";
			if(scanned_unit.equals(null) || scanned_unit.equals(""))
				scanned_unit = "0";
				TextView tv2 = new TextView(this);
			tv2.setText("(" + current_unit + ", " +  scanned_unit + ")");
			tv2.setTextColor(Color.WHITE);
			tv2.setTextSize(15);
			tr.addView(tv2);
			
			
			if(current_amount.equals(null) || current_amount.equals("")) 
				current_amount = "0";
			if(scanned_unit.equals(null) || scanned_unit.equals(""))
				scanned_amount = "0";
			TextView tv3 = new TextView(this);
			tv3.setText("(" + current_amount + ",  " + scanned_amount + ")");
			tv3.setTextColor(Color.WHITE);
			tv3.setTextSize(15);
			tr.addView(tv3);
		
			/*
			if(process_pos == 2) {
			TextView tv4 = new TextView(this);
			if(scanned_unit.equals(null))
				scanned_unit = "0";
			tv4.setText("(" + scanned_unit + ",  " + scanned_amount + ")");
			tv4.setTextColor(Color.GREEN);
			tv4.setTextSize(15);
			tr.addView(tv4);
			
			TextView tv5 = new TextView(this);
			tv5.setText(scannd_amount);
			tv5.setTextColor(Color.GREEN);
			tv5.setTextSize(12);
			tr.addView(tv5); 
			}*/
			tl.addView(tr, new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
			
			Log.d("API CALL", "set all rows");

            Log.d("API CALL", "end of first loop");
           
		}
		}
		catch (Exception e) 
		{
			Log.d("API CALL", e.getStackTrace().toString());
			
		}
		Log.d("API CALL", "End of method");
		
		//now scan product to insert it into stock 
		
			
	}

	private void scanProduct() {
		process_pos = 1;
    	Intent startNewActivityOpen = new Intent(getBaseContext(), BarcodeScannerActivity.class);
    	startActivityForResult(startNewActivityOpen, ActivityCodeHelper.ACTIVITY_BARCODE_REQUEST);
	}
	
	private void startGetProductInfos() {
		pDialog = ProgressDialog.show(context, getResources().getString(R.string.pd_title_wait), getResources().getString(R.string.pd_content_receiving_product_infos), true, false);
		String url = "http://" + PreferencesHelper.getInstance().getServer() + "/getProduct/" + current_product_barcode + "/receiving";
		Log.d("Start connectinting to: ", "server url: " + url);
		hch = new HttpConnectionHelper(url);
		new ASyncHttpConnection() {
			@Override
			public void onPostExecute(String result) {
				pDialog.dismiss();
				// Result ist das JSON Objekt
				Log.d("API CALL", result);
				Log.d("start json", result + "teeest");
				if(!hch.getError() && hch.getResponseCode() == 200) { 
				try { 
						JSONArray jArray = new JSONArray(hch.getResponseMessage());
						
						for(int i = 0; i < jArray.length(); i++) {
							JSONObject json = jArray.getJSONObject(i);
							product p = new product();
							p.setId(json.getInt("product_id"));
							current_product_id = json.getString("product_id");
							p.setAmount(json.getString("amount_warehouse"));
							current_product_amount_warehouse = json.getString("amount_warehouse");
							ListOfScannedProducts.add(p);
						}
						
	
	//					open dialog to let the user enter his amounts
						DialogHelper dialog = newInstance("0", available_units);
						dialog.show(getFragmentManager(), "tag");
					}
					catch (Exception e) {
						e.getStackTrace();
					}
				}
				else {
					
				}
			}
		}.execute(hch);
	}
	
	
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
	
	static DialogHelper newInstance(String current_unit, ArrayList<String> all_units) {
		 // need this, to pass some informationn to the dialog 
		 Log.d("DialogHelper", "started");
		 DialogHelper f = new DialogHelper();
		 	
		    Bundle args = new Bundle();
		    args.putStringArrayList("all_units", all_units);
		    f.setArguments(args);
		    
		    return f;
		}
	
	private void updateDatabase() throws JSONException {
		// erhöhe den Amount of Warehouse
		// ToDo: Update Setzliste
		// Question: update Lieferschein?
		pDialog = ProgressDialog.show(context, getResources().getString(R.string.pd_title_wait), getResources().getString(R.string.pd_content_updating), true, false);
		String url = "http://" + PreferencesHelper.getInstance().getServer() + "/delivery/create";
		hch = new HttpConnectionHelper(url,  HttpConnectionHelper.REQUEST_TYPE_POST);
//		data to post to server
		JSONArray post_data = new JSONArray();
		for(int i = 0; i < ListOfReceivingUnits.size(); i++) {
			JSONObject jobj = new JSONObject();
			jobj.put("product_id", ReceivingListHelper.getInstance().get().get(i).getId());
			jobj.put("unit_id", ReceivingListHelper.getInstance().get().get(i).getUnit_to_add());
			jobj.put("amount",ReceivingListHelper.getInstance().get().get(i).getAmount_to_add());
			post_data.put(jobj);
		}
//		Put data into $_POST variable
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("order_id", String.valueOf(current_order_id)));
		Log.d("ReceivingProducts", "post_data: " + post_data.toString());
		nameValuePairs.add(new BasicNameValuePair("array", post_data.toString()));
		hch.setPostPair(nameValuePairs);
//		inform user and start
		
		new ASyncHttpConnection() {
			@Override
			public void onPostExecute(String result) {
				pDialog.dismiss();
				Toast toast = Toast.makeText(context, result, Toast.LENGTH_LONG);
				toast.show();
				
				Log.d("database", result);
				}
		}.execute(hch);
		
	}
	

	@Override
	public void onDialogPositiveClick(DialogFragment dialog, String amount, int index, String valueOfString) {
		// TODO Auto-generated method stub
		Log.d("DialogHelper", "Amount: " + amount + " Index: " + index + " Value: " + valueOfString);
		// use REST API to update amount_warehouse in SMAR_stock
		if(index == 0) {
			valueOfString = "1";
		} else 
			valueOfString = valueOfString.split(":")[0];
		
		Log.d("DialogHelper", "amount: " + amount + " unit: " + valueOfString);
//		updateDatabase(Integer.parseInt(amount), Integer.parseInt(valueOfString));
//		put this value in extra list --> ListOfScannedProducts
		int last_index = ListOfScannedProducts.size() - 1;
		Log.d("DialogHelper", "Der letzte index ist: " + last_index);
		product p = ListOfScannedProducts.get(last_index);
		p.setAmount_to_add(Integer.parseInt(amount));
		p.setUnit_to_add(valueOfString);
		
		Log.d("DialogHelper", "added new product to intern list");
		
		
		//		scan next product
		
		AlertDialog.Builder alert = new AlertDialog.Builder(context);
		alert.setTitle(getResources().getString(R.string.ad_title_next_product));
		alert.setMessage(getResources().getString(R.string.ad_title_next_product));
		alert.setPositiveButton(getResources().getString(R.string.ad_bt_yes), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				process_pos = 1;
				scanProduct();
			}
		})
			.setNegativeButton(getResources().getString(R.string.ad_bt_no), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					Toast t = Toast.makeText(context, getResources().getString(R.string.toast_finished_scanning), Toast.LENGTH_SHORT);
					t.show();
					process_pos = 2;
					createDiffList();
					setLayout();
				}
			});
		alert.create().show();
		
	}

	public void go_on_scanning(View view) {
		process_pos = 1;
		scanProduct();
	}
	
	public void finish_scanning(View view) {
		Toast t = Toast.makeText(context, getResources().getString(R.string.toast_starting_update), Toast.LENGTH_SHORT);
		t.show();
		process_pos = 2; 
		createDiffList();
		setLayout();
		try {
			updateDatabase();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		// TODO Auto-generated method stub
//		createDiffList();
	}
	
	private void createDiffList() {
		for(int i = 0; i < ReceivingListHelper.getInstance().get().size(); i++) {
			for(int j = 0; j < ListOfScannedProducts.size(); j++) {
				product a = ReceivingListHelper.getInstance().get().get(i);
				product b = ListOfScannedProducts.get(j);
				
				//compare them 
				if(a.getId() == b.getId()) {
					// put them in one big list 
					a.setAmount_to_add(b.getAmount_to_add());
					a.setUnit_to_add(b.getUnit_to_add());
				}
			}
		}
		
		// add those product, that are scanned but not expected
		int flag = 0;
		for(int i = 0; i < ListOfScannedProducts.size(); i++) {
			for(int j = 0; j < ReceivingListHelper.getInstance().get().size(); j++) {
				product a = ListOfScannedProducts.get(i);
				product b = ReceivingListHelper.getInstance().get().get(j);
				
				if(a.getId() != b.getId()) {
					flag++;
				}
				// if flag = the size, then there is a product which is not in the list
				if (flag >= ReceivingListHelper.getInstance().get().size()) {
					ReceivingListHelper.getInstance().get().add(ListOfScannedProducts.get(i));
				}
			}
			flag = 0;
		}
		
		
//		display the list of differences
		
	}
}

















