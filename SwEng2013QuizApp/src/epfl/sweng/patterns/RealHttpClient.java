package epfl.sweng.patterns;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;

import epfl.sweng.servercomm.SwengHttpClientFactory;

/**
 * RealHttpClient represent the real subject in the proxy design pattern.
 * @author valentin
 *
 */
public final class RealHttpClient implements IHttpClient {
	
	private static RealHttpClient instance = null;
	private AbstractHttpClient swengServer = null;
	
	/**
	 * Private constructor of the singleton.
	 */
	private RealHttpClient() {
		swengServer = SwengHttpClientFactory.getInstance();
	}
	
	/**
	 * Retrieve the instance of the singleton.
	 * @return the instance of the singleton.
	 */
	public static synchronized RealHttpClient getInstance() {
		if (instance == null) {
			instance = new RealHttpClient();
		}
		return instance;
	}

	/**
	 * Execute a request to the SwEng server (submit).
	 */
	@Override
	public HttpResponse execute(HttpUriRequest request) throws IOException,
	ClientProtocolException {
		return swengServer.execute(request);
	}
	
	/**
	 * Execute a request to the SwEng server (fetch, and get the response).
	 */
	@Override
	public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1)
		throws IOException, ClientProtocolException {
		return swengServer.execute(arg0, arg1);
	}
}
