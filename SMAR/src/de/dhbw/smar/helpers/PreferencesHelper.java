package de.dhbw.smar.helpers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import de.dhbw.smar.container.SVGObjectContainer;
import de.dhbw.smar.container.SVGObjectContainerElement;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Class for loading, saving and holding preferences
 * 
 * @author Sebastian Kowalski
 * @author Stephan Giesau
 * 
 */
public class PreferencesHelper {
	// Make this class a singleton
	private static final PreferencesHelper ph = new PreferencesHelper();
	
	public static PreferencesHelper getInstance() {
		return ph;
	}
	
	private PreferencesHelper() {
		// No constructor because of singleton
	}
	
	// Constant for filepath
	public static String PREFFILE = "de.dhbw.smar.PREFERENCES_FILE";
	
	// Constant for preferences
	public static String PREFKEY_INIT_CONFIG = "pref_initial_configuration";
	public static String PREFKEY_SERVER_IP = "pref_server_ip";
	public static String PREFKEY_USE_INTERNAL_STORAGE = "pref_storage";
	public static String PREFKEY_BARCODE_SCANNER = "pref_storage";
	public static String PREFKEY_SVG_CONTAINER = "svg_container";
	
	// Some variables
	private String pref_server_ip = null;
	private int pref_storage = 0; // Default: Use internal storage
	private int pref_barcode_scanner = 0; // Default: Use camera
	private SVGObjectContainer pref_svg_container = null;
	
	// logTag
	private String logTag = "PreferencesHelper";

	// Methods for Reading & Writing single preferences from/to file (string and int)
	public static void setPreference(Context context, String sKey, String sValue) {
		SharedPreferences sharedPref = context.getSharedPreferences(PREFFILE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(sKey, sValue);
		editor.commit();
	}
	
	public static void setPreferenceInt(Context context, String sKey, int iValue) {
		SharedPreferences sharedPref = context.getSharedPreferences(PREFFILE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt(sKey, iValue);
		editor.commit();
	}
	
	public static String getPreference(Context context, String sKey) {
		SharedPreferences sharedPref = context.getSharedPreferences(PREFFILE, Context.MODE_PRIVATE);
		return sharedPref.getString(sKey, null);
	}
	
	public static int getPreferenceInt(Context context, String sKey) {
		SharedPreferences sharedPref = context.getSharedPreferences(PREFFILE, Context.MODE_PRIVATE);
		return sharedPref.getInt(sKey, -1);
	}
	
	// Loading preferences from file
	public void loadPreferences(Context context) {
		this.pref_server_ip = getPreference(context, PREFKEY_SERVER_IP);
		this.pref_barcode_scanner = getPreferenceInt(context, PREFKEY_BARCODE_SCANNER);
		this.pref_storage = getPreferenceInt(context, PREFKEY_USE_INTERNAL_STORAGE);
		
		if(getPreference(context, PREFKEY_SVG_CONTAINER) == null) {
			Log.d(logTag, "no svg container to load");
			this.pref_svg_container = new SVGObjectContainer();
		} else {
			Log.d(logTag, "loading svg container");
			String svgContainerString = getPreference(context, PREFKEY_SVG_CONTAINER);
			Log.d(logTag, "Show serialized SVGObjectContainer loaded from settings: " + svgContainerString);
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(svgContainerString.getBytes("ISO-8859-1"));
				ObjectInputStream o = new ObjectInputStream(bais);
				this.pref_svg_container = (SVGObjectContainer) o.readObject(); 
			} catch (Exception e) {
				this.pref_svg_container = null;
				Log.e(logTag, "Error while loading occured:" + e.getMessage());
				e.printStackTrace();
			}
			if(this.pref_svg_container == null) {
				Log.e(logTag, "Could not load svg container... Create a new one to pretend SMAR crashing...");
				this.pref_svg_container = new SVGObjectContainer();
			} else {
				Log.d(logTag, "svg container with timestamp " + String.valueOf(this.pref_svg_container.getLastDownload()) + " loaded.");
			}
		}
	}
	
	// Saving preferences to file
	public void savePreferences(Context context) {
		setPreference(context, PREFKEY_SERVER_IP, pref_server_ip);
		setPreferenceInt(context, PREFKEY_BARCODE_SCANNER, pref_barcode_scanner);
		setPreferenceInt(context, PREFKEY_USE_INTERNAL_STORAGE, pref_storage);
		
		Log.d(logTag, "attempt to save svg container with timestamp: " + String.valueOf(this.pref_svg_container.getLastDownload()));
		if(this.pref_svg_container != null) {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream o = new ObjectOutputStream(baos);
				o.writeObject(this.pref_svg_container);
				setPreference(context, PREFKEY_SVG_CONTAINER, baos.toString("ISO-8859-1"));
			} catch (Exception e) {
				Log.e(logTag, "Error while saving occured:" + e.getMessage());
				e.printStackTrace();
			}
		}
		Log.d(logTag, "save complete");
	}
	
	// Resetting Preferences and deleting them
	public static void resetPreferences(Context context) {
		SharedPreferences sharedPref = context.getSharedPreferences(PREFFILE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.clear();
		editor.commit();
	}
	
	// Methods for preferences in memory
	public String getServer() {
		return pref_server_ip;
	}

	public void setServer(String pref_server_ip) {
		this.pref_server_ip = pref_server_ip;
	}

	public int getStorage() {
		return pref_storage;
	}

	public void setStorage(int pref_storage) {
		this.pref_storage = pref_storage;
	}

	public int getBarcodeScanner() {
		return pref_barcode_scanner;
	}

	public void setBarcodeScanner(int pref_barcode_scanner) {
		this.pref_barcode_scanner = pref_barcode_scanner;
	}
	
	public SVGObjectContainer getSVGObjectContainer() {
		return pref_svg_container;
	}
	
	public void setSVGObjectContainer(SVGObjectContainer pref_svg_container) {
		this.pref_svg_container = pref_svg_container;
	}
}
