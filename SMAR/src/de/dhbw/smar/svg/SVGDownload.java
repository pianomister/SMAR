package de.dhbw.smar.svg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.util.Log;
import de.dhbw.smar.asynctasks.ASyncHttpConnection;
import de.dhbw.smar.container.SVGObjectContainer;
import de.dhbw.smar.container.SVGObjectContainerElement;
import de.dhbw.smar.helpers.FileHelper;
import de.dhbw.smar.helpers.HttpConnectionHelper;
import de.dhbw.smar.helpers.PreferencesHelper;

public class SVGDownload {
	private String logTag = "SVGDownload";
	private ProgressDialog pDialog;
	private Activity context;
	private HttpConnectionHelper hch;
	long curUpdate;
	SVGObjectContainer svgContainer;
	
	public SVGDownload(Activity context) {
		this.context = context;
	}
	
	public void checkSVGRepository() {
		pDialog = ProgressDialog.show(context, "Please wait", "Downloading shelf graphics...", true, false);
		
		svgContainer = PreferencesHelper.getInstance().getSVGObjectContainer();
		long lastUpdate = svgContainer.getLastDownload();
		curUpdate = System.currentTimeMillis() / 1000;
		
		Log.d(logTag, "Asking for new SVGs");
		String url = "http://" + PreferencesHelper.getInstance().getServer() + "/svg/" +  String.valueOf(lastUpdate);
		Log.d(logTag, "server url: " + url);
		hch = new HttpConnectionHelper(url);
		new ASyncHttpConnection() {
			@Override
			public void onPostExecute(String result) {
				pDialog.dismiss();
				if(!hch.getError() && hch.getResponseCode() == 200) {
					try {
						updateLocalSVGRepository(new JSONArray(hch.getResponseMessage()));
					} catch (JSONException e) {
						Log.e(logTag, e.getMessage());
						createErrorResponse();
					}
				} else {
					createErrorResponse();
				}
			}
			
		}.execute(hch);
	}
	
	public void updateLocalSVGRepository(JSONArray svgArray) {
		SVGObjectContainer svgObjectContainer = new SVGObjectContainer(curUpdate);
		
		try {
			for(int i = 0; i < svgArray.length(); i++) {
				JSONObject json = svgArray.getJSONObject(i);
				FileHelper.writeSerializable(context, 
						FileHelper.SVGOBJECTS_PATH_PREFIX + json.getString("shelf_id"), 
						new SVGObject(json.getString("graphic")));
				svgObjectContainer.addSVGObject(new SVGObjectContainerElement(FileHelper.SVGOBJECTS_PATH_PREFIX + json.getString("shelf_id"),
						Integer.parseInt(json.getString("shelf_id"))));
			}
			
			for(SVGObjectContainerElement svgObjectOld: svgContainer.getSVGObjects()) {
				if(!svgObjectContainer.issetSVGObject(svgObjectOld.getShelfID()))
					svgObjectContainer.addSVGObject(svgObjectOld);
			}
			
			PreferencesHelper.getInstance().setSVGObjectContainer(svgObjectContainer);
			pDialog.dismiss();
		} catch(Exception e) {
			Log.e(logTag, e.getMessage());
			e.printStackTrace();
			createErrorResponse();
		}
	}
	
	public void createErrorResponse() {
		pDialog.dismiss();
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);
 
		// set title
		alertDialogBuilder.setTitle("Could not download shelf graphics...");

		// set dialog message
		alertDialogBuilder
			.setMessage("This could cause SMAR stop working.\n"
					+ "Please restart the app or your device.")
			.setCancelable(false)
			.setNegativeButton("Exit App", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					context.onBackPressed();
				}
			})
			.setPositiveButton("Ignore", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});

			// create alert dialog and show it
			alertDialogBuilder.create().show();
	}
	
}
