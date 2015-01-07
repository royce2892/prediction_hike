package com.royce.prediction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CallLog;
import android.provider.ContactsContract.PhoneLookup;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.Permission.Type;
import com.sromku.simple.fb.entities.Page;
import com.sromku.simple.fb.entities.Page.Properties;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnPagesListener;
import com.sromku.simple.fb.listeners.OnProfileListener;
import com.royce.prediction.R;
import com.royce.service.AppTrackerService;

public class MainActivity extends Activity {

	private SimpleFacebook mSimpleFacebook;
	private Intent appintent;
	String name[] = new String[100];
	int time[] = new int[100];
	int pos = 0;
	StringBuffer bookList = new StringBuffer();
	StringBuffer movieList = new StringBuffer();
	StringBuffer musicList = new StringBuffer();
	String top3Contacts[] = new String[3];
	String top3Apps[] = new String[3];
	TextView tv;
	SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prefs = getSharedPreferences("pred_prefs", Context.MODE_PRIVATE);
		mSimpleFacebook = SimpleFacebook.getInstance(MainActivity.this);
		setContentView(R.layout.activity_main);
		startAppTrackerService();
		analyzeCallLog();
		if (prefs.contains("service_started"))
			getTop3Apps();
		initUi();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	// SET UI for Facebook Login or display user details
	private void initUi() {
		// TODO Auto-generated method stub
		tv = (TextView) findViewById(R.id.tv);
		tv.setMovementMethod(new ScrollingMovementMethod());
		if (!prefs.contains("fb_logged_in")) {
			tv.setText("Login");
			tv.setClickable(true);
			tv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					loginAndGetData();
				}
			});
		} else {
			tv.setClickable(false);
			displayAllDetails();
		}
	}

	private void displayAllDetails() {
		// TODO Auto-generated method stub
		StringBuffer pers = new StringBuffer();
		String name = prefs.getString("name", "none");
		String birthday = prefs.getString("age", "none");
		String email = prefs.getString("mail", "none");
		String gender = prefs.getString("gender", "none");
		if (!name.contentEquals("null") && !name.contentEquals("none")) {
			pers.append("Name = " + name + "\n");
		}
		if (!birthday.contentEquals("null") && !birthday.contentEquals("none")) {
			pers.append("Birthday = " + birthday + "\n");
		}

		if (!email.contentEquals("null") && !email.contentEquals("none"))
			pers.append("Email = " + email + "\n");
		if (!gender.contentEquals("null") && !gender.contentEquals("none"))
			pers.append("Gender = " + gender + "\n\n");

		pers.append(" Best Friends \n 1-" + top3Contacts[0] + "\n2-"
				+ top3Contacts[1] + "\n3-" + top3Contacts[2] + "\n\n");
		if (prefs.contains("service_started"))
			pers.append(" Top Apps \n 1-" + top3Apps[0] + "\n2-" + top3Apps[1]
					+ "\n3-" + top3Apps[2] + "\n\n");
		pers.append("Books read ->" + prefs.getString("book", "none") + "\n\n");
		pers.append("Movies seen ->" + prefs.getString("book", "none") + "\n\n");
		tv.setText(pers);
	}

	// Get top 3 apps from the file written in the sd card using a background
	// service
	private void getTop3Apps() {
		// TODO Auto-generated method stub
		int f = 0, s = 0, t = 0;
		String fpath = Environment.getExternalStorageDirectory().getPath()
				+ "/app-count.txt";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(fpath));
			String line = "";
			while ((line = br.readLine()) != null) {
				String array[] = line.split(":");
				int value = Integer.parseInt(array[1]);
				if (value > f) {
					t = s;
					if (top3Apps[1] != null)
						top3Apps[2] = new String(top3Apps[1]);
					s = f;
					if (top3Apps[0] != null)
						top3Apps[1] = new String(top3Apps[0]);
					f = value;
					top3Apps[0] = new String(array[0]);
				} else if (value > s) {
					t = s;
					if (top3Apps[1] != null)
						top3Apps[2] = new String(top3Apps[1]);
					s = value;
					top3Apps[1] = new String(array[0]);
				} else if (value > t) {
					t = value;
					top3Apps[2] = new String(array[0]);
				}

			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Best friends analyzer using call log
	@SuppressWarnings("deprecation")
	private void analyzeCallLog() {
		// TODO Auto-generated method stub

		Cursor managedCursor = managedQuery(CallLog.Calls.CONTENT_URI, null,
				null, null, null);
		int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
		int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
		int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
		while (managedCursor.moveToNext()) {
			String phNumber = managedCursor.getString(number);
			String callType = managedCursor.getString(type);
			String callDuration = managedCursor.getString(duration);
			int dircode = Integer.parseInt(callType);
			switch (dircode) {
			case CallLog.Calls.OUTGOING_TYPE:
				buildUpFriendData(0, phNumber, callDuration);
				break;

			case CallLog.Calls.INCOMING_TYPE:
				buildUpFriendData(1, phNumber, callDuration);
				break;

			case CallLog.Calls.MISSED_TYPE:
				// Do Nothing
				break;
			}
		}
		findTop3Contacts();
	}

	// Check call time and get indexes of maximum duration
	private void findTop3Contacts() {
		// TODO Auto-generated method stub
		int first = 0, second = 0, third = 0;
		int max = 0, max2 = 0, max3 = 0;
		int flag = 0;
		if (pos > 2) {
			for (int i = 0; i < pos; i++) {
				if (time[i] > first) {
					third = second;
					second = first;
					first = time[i];
					max = i;
				} else if (time[i] > second) {
					third = second;
					second = time[i];
					max2 = i;
				} else if (time[i] > third) {
					third = time[i];
					max3 = i;
				}
			}
		} else
			flag = 1;

		displayBest3Friends(flag, max, max2, max3);

	}

	private void displayBest3Friends(int flag, int max, int max2, int max3) {
		// TODO Auto-generated method stub
		if (flag == 0) {
			top3Contacts[0] = new String(getContactName(name[max]));
			top3Contacts[1] = new String(getContactName(name[max2]));
			top3Contacts[2] = new String(getContactName(name[max3]));
		}
	}

	// get contact name from number
	private String getContactName(String phoneNumber) {
		// TODO Auto-generated method stub
		ContentResolver cr = getApplicationContext().getContentResolver();
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(phoneNumber));
		Cursor cursor = cr.query(uri,
				new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);
		if (cursor == null) {
			return null;
		}
		String contactName = null;
		if (cursor.moveToFirst()) {
			contactName = cursor.getString(cursor
					.getColumnIndex(PhoneLookup.DISPLAY_NAME));
		}

		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}

		return contactName;
	}

	// build up friend quotients for all contacts
	private void buildUpFriendData(int i, String phNumber, String callDuration) {
		// TODO Auto-generated method stub
		String number = new String();
		if (phNumber.startsWith("+91"))
			number = phNumber.substring(3, phNumber.length());
		else if (phNumber.startsWith("0"))
			number = phNumber.substring(1, phNumber.length());
		else
			number = phNumber;
		// check if phone number exists , if yes add time else add row
		int j = 0;
		for (j = 0; j < pos; j++) {
			if (name[j].contentEquals(number)) {
				if (i == 0)
					time[j] += 2 * Integer.parseInt(callDuration);
				else
					time[j] += Integer.parseInt(callDuration);

				break;
			}
		}
		if (j == pos && Integer.parseInt(callDuration) != 0) {
			name[pos] = number;
			if (i == 0)
				time[pos] = 2 * Integer.parseInt(callDuration);
			else
				time[pos++] = Integer.parseInt(callDuration);
		}

	}

	// start App tracker service
	private void startAppTrackerService() {
		// TODO Auto-generated method stub
		if (!prefs.contains("service_started")) {
			appintent = new Intent(getBaseContext(), AppTrackerService.class);
			startService(appintent);
			Editor editor = prefs.edit();
			editor.putInt("service_started", 1);
			editor.commit();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSimpleFacebook = SimpleFacebook.getInstance(this);
		// bindService(appintent, mConnection, Context.BIND_AUTO_CREATE);

	}

	@Override
	protected void onPause() {
		super.onPause();
		// unbindService(mConnection);
		// stopService(appintent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.menu_main, menu);

		return true;
	}

	private void loginAndGetData() {
		// TODO Auto-generated method stub
		mSimpleFacebook.login(new OnLoginListener() {

			@Override
			public void onFail(String reason) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onException(Throwable throwable) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onThinking() {
				// TODO Auto-generated method stub
			}

			@Override
			public void onNotAcceptingPermissions(Type type) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onLogin() {
				// TODO Auto-generated method stub
				mSimpleFacebook.getProfile(onProfileListener);
				Page.Properties properties = new Page.Properties.Builder().add(
						Properties.NAME).build();
				mSimpleFacebook.getBooks(properties, onBooksListener);
				mSimpleFacebook.getMovies(properties, onMoviesListener);
				Editor editor = prefs.edit();
				editor.putBoolean("fb_logged_in", true);
				editor.commit();
				displayAllDetails();
			}
		});
	}

	OnProfileListener onProfileListener = new OnProfileListener() {
		@Override
		public void onComplete(Profile profile) {
			// dataSource.open();
			String name = new String("none");
			String email = new String("none");
			String gender = new String("none");
			String age = new String("none");
			try {
				name = profile.getFirstName();
			} catch (NullPointerException ex) {
			}
			try {
				email = profile.getEmail();
			} catch (NullPointerException ex) {
			}
			try {
				gender = profile.getGender();
			} catch (NullPointerException ex) {
			}
			try {
				age = profile.getBirthday();
			} catch (NullPointerException ex) {
			}
			Editor editor = prefs.edit();
			editor.putString("name", name);
			editor.putString("mail", email);
			editor.putString("age", age);
			editor.putString("gender", gender);
			editor.commit();
			// dataSource.createUser(name, age, email, gender);
			displayAllDetails();
		}

	};

	OnPagesListener onBooksListener = new OnPagesListener() {

		@Override
		public void onComplete(List<Page> books) {

			for (Page book : books)
				bookList.append(book.getName() + "    ");
			Editor editor = prefs.edit();
			editor.putString("book", bookList.toString());
			editor.commit();

		}
	};

	OnPagesListener onMoviesListener = new OnPagesListener() {

		@Override
		public void onComplete(List<Page> movies) {
			for (Page movie : movies)
				movieList.append(movie.getName() + "    ");
			Editor editor = prefs.edit();
			editor.putString("movie", movieList.toString());
			editor.commit();
		}
	};

}