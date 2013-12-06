package epfl.sweng.patterns;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import epfl.sweng.caching.Cache;
import epfl.sweng.caching.CacheException;
import epfl.sweng.caching.HandleOfflineQuery;
import epfl.sweng.caching.ICacheToProxyPrivateTasks;
import epfl.sweng.caching.IProxyToCachePrivateTasks;
import epfl.sweng.query.Parenthesis;
import epfl.sweng.quizquestions.QuizQuestion;
import epfl.sweng.testing.TestCoordinator;
import epfl.sweng.testing.TestCoordinator.TTChecks;
import epfl.sweng.utils.JSONUtils;

/**
 * ProxyHttpClient is the proxy in the proxy design pattern. It decides whether
 * the request should be sent to the real subject of to the cache.
 * 
 * @author valentin
 * 
 */
public final class ProxyHttpClient implements IHttpClient {
	private boolean offline = false;
	private static ProxyHttpClient instance = null;
	private IProxyToCachePrivateTasks myProxyToCachePrivateTasks = null;

	private String tequilaWordWithSessionID = null;
	private IHttpClient realHttpClient = null;
	private ICheckBoxTask myCheckBoxTask = null;

	private int aSyncCounter = 0;

	/**
	 * Private constructor of the singleton.
	 * @throws CacheException 
	 */
	private ProxyHttpClient() throws CacheException {
		this.realHttpClient = RealHttpClient.getInstance();
		myProxyToCachePrivateTasks = Cache
				.getProxyToCachePrivateTasks(new InnerCacheToProxyPrivateTasks());
	}

	private void setProxyToCachePrivateTasks(
			IProxyToCachePrivateTasks myProxyToCachePrivateTasksS) {
		this.myProxyToCachePrivateTasks = myProxyToCachePrivateTasksS;
	}

	/**
	 * Retrieve the instance of the singleton.
	 * 
	 * @return the instance of the singleton.
	 * @throws CacheException 
	 */
	public static ProxyHttpClient getInstance() throws CacheException {
		if (instance == null) {
			instance = new ProxyHttpClient();
		}
		return instance;
	}
	
	/**
	 * Delete the instance (usefull for test)
	 */
	public static void deleteInstance() {
		instance = null;
	}

	/**
	 * Set boolean offline to true, only if it was false before, and calls the
	 * corresponding TTChecks.
	 */
	public void goOffLine() {
		if (!offline) {
			offline = true;
			// a verifier la necessite de cela:
			// myCheckBoxTask.confirmCheckBoxTask(offline);
			TestCoordinator.check(TTChecks.OFFLINE_CHECKBOX_ENABLED);
		}
	}

