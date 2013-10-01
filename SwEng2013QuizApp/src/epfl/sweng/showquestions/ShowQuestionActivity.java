package epfl.sweng.showquestions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import epfl.sweng.R;
import epfl.sweng.entry.QuizQuestion;
import epfl.sweng.servercomm.SwengHttpClientFactory;
import epfl.sweng.testing.TestingTransactions;
import epfl.sweng.testing.TestingTransactions.TTChecks;

public class ShowQuestionActivity extends Activity {

	private QuizQuestion question = null;
	//TODO look if there is an available method that can be used to check if the AsyncTask has ended
	private ArrayList<String> convertJSONArrayToArrayListString(JSONArray jsonArray) throws JSONException {
		ArrayList<String> arrayReturn = new ArrayList<String>();
		if (jsonArray != null) {
			for (int i = 0; i < jsonArray.length(); i++) {
				arrayReturn.add(jsonArray.get(i).toString());
			}
		}

		return arrayReturn;
	}

	private void fetchQuestion() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		// Test network connection
		if (networkInfo != null && networkInfo.isConnected()) {
			try {
				new GetQuestionTask().execute(
						"https://sweng-quiz.appspot.com/quizquestions/random").get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		} else {
			// TODO No network connection available
			Toast.makeText(getBaseContext(), "No network connection available",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_question);

		fetchQuestion();
	}
	
	public void fetchAndDisplay(View v) {
		//disable the button nextQuestion and empty the TextView that indicate correctness of an answer
		Button nextQuestion = (Button) findViewById(R.id.next_question_button);
		nextQuestion.setEnabled(false);
		TextView correctness = (TextView) findViewById(R.id.correctness);
		correctness.setText("");
		fetchQuestion();
	}

	public void displayQuestion() {
		Button nextQuestion = (Button) findViewById(R.id.next_question_button);
		nextQuestion.setEnabled(false);
		
		if (question != null) {
			TextView questionTitle = (TextView) findViewById(R.id.displayed_text);
			questionTitle.setText(question.getQuestion());
	
			ListView possibleAnswers = (ListView) findViewById(R.id.multiple_choices);
	
			if (possibleAnswers != null) {
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, question.getAnswer());
	
				possibleAnswers.setAdapter(adapter);
				TestingTransactions.check(TTChecks.QUESTION_SHOWN);
				
				possibleAnswers.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> a, View v,
								int position, long id) {
							TextView correctness = (TextView) findViewById(R.id.correctness);
							Button nextQuestion = (Button) findViewById(R.id.next_question_button);
							if (!nextQuestion.isEnabled()) {
								// The right solution has not already been
								// found, thus we can react to user input
								if (id == question.getSolutionIndex()) {
									// The right answer has been found
									correctness.setText(R.string.right_answer);
									((Button) findViewById(R.id.next_question_button)).setEnabled(true);
								} else {
									correctness.setText(R.string.wrong_answer);
								}
								TestingTransactions.check(TTChecks.ANSWER_SELECTED);
							}

						}
					});
			}
			
			TextView tags = (TextView) findViewById(R.id.tags);
			if (tags != null) {
				String stringTags = "Tags : ";
				Iterator<String> itTag = question.getTags().iterator();
				while (itTag.hasNext()) {
					String tag = itTag.next();
					if (itTag.hasNext()) {
						stringTags += tag + ", ";
					}
					else {
						stringTags += tag;
					}
				}
				
				tags.setText(stringTags);
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_question, menu);
		return true;
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
				question = new QuizQuestion(jsonQuestion.getInt("id"),
						jsonQuestion.getString("question"),
							convertJSONArrayToArrayListString(jsonQuestion.getJSONArray("answers")),
						jsonQuestion.getInt("solutionIndex"),
							convertJSONArrayToArrayListString(jsonQuestion.getJSONArray("tags")));
				displayQuestion();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}
}
