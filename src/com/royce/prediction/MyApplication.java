package com.royce.prediction;

import android.app.Application;

import com.facebook.SessionDefaultAudience;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;

public class MyApplication extends Application {
	private static final String APP_ID = "853693581355555";
	private static final String APP_NAMESPACE = "prediction_royce";

	@Override
	public void onCreate() {
		super.onCreate();

		// set log to true

		// initialize facebook configuration
		Permission[] permissions = new Permission[] { 
				Permission.PUBLIC_PROFILE,
				Permission.USER_BIRTHDAY,
				Permission.USER_HOMETOWN,
				Permission.USER_RELATIONSHIPS,
				Permission.EMAIL,
				Permission.USER_LIKES ,
				Permission.USER_HOMETOWN,
				Permission.USER_ABOUT_ME
				};

		SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
			.setAppId(APP_ID)
			.setNamespace(APP_NAMESPACE)
			.setPermissions(permissions)
			.setDefaultAudience(SessionDefaultAudience.FRIENDS)
			.setAskForAllPermissionsAtOnce(true)
			.build();

		SimpleFacebook.setConfiguration(configuration);
	}
}
