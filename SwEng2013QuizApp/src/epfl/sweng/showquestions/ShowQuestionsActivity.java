package epfl.sweng.showquestions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
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
import epfl.sweng.authentication.StoreCredential;
import epfl.sweng.caching.CacheException;
import epfl.sweng.patterns.ProxyHttpClient;
import epfl.sweng.quizquestions.QuizQuestion;
import epfl.sweng.searchquestions.SearchQuestions;
import epfl.sweng.testing.TestCoordinator;
import epfl.sweng.testing.TestCoordinator.TTChecks;

/**
 * Class to show random question from the server.
 * 
 * @author juniors
 * 
 */
public class ShowQuestionsActivity extends Activity {

	private QuizQuestion question = null;
	private SearchQuestions searchQuestion = null;
	private boolean isSearch = false;

	/**
	 * Method who is called if error occurred
	 */
	private void errorDisplayQuestion() {
		// Reset the differents fields
		ListView possibleAnswers = (ListView) findViewById(R.id.multiple_choices);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, new ArrayList<String>());

		possibleAnswers.setAdapter(adapter);

		TextView questionTitle = (TextView) findViewById(R.id.displayed_text);
		questionTitle.setText("");

		TextView tags = (TextView) findViewById(R.id.tags);
		tags.setText("");

		Toast.makeText(getBaseContext(), R.string.error_retrieving_question,
				Toast.LENGTH_SHORT).show();
		TestCoordinator.check(TTChecks.QUESTION_SHOWN);

	}

	/**
	 * Method who is going to take a random question on the server.
	 * 
	 * This is going to ask an asynchronous task to do this.
	 */
	private void fetchQuestion() {
		try {
			new GetQuestionTask().execute(
					"https://sweng-quiz.appspot.com/quizquestions/random")
					.get();
		} catch (InterruptedException e) {
			Logger.getLogger("epfl.sweng.showquestions").log(Level.INFO,
					"fetchQuestion Fail", e);

		} catch (ExecutionException e) {
			Logger.getLogger("epfl.sweng.showquestions").log(Level.INFO,
					"fecthQuestion Fail", e);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_question);
		Intent showQuestionIntent = getIntent();
		String type = showQuestionIntent.getStringExtra("Type");
		if (type != null && type.equals("Search")) {
			isSearch = true;
			String search = showQuestionIntent.getStringExtra("Request");
			searchQuestion = new SearchQuestions(search);
			newSearchQuestion();

		} else {
			fetchQuestion();
		}
	}

	private void newSearchQuestion() {
		question = searchQuestion.getNextQuizQuestion(StoreCredential
				.getInstance().getSessionId(getApplicationContext()));
		if (question == null) {
			errorDisplayQuestion();
		} else {
			displayQuestion();
		}
	}

	/**
	 * Method who is going to put correctly the display for the question and
	 * search the question to display it.
	 * 
	 * @param v
	 *            View corresponding to the button "Next question"
	 */
	public void fetchAndDisplay(View v) {
		// disable the button nextQuestion and empty the TextView that indicate
		// correctness of an answer
		Button nextQuestion = (Button) findViewById(R.id.next_question_button);
		nextQuestion.setEnabled(false);
		TextView correctness = (TextView) findViewById(R.id.correctness);
		correctness.setText("");
		if (isSearch) {
			newSearchQuestion();
		} else {
			fetchQuestion();
		}
	}

	/**
	 * Method who is going to make the window to display the question.
	 */
	public void displayQuestion() {
		Button nextQuestion = (Button) findViewById(R.id.next_question_button);
		nextQuestion.setEnabled(false);
		if (question != null) {
			TextView questionTitle = (TextView) findViewById(R.id.displayed_text);
			questionTitle.setText(question.getQuestion());

			ListView possibleAnswers = (ListView) findViewById(R.id.multiple_choices);
			if (possibleAnswers != null) {
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1,
						question.getAnswers());

				possibleAnswers.setAdapter(adapter);
				TestCoordinator.check(TTChecks.QUESTION_SHOWN);

				possibleAnswers
						.setOnItemClickListener(new OnItemClickListener() {
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
										correctness
												.setText(R.string.right_answer);
										((Button) findViewById(R.id.next_question_button))
												.setEnabled(true);
									} else {
										correctness
												.setText(R.string.wrong_answer);
									}
								}
								TestCoordinator.check(TTChecks.ANSWER_SELECTED);

							}
						});
			}

			TextView tags = (TextView) findViewById(R.id.tags);
			if (tags != null) {
				StringBuffer buff = new StringBuffer();
				buff.append("Tags : ");
				Iterator<String> itTag = question.getTags().iterator();
				while (itTag.hasNext()) {
					String tag = itTag.next();
					if (itTag.hasNext()) {
						buff.append(tag + ", ");
					} else {
						buff.append(tag);
					}
				}

				tags.setText(buff.toString());
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_question, menu);
		return true;
	}

	/**
	 * Class who is use to get the question from the server
	 * 
	 * @author juniors
	 * 
	 */
	private class GetQuestionTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			HttpGet firstRandom = new HttpGet(urls[0]);
			firstRandom.setHeader(
					"Authorization",
					"Tequila "
							+ StoreCredential.getInstance().getSessionId(
									getApplicationContext()));
			ResponseHandler<String> firstHandler = new BasicResponseHandler();
			try {
				return ProxyHttpClient.getInstance().execute(firstRandom,
						firstHandler);
			} catch (ClientProtocolException e) {
				Logger.getLogger("epfl.sweng.showquestions").log(Level.INFO,
						"ShowQuestion task Fail", e);
			} catch (IOException e) {
				Logger.getLogger("epfl.sweng.showquestions").log(Level.INFO,
						"ShowQuestion task Fail", e);
			} catch (CacheException e) {
				Logger.getLogger("epfl.sweng.showquestions").log(Level.INFO,
						"Cache ShowQuestion task Fail", e);
			}

			return null;
		}

		/**
		 * Method who is gonna take the result of an URL request and parse it in
		 * a QuizQuestion Object. and after display it.
		 */
		protected void onPostExecute(String result) {
			try {
				if (result == null) {
					errorDisplayQuestion();
				} else {
					JSONObject jsonQuestion = new JSONObject(result);
					if (jsonQuestion.has("message")) {
						errorDisplayQuestion();
					} else {
						question = new QuizQuestion(result);
						displayQuestion();
					}
				}
			} catch (JSONException e) {
				Logger.getLogger("epfl.sweng.showquestions").log(Level.INFO,
						"JSON task Fail", e);
				errorDisplayQuestion();
			}
		}

	}
}
