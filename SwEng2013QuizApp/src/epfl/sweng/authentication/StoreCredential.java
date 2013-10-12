package epfl.sweng.authentication;

import android.content.Context;
import android.content.SharedPreferences;

public class StoreCredential {
	private static StoreCredential instance = null;
	public final static String NAME_VARIABLE_SESSION = "SESSION_ID";
	public final static String NAME_PREFERENCE_SESSION = "user_session";
	
	public static StoreCredential getInstance() {
		if (instance == null) {
			instance = new StoreCredential();
		} 
		return instance;
	}
	
	public void storeSessionId(String sessionId, Context context) {
		SharedPreferences prefs = context.getSharedPreferences(NAME_PREFERENCE_SESSION, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(NAME_VARIABLE_SESSION, sessionId);
		editor.commit();
	}
}
