package epfl.sweng.patterns;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import epfl.sweng.quizquestions.QuizQuestion;
import epfl.sweng.servercomm.ProxyHttpClientFactory;
import epfl.sweng.servercomm.SwengHttpClientFactory;

public final class ProxyHttpClient implements HttpClient {
	private static final int SWENG_QUIZ_APP_SUBMIT_QUESTION_FAILURE = 500;
	private static final int SWENG_QUIZ_APP_SUBMIT_QUESTION_SUCCESS = 201;
	private static boolean offline = false;
	private static ProxyHttpClient instance = null;
	private ArrayList<QuizQuestion> cacheToSend;
	private ArrayList<QuizQuestion> cache;
	private String sessionID = null;

	private ProxyHttpClient() {
		this.cacheToSend = new ArrayList<QuizQuestion>();
		this.cache = new ArrayList<QuizQuestion>();
	}

	public static ProxyHttpClient getInstance() {
		if (instance == null) {
			instance = new ProxyHttpClient();
		}
		return instance;
	}

	private void sendCacheContent() {
		for (int i = 0; i < cacheToSend.size(); ++i) {
			QuizQuestion question = cacheToSend.get(i);
			new SubmitQuestionTask().execute(question);
		}
	}

	/**
	 * Set offline parameter.
	 * 
	 * @param status
	 *            True represent offline.
	 */
	public void setOfflineStatus(boolean status) {
		boolean previousState = offline;
		offline = status;
		if (previousState && !offline) {
			// going from offline to online
			sendCacheContent();
		}
	}

	/**
	 * get offline parameter. True represent offline.
	 * 
	 * @return
	 */
	public boolean getOfflineStatus() {
		return offline;
	}

