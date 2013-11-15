package epfl.sweng.patterns;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;

public class CacheHttpClient implements IHttpClient {
	
	private static CacheHttpClient instance = null;
	
	private CacheHttpClient () {
	}
	
	public static synchronized CacheHttpClient getInstance (){
		if (instance == null){
			instance = new CacheHttpClient();
		}
		return instance;
	}


	@Override
	public HttpResponse execute(HttpUriRequest request) throws IOException,
			ClientProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1)
			throws IOException, ClientProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

}
