package com.royce.dbhelper;

/**
 * Created by RRaju on 12/12/2014.
 */



import com.royce.database.MySQLiteDataHelper;
import com.royce.model.User;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DataSource {

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteDataHelper dbHelper;
	private String[] userAllColumns = { MySQLiteDataHelper.COLUMN_NAME,
			MySQLiteDataHelper.COLUMN_AGE,
			MySQLiteDataHelper.COLUMN_EMAIL,
			MySQLiteDataHelper.COLUMN_GENDER};

	public DataSource(Context context) {
		dbHelper = new MySQLiteDataHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public User createUser(String name, String age, String email, String gender) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteDataHelper.COLUMN_NAME, name);
		values.put(MySQLiteDataHelper.COLUMN_AGE, age);
		values.put(MySQLiteDataHelper.COLUMN_EMAIL, email);
		values.put(MySQLiteDataHelper.COLUMN_GENDER, gender);

		database.insert(MySQLiteDataHelper.TABLE_USER, null, values);
		Cursor cursor = database.query(MySQLiteDataHelper.TABLE_USER,
				userAllColumns, null, null, null, null, null);
		cursor.moveToFirst();
		User newUser = cursorToUser(cursor);
		cursor.close();
		return newUser;
	}

	public void deleteUser() {
		database.delete(MySQLiteDataHelper.TABLE_USER, null, null);
	}

	
	public User getUser() {
		User user = new User();

		Cursor cursor = database.query(MySQLiteDataHelper.TABLE_USER,
				userAllColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			User _user = cursorToUser(cursor);
			user = _user;
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return user;
	}

	/*
	 * public List<Story> getAllStories() { List<Story> stories = new
	 * ArrayList<Story>();
	 * 
	 * Cursor cursor = database.query(MySQLiteDataHelper.TABLE_STORY,
	 * storyAllColumns, null, null, null, null, null);
	 * 
	 * cursor.moveToFirst(); while (!cursor.isAfterLast()) { Story _story =
	 * cursorToStory(cursor); stories.add(_story); cursor.moveToNext(); } //
	 * make sure to close the cursor cursor.close(); return stories; }
	 */

	private User cursorToUser(Cursor cursor) {
		User user = new User();
		user.setName(cursor.getString(0));
		user.setAge(cursor.getString(1));
		user.setEmail(cursor.getString(2));
		user.setGender(cursor.getString(3));

		return user;
	}

}