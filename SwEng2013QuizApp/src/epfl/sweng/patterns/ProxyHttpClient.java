package epfl.sweng.patterns;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;

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
import org.json.JSONObject;

import android.os.AsyncTask;
import epfl.sweng.quizquestions.QuizQuestion;
import epfl.sweng.testing.TestCoordinator;
import epfl.sweng.testing.TestCoordinator.TTChecks;

/**
 * ProxyHttpClient is the proxy in the proxy design pattern. It decides whether
 * the request should be sent to the real subject of to the cache.
 * 
 * @author valentin
 * 
 */
public final class ProxyHttpClient implements IHttpClient {
	private static boolean offline = false;
	private static ProxyHttpClient instance = null;

	private String tequilaWordWithSessionID = null;
	private IHttpClient realHttpClient = null;
	private CacheQuizQuestionInner myCacheQuizQuestion = null;
	private ICheckBoxTask myCheckBoxTask = null;
	
	private int aSyncCounter = 0;

	/**
	 * Private constructor of the singleton.
	 */
	private ProxyHttpClient() {
		this.realHttpClient = RealHttpClient.getInstance();
		this.myCacheQuizQuestion = new CacheQuizQuestionInner(this);
	}

	/**
	 * Retrieve the instance of the singleton.
	 * 
	 * @return the instance of the singleton.
	 */
	public static ProxyHttpClient getInstance() {
		if (instance == null) {
			instance = new ProxyHttpClient();
		}
		return instance;
	}

	/**
	 * Set boolean offline to true, only if it was false before, and calls the
	 * corresponding TTChecks.
	 */
	public void goOffLine() {
		if (!offline) {
			offline = true;
			// a verifier la necessite de cela:
//			myCheckBoxTask.confirmCheckBoxTask(offline);
			TestCoordinator.check(TTChecks.OFFLINE_CHECKBOX_ENABLED);
		}
	}

	/**
	 * Tries to go online.
	 * <p>
	 * If the cache is empty, goes immediately online. If the cache is not
	 * empty, tries to sync completely with the server, and if so goes online by
	 * calling the goOnlineDefinitely method.
	 */
	public void goOnline() {
		if (offline) {
			myCacheQuizQuestion.sendOutBox();
		}
	}

	/**
	 * Set boolean offline to false, only if it was true before, and calls the
	 * corresponding TTChecks.
	 * <p>
	 * WARNING: DON'T CALL THIS! CALL GO ONLINE WHICH HANDLE ALL!
	 */
	private void goOnlineResponse(boolean bool) {
		if (offline) {
			if (bool) {
				offline = false;
				myCheckBoxTask.confirmCheckBoxTask(offline);
				TestCoordinator.check(TTChecks.OFFLINE_CHECKBOX_DISABLED);
			} else {
				TestCoordinator.check(TTChecks.OFFLINE_CHECKBOX_ENABLED);
				myCheckBoxTask.releaseGoOnlineTask();
			}
		}
	}

	/**
	 * Get offline boolean parameter.
	 * 
	 * @return true represent offline.
	 */
	public boolean getOfflineStatus() {
		return offline;
	}
	
