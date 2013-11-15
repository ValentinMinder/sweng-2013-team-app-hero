package epfl.sweng.patterns;


public class CheckProxyHelper implements ICheckProxyHelper {

	@Override
	public Class<?> getServerCommunicationClass() {
		return RealHttpClient.getInstance().getClass();

	}

	@Override
	public Class<?> getProxyClass() {
		return ProxyHttpClient.getInstance().getClass();
	}

}
