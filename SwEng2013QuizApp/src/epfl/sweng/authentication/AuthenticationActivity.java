package epfl.sweng.authentication;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import epfl.sweng.R;
import epfl.sweng.entry.MainActivity;
import epfl.sweng.servercomm.SwengHttpClientFactory;
import epfl.sweng.showquestions.ShowQuestionsActivity;
import epfl.sweng.testing.TestingTransactions;
import epfl.sweng.testing.TestingTransactions.TTChecks;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class AuthenticationActivity extends Activity {
	
	private String authenticationToken = null;

	private void authenticationFailed() {
		authenticationToken = null;
		
		TextView username = (TextView) findViewById(R.id.gaspar_username);
		username.setText("");
		
		TextView password = (TextView) findViewById(R.id.gaspar_password);
		password.setText("");
		
		Toast.makeText(getBaseContext(), R.string.authentication_failed,
				Toast.LENGTH_LONG).show();
		
		TestingTransactions.check(TTChecks.AUTHENTICATION_ACTIVITY_SHOWN);
	}
	
	private void authenticationSuccessful() {
		Intent mainActivityIntent = new Intent(this, MainActivity.class);
		
		startActivity(mainActivityIntent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authentication);
		
		TestingTransactions.check(TTChecks.AUTHENTICATION_ACTIVITY_SHOWN);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.authentication, menu);
		return true;
	}
	
	public void step1LogInTekila(View view) {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		// Test network connection
		if (networkInfo != null && networkInfo.isConnected()) {
			new GetAuthenticationTokenTask().execute(
					"https://sweng-quiz.appspot.com/login");
		} else {
			Toast.makeText(getBaseContext(), R.string.no_network,
					Toast.LENGTH_LONG).show();
		}
	}
	
	public void step3LogInTekila() {
		if (authenticationToken != null) {
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	
			// Test network connection
			if (networkInfo != null && networkInfo.isConnected()) {
				new TekilaAuthenticationTask().execute(
						"https://tequila.epfl.ch/cgi-bin/tequila/login");
			} else {
				Toast.makeText(getBaseContext(), R.string.no_network,
						Toast.LENGTH_LONG).show();
			}
		}
		
		/*
		// Tester connection ici
		if (true) {
			Intent mainActivityIntent = new Intent(this, MainActivity.class);
			startActivity(mainActivityIntent);
		}
		else {
			TextView username = (TextView) findViewById(R.id.gaspar_username);
			username.setText("");
			
			TextView password = (TextView) findViewById(R.id.gaspar_password);
			password.setText("");
			
			TestingTransactions.check(TTChecks.AUTHENTICATION_ACTIVITY_SHOWN);
		}*/
	}
	
	public void step5LogInTekila() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		// Test network connection
		if (networkInfo != null && networkInfo.isConnected()) {
			new SendAuthenticationTokenTask().execute(
					"https://sweng-quiz.appspot.com/login");
		} else {
			Toast.makeText(getBaseContext(), R.string.no_network,
					Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * Class who is use to get the authentication token from the server (Step 1)
	 *
	 */
	private class GetAuthenticationTokenTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			HttpGet getAuthenticationToken = new HttpGet(urls[0]);
			ResponseHandler<String> firstHandler = new BasicResponseHandler();
			try {
				return SwengHttpClientFactory.getInstance().execute(getAuthenticationToken, firstHandler);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				authenticationToken = null;
			}

			return null;
		}

		/**
		 * Method who is gonna take the result of an URL request and recover the authentication token
		 */
		protected void onPostExecute(String result) {
			try {
				JSONObject jsonResponse = new JSONObject(result);
				authenticationToken = (String) jsonResponse.get("token");
				step3LogInTekila();
			} catch (JSONException e) {
				e.printStackTrace();
				authenticationToken = null;
			}
		}
	}
	
	/**
	 * Class who is use to test if username and password are correct in Tekila (Step 3)
	 *
	 */
	private class TekilaAuthenticationTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			HttpPost postAuthenticationTask = new HttpPost(urls[0]);
			
			TextView username = (TextView) findViewById(R.id.gaspar_username);
			TextView password = (TextView) findViewById(R.id.gaspar_password);
			
			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			postParameters.add(new BasicNameValuePair("username", username.getText().toString()));
			postParameters.add(new BasicNameValuePair("password", password.getText().toString()));
			postParameters.add(new BasicNameValuePair("requestkey", authenticationToken));
			
			try {
				postAuthenticationTask.setEntity(new UrlEncodedFormEntity(postParameters));
				
				ResponseHandler<String> firstHandler = new BasicResponseHandler();
				
				return SwengHttpClientFactory.getInstance().execute(postAuthenticationTask, firstHandler);
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				authenticationToken = null;
			}
			
			return null;
		}

		/**
		 * Method who is gonna take the result of an URL and test if 
		 * authentication failed or not
		 */
		protected void onPostExecute(String result) {
			// à changer c'est en attendant
			if (result == null) {
				// Authentication successful
				step5LogInTekila();
			}
			else {
				// Authentication failed
				authenticationFailed();
			}
		}
	}
	
	/**
	 * Class who sends the authentication token back to the SwEng2013QuizApp 
	 * server (Step 5 - 6 - 7)
	 */
	private class SendAuthenticationTokenTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			HttpPost getAuthenticationToken = new HttpPost(urls[0]);
			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			postParameters.add(new BasicNameValuePair("token", authenticationToken));
			ResponseHandler<String> firstHandler = new BasicResponseHandler();
			try {
				return SwengHttpClientFactory.getInstance().execute(getAuthenticationToken, firstHandler);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				authenticationToken = null;
			}

			return null;
		}

		/**
		 * Method who is gonna take the result of an URL request and recover
		 * the session_id
		 */
		protected void onPostExecute(String result) {
			try {
				JSONObject jsonResponse = new JSONObject(result);
				//TODO Il faut stocker la session id
				String session_id = (String) jsonResponse.get("session");
				
				//TODO Juste pour tester
				TextView test = (TextView) findViewById(R.id.authentication_token);
				test.setText(session_id);
				
				authenticationSuccessful();
			} catch (JSONException e) {
				authenticationFailed();
			}
		}
	}
}
