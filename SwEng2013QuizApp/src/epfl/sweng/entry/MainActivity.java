package epfl.sweng.entry;

import android.app.Activity;
import android.content.Intent;
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
import epfl.sweng.editquestions.EditQuestionActivity;
import epfl.sweng.patterns.ICheckBoxTask;
import epfl.sweng.patterns.ProxyHttpClient;
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

		Button buttonLog = (Button) findViewById(R.id.button_log);
		buttonLog.setText(R.string.log_in_tekila);

		CheckBox offline = (CheckBox) findViewById(R.id.offline);
		offline.setVisibility(View.GONE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		String session = StoreCredential.getInstance().getSessionId(
				getApplicationContext());

		CheckBox offline = (CheckBox) findViewById(R.id.offline);

		if (session.equals("")) {
			modifyButtonIfNotAuthenticated();
		} else {
			offline.setVisibility(View.VISIBLE);
			offline.setChecked(ProxyHttpClient.getInstance().getOfflineStatus());
		}
		
		myCheckBoxTask = new CheckBoxTask();
		ProxyHttpClient.getInstance().setCheckBoxTask(myCheckBoxTask);
		
		offline.setOnClickListener(new CompoundButton.OnClickListener() {
			@Override
			public void onClick(View buttonView) {
				// we save the previous state, and restablish the previous one
				CheckBox check = (CheckBox) findViewById(R.id.offline);
				boolean isNextCheck = check.isChecked();
				check.setChecked(!isNextCheck);
				// we lock the use of the checkbox
				if (!checkBoxInUse) {
					checkBoxInUse = true;
					if (!isNextCheck) {
						ProxyHttpClient.getInstance().goOnline();
					} else {
						ProxyHttpClient.getInstance().goOffLine();
					}
				} else {
					Toast.makeText(getApplicationContext(), "Please wait, " +
							"busy with a previous connection request", Toast.LENGTH_SHORT).show();
				}
			}
		});
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

		startActivity(showQuestionIntent);
	}

	public void submitQuestion(View view) {
		Intent editQuestionIntent = new Intent(this, EditQuestionActivity.class);
		startActivity(editQuestionIntent);
	}

	@Override
	public void onResume() {
		// Function calls when the activity gets the focus
		super.onResume();
		CheckBox offline = (CheckBox) findViewById(R.id.offline);
		// put online? offline? or simply proxy state.
		offline.setChecked(ProxyHttpClient.getInstance().getOfflineStatus());
	}

	@Override
	protected void onStart() {
		super.onStart();
		TestCoordinator.check(TTChecks.MAIN_ACTIVITY_SHOWN);
	}
	
	/**
	 * Confirm and the checkbox state.
	 * @param bool the new state.
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
	private class CheckBoxTask implements ICheckBoxTask{

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
