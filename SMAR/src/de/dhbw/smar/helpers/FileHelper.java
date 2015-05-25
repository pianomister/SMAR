package de.dhbw.smar.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Scanner;

import de.dhbw.smar.R;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;

/**
 * Helper functions to get files from proper directory
 * (considers internal/external file saving preference)
 * 
 * @author Stephan
 *
 */
public class FileHelper {
	
	private static String sLogTag = "FileHelper";	
	
	/**
	 * Read a file from configured disk location
	 * 
	 * @param sFilename name of file
	 * @author Stephan
	 */
	public static File readFile(Activity activity, String sFilename) {

		File targetDir = getStorageDir(activity, false);
		
		if(targetDir != null) {
			return new File(targetDir, sFilename);
		} else {
			Log.e(sLogTag, "Could not read file from " + sFilename);
		}
		
		return null;
	}
	
	
	/**
	 * Write a file into configured disk location
	 * 
	 * @param sFilename name of file
	 * @param sInput content of file
	 * @author Stephan
	 */
	public static void writeFile(Activity activity, String sFilename, String sInput) {
		
		File targetDir = getStorageDir(activity, true);
		
		if(targetDir != null) {
			FileWriter fw;
			try {
				fw = new FileWriter( new File(targetDir, sFilename) );
				fw.write(sInput);
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Log.e(sLogTag, "Could not write file to " + sFilename);
		}
	}
	
	
	/**
	 * Read a serialized object from storage
	 * http://www.javacoffeebreak.com/articles/serialization/ 
	 * 
	 * @param activity Activity for context
	 * @param sFilename Name of file
	 * @author Stephan
	 */
	public static Object readSerializable(Activity activity, String sFilename) {
		
		File targetDir = getStorageDir(activity, false);
		
		if(targetDir != null) {
			try {
				// Read from disk using FileInputStream
				FileInputStream f_in = new FileInputStream( new File(targetDir, sFilename) );
	
				// Read object using ObjectInputStream
				ObjectInputStream obj_in = new ObjectInputStream(f_in);
	
				// Read an object
				Object obj = obj_in.readObject();
				obj_in.close();
				return obj;
				
				/* TODO leave comment for reference
				if (obj instanceof Vector)
				{
					Vector vec = (Vector) obj;
				}*/
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Log.e(sLogTag, "Could not read Serializable from " + sFilename);
		}
		
		return null;
	}
	
	
	/**
	 * Write a serialized object to storage
	 * http://www.javacoffeebreak.com/articles/serialization/
	 * 
	 * @param activity Activity for context
	 * @param sFilename Name of file
	 * @param object Object to be serialized into file
	 * @author Stephan
	 */
	public static void writeSerializable(Activity activity, String sFilename, Serializable object) {
		
		File targetDir = getStorageDir(activity, false);
			
		if(targetDir != null) {
			try {
				// Write to disk with FileOutputStream
				FileOutputStream f_out = new FileOutputStream( new File(targetDir, sFilename) );
				
				// Write object with ObjectOutputStream
				ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
				
				// Write object out to disk
				obj_out.writeObject(object);
				obj_out.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Log.e(sLogTag, "Could not write Serializable to " + sFilename);
		}
	}
	
	
	/**
	 * Checks preferences for internal/external storage and returns proper directory
	 * 
	 * @return internal or external storage directory, according preferences
	 * @author Stephan
	 */
	public static File getStorageDir(Activity activity, boolean bCheckWritable) {
		
		// check for read resp. read+write capability of external device
		bCheckWritable = bCheckWritable ? isExternalStorageWritable() : isExternalStorageReadable();
		
		// check preference for storage usage
		boolean bUseInternalStorage = (PreferencesHelper.getPreferenceInt(activity, activity.getString(R.string.prefname_use_internal_storage)) == 1) ? true : false; 
		
		// internal storage
		if(bUseInternalStorage) {
			return activity.getFilesDir();
		// external storage
		} else {
			if(bCheckWritable) {
				File storageDir = activity.getExternalFilesDir(null);
				if (!storageDir.exists() && !storageDir.mkdirs()) {
					Log.e(sLogTag, "Directory not created");
			    }
				return storageDir;
			} else {
				Log.e(sLogTag, "External storage not available for this action");
			}
		}
		
		return null;
	}
	
	
	/**
	 * Reads a file and returns content
	 * http://stackoverflow.com/questions/326390/how-to-create-a-java-string-from-the-contents-of-a-file
	 * 
	 * @param file File to read
	 * @return content of file as String
	 * @throws FileNotFoundException
	 */
	public static String getFileContents(File file) {
		String text = null;
		
		try {
			text = new Scanner(file).useDelimiter("\\A").next();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return text;
	}
	
	
	/**
	 * Checks if external storage is available for read and write
	 */
	public static boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}

	
	/**
	 * Checks if external storage is available to at least read
	 */
	public static boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}
}
