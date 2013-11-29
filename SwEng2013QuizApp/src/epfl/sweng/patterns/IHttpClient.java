package epfl.sweng.patterns;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;

import epfl.sweng.caching.CacheException;

public interface IHttpClient {
	HttpResponse execute(HttpUriRequest request) throws IOException,
			ClientProtocolException, CacheException;

	<T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1)
		throws IOException, ClientProtocolException, CacheException;
}
