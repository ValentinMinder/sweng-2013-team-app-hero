package epfl.sweng.patterns;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.RequestLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.preference.PreferenceActivity.Header;

import epfl.sweng.quizquestions.QuizQuestion;
import epfl.sweng.servercomm.SwengHttpClientFactory;

public class ProxyHttpClient implements HttpClient {
	private static boolean offline = false;
	private ArrayList<QuizQuestion> cache;

	public ProxyHttpClient() {
		this.cache = new ArrayList();
	}

	/**
	 * Set offline parameter.
	 * 
	 * @param status
	 *            True represent offline.
	 */
	public static void setOfflineStatus(boolean status) {
		offline = status;
	}

	/**
	 * get offline parameter. True represent offline.
	 * 
	 * @return
	 */
	public static boolean getOfflineStatus() {
		return offline;
	}

	@Override
	public HttpResponse execute(HttpUriRequest request) throws IOException,
			ClientProtocolException {

		if (!offline) {
			return SwengHttpClientFactory.getInstance().execute(request);
		}
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
			String content = EntityUtils.toString(post.getEntity());
			//extraire et ajouter au cache
			System.out.println(content);
		}
		return null;
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
	public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1)
			throws IOException, ClientProtocolException {

		if (!offline) {
			return SwengHttpClientFactory.getInstance().execute(arg0, arg1);
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
}
