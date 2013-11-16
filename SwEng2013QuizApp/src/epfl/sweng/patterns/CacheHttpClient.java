package epfl.sweng.patterns;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

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
	private ArrayList<QuizQuestion> failBox;
	private String tequilaWordWithSessionID = null;
	private int aSyncCounter = 0;


	/**
	 * Private constructor of the singleton.
	 */
	private CacheHttpClient(ProxyHttpClient myProxyHttpClient,
			IHttpClient myRealHttpClient) {
		this.cache = new ArrayList<QuizQuestion>();
		this.toSendBox = new ArrayList<QuizQuestion>();
		this.failBox = new ArrayList<QuizQuestion>();
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
		String method = request.getMethod();
		if (method.equals("POST")) {
			HttpPost post = (HttpPost) request;
			// checks that the auth is consistent.
			Header[] headers = post.getHeaders("Authorization");
			if (headers.length != 1
					|| !checkBasicAuthentificationSpecification(headers[0]
							.getValue())) {
				return new BasicHttpResponse(new BasicStatusLine(
						new ProtocolVersion("HTTP", 2, 1),
						HttpStatus.SC_UNAUTHORIZED, "UNAUTHORIZED"));
			} else {
				tequilaWordWithSessionID = headers[0].getValue();
				// extract and add to the cache
				String jsonContent = EntityUtils.toString(post.getEntity());
				QuizQuestion myQuizQuestion;
				try {
					// owner/id are needed to construct quizquestion and set as
					// default values
					myQuizQuestion = new QuizQuestion(jsonContent);
					addQuestionToCache(myQuizQuestion);
					// if we are online, we 
					// si on est online, on envoie la question au serveur
					int status = 0;
					if (!myProxyHttpClient.getOfflineStatus()) {
						SubmitQuestionTask mySubmitQuestionTask = new SubmitQuestionTask();
						mySubmitQuestionTask.execute(myQuizQuestion);
						try {
							status = mySubmitQuestionTask.get();
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (ExecutionException e) {
							e.printStackTrace();
						}
					}
					// if we are online, or if the question didn't get to get server, we add it to ToSendBox
					if (myProxyHttpClient.getOfflineStatus() || status != HttpStatus.SC_ACCEPTED) {
						addQuestionToSendBox(myQuizQuestion);
					}
					// if proxy accepted the question, reply okay (201) and
					// return
					// the question as json to confirm
					return new BasicHttpResponse(new BasicStatusLine(
							new ProtocolVersion("HTTP", 2, 1),
							HttpStatus.SC_CREATED,
							myQuizQuestion.toPostEntity()));
				} catch (JSONException e) {
					// if the question is malformed, we send a 500 error code
					return new BasicHttpResponse(new BasicStatusLine(
							new ProtocolVersion("HTTP", 2, 1),
							HttpStatus.SC_INTERNAL_SERVER_ERROR,
							"INTERNAL SERVER ERROR"));
				}
			}
		}
		// only post method is accepted here, so return Method Not Allowed Error
		return new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion(
				"HTTP", 2, 1), HttpStatus.SC_METHOD_NOT_ALLOWED,
				"METHOD NOT ALLOWED"));
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
	 * Basic method that check if the sessionID is consistent.
	 * 
	 * @param authToken
	 *            session id to test.
	 * @return true if it's consistent.
	 */
	private boolean checkBasicAuthentificationSpecification(String authToken) {
		if (authToken == null) {
			return false;
		}
		if (!authToken.startsWith("Tequila ")) {
			return false;
		}
		int specifiedLength = "Tequila dvoon4y2wp1r2biq052dppkxghyrob14"
				.length();
		if (authToken.length() != specifiedLength) {
			return false;
		}
		// String token = authToken.substring("tequila ".length()+1);
		// if (!token.matches("a-z0-9")){
		// return false;
		// }
		return true;
	}
	
//	/**
//	 * Get the tequila header.
//	 * 
//	 * @return the tequila header.
//	 */
//	public String getTequilaWordWithSessionID() {
//		return tequilaWordWithSessionID;
//	}

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
		if (toSendBox.size() == 0) { // && failBox.size() == 0) {
			myProxyHttpClient.goOnlineResponse(true);
		} else {
			int k = toSendBox.size();
			aSyncCounter = k;
			boolean flag = true;
			for (int i = 0; flag && i < k; ++i) {
				QuizQuestion question = toSendBox.get(0);
				toSendBox.remove(0);
				SubmitQuestionTask mySubmitQuestionTask = new SubmitQuestionTask();
				mySubmitQuestionTask.execute(question);
				try {
					// bloquant, attend l'execution complète de l'asynctask
					int httpStatus = mySubmitQuestionTask.get();
					httpStatus = httpStatus + 0;
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public void aSyncCounter() {
		aSyncCounter--;
		if (aSyncCounter == 0) { // && toSendBox.size() == 0) {
			boolean status = failBox.size() == 0;
			toSendBox.addAll(0, failBox);
			failBox.clear();
			myProxyHttpClient.goOnlineResponse(status);
		}
		
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
			post.setHeader("Authorization", tequilaWordWithSessionID);

			try {
				myQuestion = questionElement[0];
				post.setEntity(new StringEntity(myQuestion.toPostEntity()));
				HttpResponse response = myRealHttpClient.execute(post);
				Integer statusCode = response.getStatusLine().getStatusCode();
				response.getEntity().consumeContent();
				return statusCode;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				return HttpStatus.SC_BAD_GATEWAY;
			} catch (IOException e) {
				// en particulier si y a pas de réseau!!
				e.printStackTrace();
				return HttpStatus.SC_BAD_GATEWAY;
			}
			// to-do code de failure			
			return HttpStatus.SC_BAD_GATEWAY;
		}

		/**
		 * result is the http status code given by server.
		 */
		@Override
		protected void onPostExecute(Integer result) {
			if (result.compareTo(Integer.valueOf(HttpStatus.SC_CREATED)) == 0) {
				// je sais, y a rien, mais pas touche à mon if!
			} else if (!myProxyHttpClient.getOfflineStatus()
					&& result.compareTo(Integer
							.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR)) >= 0) {
				myProxyHttpClient.goOffLine();
				failBox.add(myQuestion);
//				System.out.println("async fail + offline " + myQuestion.toPostEntity());

			} else {
				failBox.add(myQuestion);
//				System.out.println("async fail " + myQuestion.toPostEntity());

			}
			aSyncCounter();
		}
	}
}
