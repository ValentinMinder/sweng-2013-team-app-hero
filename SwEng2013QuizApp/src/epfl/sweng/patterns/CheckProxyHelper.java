package epfl.sweng.patterns;

import epfl.sweng.servercomm.ProxyHttpClientFactory;
import epfl.sweng.servercomm.SwengHttpClientFactory;

public class CheckProxyHelper implements ICheckProxyHelper {

	@Override
	public Class<?> getServerCommunicationClass() {
		return SwengHttpClientFactory.getInstance().getClass();
	}

	@Override
	public Class<?> getProxyClass() {
		return ProxyHttpClientFactory.getInstance().getClass();
	}

}
