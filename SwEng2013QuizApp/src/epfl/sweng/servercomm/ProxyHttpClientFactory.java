package epfl.sweng.servercomm;

import org.apache.http.client.HttpClient;

import epfl.sweng.patterns.ProxyHttpClient;

/**
 * This factory creates HttpClients. It also allows to inject custom HttpClients
 * for testing.
 */
public class ProxyHttpClientFactory {
	private static HttpClient httpClient;
	
	public static synchronized HttpClient getInstance() {
		if (httpClient == null) {
			httpClient = new ProxyHttpClient();
		}

		return httpClient;
	}
}