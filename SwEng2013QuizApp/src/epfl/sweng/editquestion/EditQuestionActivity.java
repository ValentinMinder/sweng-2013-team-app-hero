package epfl.sweng.editquestion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
 * @author Xhanto
 * This class is used to submit a new question to the server.
 *
 */
public class EditQuestionActivity extends Activity {


	private final int correctCst=0;
	private final int removeCst=1000;
	private final int answerCst=2000;
	private final int gridCst=3000;
	
	private int correctIndex;
	private int removeIndex;
	private int answerIndex;
	private int gridIndex;
	private LinearLayout container;
	private EditText questionField;
	private int idIndex=0;
	private LinkedList<Integer> idList = new LinkedList<Integer>();
	private Button submit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_question);

		correctIndex=correctCst;
		removeIndex=removeCst;
		answerIndex=answerCst;
		gridIndex=gridCst;
		
		container = (LinearLayout) findViewById(R.id.container);
		submit = (Button) findViewById(R.id.submit_question);
		questionField = (EditText) findViewById(R.id.type_question);
		questionField.addTextChangedListener(textListener);
		
		submit.setEnabled(false);
		
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
	/**
	 * Method to set enable a button who is put in parameter.
	 * @param sub the button who will be set enable if the variable audit = 0.
	 */
	private void submitControler(Button sub) {
		if (audit()==0) {
			sub.setEnabled(true);
		} else {
			sub.setEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_question, menu);
		return true;
	}
	
	 
	private View.OnClickListener answerHandler = new View.OnClickListener() {

		@Override
		public void onClick(View view) {

			for (int i=0; i<idList.size(); i++) {
				Button allFalse = (Button) findViewById(idList.get(i));
				allFalse.setText("\u2718");
			}

			((Button) findViewById(view.getId())).setText("\u2714");
			submitControler(submit);

		}
	};

	private View.OnClickListener removeHandler = new View.OnClickListener() {

		@Override
		public void onClick(View view) {

			int idToRemove = view.getId();
			GridLayout delGrid = (GridLayout) findViewById(idToRemove+answerCst);
			EditText delAnswer = (EditText) findViewById(idToRemove+removeCst);
			delGrid.removeAllViews();
			container.removeView(delGrid);
			container.removeView(delAnswer);
			idList.remove((Integer) (idToRemove-removeCst));
			submitControler(submit);

		}
	};
	
	private TextWatcher textListener = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			submitControler(submit);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			
		}
		
	};


	/**
	 * Method to add an answer of a new question
	 * @param view 
	 */
	//View a faire
	public void addAnswer(View view) {
		GridLayout nextGrid = new GridLayout(this);
		EditText nextAnswer = new EditText(this);
		Button nextCorrect = new Button(this);
		Button nextRemove = new Button(this); 

		nextGrid.setId(gridIndex);
		nextAnswer.setId(answerIndex);
		nextAnswer.setHint("Type in an answer");
		nextAnswer.addTextChangedListener(textListener);

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
		
		submitControler(submit);


	}
	/**
	 * Method to submit the Question 
	 * @param view
	 */
	//View a faire
	public void submitQuestion(View view) {
		EditText editQuestion = (EditText) findViewById(R.id.type_question);
		EditText tagsText = (EditText) findViewById(R.id.tags);
		ArrayList<String> answers = new ArrayList<String>();

		int solutionIndex=-1;
		String questionBody = editQuestion.getText().toString();
		String tagString = tagsText.getText().toString();

		ArrayList<String> tags = new ArrayList<String>(Arrays.asList(tagString.split("\\W+")));

		for (int i = 0; i<idList.size(); i++) {
			EditText ans = (EditText) findViewById(idList.get(i)+answerCst);
			String ansString = ans.getText().toString();
			answers.add(ansString);

			Button correct = (Button) findViewById(idList.get(i));
			if (correct.getText().equals("\u2714")) {
				solutionIndex=i;
			}	
		}

		QuizQuestion question = new QuizQuestion(0, questionBody, answers, solutionIndex, tags);
		Toast.makeText(this, question.toString(), Toast.LENGTH_SHORT).show(); // TO REMOVE BEFORE DEADLINE
		

	}
	/**
	 * Method audit to count the number of errors in the question
	 * @return
	 */
	public int audit() {
		int checkErrors=0;
		boolean oneTrue = false;
		EditText editQuestion = (EditText) findViewById(R.id.type_question);
		String question = editQuestion.getText().toString();
		if (idList.size()<2 || question.trim().length()==0) {
			checkErrors++;
		}

		for (int i=0; i<idList.size(); i++) {
			Button isCorrect = (Button) findViewById(idList.get(i));
			if (isCorrect.getText().equals("\u2714")) {
				oneTrue=true;
			}
			
			EditText isFull = (EditText) findViewById(idList.get(i)+answerCst);
			if (isFull.getText().toString().trim().length()==0) {
				checkErrors++;
			}
		}
		
		if (!oneTrue) {
			checkErrors++;
		}
		
		return checkErrors;

	}


}
