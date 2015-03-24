package de.dhbw.smar.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesHelper {

	public static void setPreference(Activity activity, String sKey, String sValue) {
		SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(sKey, sValue);
		editor.commit();
	}
	
	public static void setPreferenceInt(Activity activity, String sKey, int iValue) {
		SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt(sKey, iValue);
		editor.commit();
	}
	
	public static String getPreference(Activity activity, String sKey) {
		SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
		return sharedPref.getString(sKey, null);
	}
	
	public static int getPreferenceInt(Activity activity, String sKey) {
		SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
		return sharedPref.getInt(sKey, -1);
	}
}
