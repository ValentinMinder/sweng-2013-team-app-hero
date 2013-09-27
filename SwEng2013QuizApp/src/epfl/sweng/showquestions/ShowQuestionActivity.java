package epfl.sweng.showquestions;

import java.util.ArrayList;

import epfl.sweng.R;
import epfl.sweng.entry.QuizQuestion;
import epfl.sweng.testing.TestingTransactions;
import epfl.sweng.testing.TestingTransactions.TTChecks;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

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
		QuizQuestion mockQuestion = new QuizQuestion(1111, "what is the answer", answer, 1, tags);
		Intent startingIntent = getIntent();
		
		String questionText = startingIntent.getStringExtra(epfl.sweng.entry.MainActivity.class.getName());
		
		
		TextView questionTitle = (TextView) findViewById(R.id.displayed_text);
		questionTitle.setText(mockQuestion.getQuestion());
	
		
		//RadioGroup answerGroup
		RadioGroup possibleAnswers = (RadioGroup) findViewById(R.id.multiple_choices);
		
		for (int i = 0; i<mockQuestion.getAnswer().size();i++){
			RadioButton button = new RadioButton(this);
			button.setText(answer.get(i));
			possibleAnswers.addView(button);
			
		}
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_question, menu);
		return true;
	}

}
