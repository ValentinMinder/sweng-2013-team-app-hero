package epfl.sweng.searchquestions;

import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.http.HttpResponse;

import android.os.AsyncTask;
import epfl.sweng.quizquestions.QuizQuestion;

public class SearchQuestions {
	private static SearchQuestions instance = null;
	private String nextID = null;
	private String request = null;
	private LinkedList<QuizQuestion> cachedRequestArray =  null;
	
	private SearchQuestions() {
		cachedRequestArray = new LinkedList<QuizQuestion>();
	}
	
	public static synchronized SearchQuestions getInstance() {
		if (instance == null){
			instance = new SearchQuestions();
		}
		return instance;
	}
	
	public QuizQuestion getNextQuizQuestion() {
		if (cachedRequestArray.isEmpty()) {
			// TODO retrieve from server, and fill the array
		}
		// if we have a remaining array of question.
		if (!cachedRequestArray.isEmpty()) {
			return cachedRequestArray.peekFirst();
		}
		// if the array was empty and the server didn't get any more question.
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
