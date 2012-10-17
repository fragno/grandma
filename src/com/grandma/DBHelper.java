package com.grandma;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	private SQLiteDatabase db;
	private static final int DATABASE_VERSION = 1;
	private static final String DB_NAME = "sample.db";
	private static final String TABLE_NAME = "friends";

	/**
	 * Constructor
	 * @param context the application context
	 */
	public DBHelper(Context context) {
	    super(context, DB_NAME, null, DATABASE_VERSION);
	    db = getWritableDatabase();
	}

	/**
	 * Called at the time to create the DB.
	 * The create DB statement
	 * @param the SQLite DB
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
	    db.execSQL(
	            "create table " + TABLE_NAME + " (_id integer primary key autoincrement, " +
	            " fid text not null, name text not null) ");
	}

	/**
	 * The Insert DB statement
	 * @param id the friends id to insert
	 * @param name the friend's name to insert
	 */
	public void insert(String id, String name) {
	    db.execSQL("INSERT INTO friends('fid', 'name') values ('"
	            + id + "', '"
	            + name + "')");
	}

	/**
	 * Wipe out the DB
	 */
	public void clearAll() {
	    db.delete(TABLE_NAME, null, null);
	}

	/**
	 * Select All the returns a Cursor
	 * @return the cursor for the DB selection
	 */
	public Cursor cursorSelectAll() {
	    Cursor cursor = this.db.query(
	            TABLE_NAME, // Table Name
	            new String[] { "fid", "name" }, // Columns to return
	            null,       // SQL WHERE
	            null,       // Selection Args
	            null,       // SQL GROUP BY
	            null,       // SQL HAVING
	            "name");    // SQL ORDER BY
	    return cursor;
	 }

	/**
	 * Select All that returns an ArrayList
	 * @return the ArrayList for the DB selection
	 */
	public ArrayList<Friend> listSelectAll() {
	    ArrayList<Friend> list = new ArrayList<Friend>();
	    Cursor cursor = this.db.query(TABLE_NAME, new String[] { "fid", "name" }, null, null, null, null, "name");
	    if (cursor.moveToFirst()) {
	        do {
	            Friend f = new Friend();
	            f.id = cursor.getString(0);
	            f.name = cursor.getString(1);
	            list.add(f);
	        } while (cursor.moveToNext());
	    }
	    if (cursor != null && !cursor.isClosed()) {
	        cursor.close();
	    }
	    return list;
	}

	/**
	 * Invoked if a DB upgrade (version change) has been detected
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    // Here add any steps needed due to version upgrade
	    // for example, data format conversions, old tables no longer needed, etc
	 }

}