	@Override
	public HttpResponse execute(HttpUriRequest request) throws IOException,
			ClientProtocolException {

		// TODO Traiter le cas offline
		/*
		 * Julien : Je ne sais pas vraiment comment faire pour récupérer le
		 * contenu du post pour pouvoir extraire la quizquestion et donc
		 * l'ajouter au cache (ArrayList) Si quelqu'un a une idée plus clean
		 * c'est volontiers ! (je suis p-e a coté de la plaque)
		 */
		String method = request.getMethod();
		if (method.equals("POST")) {
			HttpPost post = (HttpPost) request;
			Header[] headers = post.getHeaders("Authorization");
			if (headers.length >= 1) {
				sessionID = headers[0].getValue();
			}
			// TODO: check authenfication dans le proxy (moyen a preciser... pas tres sur comment faire le valou)
			boolean authentificationValidated = true;
			if (!authentificationValidated) {
				//TODO: change n° retrun (301) + exact return message
				return new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion(
						"HTTP", 2, 1), SWENG_QUIZ_APP_SUBMIT_QUESTION_FAILURE,
						"Non-authorized"));
			}
			String jsonContent = EntityUtils.toString(post.getEntity());
			// extract and add to the cache
			QuizQuestion question;
			try {
				//TODO: check owner/id (empty sended by client, but needed to construct quizquestion)
				question = new QuizQuestion(jsonContent);
				cacheToSend.add(question);
				cache.add(question);
				
				if (!offline) {
					sendCacheContent();
				}
				// if proxy accepted the question, reply okay (201) and return the question as json to confirm
				// TODO: check owner/id...
				return new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion(
						"HTTP", 2, 1), SWENG_QUIZ_APP_SUBMIT_QUESTION_SUCCESS,
						question.toPostEntity()));
			} catch (JSONException e) {
				return new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion(
						"HTTP", 2, 1), SWENG_QUIZ_APP_SUBMIT_QUESTION_FAILURE,
						"Message: malformed question"));
			}
		}
		// TODO return BAD METHOD ONLY POST ACCEPTED ( +check error n°)
		return new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion(
				"HTTP", 2, 1), SWENG_QUIZ_APP_SUBMIT_QUESTION_FAILURE,
				"error message to check"));
	}

	@Override
	public HttpResponse execute(HttpUriRequest request, HttpContext context)
			throws IOException, ClientProtocolException {
		if (!offline) {
			return SwengHttpClientFactory.getInstance().execute(request,
					context);
		}
		// Traiter le cas offline
		return null;
	}

	@Override
	public HttpResponse execute(HttpHost target, HttpRequest request)
			throws IOException, ClientProtocolException {
		if (!offline) {
			return SwengHttpClientFactory.getInstance()
					.execute(target, request);
		}
		// Traiter le cas offline
		return null;
	}

	@Override
	public HttpResponse execute(HttpHost target, HttpRequest request,
			HttpContext context) throws IOException, ClientProtocolException {
		if (!offline) {
			return SwengHttpClientFactory.getInstance().execute(target,
					request, context);
		}
		// Traiter le cas offline
		return null;
	}

	@Override
	public <T> T execute(HttpUriRequest arg0,
			ResponseHandler<? extends T> arg1, HttpContext arg2)
			throws IOException, ClientProtocolException {
		if (!offline) {
			return SwengHttpClientFactory.getInstance().execute(arg0, arg1,
					arg2);
		}
		// Traiter le cas offline
		return null;
	}
	
	@Override
	public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1)
			throws IOException, ClientProtocolException {
		ResponseHandler<String> rh = (ResponseHandler<String>) arg1;
		if (!offline) {
			String response;
			try {
				//TODO a tester pas sur que ça soit fonctionnel
				response = new GetQuestionTask().execute("https://sweng-quiz.appspot.com/quizquestions/random").get();
				if (!offline) {
					QuizQuestion question;
					question = new QuizQuestion(response);
					//TODO Verifier si l'id de la question existe déjà dans la liste ?
					cache.add(question);
					return (T) response;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		int size = cache.size();
		int index = (int) (Math.random()*(size-1));
		return (T) cache.get(index).toPostEntity();
	}

	@Override
	public <T> T execute(HttpHost arg0, HttpRequest arg1,
			ResponseHandler<? extends T> arg2) throws IOException,
			ClientProtocolException {
		if (!offline) {
			return SwengHttpClientFactory.getInstance().execute(arg0, arg1,
					arg2);
		}
		// Traiter le cas offline
		return null;
	}

	@Override
	public <T> T execute(HttpHost arg0, HttpRequest arg1,
			ResponseHandler<? extends T> arg2, HttpContext arg3)
			throws IOException, ClientProtocolException {
		if (!offline) {
			return SwengHttpClientFactory.getInstance().execute(arg0, arg1,
					arg2, arg3);
		}
		// Traiter le cas offline
		return null;
	}

	@Override
	public ClientConnectionManager getConnectionManager() {
		if (!offline) {
			return SwengHttpClientFactory.getInstance().getConnectionManager();
		}
		// Traiter le cas offline
		return null;
	}

	@Override
	public HttpParams getParams() {
		if (!offline) {
			return SwengHttpClientFactory.getInstance().getParams();
		}
		// Traiter le cas offline
		return null;
	}

	/**
	 * 
	 * Task to submit a question
	 * 
	 * @author Julien
	 * 
	 */
	private class SubmitQuestionTask extends
			AsyncTask<QuizQuestion, Void, Integer> {

		/**
		 * Execute and retrieve the answer from the website.
		 */
		@Override
		protected Integer doInBackground(QuizQuestion... questionElement) {
			String serverURL = "https://sweng-quiz.appspot.com/";
			HttpPost post = new HttpPost(serverURL + "quizquestions/");
			post.setHeader("Content-type", "application/json");
			post.setHeader("Authorization", sessionID);

			try {
				post.setEntity(new StringEntity(questionElement[0]
						.toPostEntity()));

				HttpResponse response = ProxyHttpClientFactory.getInstance()
						.execute(post);

				Integer statusCode = response.getStatusLine().getStatusCode();
				if (statusCode.compareTo(Integer
						.valueOf(SWENG_QUIZ_APP_SUBMIT_QUESTION_FAILURE)) == 0) {
					offline = true;
				} else {
					cacheToSend.remove(questionElement[0]);
				}
				return statusCode;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (HttpResponseException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return 0;
		}

		protected void onPostExecute(Integer result) {

		}

	}
	
	
	/**
	 * Class who is use to get the question from the server
	 * 
	 * @author juniors
	 * 
	 */
	private class GetQuestionTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			//TODO faire attention car sessionID == null non traité (à corriger)
			HttpGet firstRandom = new HttpGet(urls[0]);
			firstRandom.setHeader(
					"Authorization",
					"Tequila "
							+ sessionID);
			ResponseHandler<String> firstHandler = new BasicResponseHandler();
			try {
				return ProxyHttpClientFactory.getInstance().execute(
						firstRandom, firstHandler);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}

		/**
		 * Method who is gonna take the result of an URL request and parse it in
		 * a QuizQuestion Object. and after display it.
		 */
		protected void onPostExecute(String result) {
			try {
				if (result == null) {
					setOfflineStatus(false);
				} else {
					JSONObject jsonQuestion = new JSONObject(result);
					if (jsonQuestion.has("message")) {
						setOfflineStatus(false);
					} else {
						super.onPostExecute(result);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
				setOfflineStatus(false);
			}
		}

	}
}