	/**
	 * Tries to go online.
	 * <p>
	 * If the cache is empty, goes immediately online. If the cache is not
	 * empty, tries to sync completely with the server, and if so goes online by
	 * calling the goOnlineDefinitely method.
	 * @throws CacheException 
	 */
	public void goOnline() throws CacheException {
		if (offline) {
			myProxyToCachePrivateTasks.sendOutBox();
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
	 * 
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
		if (authToken == null || !authToken.startsWith("Tequila ")) {
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
	 * @throws CacheException 
	 */
	@Override
	public HttpResponse execute(HttpUriRequest request) throws IOException,
			ClientProtocolException, CacheException {
		HttpResponse serverResponse = null;
		boolean previousOfflineStatus = getOfflineStatus();
		boolean onlineSuccesfulComm = false;
		boolean goOffline = false;
		if (!previousOfflineStatus) {
			Integer statusCode = -1;
			try {
				serverResponse = realHttpClient.execute(request);
				statusCode = serverResponse.getStatusLine().getStatusCode();
			} catch (ClientProtocolException e) {
				Logger.getLogger("epfl.sweng.patterns").log(Level.INFO,
						"executing request", e);
				goOffline = true;
			} catch (IOException e) {
				Logger.getLogger("epfl.sweng.patterns").log(Level.INFO,
						"executing request", e);
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
				return executeAtTheEnd(new BasicHttpResponse(
						new BasicStatusLine(new ProtocolVersion("HTTP", 2, 1),
								HttpStatus.SC_UNAUTHORIZED, "UNAUTHORIZED")),
						goOffline, onlineSuccesfulComm, serverResponse);
			} else {
				tequilaWordWithSessionID = headers[0].getValue();
				// extract and add to the cache
				String jsonContent = EntityUtils.toString(post.getEntity());
				QuizQuestion myQuizQuestion;
				try {
					// owner/id are needed to construct quizquestion and set as
					// default values
					myQuizQuestion = new QuizQuestion(jsonContent);
					if (previousOfflineStatus || !onlineSuccesfulComm) {
						// if we are offline we add it to ToSendBox
						// if we are online and there was an error
						myProxyToCachePrivateTasks
								.addQuestionToOutBox(myQuizQuestion);
					} else {
						myProxyToCachePrivateTasks
						.addQuestionToCache(myQuizQuestion);
					}
					// if proxy accepted the question, reply okay (201) and
					// return
					// the question as json to confirm
					return executeAtTheEnd(
							new BasicHttpResponse(new BasicStatusLine(
									new ProtocolVersion("HTTP", 2, 1),
									HttpStatus.SC_CREATED,
									myQuizQuestion.toPostEntity())), goOffline,
							onlineSuccesfulComm, serverResponse);
				} catch (JSONException e) {
					Logger.getLogger("epfl.sweng.patterns").log(Level.INFO,
							"httpClient fail", e);
					// if the question is malformed, we send a 500 error code
					return executeAtTheEnd(new BasicHttpResponse(
							new BasicStatusLine(new ProtocolVersion("HTTP", 2,
									1), HttpStatus.SC_INTERNAL_SERVER_ERROR,
									"INTERNAL SERVER ERROR")), goOffline,
							onlineSuccesfulComm, serverResponse);
				}
			}
		} else {
			// only post method is accepted here, so return Method Not Allowed
			// Error
			return executeAtTheEnd(new BasicHttpResponse(new BasicStatusLine(
					new ProtocolVersion("HTTP", 2, 1),
					HttpStatus.SC_METHOD_NOT_ALLOWED, "METHOD NOT ALLOWED")),
					goOffline, onlineSuccesfulComm, serverResponse);
		}
		// we dont care about the server response: only proxy response is
		// important for client
		// if (response != null && !previousOfflineStatus){
		// return response;
		// }
	}

	/**
	 * For modularity reason: confirm the question, go offline if request, and
	 * sent back the httpResponse.
	 * 
	 * @param proxyResponse
	 * @param goOffline
	 * @return
	 */
	private HttpResponse executeAtTheEnd(HttpResponse proxyResponse,
			boolean goOffline, boolean onlineSuccesfulComm,
			HttpResponse serverResponse) {
		TestCoordinator.check(TTChecks.NEW_QUESTION_SUBMITTED);
		if (goOffline) {
			offline = true;
		}
		if (onlineSuccesfulComm && serverResponse != null) {
			return serverResponse;
		}
		return proxyResponse;
	}

	/**
	 * Execute a request to fetch a question.
	 * <p>
	 * Redirect the call to the real subject or the cache, depending on the
	 * offline boolean.
	 * @throws CacheException 
	 */
	@Override
	public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1)
			throws IOException, ClientProtocolException, CacheException {
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
					} else if (!jsonQuestion.has("questions")) {
						// if the retrieved question is malformed or it's not
						// question: exception > offline
						QuizQuestion myQuizQuestion = new QuizQuestion(
								(String) t);
						// for the moment, we dont check if the id of the
						// question already exist in the list. Hint: SET
						myProxyToCachePrivateTasks
								.addQuestionToCache(myQuizQuestion);
						return t;
					} else if (jsonQuestion.has("questions")) {
						// add all the question to the cache
						ArrayList<String> arrayString = JSONUtils
								.convertJSONArrayToArrayListString(jsonQuestion
										.getJSONArray("questions"));

						for (String s : arrayString) {
							myProxyToCachePrivateTasks
									.addQuestionToCache(new QuizQuestion(s));
						}
						return t;
					}
				}
				// check the behavior of online unsucessful exception
			} catch (JSONException e) {
				Logger.getLogger("epfl.sweng.patterns").log(Level.INFO,
						"executing request", e);
				goOffLine();
			} catch (UnknownHostException e) {
				Logger.getLogger("epfl.sweng.patterns").log(Level.INFO,
						"executing request", e);
				goOffLine();
			}
		} else {
			// if offline we fetch the cache
			if (arg0.getURI().toString().contains("search")) {
				HttpPost post = (HttpPost) arg0;
				String jsonString = EntityUtils.toString(post.getEntity());
				try {
					JSONObject json = new JSONObject(jsonString);
					String query = json.getString("query");
					String next = "null";
					System.out.println("im here");
					try {
						next = json.getString("from");
					} catch (JSONException e) {
						Logger.getLogger("epfl.sweng.patterns").log(Level.INFO,
								"client fail", e);					}
					Log.e("test", query);
					String clearQuery = Parenthesis.parenthesis(query);
					ArrayList<String> listJSONQuestions = HandleOfflineQuery.getInstance()
							.query(clearQuery, next);

					String ret = "{\n \"questions\": ";
					ret += new JSONArray(listJSONQuestions).toString();
					String token = HandleOfflineQuery.getInstance().getPreviousToken();
					if (token == null) {
						token = "null";
					}
					ret += ", \n\"next\": \"" + token + "\"\n}";
					System.out.println(ret);
					return (T) ret;
				} catch (JSONException e) {
					Logger.getLogger("epfl.sweng.patterns").log(Level.INFO,
							"executing request", e);
				}
			} else {
				return (T) myProxyToCachePrivateTasks
						.getRandomQuestionFromCache();
			}
		}

		// if server disconnected, we return null;
		return null;
	}

	/**
	 * An async task can notify when it's done.
	 * @throws CacheException 
	 */
	private void aSyncCounter() throws CacheException {
		aSyncCounter--;
		System.out.println(aSyncCounter);
		if (aSyncCounter == 0) { // && toSendBox.size() == 0) {
			System.out.println("asny 0");
			goOnlineResponse(myProxyToCachePrivateTasks.getSentStatus());
		}

	}

	/**
	 * Send a question.
	 * 
	 * @param question
	 *            the question to be sent.
	 * @return the http status, -1 if fail to end the task.
	 */
	private int sendQuestion(QuizQuestion question) {
		SubmitQuestionTask mySubmitQuestionTask = new SubmitQuestionTask();
		mySubmitQuestionTask.execute(question);
		int httpStatus = -1;
		// try {
		// // /!\ bloquant, attend l'execution complète de l'asynctask
		// // attention, c'est peut etre faux!
		// httpStatus = mySubmitQuestionTask.get();
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// } catch (ExecutionException e) {
		// e.printStackTrace();
		// }
		return httpStatus;
	}

	/**
	 * A question to failBox.
	 * 
	 * @param myQuestion
	 */
	private void addToFailBox(QuizQuestion myQuestion) {
		myProxyToCachePrivateTasks.addToFailBox(myQuestion);
	}

	/**
	 * Set the async counter.
	 * 
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
			} catch (Exception e) {
				Logger.getLogger("epfl.sweng.patterns").log(Level.INFO,
						"submitting task", e);
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
				try {
					myProxyToCachePrivateTasks.addQuestionToCache(myQuestion);
				} catch (CacheException e) {
					Logger.getLogger("epfl.sweng.patterns").log(Level.INFO,
							"submitting task", e);
				}
			} else if (!getOfflineStatus()
					&& result.compareTo(Integer
							.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR)) >= 0) {
				addToFailBox(myQuestion);
				goOffLine();
				System.out.println("async fail + offline "
						+ myQuestion.toPostEntity());

			} else {
				addToFailBox(myQuestion);
				System.out.println("async fail " + myQuestion.toPostEntity());

			}
			
			try {
				aSyncCounter();
			} catch (CacheException e) {
				Logger.getLogger("epfl.sweng.patterns").log(Level.INFO,
						"submitting task", e);
			}
		}
	}

	/**
	 * Private class to interact from the cache to the proxy.
	 * <p>
	 * Created to ensure that nobody else than the cache calls these methods.
	 * 
	 * @author valentin
	 * 
	 */
	private class InnerCacheToProxyPrivateTasks implements
			ICacheToProxyPrivateTasks {

		@Override
		public void setProxyToCachePrivateTasks(
				IProxyToCachePrivateTasks myProxyToCachePrivateTasksS) {
			instance.setProxyToCachePrivateTasks(myProxyToCachePrivateTasksS);
		}

		@Override
		public void goOnlineResponse(boolean bool) {
			instance.goOnlineResponse(bool);
		}

		@Override
		public int sendQuestion(QuizQuestion question) {
			return instance.sendQuestion(question);
		}

		@Override
		public void setASyncCounter(int k) {
			instance.setASyncCounter(k);
		}
	}
}
