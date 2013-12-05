package epfl.sweng.patterns;

import java.util.logging.Level;
import java.util.logging.Logger;

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
			Logger.getLogger("epfl.sweng.patterns").log(Level.INFO,
					"Proxy fail", e);
			return null;
		}
	}

}
