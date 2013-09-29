package epfl.sweng.showquestions;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import epfl.sweng.R;
import epfl.sweng.entry.QuizQuestion;

public class ShowQuestionActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_question);
		ArrayList<String> answer = new ArrayList<String>();
		answer.add("42");
		answer.add("21");
		ArrayList<String> tags = new ArrayList<String>();
		tags.add("life");
		QuizQuestion mockQuestion = new QuizQuestion(1111, new String(
				"what is the answer"), answer, 1, tags);

		Intent startingIntent = getIntent();

		TextView questionTitle = (TextView) findViewById(R.id.displayed_text);
		// questionTitle.setText(mockQuestion.getQuestion());

		ListView possibleAnswers = (ListView) findViewById(R.id.multiple_choices);
		if (possibleAnswers != null) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, answer);

			possibleAnswers.setAdapter(adapter);
		} 

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_question, menu);
		return true;
	}

}
