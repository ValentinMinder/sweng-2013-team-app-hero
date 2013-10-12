package epfl.sweng.authentication;

public class StoreCredential {
	private static StoreCredential instance = null;
	
	public static StoreCredential getInstance() {
		if (instance == null) {
			instance = new StoreCredential();
		} 
		return instance;
	}
}
