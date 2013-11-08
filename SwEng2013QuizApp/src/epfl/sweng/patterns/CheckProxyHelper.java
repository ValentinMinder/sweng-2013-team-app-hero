package epfl.sweng.patterns;

import epfl.sweng.servercomm.SwengHttpClientFactory;

public class CheckProxyHelper implements ICheckProxyHelper {

	@Override
	public Class<?> getServerCommunicationClass() {
		// TODO Auto-generated method stub
		return SwengHttpClientFactory.getInstance().getClass();
	}

	@Override
	public Class<?> getProxyClass() {
		// TODO Auto-generated method stub
		return this.getClass();
	}

}
