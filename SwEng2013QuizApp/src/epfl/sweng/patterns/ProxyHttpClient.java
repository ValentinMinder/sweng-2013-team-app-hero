package epfl.sweng.patterns;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.widget.CheckBox;
import epfl.sweng.R;
import epfl.sweng.quizquestions.QuizQuestion;
import epfl.sweng.servercomm.SwengHttpClientFactory;
import epfl.sweng.testing.TestCoordinator;
import epfl.sweng.testing.TestCoordinator.TTChecks;

public final class ProxyHttpClient implements HttpClient {
	private static boolean offline = false;
	private static ProxyHttpClient instance = null;
	private ArrayList<QuizQuestion> cacheToSend;
	private ArrayList<QuizQuestion> cache;
	private String tequilaWordWithSessionID = null;

	private ProxyHttpClient() {
		this.cacheToSend = new ArrayList<QuizQuestion>();
		this.cache = new ArrayList<QuizQuestion>();

		// obviously this is for tests and we have to delete!
		// pour éviter le problème de cache vide et surtout faciliter les tests.
		/*
		 * ArrayList<String> answers = new ArrayList<String>();
		 * answers.add("delete me!");
		 * answers.add("delete me and shotgun the guy who wrote this shit");
		 * HashSet<String> tags = new HashSet<String>();
		 * tags.add("test only. TODO = delete"); cache.add(new
		 * QuizQuestion("This is the not empty cache...", answers, 1, tags, 0,
		 * "valou"));
		 */
	}

	public static ProxyHttpClient getInstance() {
		if (instance == null) {
			instance = new ProxyHttpClient();
		}
		return instance;
	}

	private void sendCacheContent() {
		if (cacheToSend.size() == 0) {
			TestCoordinator.check(TTChecks.OFFLINE_CHECKBOX_DISABLED);
			offline = false;
		}

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
		// Car utilisé dans submitQuestion l'application passe 
		// en mode offline que si un problème donc je pense pas 
		// que ça pose pas de soucis (et d'après le sujet il faut
		// rappeller le TestCoordinator si une erreur intervient
		// même si on était encore hors ligne car on attendait 
		// d'avoir envoyé tout le cache pour passer en "online"
		//if (!previousState && offline) {
		if (offline) {
			// going from online to offline
			TestCoordinator.check(TTChecks.OFFLINE_CHECKBOX_ENABLED);
		}

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
		 * c'est volontiers ! (je suis p-e a coté de la plaque) Valou > Julien:
		 * est-ce que c'est regle now? Moi la recup de la question me semble
		 * okay, c'est sur les return (n° http/message) et l'authentification
		 * que je dois encore travailler.
		 */
		String method = request.getMethod();
		if (method.equals("POST")) {
			HttpPost post = (HttpPost) request;
			Header[] headers = post.getHeaders("Authorization");
			if (headers.length >= 1) {
				tequilaWordWithSessionID = headers[0].getValue();
			}
			// TODO: check authenfication dans le proxy (moyen a preciser...
			// Valou pas tres sur comment faire )
			boolean authentificationValidated = true;
			if (!authentificationValidated) {
				return new BasicHttpResponse(new BasicStatusLine(
						new ProtocolVersion("HTTP", 2, 1),
						HttpStatus.SC_UNAUTHORIZED, "UNAUTHORIZED"));
			}
			String jsonContent = EntityUtils.toString(post.getEntity());
			// extract and add to the cache
			QuizQuestion question;
			try {
				// owner/id are needed to construct quizquestion and set as
				// default values
				question = new QuizQuestion(jsonContent);
				cacheToSend.add(question);
				cache.add(question);
				System.out.println("offline status" + offline);
				System.out.println("Recieved" + question);
				if (!offline) {
					sendCacheContent();
				}
				// if proxy accepted the question, reply okay (201) and return
				// the question as json to confirm
				return new BasicHttpResponse(new BasicStatusLine(
						new ProtocolVersion("HTTP", 2, 1),
						HttpStatus.SC_CREATED, question.toPostEntity()));
			} catch (JSONException e) {
				// if the question is malformed, we send a 500 error code
				return new BasicHttpResponse(new BasicStatusLine(
						new ProtocolVersion("HTTP", 2, 1),
						HttpStatus.SC_INTERNAL_SERVER_ERROR,
						"INTERNAL SERVER ERROR"));
			}
		}
		// only post method is accepted here, so return Method Not Allowed Error
		return new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion(
				"HTTP", 2, 1), HttpStatus.SC_METHOD_NOT_ALLOWED,
				"METHOD NOT ALLOWED"));
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

		// Valou method (works, but bypassing the async task)
		// this execute is called from an async task in showquestions
		// are we allowed to fetch DIRECTLY from server?
		if (!offline) {
			try {
				T t = SwengHttpClientFactory.getInstance().execute(arg0, arg1);
				if (t == null || t.getClass() != String.class) {
					setOfflineStatus(false);
				} else {
					JSONObject jsonQuestion = new JSONObject((String) t);
					if (jsonQuestion.has("message")) {
						setOfflineStatus(false);
					} else {
						// si la question est mal formee ou que c'est pas un
						// questions > exception > offline
						QuizQuestion q = new QuizQuestion((String) t);
						// //TODO Verifier si l'id de la question existe déjà
						// dans la liste ?
						cache.add(q);
						return t;
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
				setOfflineStatus(false);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				setOfflineStatus(false);
			}
		}

		// offline on cherche dans le cache
		if (!cache.isEmpty()) {
			int size = cache.size();
			// Valou > Julien... faut pas faire -1!!!
			int index = (int) (Math.random() * size);
			return (T) cache.get(index).toPostEntity();
		}
		// offline et cache vide
		return null;
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
				HttpResponse response = SwengHttpClientFactory.getInstance()
						.execute(post);

				Integer statusCode = response.getStatusLine().getStatusCode();
				
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
			// Valou: en cas d'exeption en local, on lance la failure
			return HttpStatus.SC_INTERNAL_SERVER_ERROR;
		}

		/**
		 * result is the http status code given by server.
		 */
		@Override
		protected void onPostExecute(Integer result) {
			if (result.compareTo(Integer.valueOf(HttpStatus.SC_CREATED)) == 0) {
				cacheToSend.remove(myQuestion);
				if (cacheToSend.size() == 0) {
					offline = false;
					TestCoordinator.check(TTChecks.OFFLINE_CHECKBOX_DISABLED);
				}
			} else {
				setOfflineStatus(true);
			}
		}

	}
}
