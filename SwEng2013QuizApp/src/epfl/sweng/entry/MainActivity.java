package epfl.sweng.entry;


import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;

import epfl.sweng.R;
import epfl.sweng.servercomm.SwengHttpClientFactory;
import epfl.sweng.showquestions.ShowQuestionActivity;
import epfl.sweng.testing.TestingTransactions;
import epfl.sweng.testing.TestingTransactions.TTChecks;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

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
	//	fetchQuestion();
		Intent showQuestionIntent = new Intent(this, ShowQuestionActivity.class);
		startActivity(showQuestionIntent);
	}
	
	public void submitQuestion(View view) {
			
	}
	
	public void fetchQuestion() {
		HttpGet firstRandom = new HttpGet("https://sweng-quiz.appspot.com/quizquestions/random");
		ResponseHandler<String> firstHandler = new BasicResponseHandler();
		try {
			String firstQuestion = SwengHttpClientFactory.getInstance().execute(firstRandom, firstHandler);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
}
	

