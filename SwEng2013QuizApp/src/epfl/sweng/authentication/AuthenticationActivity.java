package epfl.sweng.authentication;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import epfl.sweng.R;
import epfl.sweng.entry.MainActivity;
import epfl.sweng.servercomm.SwengHttpClientFactory;
import epfl.sweng.testing.TestCoordinator;
import epfl.sweng.testing.TestCoordinator.TTChecks;

public class AuthenticationActivity extends Activity {

	private final static int TEQUILA_RESPONSE_AUTHENTICATION_SUCCESSFUL = 302;
	private final static int SWENG_QUIZ_APP_RESPONSE_SUCCESSFUL = 200;

	private String authenticationToken = null;

	/**
	 * Method who is called if authentication failed, clear the text values of
	 * the text view
	 */
	private void authenticationFailed() {
		authenticationToken = null;

		TextView username = (TextView) findViewById(R.id.gaspar_username);
		username.setText("");

		TextView password = (TextView) findViewById(R.id.gaspar_password);
		password.setText("");

		Toast.makeText(getBaseContext(), R.string.authentication_failed,
				Toast.LENGTH_LONG).show();

		TestCoordinator.check(TTChecks.AUTHENTICATION_ACTIVITY_SHOWN);
	}

	/**
	 * Method who is called if authentication successful, session id is stored
	 * in SharedPreferences
	 * 
	 * @param session_id
	 */
	private void authenticationSuccessful(String sessionId) {
		StoreCredential.getInstance().storeSessionId(sessionId,
				getApplicationContext());
		this.finish();

		Intent mainActivityIntent = new Intent(this, MainActivity.class);

		startActivity(mainActivityIntent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authentication);

		TestCoordinator.check(TTChecks.AUTHENTICATION_ACTIVITY_SHOWN);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.authentication, menu);
		return true;
	}

	/**
	 * Method who is used to recover authentication token
	 */
	public void step1LogInTekila(View view) {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		// Test network connection
		if (networkInfo != null && networkInfo.isConnected()) {
			new GetAuthenticationTokenTask()
					.execute("https://sweng-quiz.appspot.com/login");
		} else {
			Toast.makeText(getBaseContext(), R.string.no_network,
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Method who is used to test username and password (Step 3)
	 */
	public void step3LogInTekila() {
		if (authenticationToken != null) {
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

			// Test network connection
			if (networkInfo != null && networkInfo.isConnected()) {
				new TekilaAuthenticationTask()
						.execute("https://tequila.epfl.ch/cgi-bin/tequila/login");
			} else {
				Toast.makeText(getBaseContext(), R.string.no_network,
						Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * Method who is used to test authentication token (Step 5)
	 */
	public void step5LogInTekila() {
		if (authenticationToken != null) {
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

			// Test network connection
			if (networkInfo != null && networkInfo.isConnected()) {
				new SendAuthenticationTokenTask()
						.execute("https://sweng-quiz.appspot.com/login");
			} else {
				Toast.makeText(getBaseContext(), R.string.no_network,
						Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * Class who is used to get the authentication token from the server (Step
	 * 1)
	 * 
	 */
	private class GetAuthenticationTokenTask extends
			AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			HttpGet getAuthenticationToken = new HttpGet(urls[0]);
			try {
				HttpResponse response = SwengHttpClientFactory.getInstance()
						.execute(getAuthenticationToken);

				if (response.getStatusLine().getStatusCode() == SWENG_QUIZ_APP_RESPONSE_SUCCESSFUL) {
					return EntityUtils.toString(response.getEntity());
				}
			} catch (ClientProtocolException e) {
				Logger.getLogger("epfl.sweng.authentication").log(Level.INFO,
						"Authentification task", e);
			} catch (IOException e) {
				Logger.getLogger("epfl.sweng.authentication").log(Level.INFO,
						"Authentification task", e);
			}

			return null;
		}

		/**
		 * Method who is gonna take the result of an URL request and recover the
		 * authentication token
		 */
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					JSONObject jsonResponse = new JSONObject(result);
					if (jsonResponse.has("token")) {
						authenticationToken = (String) jsonResponse
								.get("token");
						step3LogInTekila();
					} else {
						authenticationFailed();
					}
				} catch (JSONException e) {
					Logger.getLogger("epfl.sweng.authentification").log(
							Level.INFO, "authentification fail", e);
					authenticationToken = null;
					authenticationFailed();
				}
			} else {
				authenticationToken = null;
				authenticationFailed();
			}
		}
	}

	/**
	 * Class who is use to test if username and password are correct in Tekila
	 * (Step 3)
	 * 
	 */
	private class TekilaAuthenticationTask extends
			AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			HttpPost postAuthentication = new HttpPost(urls[0]);

			TextView username = (TextView) findViewById(R.id.gaspar_username);
			TextView password = (TextView) findViewById(R.id.gaspar_password);

			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			postParameters.add(new BasicNameValuePair("username", username
					.getText().toString()));
			postParameters.add(new BasicNameValuePair("password", password
					.getText().toString()));
			postParameters.add(new BasicNameValuePair("requestkey",
					authenticationToken));

			try {
				postAuthentication.setEntity(new UrlEncodedFormEntity(
						postParameters));
				HttpResponse response = SwengHttpClientFactory.getInstance()
						.execute(postAuthentication);
				if (response.getStatusLine().getStatusCode() == TEQUILA_RESPONSE_AUTHENTICATION_SUCCESSFUL) {
					return "success";
				} else {
					return "failed";
				}
			} catch (UnsupportedEncodingException e) {
				Logger.getLogger("epfl.sweng.authentication").log(Level.INFO,
						"Authentification task", e);
			} catch (ClientProtocolException e) {
				Logger.getLogger("epfl.sweng.authentication").log(Level.INFO,
						"Authentification task", e);
			} catch (IOException e) {
				Logger.getLogger("epfl.sweng.authentication").log(Level.INFO,
						"Authentification task", e);
			}

			authenticationToken = null;
			return null;
		}

		/**
		 * Method who is gonna take the result of an URL and test if
		 * authentication failed or not
		 */
		protected void onPostExecute(String result) {
			if (result.equals("success")) {
				// Authentication successful
				step5LogInTekila();
			} else {
				// Authentication failed
				authenticationFailed();
			}
		}
	}

	/**
	 * Class who sends the authentication token back to the SwEng2013QuizApp
	 * server and recover the session id (Step 5 - 6 - 7)
	 */
	private class SendAuthenticationTokenTask extends
			AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			HttpPost getAuthenticationToken = new HttpPost(urls[0]);

			try {
				getAuthenticationToken.setEntity(new StringEntity(
						"{\"token\": \"" + authenticationToken + "\"}"));
				getAuthenticationToken.setHeader("Content-type",
						"application/json");

				HttpResponse response = SwengHttpClientFactory.getInstance()
						.execute(getAuthenticationToken);

				if (response.getStatusLine().getStatusCode() == SWENG_QUIZ_APP_RESPONSE_SUCCESSFUL) {
					return EntityUtils.toString(response.getEntity());
				}
			} catch (ClientProtocolException e) {
				Logger.getLogger("epfl.sweng.authentication").log(Level.INFO,
						"Authentification task", e);
			} catch (IOException e) {
				Logger.getLogger("epfl.sweng.authentication").log(Level.INFO,
						"Authentification task", e);
			} finally {
				authenticationToken = null;
			}

			return null;
		}

		/**
		 * Method who is gonna take the result of an URL request and recover the
		 * session_id
		 */
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					JSONObject jsonResponse = new JSONObject(result);
					String sessionId = (String) jsonResponse.get("session");
					authenticationSuccessful(sessionId);
				} catch (JSONException e) {
					Logger.getLogger("epfl.sweng.authentication").log(
							Level.INFO, "Authentification task", e);
					authenticationFailed();
				}
			} else {
				authenticationFailed();
			}
		}
	}
}
