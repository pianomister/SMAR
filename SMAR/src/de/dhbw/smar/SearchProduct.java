package de.dhbw.smar;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import de.dhbw.smar.helpers.HttpConnectionHelper;

public class SearchProduct extends Activity {

	final Context context = this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_product);
		
		// Start searching the finding a product workflow
		//startSearchProductWorkflow();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Penis");
		builder.show();
		
		
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
		//Set variables
				String sURL = null;
				String response = null;
				String sProduct = null;
				
				//Starte den Barcode-Scanner 
				
				//Nachdem der Code erkannt wurde, starte REST API aufruf. 

				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(sURL);
				ResponseHandler<String> handler = new BasicResponseHandler();
				try {
					response = client.execute(request, handler);
				}
				catch (ClientProtocolException e) {
					e.printStackTrace();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				client.getConnectionManager().shutdown();
				
				//Aus dem erhaltenem String ein JSON Objekt erstellen
				try { 
					JSONObject json = new JSONObject(response);
					//Parse JSONObject
					sProduct = json.getString("name");
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
				
				//Create Picture
				
				//Anzeige der Informationen
				TextView tvProduct = (TextView) findViewById(R.id.tv_product);
				TextView tvStock = (TextView) findViewById(R.id.tv_stock);
				TextView tvSalesArea = (TextView) findViewById(R.id.tv_sales_area);
				tvProduct.setText(sProduct);
				
				//das Layout setzen
				FrameLayout layout = (FrameLayout) findViewById(R.id.tv_stock);
	}
	
	// ToDo: Implement Warenannahme
    private void showQuestionProductPutIntoShelf() {
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);
 
			// set title
			alertDialogBuilder.setTitle("Product int shelf");
 
			// set dialog message
			alertDialogBuilder
				.setMessage("Do you want to put this article into the shelf?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// if this button is clicked you switch to 
						// the activity to put products into shelf
						Intent intent = new Intent(SearchProduct.this, InsertProduct.class);
						
						Bundle b = new Bundle();
						b.putInt("workflow_position", 1);
						intent.putExtras(b);
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
    
    private String httpConnection(String url, HttpRequest request) {
    	//Handled die HttpConnectiono 
    	//Führt einen seperaten Thread aus
    	//
    	/* ExecutorService executor =  Executors.newSingleThreadExecutor();
    	// HttpConnectionHelper connection = new HttpConnectionHelper(url, request);
    	// Future<String> json = executor.submit(connection);
    	try { 
    		return json.get();
    	}
    	catch (Exception e) {
    		return null;
    	} */
    	return "bla";
    }
    
}
