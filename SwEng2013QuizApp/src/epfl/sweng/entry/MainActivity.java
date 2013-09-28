package epfl.sweng.entry;


import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import epfl.sweng.R;
import epfl.sweng.editquestion.EditQuestionActivity;
import epfl.sweng.servercomm.SwengHttpClientFactory;
import epfl.sweng.showquestions.ShowQuestionActivity;
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

public class MainActivity extends Activity {

	private QuizQuestion question = null;
	
	private ArrayList<String> convertJSONArrayToArrayListString(JSONArray jsonArray) throws JSONException{
		ArrayList<String> arrayReturn = new ArrayList<String>();
		if (jsonArray != null){
			for (int i = 0; i < jsonArray.length(); i++){
				arrayReturn.add(jsonArray.get(i).toString());
			}
		}
		
		return arrayReturn;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		TestingTransactions.check(TTChecks.MAIN_ACTIVITY_SHOWN);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	public void showQuestion(View view) {
		fetchQuestion();
		
		/*String jsonResponse = "{\n \"tags\": [\n  \"capitals\", \n  \"geography\", \n  \"countries\"\n ], \n \"solutionIndex\": 3, \n \"question\": \"What is the capital of Slovenia?\", \n \"answers\": [\n  \"Vatican City\", \n  \"Bogot\\u00e1\", \n  \"Kiev\", \n  \"Ljubljana\"\n ], \n \"owner\": \"sehaag\", \n \"id\": 5295935194136576\n}";
		
		try {
			JSONObject jsonQuestion = new JSONObject(jsonResponse);
			question = new QuizQuestion(
					jsonQuestion.getInt("id"), 
					jsonQuestion.getString("question"),
					convertJSONArrayToArrayListString(jsonQuestion.getJSONArray("answers")),
					jsonQuestion.getInt("solutionIndex"),
					convertJSONArrayToArrayListString(jsonQuestion.getJSONArray("tags")));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		catch (Throwable e) {
			e.printStackTrace();
		}*/
		
		Intent showQuestionIntent = new Intent(this, ShowQuestionActivity.class);
		
		if (question != null) {
			showQuestionIntent.putExtra("test", question.question);
		}
		else {
			showQuestionIntent.putExtra("test", "bug powa");
		}
		
		startActivity(showQuestionIntent);
	}
	
	public void submitQuestion(View view) {
		
		Intent editQuestionIntent = new Intent(this, EditQuestionActivity.class);

		startActivity(editQuestionIntent);
	}
	
	public void fetchQuestion() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        
        // Test network connection
        if (networkInfo != null && networkInfo.isConnected()) {
			try {
				new GetQuestionTask().execute("https://sweng-quiz.appspot.com/quizquestions/random").get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } else {
            // TODO No network connection available
        }
	}
	
	private class GetQuestionTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			HttpGet firstRandom = new HttpGet(urls[0]);
			ResponseHandler<String> firstHandler = new BasicResponseHandler();
			try {
				return SwengHttpClientFactory.getInstance().execute(firstRandom, firstHandler);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		protected void onPostExecute(String result) {					
			try {
				JSONObject jsonQuestion = new JSONObject(result);
				question = new QuizQuestion(
						jsonQuestion.getInt("id"), 
						jsonQuestion.getString("question"),
						convertJSONArrayToArrayListString(jsonQuestion.getJSONArray("answers")),
						jsonQuestion.getInt("solutionIndex"),
						convertJSONArrayToArrayListString(jsonQuestion.getJSONArray("tags")));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
	

