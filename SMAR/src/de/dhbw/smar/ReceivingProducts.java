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
import de.dhbw.smar.helpers.product;

public class ReceivingProducts extends Activity implements DialogHelper.ShareDialogListener{

	private int process_pos = 0;
	private String current_barcode_receiving_note;
	private HttpConnectionHelper hch;
	final Context context = this;
	private ProgressDialog pDialog;
	private String current_product_barcode;
	private String current_product_id;
	private ArrayList<product> ListOfReceivingUnits = new ArrayList<product>();
	private ArrayList<String> available_units = new ArrayList<String>();
	private String current_product_amount_warehouse;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_receiving_products);
		startProductStore();
		
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
		// starte den barcode scanner
		// setze flag, damit nicht immer wieder Lieferschein gescanned werden
		
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
	    		if(this.process_pos == 0) {
    			this.process_pos = 1;
	    		this.current_barcode_receiving_note = resultBarcode;
	    		//start rest api to get the infos about receiving note 
	    		Log.d("API CALL", "Start getting infos to Lieferschein");
	    		startGetReceivingInfos();
	    		}
	    		else if(this.process_pos == 1) {
	    			// scan here products
	    			this.current_product_barcode = resultBarcode;
	    			Log.d("API CALL", "Start getting product infos after setlayout");
	    			startGetProductInfos();
	    		}
	    	}
	    }
    	else {
    		AlertDialog.Builder alert = new AlertDialog.Builder(context);
    		alert.setTitle("Failure");
    		alert.setMessage("Couldn't read this code. Check code and try again")
    			 .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int id) {
    				dialog.dismiss();
    				
    			}
    		});
    		alert.create().show();
    	}
		
	}

	
	protected void startGetReceivingInfos() {
		pDialog = ProgressDialog.show(context, "Please wait", "Receiving product information...", true, false);
		String url = "http://" + PreferencesHelper.getInstance().getServer() + "/Receiving/" + current_barcode_receiving_note;
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
						Log.d("start json", result + "teeest");
						JSONArray jArray = new JSONArray(hch.getResponseMessage());
						
						for(int i = 0; i < jArray.length(); i++) {
							JSONObject json = jArray.getJSONObject(i);
							product p = new product();
							p.setName(json.getString("product_name"));
							p.setReceiving_name(json.getString("receiving_name"));
							p.setAmount(json.getString("amount"));
							p.setUnit(json.getString("unit"));
							p.setReceiving_date(json.getString("receiving_date"));
							
							ListOfReceivingUnits.add(p);
						}
						
						
						CharSequence text = "Got Receiving Note";
						int duration = Toast.LENGTH_LONG;

						Toast toast = Toast.makeText(context, text, duration);
						toast.show();
						Log.d("read json", "reading json...");
						

						//layout names setzen
						Log.d("setLayout", "layout wird jetzt im nächsten schritt gesetzt");
						setLayout();
						
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
					p.setTitle("Failure. Try again with another barcode.");
					p.setNeutralButton("ok", new DialogInterface.OnClickListener() {
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
		Log.d("API CALL", String.valueOf(ListOfReceivingUnits.size()));
		
		TableLayout tl = (TableLayout)findViewById(R.id.table_receiving_products);
		//tl.removeAllViews();
		
		int flag = 1;
		try {
		Log.d("API CALL", "starting setting layout");
		for(int i = 0; i < ListOfReceivingUnits.size(); i++) {
			Log.d("API CALL", "inside loop");

			
			Log.d("API CALL", "before first item");
			// set headings
			if(flag == 1) {
				TableRow tr = new TableRow(this);
				tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
				
				TextView tv1 = new TextView(this);
				tv1.setText("Name");
				tv1.setTextSize(15);
				tr.addView(tv1);
				
				TextView tv2 = new TextView(this);
				tv2.setText("Unit");
				tv2.setTextSize(15);
				tr.addView(tv2);
				
				TextView tv3 = new TextView(this);
				tv3.setText("Amount");
				tv3.setTextSize(15);
				tr.addView(tv3);
				
				TextView tv4 = new TextView(this);
				tv4.setText("R. name");
				tv4.setTextSize(15);
				tr.addView(tv4);
				
				TextView tv5 = new TextView(this);
				tv5.setText("Date");
				tv5.setTextSize(15);
				tr.addView(tv5);
				
				Log.d("API CALL", "after last item");
				
				tl.addView(tr);
                flag=0;
			}
			
			// set values
			Log.d("API CALL", "outside if statement");
			String current_name = ListOfReceivingUnits.get(i).getName();
			Log.d("API CALL", "try get first name " + current_name );
			String current_unit = ListOfReceivingUnits.get(i).getUnit();
			String current_amount = ListOfReceivingUnits.get(i).getAmount();
			String r_name = ListOfReceivingUnits.get(i).getReceiving_name();
			String date = ListOfReceivingUnits.get(i).getReceiving_date();
			
			TableRow tr = new TableRow(this);
			tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
			
			Log.d("API CALL", "got all values");
			TextView tv1 = new TextView(this);
			tv1.setText(current_name);
			tv1.setTextColor(Color.BLACK);
			tv1.setTextSize(15);
			tr.addView(tv1);
			
			TextView tv2 = new TextView(this);
			tv2.setText(current_unit);
			tv2.setTextColor(Color.BLACK);
			tv2.setTextSize(15);
			tr.addView(tv2);
			
			TextView tv3 = new TextView(this);
			tv3.setText(current_amount);
			tv3.setTextColor(Color.BLACK);
			tv3.setTextSize(15);
			tr.addView(tv3);
			
			TextView tv4 = new TextView(this);
			tv4.setText(r_name);
			tv4.setTextColor(Color.BLACK);
			tv4.setTextSize(15);
			tr.addView(tv4);
			
			TextView tv5 = new TextView(this);
			tv5.setText(date);
			tv5.setTextColor(Color.BLACK);
			tv5.setTextSize(12);
			tr.addView(tv5);
			
			tl.addView(tr);
			Log.d("API CALL", "set all rows");
			
			/*
			final View vline = new View(context);
			Log.d("API CALL", "new line");
			vline.setLayoutParams(new TableRow.LayoutParams(10,5));
            Log.d("API CALL", "new line params set");
            vline.setBackgroundColor(Color.BLUE);
            tl.addView(vline); // add line below heading	
            */
            Log.d("API CALL", "end of first loop");
           
		}
		}
		catch (Exception e) 
		{
			Log.d("API CALL", e.getStackTrace().toString());
			
		}
		Log.d("API CALL", "End of method");
		
		//now scan product to insert it into stock 
		scanProduct();
	}

	private void scanProduct() {
    	Intent startNewActivityOpen = new Intent(getBaseContext(), BarcodeScannerActivity.class);
    	startActivityForResult(startNewActivityOpen, ActivityCodeHelper.ACTIVITY_BARCODE_REQUEST);
	}
	
	private void startGetProductInfos() {
		pDialog = ProgressDialog.show(context, "Please wait", "Receiving product information...", true, false);
		String url = "http://" + PreferencesHelper.getInstance().getServer() + "/getProduct/" + current_product_barcode;
		Log.d("Start connectinting to: ", "server url: " + url);
		hch = new HttpConnectionHelper(url);
		new ASyncHttpConnection() {
			@Override
			public void onPostExecute(String result) {
				pDialog.dismiss();
				// Result ist das JSON Objekt
				Log.d("API CALL", result);
				Log.d("start json", result + "teeest");
				try { 
					JSONArray jArray = new JSONArray(hch.getResponseMessage());
					
					for(int i = 0; i < jArray.length(); i++) {
						JSONObject json = jArray.getJSONObject(i);
						current_product_id = json.getString("product_id");
						current_product_amount_warehouse = json.getString("amount_warehouse");
					}
					Log.d("API CALL", "Got Product_id: " + current_product_id + " and the amount, which is currently in the warehose: " + current_product_amount_warehouse );
					//get all available units
					searchAvailableUnits();
				}
				catch (Exception e) {
					e.getStackTrace();
				}
			}
		}.execute(hch);
	}
	
	public void onDialogPositiveClick(DialogFragment dialog,String selected_unit, String selected_amount, int index) {
		//After ok, clicked
		//we got selected_unit and the amount
		//call REST API to update database
		if(index == 1) {
			selected_unit = "1";
		} else {
			selected_unit = selected_unit.split(" ")[1];
		}
		
		int amount = Integer.parseInt(selected_amount);
		int unit_size = Integer.parseInt(selected_unit);
		
		updateDatabase(amount, unit_size);
		
	}
	
	private void searchAvailableUnits() {
		 //get all available Units to this product
		 pDialog = ProgressDialog.show(context, "Please wait", "Receiving product information...", true, false);
			String url = "http://" + PreferencesHelper.getInstance().getServer() + "/getUnits";
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
									available_units.add(json.getString("name")+";"+json.getString("capacity"));
									Log.d("API CALLL","getting units: " +  json.getString("name")+";"+json.getString("capacity"));
								}
							}
						
							
							Log.d("API CALL", "I got the unit, fuck yeah");
							// open dialog and ask user to enter data
							DialogHelper dialog = newInstance("0", available_units);
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
		    args.putStringArrayList("all_units", all_units);
		    
		    Log.d("DialogHelper", "parameters set");
		    Log.d("DialogHelper", "param1: " + all_units.get(0));
		    f.setArguments(args);
		    
		    Log.d("DialogHelper", "param1: " + all_units.get(1));
		    
		    return f;
		}
	
	private void updateDatabase(int amount, int unit_size) {
		// erhöhe den Amount of Warehouse
		// ToDo: Update Setzliste
		// Question: update Lieferschein?
		int howMuchToTransfer;
		howMuchToTransfer = Integer.parseInt(current_product_amount_warehouse) + ( amount * unit_size);
		
		Log.d("API CALL", "Starting Updating. Transfer: " + howMuchToTransfer);
		pDialog = ProgressDialog.show(context, "Please wait", "Receiving product information...", true, false);
		String url = "http://" + PreferencesHelper.getInstance().getServer() + "/Product";
		Log.d("API CALL: ", "server url: " + url);
		hch = new HttpConnectionHelper(url,  HttpConnectionHelper.REQUEST_TYPE_POST);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("product_id", this.current_product_id));
		nameValuePairs.add(new BasicNameValuePair("amount", String.valueOf(howMuchToTransfer)));
		hch.setPostPair(nameValuePairs);
		new ASyncHttpConnection() {
			@Override
			public void onPostExecute(String result) {
				pDialog.dismiss();
				// Result ist das JSON Objekt
				Log.d("API CALL", result);
				Log.d("start json", result + "teeest");
				try { 
					JSONArray jArray = new JSONArray(hch.getResponseMessage());
					String result_message = "";
					JSONObject json = new JSONObject(hch.getResponseMessage());
					result_message = json.getString("result");
					/*for(int i = 0; i < jArray.length(); i++) {
						JSONObject json = jArray.getJSONObject(i);
						result_message = json.getString("result");
						Log.d("API CALL", " : result_message:"+ result_message);
						Log.d("API CALL", ": resultss: " + result);
					}*/
					if(result_message.equals("success")) {
						AlertDialog.Builder p = new AlertDialog.Builder(context);
						p.setTitle("Successfully inserted");
						p.setNeutralButton("ok", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								scanProduct();
							}
						});
					} 
					else {
						AlertDialog.Builder p = new AlertDialog.Builder(context);
						p.setTitle("Failure. Try again.");
						p.setNeutralButton("ok", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								scanProduct();
							}
						});
					}
				}
				catch (Exception e) {
					e.getStackTrace();
				}
			}
		}.execute(hch);
				
	}
	
	private void setLayout1() {
		/* Find Tablelayout defined in main.xml */
		TableLayout tl = (TableLayout) findViewById(R.id.table_receiving_products);
		/* Create a new row to be added. */
		TableRow tr = new TableRow(this);
		tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
		/* Create a Button to be the row-content. */
		Button b = new Button(this);
		b.setText("Dynamic Button");
		b.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
		/* Add Button to row. */
		
		TextView tv = new TextView(this);
		tv.setText("hallo");
		tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
		
		tr.addView(tv);
		/* Add row to TableLayout. */
		TextView tv2 = new TextView(this);
		tv2.setText("welt");
		tv2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
		
		tr.addView(tv2);
		//tr.setBackgroundResource(R.drawable.sf_gradient_03);
		tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
		
		tr = new TableRow(this);
		tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
		
		TextView tv3 = new TextView(this);
		tv3.setText("hallo");
		tv3.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
		
		tr.addView(tv3);
		/* Add row to TableLayout. */
		TextView tv4 = new TextView(this);
		tv4.setText("welt");
		tv4.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
		tr.addView(tv4);
		
		tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog, String amount, int index, String valueOfString) {
		// TODO Auto-generated method stub
		//
		Log.d("DialogHelper", "Amount: " + amount + " Index: " + index + " Value: " + valueOfString);
		// use REST API to update amount_warehouse in SMAR_stock
		if(index == 0) {
			valueOfString = "1";
		} else 
			valueOfString = valueOfString.split(";")[1];
		
		Log.d("DialogHelper", "amount: " + amount + " unit: " + valueOfString);
		updateDatabase(Integer.parseInt(amount), Integer.parseInt(valueOfString));
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		// TODO Auto-generated method stub
		
	}
}

















