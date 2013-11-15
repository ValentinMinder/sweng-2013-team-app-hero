package epfl.sweng.patterns;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import android.os.AsyncTask;

import epfl.sweng.quizquestions.QuizQuestion;
import epfl.sweng.testing.TestCoordinator;
import epfl.sweng.testing.TestCoordinator.TTChecks;

public final class CacheHttpClient implements IHttpClient {

	private static CacheHttpClient instance = null;
	private ProxyHttpClient myProxyHttpClient = null;
	private IHttpClient myRealHttpClient = null;
	private ArrayList<QuizQuestion> cache;
	private ArrayList<QuizQuestion> cacheToSend;

	private CacheHttpClient(ProxyHttpClient myProxyHttpClient,
			IHttpClient myRealHttpClient) {
		this.cache = new ArrayList<QuizQuestion>();
		this.cacheToSend = new ArrayList<QuizQuestion>();
		this.myProxyHttpClient = myProxyHttpClient;
		this.myRealHttpClient = myRealHttpClient;
	}

	public static synchronized CacheHttpClient getInstance(
			ProxyHttpClient myProxyHttpClient, IHttpClient myRealHttpClient) {
		if (instance == null) {
			instance = new CacheHttpClient(myProxyHttpClient, myRealHttpClient);
		}
		return instance;
	}

	@Override
	public HttpResponse execute(HttpUriRequest request) throws IOException,
			ClientProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1)
		throws IOException, ClientProtocolException {
		if (!cache.isEmpty()) {
			int size = cache.size();
			int index = (int) (Math.random() * size);
			return (T) cache.get(index).toPostEntity();
		}
		// offline et cache vide
		return null;
	}

	public boolean addQuestionToCache(QuizQuestion myQuizQuestion) {
		return cache.add(myQuizQuestion);
	}

	public boolean addQuestionToTemporaryCache(QuizQuestion myQuizQuestion) {
		return cacheToSend.add(myQuizQuestion);
	}

	public boolean sendTemporaryCache() {
		if (cacheToSend.size() == 0) {
			myProxyHttpClient.goOnlineDefinitely();
		}

		for (int i = 0; i < cacheToSend.size(); ++i) {
			QuizQuestion question = cacheToSend.get(i);
			new SubmitQuestionTask().execute(question);
		}
		return false;
	}

	/**
	 * 
	 * Task to submit a question
	 * 
	 * @author Valentin
	 * 
	 */
	private class SubmitQuestionTask extends
			AsyncTask<QuizQuestion, Void, Integer> {

		private QuizQuestion myQuestion = null;

		/**
		 * Execute and retrieve the answer from the website.
		 */
		@Override
		protected Integer doInBackground(QuizQuestion... questionElement) {
			String serverURL = "https://sweng-quiz.appspot.com/";
			HttpPost post = new HttpPost(serverURL + "quizquestions/");
			post.setHeader("Content-type", "application/json");
			post.setHeader("Authorization",
					myProxyHttpClient.getTequilaWordWithSessionID());

			try {
				myQuestion = questionElement[0];
				post.setEntity(new StringEntity(myQuestion.toPostEntity()));
				HttpResponse response = myRealHttpClient.execute(post);

				Integer statusCode = response.getStatusLine().getStatusCode();

				return statusCode;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				return HttpStatus.SC_INTERNAL_SERVER_ERROR;
			} catch (IOException e) {
				e.printStackTrace();
				return HttpStatus.SC_INTERNAL_SERVER_ERROR;
			}
			// Valou: en cas d'exeption en local, on lance la failure
			return HttpStatus.SC_SERVICE_UNAVAILABLE;
		}

		/**
		 * result is the http status code given by server.
		 */
		@Override
		protected void onPostExecute(Integer result) {
			if (result.compareTo(Integer.valueOf(HttpStatus.SC_CREATED)) == 0) {
				cacheToSend.remove(myQuestion);
				// passer online a la premiere envoye succes, ou quand toutes
				// envoyées
				if (cacheToSend.size() == 0) {
					myProxyHttpClient.goOnlineDefinitely();
				}
			} else if (!myProxyHttpClient.getOfflineStatus()
					&& result.compareTo(Integer
							.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR)) >= 0) {
				myProxyHttpClient.goOffLine();

			}
		}

	}

}
