package com.royce.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class AppTrackerService extends Service {

	

	private final IBinder mBinder = new MyBinder();
	String foregroundTaskAppName = "none";
	public int THREE_MINUTES = 1000 * 60 *3;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		try {
			File myFile = new File(Environment.getExternalStorageDirectory()
					.getPath() + "/app-count.txt");
			if (!myFile.exists()) {
				myFile.createNewFile();
				FileOutputStream fOut = new FileOutputStream(myFile);
				OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
				myOutWriter.close();
				fOut.close();
				Log.i("file", "created");
			}
		} catch (Exception e) {
			Log.i("file", "error");

		}
		final Handler handler = new Handler();
		Runnable runable = new Runnable() {

			@Override
			public void run() {
				try {
					getCurrentRunningApp();
					handler.postDelayed(this, THREE_MINUTES);
				} catch (Exception e) {
					// TODO: handle exception
				} finally {
					handler.postDelayed(this, THREE_MINUTES);
				}
			}
		};
		handler.postDelayed(runable, THREE_MINUTES);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		return Service.START_STICKY;
	}
	
	

	private void getCurrentRunningApp() {
		// TODO Auto-generated method stub
		ActivityManager am = (ActivityManager) AppTrackerService.this
				.getSystemService(ACTIVITY_SERVICE);
		// The first in the list of RunningTasks is always the foreground task.
		RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
		String foregroundTaskPackageName = foregroundTaskInfo.topActivity
				.getPackageName();
		PackageManager pm = AppTrackerService.this.getPackageManager();
		PackageInfo foregroundAppPackageInfo = null;
		try {
			foregroundAppPackageInfo = pm.getPackageInfo(
					foregroundTaskPackageName, 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		foregroundTaskAppName = foregroundAppPackageInfo.applicationInfo
				.loadLabel(pm).toString();
		// Log.i("app",foregroundTaskAppName);
		buildData(foregroundTaskAppName);
	}

	private void buildData(String appName) {
		// TODO Auto-generated method stub
		StringBuffer write = new StringBuffer();
		try {
			String fpath = Environment.getExternalStorageDirectory().getPath()
					+ "/app-count.txt";
			BufferedReader br = new BufferedReader(new FileReader(fpath));
			String line = "";
			while ((line = br.readLine()) != null) {
				String array[] = line.split(":");
				if (array[0].contentEquals(appName)) {
					int l = Integer.parseInt(array[1]);
					String v = String.valueOf(l + 1);
					write.append(appName + ":" + v + "\n");
					break;
				} else
					write.append(line + "\n");
			}
			if (line == null)
				write.append(appName + ":" + "1" + "\n");
			br.close();
			String result = write.toString();
			Log.i("file", "read");
			writer(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Boolean writer(String fcontent) {
		try {
			String fpath = Environment.getExternalStorageDirectory().getPath()
					+ "/app-count.txt";
			File file = new File(fpath);
			// If file does not exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(fcontent);
			bw.close();
			Log.i("file", "write");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	public class MyBinder extends Binder {
		public AppTrackerService getService() {
			return AppTrackerService.this;
		}
	}

	

}