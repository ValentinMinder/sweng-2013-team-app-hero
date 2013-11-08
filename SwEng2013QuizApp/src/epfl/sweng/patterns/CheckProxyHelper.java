package epfl.sweng.patterns;

public class CheckProxyHelper implements ICheckProxyHelper {

	@Override
	public Class<?> getServerCommunicationClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getProxyClass() {
		// TODO Auto-generated method stub
		return this.getClass();
	}

}
