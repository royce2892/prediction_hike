package com.royce.database;

/**
 * Created by RRaju on 12/11/2014.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteDataHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "pred.db";
	private static final int DATABASE_VERSION = 1;

	public static final String TABLE_USER = "user";
	
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_AGE = "age";
	public static final String COLUMN_EMAIL = "email";
	public static final String COLUMN_GENDER = "gender";



	// Database creation sql statement
	private static final String USER_CREATE = "create table " +TABLE_USER
			+ "(" + COLUMN_NAME + " text primary key, "
			+ COLUMN_AGE + " text not null,"
			+ COLUMN_EMAIL + " text not null,"
			+ COLUMN_GENDER + " text not null);";

	public MySQLiteDataHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(USER_CREATE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(MySQLiteDataHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
		
		onCreate(db);
	}

}