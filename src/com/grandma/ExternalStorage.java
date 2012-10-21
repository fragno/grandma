//////////////////////////////////////////////////////////////////
// External Storage APIs /////////////////////////////////////////
//////////////////////////////////////////////////////////////////

/**
 * Helper Method to Test if external Storage is Available
 */

package com.grandma;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;
import android.util.Log;


public final class ExternalStorage {

	///////////////////////////////////////////////////////////////////////////
	///Read/Write External Storage/////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	/**
	 * Helper Method to Test if external Storage is Available
	 * 
	 * @return
	 */
	public static boolean isExternalStorageAvailable() {
		boolean state = false;
		String extStorageState = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
			state = true;
		}
		return state;
	}

	/**
	 * Helper Method to Test if external Storage is read only
	 * 
	 * @return
	 */
	public static boolean isExternalStorageReadOnly() {
		boolean state = false;
		String extStorageStateString = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageStateString)) {
			state = true;
		}
		return state;
	}

	// ///////////////////////////////////////////////////////////////////////

	/**
	 * Write to external public directory
	 * 
	 * @param packageName
	 *            - mkdirs parameter
	 * @param filename
	 *            - the filename write to
	 * @param content
	 *            - the content to write
	 */
	public static void writeToExternalStoragePublic(String packageName,
			String filename, byte[] content) {

		// API Level 7 or lower, use getExternalStorageDirectory()
		// to open a File that represents the root of the external storage, but
		// writing to root is not recommended, and instead app should write to
		// app-specific directory, as shown below.

		String pathName = "/Android/data/" + packageName + "/files/";

		if (isExternalStorageAvailable() && !isExternalStorageReadOnly()) {
			try {
				File root = Environment.getExternalStorageDirectory();
				File path = new File(root.getAbsolutePath() + pathName);
				path.mkdirs();
				File file = new File(path + File.separator + filename);
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(content);
				fos.close();
			} catch (FileNotFoundException e) {
				// TODO: handle exception
				e.printStackTrace();
			} catch (IOException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}

	/**
	 * Read a file from external storage
	 * 
	 * @param packageName
	 * @param filename
	 * @return the file content
	 */
	public static byte[] readFromExternalStoragePublic(String packageName,
			String filename) {
		int len = 1024;
		byte[] buffer = new byte[len];
		String pathName = "/Android/data/" + packageName + "/files/";

		if (isExternalStorageAvailable()) {
			try {
				File file = new File(Environment.getExternalStorageDirectory()
						.getAbsolutePath()
						+ pathName
						+ File.separator
						+ filename);

				FileInputStream fis = new FileInputStream(file);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int nrb = fis.read(buffer, 0, len); // Read up to len bytes
				while (nrb != -1) {
					baos.write(buffer, 0, nrb);
					nrb = fis.read(buffer, 0, len);
				}
				buffer = baos.toByteArray();
				fis.close();

			} catch (FileNotFoundException e) {
				// TODO: handle exception
				Log.d(packageName + ".readFromExternalStoragePublic()",
						"FileNotFoundException: " + e);
				e.printStackTrace();
			} catch (IOException e) {
				Log.d(packageName + ".readFromExternalStoragePublic()",
						"IOException: " + e);
				e.printStackTrace();
			}
		}

		return buffer;
	}

	/**
	 * Delete external public file
	 * 
	 * @param packageName
	 * @param filename
	 */
	public static void deleteExternalStoragePublicFile(String packageName,
			String filename) {
		String pathName = "/Android/data/" + packageName + "/files/";
		File file = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + pathName + File.separator + filename);
		if (file != null) {
			file.delete();
		}
	}


	///////////////////////////////////////////////////////////////////////////
	///Read/write Internal Storage/////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * Write content to internal storage making the content private to the 
	 * application. The method can be easily changed to take the MODE as
	 * argument and let the caller dictate the visibility: MODE_PRIVATE,
	 * MODE_WORLD_WRITEABLE, MODE_WORLD_READABLE, etc.
	 * 
	 * @param filename
	 * @param content
	 */
	public static void writeInternalStoragePrivate(Context appContext, String filename, byte[] content) {
		try {
			// 	MODE_PRIVATE 
			//			creates the file (or replaces a file of same name) and makes
			// 			it private to your application.
			//	MODE_WORLD_WRITEABLE
			//	MODE_WORLD_READABLE
			//	MODE_APPEND
			FileOutputStream fos = appContext.openFileOutput(filename, Context.MODE_PRIVATE);
			fos.write(content);
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();	
		}
	}
	
	/**
	 * Read a file from internal storage
	 * @param appContext
	 * @param filename
	 * @return the file content
	 */
	public static byte[] readInternalStoragePrivate(Context appContext, String filename) {
		int len = 1024;
		byte[] buffer = new byte[len];
		try {
			FileInputStream fis = appContext.openFileInput(filename);		
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int nrb = fis.read(buffer, 0, len);
			while(nrb != -1) {
				baos.write(buffer, 0, nrb);
				nrb = fis.read(buffer, 0, len);
			}
			buffer = baos.toByteArray();
			fis.close();
		} catch (FileNotFoundException e) {			
			// TODO: handle exception
			Log.d("readInternalStoragePrivate",
					"FileNotFoundException: " + e);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO: handle exception
			Log.d("readInternalStoragePrivate",
					"IOException: " + e);
			e.printStackTrace();
		}
		return buffer;
	}
	
	/**
	 * Delete internal private file
	 * @param appContext
	 * @param filename
	 */
	public static void deleteInternalStoragePrivate(Context appContext, String filename) {
		File file = appContext.getFileStreamPath(filename);
		if(file != null) {
			file.delete();
		}
	}	
	//////////////////////////////////////////////////////////////////////////
	///Cache Storage//////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////
	
	/**
	 * Get internal cache directory
	 * @param appContext
	 * @return
	 */
	public static String getInternalCacheDirectory(Context appContext) {
		String cacheDirPath = null;
		File cacheDir = appContext.getCacheDir();
		if(cacheDir != null) {
			cacheDirPath = cacheDir.getParent();
		}
		return cacheDirPath;
	}
	
	/**
	 * Helper method to retrieve the absolute path to the application specific
     * external cache directory on the filesystem. These files will be ones that
     * get deleted when the app is uninstalled or when the device runs low on
     * storage. There is no guarantee when these files will be deleted.
	 * 
	 * @param packageName
	 * @return the the absolute path to the application specific cache directory
	 */
	public static String getExternalCacheDirectory(String packageName) {
		String extCacheDirPath = null;
		String pathName = "/Android/data/" + packageName + "/cache/"; 
		File cacheDir = new File(pathName);
		if(cacheDir != null) {
			extCacheDirPath = cacheDir.getPath();
		}
		return extCacheDirPath;
	}
	
	/////////////////////////////////////////////////////////////////////////
	
}
