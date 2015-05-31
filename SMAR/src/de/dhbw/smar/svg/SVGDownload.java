package de.dhbw.smar.svg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.util.Log;
import de.dhbw.smar.R;
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
		Log.d(logTag, "Starting download from SVG graphics");
		Log.d(logTag, "Showing progress dialog");
		pDialog = ProgressDialog.show(context, 
				context.getResources().getString(R.string.pd_title_wait), 
				context.getResources().getString(R.string.pd_content_shelfdownload), true, false);
		
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
				Log.d(logTag, "got Response");
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
		Log.d(logTag, "Updating local SVG Repository");
		SVGObjectContainer svgObjectContainer = new SVGObjectContainer(curUpdate);
		
		try {
			for(int i = 0; i < svgArray.length(); i++) {
				JSONObject json = svgArray.getJSONObject(i);
				FileHelper.writeSerializable(context, 
						FileHelper.SVGOBJECTS_PATH_PREFIX + json.getString("shelf_id"), 
						new SVGObject(json.getString("graphic")));
				svgObjectContainer.addSVGObject(
						new SVGObjectContainerElement(FileHelper.SVGOBJECTS_PATH_PREFIX + json.getString("shelf_id"),
						Integer.parseInt(json.getString("shelf_id"))));
			}
			
			for(SVGObjectContainerElement svgObjectOld: svgContainer.getSVGObjects()) {
				if(!svgObjectContainer.issetSVGObject(svgObjectOld.getShelfID()))
					svgObjectContainer.addSVGObject(svgObjectOld);
			}
			
			PreferencesHelper.getInstance().setSVGObjectContainer(svgObjectContainer);
			Log.d(logTag, "Close progress dialog");
			pDialog.dismiss();
		} catch(Exception e) {
			Log.e(logTag, e.getMessage());
			e.printStackTrace();
			createErrorResponse();
		}
	}
	
	public void createErrorResponse() {
		Log.d(logTag, "ERROR: Close progress dialog, show alert dialog");
		pDialog.dismiss();
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);
 
		// set title
		alertDialogBuilder.setTitle(context.getResources().getString(R.string.ad_title_shelfdownloaderror));

		// set dialog message
		alertDialogBuilder
			.setMessage(context.getResources().getString(R.string.ad_content_shelfdownloaderror))
			.setCancelable(false)
			.setNegativeButton(context.getResources().getString(R.string.ad_bt_exitapp), 
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					context.onBackPressed();
				}
			})
			.setPositiveButton(context.getResources().getString(R.string.ad_bt_ignore), 
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});

			// create alert dialog and show it
			alertDialogBuilder.create().show();
	}
	
}
