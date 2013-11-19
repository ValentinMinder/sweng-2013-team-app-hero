package epfl.sweng.searchquestions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import android.os.AsyncTask;
import epfl.sweng.authentication.StoreCredential;
import epfl.sweng.patterns.ProxyHttpClient;
import epfl.sweng.quizquestions.QuizQuestion;

public class SearchQuestions {
	private static SearchQuestions instance = null;
	private String nextID = null;
	private String request = null;
	private LinkedList<QuizQuestion> cachedRequestArray = null;

	private SearchQuestions(String requestS) {
		cachedRequestArray = new LinkedList<QuizQuestion>();
		this.request = requestS;
	}

	public static synchronized SearchQuestions getInstance(String requestS) {
		if (instance == null) {
			instance = new SearchQuestions(requestS);
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
			HttpPost post = new HttpPost(
					"https://sweng-quiz.appspot.com/search");
//			post.setHeader(
//					"Authorization",
//					"Tequila "
//							+ StoreCredential.getInstance().getSessionId(
//									getApplicationContext()));
				
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
