package epfl.sweng.entry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import epfl.sweng.R;
import epfl.sweng.authentication.AuthenticationActivity;
import epfl.sweng.authentication.StoreCredential;
import epfl.sweng.editquestions.EditQuestionActivity;
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

//	private Semaphore checkboxSem = new Semaphore(100);
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
//		ProxyHttpClient.getInstance().setSemaphore(checkboxSem);
//		offline.setOnClickListener(new CompoundButton.OnClickListener() {
//			@Override
//			public void onClick(View buttonView) {
//				if (((CheckBox) buttonView).isChecked()) {
//					ProxyHttpClient.getInstance().goOnline();
//					try {
//						checkboxSem.acquire();
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					((CheckBox) buttonView).setChecked(false);
//					checkboxSem.release();
//				} else {
//					System.out.println("vagin");
//					ProxyHttpClient.getInstance().goOffLine();
//					((CheckBox) buttonView).setChecked(true);
//				}
////				while(ProxyHttpClient.getInstance().getOfflineStatus())
//			}
//		});

		offline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
//				CheckBox check = (CheckBox) findViewById(R.id.offline);

				if (isChecked) {
					ProxyHttpClient.getInstance().goOffLine();
					System.out.println("going offline");
				} else {
					System.out.println("going online");
//					check.setChecked(!isChecked);
					
					ProxyHttpClient.getInstance().goOnline();
//					try {
//						System.out.println("sema aquire main");
//						checkboxSem.acquire();
//						System.out.println("sema dequire main");
//
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					check.setChecked(isChecked);
//					checkboxSem.release();
					System.out.println("we are online");
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
		offline.setChecked(ProxyHttpClient.getInstance().getOfflineStatus());
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		TestCoordinator.check(TTChecks.MAIN_ACTIVITY_SHOWN);
	}

}
