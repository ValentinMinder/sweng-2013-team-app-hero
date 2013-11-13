package epfl.sweng.patterns;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;

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
import epfl.sweng.quizquestions.QuizQuestion;
import epfl.sweng.servercomm.SwengHttpClientFactory;

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
		ArrayList<String> answers = new ArrayList<String>();
		answers.add("delete me!");		
		answers.add("delete me and shotgun the guy who wrote this shit");
		HashSet<String> tags = new HashSet<String>();
		tags.add("test only. TODO = delete");
		cache.add(new QuizQuestion("This is the not empty cache...", answers, 1, tags, 0, "valou"));
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
		 * Valou > Julien: est-ce que c'est regle now? Moi la recup de la question me semble okay,
		 * c'est sur les return (n° http/message) et l'authentification que je dois encore travailler.
		 */
		String method = request.getMethod();
		if (method.equals("POST")) {
			HttpPost post = (HttpPost) request;
			Header[] headers = post.getHeaders("Authorization");
			if (headers.length >= 1) {
				tequilaWordWithSessionID = headers[0].getValue();
			}
			// TODO: check authenfication dans le proxy (moyen a preciser... Valou pas tres sur comment faire )
			boolean authentificationValidated = true;
			if (!authentificationValidated) {
				return new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion(
						"HTTP", 2, 1), HttpStatus.SC_UNAUTHORIZED,
						"UNAUTHORIZED"));
			}
			String jsonContent = EntityUtils.toString(post.getEntity());
			// extract and add to the cache
			QuizQuestion question;
			try {
				// owner/id are needed to construct quizquestion and set as default values
				question = new QuizQuestion(jsonContent);
				cacheToSend.add(question);
				cache.add(question);
				System.out.println("offline status" + offline);
				System.out.println("Recieved" + question);
				if (!offline) {
					sendCacheContent();
				}
				// if proxy accepted the question, reply okay (201) and return the question as json to confirm
				return new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion(
						"HTTP", 2, 1), HttpStatus.SC_CREATED,
						question.toPostEntity()));
			} catch (JSONException e) {
				// if the question is malformed, we send a 500 error code
				return new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion(
						"HTTP", 2, 1), HttpStatus.SC_INTERNAL_SERVER_ERROR,
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
		
		//  Valou method (works, but bypassing the async task)
		// this execute is called from an async task in showquestions
		// are we allowed to fetch DIRECTLY from server?
		if (!offline) {
			T t = SwengHttpClientFactory.getInstance().execute(arg0, arg1);
			try {
				if (t == null || t.getClass() != String.class) {
					setOfflineStatus(false);
				} else {
					JSONObject jsonQuestion = new JSONObject((String) t);
					if (jsonQuestion.has("message")) {
						setOfflineStatus(false);
					} else {
						// si la question est mal formee ou que c'est pas un questions > exception > offline
						QuizQuestion q = new QuizQuestion((String) t);
//						//TODO Verifier si l'id de la question existe déjà dans la liste ?
						cache.add(q);
						return t;
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
				setOfflineStatus(false);
			}
		}
		
		//  previous: Antoine version (should be okay, but doesn't work)
//		ResponseHandler<String> rh = (ResponseHandler<String>) arg1;
//        if (!offline) {
//                String response;
//                try {
//                        //TODO a tester pas sur que ça soit fonctionnel
//                        response = new GetQuestionTask().execute("https://sweng-quiz.appspot.com/quizquestions/random").get();
//                        if (!offline) {
//                                QuizQuestion question;
//                                question = new QuizQuestion(response);
//                                //TODO Verifier si l'id de la question existe déjà dans la liste ?
//                                // warning: c'est le serveur qui donne les id, pas le proxy... bien gerer ce souci.
//                                cache.add(question);
//                                return (T) response;
//                        }
//                } catch (JSONException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                } catch (InterruptedException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                } catch (ExecutionException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                }
//        }
		
		// offline on cherche dans le cache
		if (!cache.isEmpty()) {
			int size = cache.size();
			// Valou > Julien... faut pas faire -1!!!
			int index = (int) (Math.random()*(size));
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
			post.setHeader("Authorization",	tequilaWordWithSessionID);

			try {
				myQuestion = questionElement[0];
				post.setEntity(new StringEntity(myQuestion.toPostEntity()));
				HttpResponse response = SwengHttpClientFactory.getInstance()
						.execute(post);

				Integer statusCode = response.getStatusLine().getStatusCode();
//				if (statusCode.compareTo(Integer
//						.valueOf(SWENG_QUIZ_APP_SUBMIT_QUESTION_SUCCESS)) == 0) {
//					cacheToSend.remove(myQuestion);
//				} else {
//					offline = true;
//				}
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
			// Valou: je prefere check le status et enlever la question APRES avoir submit, 
			// mais peut-etre que ca fait de la merde au niveau des threads...
			if (result.compareTo(Integer
					.valueOf(HttpStatus.SC_CREATED)) == 0) {
				cacheToSend.remove(myQuestion);
			} else {
				offline = true;
			}
		}

	}
	
	
//	/**
//	 * Class who is use to get the question from the server
//	 * ONLY USED BY ANTOINE'S METHOD
//	 * @author juniors
//	 * 
//	 */
//	private class GetQuestionTask extends AsyncTask<String, Void, String> {
//
//		@Override
//		protected String doInBackground(String... urls) {
//			//TODO faire attention car sessionID == null non traité (à corriger)
//			HttpGet firstRandom = new HttpGet(urls[0]);
//			//TODO restablish this
//			// firstRandom.setHeader("Authorization", tequilaWordWithSessionID);
//
//			ResponseHandler<String> firstHandler = new BasicResponseHandler();
//			try {
//				return SwengHttpClientFactory.getInstance().execute(
//						firstRandom, firstHandler);
//			} catch (ClientProtocolException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			// check le n° 200 en retour
//			//Valou: qqch a changer ici.
//			return "";
//		}
//
//		/**
//		 * Method who is gonna take the result of an URL request and parse it in
//		 * a QuizQuestion Object. and after display it.
//		 */
//		protected void onPostExecute(String result) {
//			try {
//				if (result == null) {
//					setOfflineStatus(false);
//				} else {
//					JSONObject jsonQuestion = new JSONObject(result);
//					// TODO: Valou: plutot que check le message... check le n° http retourné
//					// (je sais, c'est un ctrl-c-v de ce que j'avais fait avant... mais c'est pas top je troube
//					if (jsonQuestion.has("message")) {
//						setOfflineStatus(false);
//					} else {
//						super.onPostExecute(result);
//					}
//				}
//			} catch (JSONException e) {
//				e.printStackTrace();
//				setOfflineStatus(false);
//			}
//		}
//
//	}
}
