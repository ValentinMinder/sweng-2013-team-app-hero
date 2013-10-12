package epfl.sweng.authentication;

import android.content.Context;
import android.content.SharedPreferences;

public class StoreCredential {
	private static StoreCredential instance = null;
	private final String NAME_VARIABLE_SESSION = "SESSION_ID";
	private final String NAME_PREFERENCE_SESSION = "user_session";
	
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
		//TODO lot of repetition here, see if we can factorise a little bit
		SharedPreferences preferences = context.getSharedPreferences(NAME_PREFERENCE_SESSION, Context.MODE_PRIVATE);
		SharedPreferences.Editor ed = preferences.edit();
		ed.remove(NAME_VARIABLE_SESSION);
		ed.commit();
	}
}
