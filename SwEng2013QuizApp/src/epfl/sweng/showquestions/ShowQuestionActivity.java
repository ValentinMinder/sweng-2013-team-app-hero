package epfl.sweng.showquestions;

import epfl.sweng.R;
import epfl.sweng.testing.TestingTransactions;
import epfl.sweng.testing.TestingTransactions.TTChecks;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.widget.TextView;

public class ShowQuestionActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_question);
		
		Intent startingIntent = getIntent();
		
		String questionText = startingIntent.getStringExtra(epfl.sweng.entry.MainActivity.class.getName());
		
		TextView textView = (TextView) findViewById(R.id.displayed_text);
		textView.setText(questionText);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_question, menu);
		return true;
	}

}
