package de.dhbw.smar.helpers;

import android.content.Context;
import android.content.SharedPreferences;

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
	
	// Some variables
	private String pref_server_ip = null;
	private int pref_storage = 0; // Default: Use internal storage
	private int pref_barcode_scanner = 0; // Default: Use camera

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
	}
	
	// Saving preferences to file
	public void savePreferences(Context context) {
		setPreference(context, PREFKEY_SERVER_IP, pref_server_ip);
		setPreferenceInt(context, PREFKEY_BARCODE_SCANNER, pref_barcode_scanner);
		setPreferenceInt(context, PREFKEY_USE_INTERNAL_STORAGE, pref_storage);
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
}
