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
 * ProxyHttpClient is the proxy in the proxy design pattern.
 * It decides whether the request should be sent to the real subject of to the cache.
 * @author valentin
 *
 */
public final class ProxyHttpClient implements IHttpClient {
	private static boolean offline = false;
	private static ProxyHttpClient instance = null;

	private String tequilaWordWithSessionID = null;
	private IHttpClient realHttpClient = null;
	private CacheHttpClient cacheHttpClient = null;

	/**
	 * Private constructor of the singleton.
	 */
	private ProxyHttpClient() {
		this.realHttpClient = RealHttpClient.getInstance();
		this.cacheHttpClient = CacheHttpClient.getInstance(this, realHttpClient);
	}

	/**
	 * Retrieve the instance of the singleton.
	 * @return the instance of the singleton.
	 */
	public static ProxyHttpClient getInstance() {
		if (instance == null) {
			instance = new ProxyHttpClient();
		}
		return instance;
	}
	
	/**
	 * Set boolean offline to true, only if it was false before,
	 * and calls the corresponding TTChecks.
	 */
	public void goOffLine() {
		if (!offline){
			offline = true;
			TestCoordinator.check(TTChecks.OFFLINE_CHECKBOX_ENABLED);
		}
	}
	
	/**
	 * Tries to go online. 
	 * <p>
	 * If the cache is empty, goes immediately online.
	 * If the cache is not empty, tries to sync completely with the server, 
	 * and if so goes online by calling the goOnlineDefinitely method.
	 */
	public void goOnline(){
		if (offline) {
			cacheHttpClient.sendTemporaryCache();
		}
	}
	
	/**
	 * Set boolean offline to false, only if it was true before, 
	 * and calls the corresponding TTChecks.
	 * <p>
	 * WARNING: DON'T CALL THIS! CALL GO ONLINE WHICH HANDLE ALL!
	 */
	protected void goOnlineDefinitely() {
		if (offline) {
			offline = false;
			TestCoordinator.check(TTChecks.OFFLINE_CHECKBOX_DISABLED);
		}
	}

	/**
	 * Get offline boolean parameter.  
	 * @return true represent offline.
	 */
	public boolean getOfflineStatus() {
		return offline;
	}
	
	/**
	 * Basic method that check if the sessionID is consistent.
	 * @param authToken session id to test.
	 * @return true if it's consistent.
	 */
	private boolean checkBasicAuthentificationSpecification(String authToken) {
		if (authToken == null) {
			return false;
		}
		if (!authToken.startsWith("Tequila ")) {
			return false;
		}
		int specifiedLength = "Tequila dvoon4y2wp1r2biq052dppkxghyrob14".length();
		if (authToken.length() != specifiedLength) {
			return false;
		}
//		String token = authToken.substring("tequila ".length()+1);
//		if (!token.matches("a-z0-9")){
//			return false;
//		}
		return true;
	}
	
	/**
	 * Get the tequila header.
	 * @return the tequila header.
	 */
	public String getTequilaWordWithSessionID() {
		return tequilaWordWithSessionID;
	}

	@Override
	public HttpResponse execute(HttpUriRequest request) throws IOException,
			ClientProtocolException {

		String method = request.getMethod();
		if (method.equals("POST")) {
			HttpPost post = (HttpPost) request;
			// checks that the auth is consistent.
			Header[] headers = post.getHeaders("Authorization");
			if (headers.length != 1 || !checkBasicAuthentificationSpecification(headers[0].getValue())) {
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
					cacheHttpClient.addQuestionToCache(myQuizQuestion);
					cacheHttpClient.addQuestionToTemporaryCache(myQuizQuestion);
					if (!offline) {
						cacheHttpClient.sendTemporaryCache();
					}
					// if proxy accepted the question, reply okay (201) and return
					// the question as json to confirm
					return new BasicHttpResponse(new BasicStatusLine(
							new ProtocolVersion("HTTP", 2, 1),
							HttpStatus.SC_CREATED, myQuizQuestion.toPostEntity()));
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
						// si la question est mal formee ou que c'est pas un
						// questions > exception > offline
						QuizQuestion myQuizQuestion = new QuizQuestion((String) t);
						// pour l'instant, on ne verifie pas si l'id de la question 
						// existe déjà dans la liste. Utiliser Set pour cela.
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
		}

		// if offline, or server disconnected, we fetch the cache
		return cacheHttpClient.execute(arg0, arg1);
	}
}
