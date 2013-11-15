package epfl.sweng.patterns;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;

import epfl.sweng.servercomm.SwengHttpClientFactory;

public final class RealHttpClient implements IHttpClient {
	
	private static RealHttpClient instance = null;
	private AbstractHttpClient swengServer = null;
	
	private RealHttpClient() {
		swengServer = SwengHttpClientFactory.getInstance();
	}
	
	public static synchronized RealHttpClient getInstance() {
		if (instance == null) {
			instance = new RealHttpClient();
		}
		return instance;
	}

	public HttpResponse execute(HttpUriRequest request) throws IOException,
	ClientProtocolException {
		return swengServer.execute(request);
	}
	
	public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1)
		throws IOException, ClientProtocolException {
		return swengServer.execute(arg0, arg1);
	}
}
