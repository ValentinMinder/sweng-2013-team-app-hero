package epfl.sweng.entry;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import epfl.sweng.R;
import epfl.sweng.authentication.AuthenticationActivity;
import epfl.sweng.authentication.StoreCredential;
import epfl.sweng.editquestions.EditQuestionActivity;
import epfl.sweng.showquestions.ShowQuestionsActivity;
import epfl.sweng.testing.TestCoordinator;
import epfl.sweng.testing.TestCoordinator.TTChecks;
/**
 * 
 * Main activity of our application
 * @author AppHero
 *
 */
public class MainActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		String session = StoreCredential.getInstance().getSessionId(getApplicationContext());
		
		if (session.equals("")) {
			Button button1 = (Button) findViewById(R.id.button1);
			button1.setEnabled(false);
			
			Button button2 = (Button) findViewById(R.id.button2);
			button2.setEnabled(false);
			
			Button buttonLog = (Button) findViewById(R.id.button_log);
			buttonLog.setText(R.string.log_in_tekila);
		}
		
		TestCoordinator.check(TTChecks.MAIN_ACTIVITY_SHOWN);
		
		/* A utiliser lorsqu'on veut r���cup���rer la session_id de l'utilisateur
		SharedPreferences preferences = 
		getSharedPreferences
		(AuthenticationActivity
		.namePreferenceSession,MODE_PRIVATE);
		preferences.getString(AuthenticationActivity.nameVariableSession, "");
		*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void logInOut(View view) {
		String session = StoreCredential.getInstance().getSessionId(getApplicationContext());

		
		if (!session.equals("")) {
			//TODO which context shall we pass, getBaseContext, applicationContext ... ? 
			StoreCredential.getInstance().removeSessionId(getApplicationContext());
			TestCoordinator.check(TTChecks.LOGGED_OUT);
		}
		
		this.finish();
		
		Intent logInIntent = new Intent(this, AuthenticationActivity.class);
		
		startActivity(logInIntent);
	}
	
	public void showQuestion(View view) {		
		Intent showQuestionIntent = new Intent(this, ShowQuestionsActivity.class);
		
		startActivity(showQuestionIntent);
	}
	
	public void submitQuestion(View view) {
		
		Intent editQuestionIntent = new Intent(this, EditQuestionActivity.class);

		startActivity(editQuestionIntent);
	}	
}
	

