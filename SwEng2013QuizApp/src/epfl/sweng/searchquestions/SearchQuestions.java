package epfl.sweng.searchquestions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import epfl.sweng.patterns.ProxyHttpClient;
import epfl.sweng.quizquestions.QuizQuestion;
import epfl.sweng.utils.JSONUtils;

public final class SearchQuestions {
	private static SearchQuestions instance = null;
	private String nextID = null;
	private String request = null;
	private ArrayList<QuizQuestion> cachedRequestArray = null;

	private SearchQuestions(String requestS) {
		cachedRequestArray = new ArrayList<QuizQuestion>();
		this.request = requestS;
	}

	public static synchronized SearchQuestions getInstance(String requestS) {
		if (instance == null) {
			instance = new SearchQuestions(requestS);
		}
		return instance;
	}

	public QuizQuestion getNextQuizQuestion(String sessionID) {
		// TODO traiter le cas next maybe ?
		// stocker les questions dans le cache même si elles ne sont pas
		// affichées non ?
		// "You must store questions even if they have not yet been displayed to
		// the user
		// (e.g., the app received a batch of questions but the user has not yet
		// iterated through all of them)."
		if (cachedRequestArray.isEmpty()) {
			GetQuestionTask task = new GetQuestionTask();
			task.execute(sessionID);
			try {
				task.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		// if we have a remaining array of question.
		if (!cachedRequestArray.isEmpty()) {
			QuizQuestion question = cachedRequestArray.get(0);
			cachedRequestArray.remove(0);
			return question;
		}
		// if the array was empty and the server didn't get any more question.
		return null;
	}

	public void setRequest(String requestS) {
		this.request = requestS;
	}

	private class GetQuestionTask extends AsyncTask<String, Void, String> {

		/**
		 * Execute and retrieve the answer from the website.
		 */
		@Override
		protected String doInBackground(String... questionElement) {
			HttpPost post = new HttpPost(
					"https://sweng-quiz.appspot.com/search");
			post.setHeader("Content-type", "application/json");
			post.setHeader("Authorization", "Tequila " + questionElement[0]);
			String jsonQuery = "{\n\"query\": \"" + request + "\"\n}";
			if (nextID != null) {
				jsonQuery = "{\n\"query\": \"" + request + "\"\n" +
						"\"next\": \"" + nextID + "\"\n}";
			}
			try {
				post.setEntity(new StringEntity(jsonQuery));
				ResponseHandler<String> response = new BasicResponseHandler();
				String content = ProxyHttpClient.getInstance().execute(post,
						response);

				if (content == null) {
					return null;
				}

				try {
					JSONObject array = new JSONObject(content);

					ArrayList<String> arrayString = JSONUtils
							.convertJSONArrayToArrayListString(array
									.getJSONArray("questions"));
					try {
						String next = array.getString("next");
						if (next != null) {
							nextID = next;
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					for (String s : arrayString) {
						cachedRequestArray.add(new QuizQuestion(s));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;

		}

		/**
		 * Execute and retrieve the answer from the website.
		 */
		protected void onPostExecute(String content) {

		}
	}
}