	/**
	 * Set the checkboxTask.
	 * @param myCheckBoxTaskT
	 */
	public void setCheckBoxTask(ICheckBoxTask myCheckBoxTaskT) {
		this.myCheckBoxTask = myCheckBoxTaskT;		
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

	/**
	 * Execute a request to submit a question.
	 * <p>
	 * Retrieve a question and stores into the cache. If online, redirect also
	 * the request to the real subject.
	 */
	@Override
	public HttpResponse execute(HttpUriRequest request) throws IOException,
			ClientProtocolException {
		HttpResponse response = null;
		boolean previousOfflineStatus = getOfflineStatus();
		boolean onlineSuccesfulComm = false;
		boolean goOffline = false;
		if (!previousOfflineStatus) {
			Integer statusCode = -1;
			try {
				response = realHttpClient.execute(request);
				statusCode = response.getStatusLine().getStatusCode();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				goOffline = true;
			} catch (IOException e) {
				e.printStackTrace();
				goOffline = true;
			}
			
			if (statusCode.compareTo(Integer.valueOf(HttpStatus.SC_CREATED)) == 0) {
				onlineSuccesfulComm = true;
			} else if (!getOfflineStatus()
					&& statusCode.compareTo(Integer
							.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR)) >= 0) {
				onlineSuccesfulComm = false;
				goOffline = true;
			} else {
				onlineSuccesfulComm = false;
			}
		}
		
		String method = request.getMethod();
		if (method.equals("POST")) {
			HttpPost post = (HttpPost) request;
			// checks that the auth is consistent.
			Header[] headers = post.getHeaders("Authorization");
			if (headers.length != 1  
					|| !checkBasicAuthentificationSpecification(headers[0]
							.getValue())) {
				return executeAtTheEnd(new BasicHttpResponse(new BasicStatusLine(
						new ProtocolVersion("HTTP", 2, 1),
						HttpStatus.SC_UNAUTHORIZED, "UNAUTHORIZED")), goOffline);
			} else {
				tequilaWordWithSessionID = headers[0].getValue();
				// extract and add to the cache
				String jsonContent = EntityUtils.toString(post.getEntity());
				QuizQuestion myQuizQuestion;
				try {
					// owner/id are needed to construct quizquestion and set as
					// default values
					myQuizQuestion = new QuizQuestion(jsonContent);
					myCacheQuizQuestion.addQuestionToCache(myQuizQuestion);
					if (previousOfflineStatus || !onlineSuccesfulComm) {
						// if we are offline we add it to ToSendBox
						// if we are online and there was an error
						myCacheQuizQuestion.addQuestionToOutBox(myQuizQuestion);
					}
					// if proxy accepted the question, reply okay (201) and
					// return
					// the question as json to confirm
					return executeAtTheEnd(new BasicHttpResponse(new BasicStatusLine(
							new ProtocolVersion("HTTP", 2, 1),
							HttpStatus.SC_CREATED,
							myQuizQuestion.toPostEntity())), goOffline);
				} catch (JSONException e) {
					// if the question is malformed, we send a 500 error code
					return executeAtTheEnd(new BasicHttpResponse(new BasicStatusLine(
							new ProtocolVersion("HTTP", 2, 1),
							HttpStatus.SC_INTERNAL_SERVER_ERROR,
							"INTERNAL SERVER ERROR")), goOffline);
				}
			}
		} else {
			// only post method is accepted here, so return Method Not Allowed Error
			return executeAtTheEnd(new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion(
					"HTTP", 2, 1), HttpStatus.SC_METHOD_NOT_ALLOWED,
					"METHOD NOT ALLOWED")), goOffline);
		}
		// we dont care about the server response: only proxy response is important for client
