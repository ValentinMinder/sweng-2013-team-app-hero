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

/**
 * CacheHttpClient is the cache in the proxy design pattern. It stores a cache
 * of all question already fetched and question to be submitted.
 * 
 * @author valentin
 * 
 */
public final class CacheHttpClient implements IHttpClient {

	private static CacheHttpClient instance = null;
	private ProxyHttpClient myProxyHttpClient = null;
	private IHttpClient myRealHttpClient = null;
	private ArrayList<QuizQuestion> cache;
	private ArrayList<QuizQuestion> toSendBox;

	/**
	 * Private constructor of the singleton.
	 */
	private CacheHttpClient(ProxyHttpClient myProxyHttpClient,
			IHttpClient myRealHttpClient) {
		this.cache = new ArrayList<QuizQuestion>();
		this.toSendBox = new ArrayList<QuizQuestion>();
		this.myProxyHttpClient = myProxyHttpClient;
		this.myRealHttpClient = myRealHttpClient;
	}

	/**
	 * Retrieve the instance of the singleton.
	 * 
	 * @return the instance of the singleton.
	 */
	public static synchronized CacheHttpClient getInstance(
			ProxyHttpClient myProxyHttpClient, IHttpClient myRealHttpClient) {
		if (instance == null) {
			instance = new CacheHttpClient(myProxyHttpClient, myRealHttpClient);
		}
		return instance;
	}

	/**
	 * Execute a request to submit a question.
	 * <p>
	 * WARNING: DOES NOTHING NOW (BUT WORKS PERFECTLY).
	 */
	@Override
	public HttpResponse execute(HttpUriRequest request) throws IOException,
			ClientProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Execute a request to fetch a question from the cache.
	 */
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

	/**
	 * Add a question to the cache.
	 * 
	 * @param myQuizQuestion
	 *            QuizQuestion to add
	 * @return
	 */
	public boolean addQuestionToCache(QuizQuestion myQuizQuestion) {
		return cache.add(myQuizQuestion);
	}

	/**
	 * Add a question to the sendBox.
	 * 
	 * @param myQuizQuestion
	 *            QuizQuestion to add
	 * @return
	 */
	public boolean addQuestionToSendBox(QuizQuestion myQuizQuestion) {
		return toSendBox.add(myQuizQuestion);
	}

	/**
	 * Send the toSendBox to the real subject.
	 * 
	 * @return
	 */
	public boolean sendToSendBox() {
		if (toSendBox.size() == 0) {
			myProxyHttpClient.goOnlineDefinitely();
		}

		for (int i = 0; i < toSendBox.size(); ++i) {
			QuizQuestion question = toSendBox.get(i);
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
				toSendBox.remove(myQuestion);
				// passer online a la premiere envoye succes, ou quand toutes
				// envoyées
				if (toSendBox.size() == 0) {
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
