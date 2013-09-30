package epfl.sweng.editquestion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import epfl.sweng.R;
import epfl.sweng.entry.QuizQuestion;

/**
 * 
 * @author xhanto
 *
 */
public class EditQuestionActivity extends Activity {

	private int correctAnswer;
	private int correctIndex=0;
	private int removeIndex=1000;
	private int answerIndex=2000;
	private int gridIndex=3000;
	private LinearLayout container;
	private int idIndex=0;
	private LinkedList<Integer> idList = new LinkedList<Integer>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_question);

		container = (LinearLayout) findViewById(R.id.container);

		GridLayout grid = new GridLayout(this);
		EditText answer = new EditText(this);
		Button correct = new Button(this);
		Button remove = new Button(this); 

		grid.setId(gridIndex);

		answer.setId(answerIndex);
		answer.setHint("Type in an answer");

		correct.setText("\u2718");
		correct.setId(correctIndex);
		correct.setOnClickListener(answerHandler);

		remove.setText("\u002D");
		remove.setId(removeIndex);
		remove.setOnClickListener(removeHandler);

		container.addView(answer);
		container.addView(grid);

		grid.addView(correct);
		grid.addView(remove);

		idList.add(idIndex);

		correctIndex++;
		removeIndex++;
		answerIndex++;
		gridIndex++;
		idIndex++;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_question, menu);
		return true;
	}

	View.OnClickListener answerHandler = new View.OnClickListener() {

		@Override
		public void onClick(View view) {

			for(int i=0;i<idList.size(); i++) {
				Button allFalse = (Button) findViewById((idList.get(i)));
				allFalse.setText("\u2718");
			}

			((Button)findViewById(view.getId())).setText("\u2714");
		}
	};

	View.OnClickListener removeHandler = new View.OnClickListener() {

		@Override
		public void onClick(View view) {

			int idToRemove = view.getId();
			GridLayout delGrid = (GridLayout) findViewById((idToRemove+2000));
			EditText delAnswer = (EditText) findViewById((idToRemove+1000));
			delGrid.removeAllViews();
			container.removeView(delGrid);
			container.removeView(delAnswer);
			idList.remove((Integer)(idToRemove-1000));

		}
	};



	public void addAnswer(View view) {
		GridLayout nextGrid = new GridLayout(this);
		EditText nextAnswer = new EditText(this);
		Button nextCorrect = new Button(this);
		Button nextRemove = new Button(this); 

		nextGrid.setId(gridIndex);
		nextAnswer.setId(answerIndex);
		nextAnswer.setHint("Type in an answer");


		nextCorrect.setText("\u2718");
		nextCorrect.setId(correctIndex);
		nextCorrect.setOnClickListener(answerHandler);

		nextRemove.setText("\u002D");
		nextRemove.setId(removeIndex);
		nextRemove.setOnClickListener(removeHandler);

		idList.add(idIndex);
		idIndex++;
		answerIndex++;
		correctIndex++;
		removeIndex++;
		gridIndex++;

		container.addView(nextAnswer);
		container.addView(nextGrid);
		nextGrid.addView(nextCorrect);
		nextGrid.addView(nextRemove);

	}

	public void submitQuestion(View view) {
		if(checkSubmit()){
		EditText editQuestion = (EditText) findViewById(R.id.type_question);
		EditText tagsText = (EditText) findViewById(R.id.tags);
		ArrayList<String> answers = new ArrayList<String>();

		int solutionIndex=-1;
		String questionBody = editQuestion.getText().toString();
		String tagString = tagsText.getText().toString();

		ArrayList<String> tags = new ArrayList<String>(Arrays.asList(tagString.split("[^A-Za-z0-9]")));

		for(int i = 0; i<idList.size(); i++) {
			EditText ans = (EditText) findViewById((idList.get(i)+2000));
			String ansString = ans.getText().toString();
			answers.add(ansString);

			Button correct = (Button) findViewById((idList.get(i)));
			if(correct.getText().equals("\u2714")) {
				solutionIndex=i;
			}	
		}


		QuizQuestion question = new QuizQuestion(0, questionBody, answers, solutionIndex, tags);
		Toast.makeText(this, question.toString(), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "Errors in question format", Toast.LENGTH_SHORT).show();

		}
		

	}

	public boolean checkSubmit() {
		int check=0;
		boolean oneTrue = false;
		EditText editQuestion = (EditText) findViewById(R.id.type_question);
		String question = editQuestion.getText().toString();
		if(idList.size()<2 || question.isEmpty() || question.equals(" ") ) {
			check++;
		}

		for(int i=0;i<idList.size(); i++) {
			Button isCorrect = (Button) findViewById((idList.get(i)));
			if(isCorrect.getText().equals("\u2714")) {
				oneTrue=true;
			}
			EditText isFull = (EditText) findViewById((idList.get(i)+2000));
			if(isFull.getText().toString().equals(" ") || isFull.getText().toString().isEmpty()){
				check++;
			}
		}

		if(check==0 && oneTrue){
			return true; 
		}
		
		return false;

	}


}
