package epfl.sweng.entry;


import epfl.sweng.R;
import epfl.sweng.editquestions.EditQuestionActivity;
import epfl.sweng.showquestions.ShowQuestionsActivity;
import epfl.sweng.testing.TestingTransactions;
import epfl.sweng.testing.TestingTransactions.TTChecks;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
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
		
		TestingTransactions.check(TTChecks.MAIN_ACTIVITY_SHOWN);
		
		/* A utiliser lorsqu'on veut r�cup�rer la session_id de l'utilisateur
		SharedPreferences preferences = getSharedPreferences(AuthenticationActivity.namePreferenceSession, MODE_PRIVATE);
		preferences.getString(AuthenticationActivity.nameVariableSession, "");
		*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
	

