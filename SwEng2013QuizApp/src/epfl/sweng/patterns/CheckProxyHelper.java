package epfl.sweng.patterns;

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
		return ProxyHttpClient.getInstance().getClass();
	}

}
