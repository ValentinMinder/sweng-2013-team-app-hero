package epfl.sweng.editquestion;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import epfl.sweng.R;
/**
 * 
 * @author xhanto
 *
 */
public class EditQuestionActivity extends Activity {

	private EditText editQuestion, editAnswer;
	private Button correct, remove, add;
	private ArrayList<Button> correctButtons;
	private ArrayList<EditText> answers;
	private int correctAnswer;
	private int correctIndex=0;
	private int removeIndex=500;
	private int answerIndex=1000;
	private int gridIndex=1500;
	private LinearLayout container;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_question);
		container = (LinearLayout) findViewById(R.id.container);
		editQuestion = (EditText) findViewById(R.id.type_question);

		add = (Button) findViewById(R.id.add);
		GridLayout grid = new GridLayout(this);
		EditText answer = new EditText(this);
		Button correct = new Button(this);
		Button remove = new Button(this); 

		grid.setId(gridIndex);
		answer.setId(answerIndex);
		answer.setHint("Type in an answer");

		correct.setText("\u2718");
		correct.setId(correctIndex);
		//	nextCorrect.setOnClickListener(setAnswer());

		remove.setText("\u002D");
		remove.setId(removeIndex);
		remove.setOnClickListener(removeHandler);

		container.addView(answer);
		container.addView(grid);
		grid.addView(correct);
		grid.addView(remove);

		correctIndex++;
		removeIndex++;
		answerIndex++;
		gridIndex++;

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
			correct.setText("\u2714");
			// correctIndex=
		}
	};

	View.OnClickListener removeHandler = new View.OnClickListener() {

		@Override
		public void onClick(View view) {
			int essai = view.getId();
			GridLayout delGrid = (GridLayout) findViewById((essai+1000));
			EditText delAnswer = (EditText) findViewById((essai+500));
			delGrid.removeAllViews();
			container.removeView(delGrid);
			container.removeView(delAnswer);
			gridIndex--;
			answerIndex--;
			removeIndex--;
			correctIndex--;
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

		answerIndex++;
		correctIndex++;
		removeIndex++;
		gridIndex++;

		container.addView(nextAnswer);
		container.addView(nextGrid);
		nextGrid.addView(nextCorrect);
		nextGrid.addView(nextRemove);

	}


}
