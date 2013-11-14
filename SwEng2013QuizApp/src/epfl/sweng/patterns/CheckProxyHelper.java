package epfl.sweng.patterns;

import epfl.sweng.servercomm.ProxyHttpClientFactory;

public class CheckProxyHelper implements ICheckProxyHelper {

	@Override
	public Class<?> getServerCommunicationClass() {
//		return SwengHttpClientFactory.getInstance().getClass();
		return (new HttpClientByPassNetwork()).getClass();

	}

	@Override
	public Class<?> getProxyClass() {
		return ProxyHttpClientFactory.getInstance().getClass();
	}

}
