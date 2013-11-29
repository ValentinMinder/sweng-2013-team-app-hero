package epfl.sweng.patterns;

import epfl.sweng.caching.CacheException;

public class CheckProxyHelper implements ICheckProxyHelper {

	/**
	 * Get the real subject class in proxy design pattern.
	 */
	@Override
	public Class<?> getServerCommunicationClass() {
		return RealHttpClient.getInstance().getClass();

	}

	/**
	 * Get the proxy class in proxy design pattern.
	 */
	@Override
	public Class<?> getProxyClass() {
		try {
			return ProxyHttpClient.getInstance().getClass();
		} catch (CacheException e) {
			return null;
		}
	}

}
