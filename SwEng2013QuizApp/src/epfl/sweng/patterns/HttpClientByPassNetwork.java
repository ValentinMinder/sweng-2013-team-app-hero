package epfl.sweng.patterns;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;

import epfl.sweng.servercomm.SwengHttpClientFactory;

public class HttpClientByPassNetwork implements IHttpClient {

	public HttpResponse execute(HttpUriRequest request) throws IOException,
	ClientProtocolException {
		return SwengHttpClientFactory.getInstance().execute(request);
	}
	
	public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1)
		throws IOException, ClientProtocolException {
		return SwengHttpClientFactory.getInstance().execute(arg0, arg1);
	}
}
