package epfl.sweng.searchquestions;

import java.util.ArrayList;
import org.apache.http.HttpResponse;
import android.os.AsyncTask;
import epfl.sweng.quizquestions.QuizQuestion;

public class SearchQuestions {
	private static SearchQuestions instance = null;
	private String nextID = null;
	private String request = null;
	private ArrayList<QuizQuestion> lastRequestArray = null;
	
	private SearchQuestions() {
		lastRequestArray = new ArrayList<QuizQuestion>();
	}
	
	public static synchronized SearchQuestions getInstance() {
		if (instance == null){
			instance = new SearchQuestions();
		}
		return instance;
	}
	
	public QuizQuestion getNextQuizQuestion() {
		// TODO non implemented method
		return null;
	}
	
	public void setRequest(String requestS) {
		this.request = requestS;
	}
	
	
	private class GetQuestionTask extends AsyncTask<String, Void, HttpResponse> {

		/**
		 * Execute and retrieve the answer from the website.
		 */
		@Override
		protected HttpResponse doInBackground(String... questionElement) {
			// TODO non implemented method
			return null;
		}

		/**
		 * Execute and retrieve the answer from the website.
		 */
		protected void onPostExecute(HttpResponse httpResponse) {
			// TODO non implemented method
		}
	}
}
