package epfl.sweng.authentication;

import android.content.Context;
import android.content.SharedPreferences;

public class StoreCredential {
	private static StoreCredential instance = null;
	private static final String NAME_VARIABLE_SESSION = "SESSION_ID";
	private static final String NAME_PREFERENCE_SESSION = "user_session";
	private static final String NAME_VARIABLE_MODE_APP_OFFLINE = "mode_app_offline";
	private static final Boolean VALUE_DEFAULT_MODE_APP = false;
	
	public static StoreCredential getInstance() {
		if (instance == null) {
			instance = new StoreCredential();
		} 
		return instance;
	}
	
	public void storeSessionId(String sessionId, Context context) {
		SharedPreferences preferences = context.getSharedPreferences(NAME_PREFERENCE_SESSION, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(NAME_VARIABLE_SESSION, sessionId);
		editor.commit();
	}
	
	public String getSessionId(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(NAME_PREFERENCE_SESSION, Context.MODE_PRIVATE);
		String session = preferences.getString(NAME_VARIABLE_SESSION, "");
		return session;
	}
	
	public void removeSessionId(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(NAME_PREFERENCE_SESSION, Context.MODE_PRIVATE);
		SharedPreferences.Editor ed = preferences.edit();
		ed.remove(NAME_VARIABLE_SESSION);
		ed.commit();
	}
	
	public void setModeAppOffline(Context context, Boolean value) {
		SharedPreferences preferences = context.getSharedPreferences(NAME_PREFERENCE_SESSION, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(NAME_VARIABLE_MODE_APP_OFFLINE, value);
		editor.commit();
	}
	
	public Boolean getModeAppOffline(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(NAME_PREFERENCE_SESSION, Context.MODE_PRIVATE);
		Boolean mode_app = VALUE_DEFAULT_MODE_APP;
		if (!preferences.contains(NAME_VARIABLE_MODE_APP_OFFLINE)) {
			setModeAppOffline(context, VALUE_DEFAULT_MODE_APP);
		}
		else {
			mode_app = preferences.getBoolean(NAME_VARIABLE_MODE_APP_OFFLINE, VALUE_DEFAULT_MODE_APP);
		}
		
		return mode_app;
	}
}