//		if (response != null && !previousOfflineStatus){
//			return response;
//		}		
	}
	
	/**
	 * For modularity reason: confirm the question, go offline if request, and sent back
	 * the httpResponse.
	 * @param httpResponse
	 * @param goOffline
	 * @return
	 */
	private HttpResponse executeAtTheEnd(HttpResponse httpResponse, boolean goOffline) {
		TestCoordinator.check(TTChecks.NEW_QUESTION_SUBMITTED);
		if (goOffline) {
			offline = true;
		}
		return httpResponse;
	}

	/**
	 * Execute a request to fetch a question.
	 * <p>
	 * Redirect the call to the real subject or the cache, depending on the
	 * offline boolean.
	 */
	@Override
	public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1)
		throws IOException, ClientProtocolException {
		// online, we fetch from server
		if (!offline) {
			try {
				T t = realHttpClient.execute(arg0, arg1);
				if (t == null || t.getClass() != String.class) {
					goOffLine();
				} else {
					JSONObject jsonQuestion = new JSONObject((String) t);
					if (jsonQuestion.has("message")) {
						goOffLine();
					} else {
						// if the retrieved question is malformed or it's not
						// question: exception > offline
						QuizQuestion myQuizQuestion = new QuizQuestion(
								(String) t);
						// for the moment, we dont check if the id of the
						// question already exist in the list. Hint: SET
						myCacheQuizQuestion.addQuestionToCache(myQuizQuestion);
						return t;
					}
				}
				// check the behavior of online unsucessful exception
			} catch (JSONException e) {
				e.printStackTrace();
				goOffLine();
			} catch (UnknownHostException e) {
				System.out.println("we catched the exception");
				e.printStackTrace();
				goOffLine();
			}
		} else {
			// if offline we fetch the cache
			return (T) myCacheQuizQuestion.getRandomQuestionFromCache();
		}

		// if server disconnected, we return null;
		return null;
	}
	
	/**
	 * An async task can notify when it's done.
	 */
	private void aSyncCounter() {
		aSyncCounter--;
		System.out.println(aSyncCounter);
		if (aSyncCounter == 0) { // && toSendBox.size() == 0) {
			System.out.println("asny 0");
			goOnlineResponse(myCacheQuizQuestion.getSentStatus());
		}
		
	}
	
	/**
	 * Send a question.
	 * @param question the question to be sent.
	 * @return the http status, -1 if fail to end the task.
	 */
	private int sendQuestion(QuizQuestion question) {
		SubmitQuestionTask mySubmitQuestionTask = new SubmitQuestionTask();
		mySubmitQuestionTask.execute(question);
		int httpStatus = -1;
//		try {
//			// /!\ bloquant, attend l'execution complète de l'asynctask
//			// attention, c'est peut etre faux!
//			httpStatus = mySubmitQuestionTask.get();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} catch (ExecutionException e) {
//			e.printStackTrace();
//		}
		return httpStatus;
	}
	
	/**
	 * A question to failBox.
	 * @param myQuestion
	 */
	private void addToFailBox(QuizQuestion myQuestion) {
		myCacheQuizQuestion.addToFailBox(myQuestion);
	}
	
	/**
	 * Set the async counter.
	 * @param k
	 */
	private void setASyncCounter(int k) {
		aSyncCounter = k;
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
				HttpResponse response = realHttpClient.execute(post);
				Integer statusCode = response.getStatusLine().getStatusCode();
				response.getEntity().consumeContent();
				System.out.println("statzus " + statusCode);
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
			} else if (!getOfflineStatus()
					&& result.compareTo(Integer
							.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR)) >= 0) {
				addToFailBox(myQuestion);
				goOffLine();
				System.out.println("async fail + offline " + myQuestion.toPostEntity());

			} else {
				addToFailBox(myQuestion);
				System.out.println("async fail " + myQuestion.toPostEntity());

			}
			aSyncCounter();
		}
	}
	
	/** 
	 * CacheQuizQuestion is a cache in the proxy design pattern. It stores 
	 * all question already fetched and question to be submitted.
	 * @author valentin
	 *
	 */
	private class CacheQuizQuestionInner {
		private ProxyHttpClient myProxyHttpClient = null;
		
		private ArrayList<QuizQuestion> myCacheQuizQuestion;
		private ArrayList<QuizQuestion> outBox;
		private ArrayList<QuizQuestion> failBox;

		/**
		 * Private constructor of the singleton.
		 */
		public CacheQuizQuestionInner(ProxyHttpClient myProxyHttpClient) {
			this.myCacheQuizQuestion = new ArrayList<QuizQuestion>();
			this.outBox = new ArrayList<QuizQuestion>();
			this.failBox = new ArrayList<QuizQuestion>();
			this.myProxyHttpClient = myProxyHttpClient;
		}

		/**
		 * Fetch a question from the cache.
		 */
		protected String getRandomQuestionFromCache() {
			if (!myCacheQuizQuestion.isEmpty()) {
				int size = myCacheQuizQuestion.size();
				int index = (int) (Math.random() * size);
				return myCacheQuizQuestion.get(index).toPostEntity();
			}
			// offline and empty cache
			return null;
		}

		/**
		 * Add a question to the cache.
		 * 
		 * @param myQuizQuestion
		 *            QuizQuestion to add
		 * @return
		 */
		protected boolean addQuestionToCache(QuizQuestion myQuizQuestion) {
			return myCacheQuizQuestion.add(myQuizQuestion);
		}

		/**
		 * Add a question to the outBox.
		 * 
		 * @param myQuizQuestion
		 *            QuizQuestion to add
		 * @return
		 */
		protected boolean addQuestionToOutBox(QuizQuestion myQuizQuestion) {
			return outBox.add(myQuizQuestion);
		}

		/**
		 * Send the outBox to the real subject.
		 * <p>
		 * For each question, it ask the proxy to send the question to the real subject.
		 * 
		 * @return
		 */
		protected boolean sendOutBox() {
			if (outBox.size() == 0) { // && failBox.size() == 0) {
				myProxyHttpClient.goOnlineResponse(true);
			} else {
				int k = outBox.size();
				myProxyHttpClient.setASyncCounter(k);
				boolean flag = true;
				for (int i = 0; flag && i < k; ++i) {
					QuizQuestion question = outBox.get(0);
					outBox.remove(0);
					myProxyHttpClient.sendQuestion(question);
				}
			}
			return false;
		}
		
		/**
		 * Add a question to the failBox.
		 * @param myQuestion
		 */
		protected void addToFailBox(QuizQuestion myQuestion) {
			failBox.add(myQuestion);
		}
		
		/**
		 * Checks if all questions have been sent.
		 * @return true if not question was in the failBox.
		 */
		protected boolean getSentStatus() {
			boolean status = failBox.size() == 0;
			outBox.addAll(0, failBox);
			failBox.clear();
			System.out.println("sent status" + status);
			return status;
		}
	}
}
