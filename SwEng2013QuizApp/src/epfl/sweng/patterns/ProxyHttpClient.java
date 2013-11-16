package epfl.sweng.patterns;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

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
	private CacheHttpClient cacheHttpClient = null;
	private ICheckBoxTask myCheckBoxTask = null;

	/**
	 * Private constructor of the singleton.
	 */
	private ProxyHttpClient() {
		this.realHttpClient = RealHttpClient.getInstance();
		this.cacheHttpClient = CacheHttpClient
				.getInstance(this, realHttpClient);
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
			myCheckBoxTask.confirmCheckBoxTask(offline);
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
			cacheHttpClient.sendToSendBox();
		}
	}

	/**
	 * Set boolean offline to false, only if it was true before, and calls the
	 * corresponding TTChecks.
	 * <p>
	 * WARNING: DON'T CALL THIS! CALL GO ONLINE WHICH HANDLE ALL!
	 */
	protected void goOnlineResponse(boolean bool) {
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
	
	public void setCheckBoxTask(ICheckBoxTask myCheckBoxTaskT) {
		this.myCheckBoxTask = myCheckBoxTaskT;		
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
		// we go to the cache, which decides to send the question to the real subject
		return cacheHttpClient.execute(request);
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
						cacheHttpClient.addQuestionToCache(myQuizQuestion);
						return t;
					}
				}
				// check the behavior of online unsucessful exception
			} catch (JSONException e) {
				e.printStackTrace();
				goOffLine();
			} catch (UnknownHostException e) {
				e.printStackTrace();
				goOffLine();
			}
		} else {
			// if offline we fetch the cache
			return cacheHttpClient.execute(arg0, arg1);
		}

		// if server disconnected, we return null;
		return null;
	}
}
