package epfl.sweng.entry;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import epfl.sweng.R;
import epfl.sweng.authentication.AuthenticationActivity;
import epfl.sweng.authentication.StoreCredential;
import epfl.sweng.authentication.AuthenticationActivity.GetAuthenticationTokenTask;
import epfl.sweng.caching.Cache;
import epfl.sweng.caching.CacheException;
import epfl.sweng.editquestions.EditQuestionActivity;
import epfl.sweng.patterns.ICheckBoxTask;
import epfl.sweng.patterns.ProxyHttpClient;
import epfl.sweng.searchquestions.SearchActivity;
import epfl.sweng.showquestions.ShowQuestionsActivity;
import epfl.sweng.testing.TestCoordinator;
import epfl.sweng.testing.TestCoordinator.TTChecks;

/**
 * 
 * Main activity of our application
 * 
 * @author AppHero
 * 
 */
public class MainActivity extends Activity {
	private ICheckBoxTask myCheckBoxTask = null;
	private boolean checkBoxInUse = false;

	/**
	 * Method who is called for modify each buttons when the user isn't
	 * authenticated
	 */
	private void modifyButtonIfNotAuthenticated() {
		Button button1 = (Button) findViewById(R.id.button1);
		button1.setEnabled(false);

		Button button2 = (Button) findViewById(R.id.button2);
		button2.setEnabled(false);

		Button button3 = (Button) findViewById(R.id.button3);
		button3.setEnabled(false);

		Button buttonLog = (Button) findViewById(R.id.button_log);
		buttonLog.setText(R.string.log_in_tekila);

		CheckBox offline = (CheckBox) findViewById(R.id.offline);
		offline.setVisibility(View.GONE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Cache.setDirectoryFiles(getApplicationContext().getFilesDir()
				.getAbsolutePath());
		String session = StoreCredential.getInstance().getSessionId(
				getApplicationContext());

		CheckBox offline = (CheckBox) findViewById(R.id.offline);

		if (session.equals("")) {
			modifyButtonIfNotAuthenticated();
		} else {
			offline.setVisibility(View.VISIBLE);
			try {
				offline.setChecked(ProxyHttpClient.getInstance().getOfflineStatus());
			} catch (CacheException e) {
				Logger.getLogger("epfl.sweng.entry").log(Level.INFO,
						"Creating main activity", e);
			}
		}

		myCheckBoxTask = new CheckBoxTask();
		try {
			ProxyHttpClient.getInstance().setCheckBoxTask(myCheckBoxTask);
		} catch (CacheException e) {
			Logger.getLogger("epfl.sweng.entry").log(Level.INFO,
					"Creating main activity, setting checkboxtask", e);
		}

		offline.setOnClickListener(new CompoundButton.OnClickListener() {
			@Override
			public void onClick(View buttonView) {
				// we save the next state, and restablish the previous one
				CheckBox check = (CheckBox) findViewById(R.id.offline);
				boolean isNextCheck = check.isChecked();
				check.setChecked(!isNextCheck);
				// we lock the use of the checkbox (NOT USED ANYMORE)
				if (!isNextCheck) {
					ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

					// Test network connection
					if (networkInfo != null && networkInfo.isConnected()) {
						System.out.println("trying to goonline");
						try {
							ProxyHttpClient.getInstance().goOnline();
						} catch (CacheException e) {
							Logger.getLogger("epfl.sweng.entry").log(Level.INFO,
									"Creating main activity, in the offline listener", e);
						}
						System.out.println("going online submitted");
					} else {
						Toast.makeText(getBaseContext(), R.string.no_network,
								Toast.LENGTH_LONG).show();
					}
				} else {
					try {
						ProxyHttpClient.getInstance().goOffLine();
					} catch (CacheException e) {
						Logger.getLogger("epfl.sweng.entry").log(Level.INFO,
								"Creating main activity, in the offline listener", e);
					}
					check.setChecked(isNextCheck);
				}
				// if (!checkBoxInUse) {
				// checkBoxInUse = true;
				// if (!isNextCheck) {
				// ProxyHttpClient.getInstance().goOnline();
				// } else {
				// ProxyHttpClient.getInstance().goOffLine();
				// check.setChecked(isNextCheck);
				// checkBoxInUse = false;
				// }
				// } else {
				// Toast.makeText(getApplicationContext(), "Please wait, " +
				// "busy with a previous connection request",
				// Toast.LENGTH_SHORT).show();
				// }
			}
		});
		
		TestCoordinator.check(TTChecks.MAIN_ACTIVITY_SHOWN);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void logInOut(View view) {
		String session = StoreCredential.getInstance().getSessionId(
				getApplicationContext());

		if (!session.equals("")) {
			StoreCredential.getInstance().removeSessionId(
					getApplicationContext());

			modifyButtonIfNotAuthenticated();

			TestCoordinator.check(TTChecks.LOGGED_OUT);
		} else {
			this.finish();

			Intent logInIntent = new Intent(this, AuthenticationActivity.class);

			startActivity(logInIntent);
		}
	}

	public void showQuestion(View view) {
		Intent showQuestionIntent = new Intent(this,
				ShowQuestionsActivity.class);
		showQuestionIntent.putExtra("Type", "Random");
		startActivity(showQuestionIntent);
	}

	public void submitQuestion(View view) {
		Intent editQuestionIntent = new Intent(this, EditQuestionActivity.class);
		startActivity(editQuestionIntent);
	}

	public void searchQuestion(View view) {
		Intent searchActivity = new Intent(this, SearchActivity.class);

		startActivity(searchActivity);
	}

	@Override
	public void onResume() {
		// Function calls when the activity gets the focus
		super.onResume();
		CheckBox offline = (CheckBox) findViewById(R.id.offline);
		// put online? offline? or simply proxy state.
		try {
			offline.setChecked(ProxyHttpClient.getInstance().getOfflineStatus());
		} catch (CacheException e) {
			Logger.getLogger("epfl.sweng.entry").log(Level.INFO,
					"Resuming main activity", e);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		//TestCoordinator.check(TTChecks.MAIN_ACTIVITY_SHOWN);
	}

	/**
	 * Confirm and the checkbox state.
	 * 
	 * @param bool
	 *            the new state.
	 */
	private void confirmCheckBoxState(boolean bool) {
		CheckBox offline = (CheckBox) findViewById(R.id.offline);
		offline.setChecked(bool);
	}

	/**
	 * Release the use of the checkbox.
	 */
	private void release() {
		checkBoxInUse = false;
	}

	/**
	 * 
	 * Private class to interact with the checkbox.
	 * 
	 * @author Valentin
	 * 
	 */
	private class CheckBoxTask implements ICheckBoxTask {

		@Override
		public void releaseGoOnlineTask() {
			release();
		}

		@Override
		public void confirmCheckBoxTask(boolean bool) {
			confirmCheckBoxState(bool);
			release();
		}
	}
}
