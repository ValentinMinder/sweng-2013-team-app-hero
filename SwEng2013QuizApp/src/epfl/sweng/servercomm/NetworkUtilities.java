package epfl.sweng.servercomm;

import android.os.AsyncTask;

public class NetworkUtilities {

	// TODO . tyring to factorize this
	public void executeNetworkASzncTask(String execution, AsyncTask<String, Void, String> aSync) {
//		ConnectivityManager connMgr = (ConnectivityManager) android.app.Activity.getSystemService(Context.CONNECTIVITY_SERVICE);
//		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//
//		// Test network connection
//		if (networkInfo != null && networkInfo.isConnected()) {
//			try {
//				aSync.execute(execution).get();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			} catch (ExecutionException e) {
//				e.printStackTrace();
//			}
//		} else {
//			Toast.makeText(getBaseContext(), "No network connection available",
//					Toast.LENGTH_LONG).show();
//		}
	}
}
