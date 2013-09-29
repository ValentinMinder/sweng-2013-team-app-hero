package epfl.sweng.editquestion;

import java.util.ArrayList;

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
	private int correctIndex;
	private int buttonIndex;
	private LinearLayout container;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_question);
		container = (LinearLayout) findViewById(R.id.container);
		editQuestion = (EditText) findViewById(R.id.type_question);
		editAnswer = (EditText) findViewById(R.id.type_answer);
		correct = (Button) findViewById(R.id.correct);
		remove = (Button) findViewById(R.id.remove);
		add = (Button) findViewById(R.id.add);


		//answers.add(editAnswer);
		//correctButtons.add(correct);

		buttonIndex=0;


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_question, menu);
		return true;
	}

	public void setAnswer(View view) {
		correct.setText("\u2714");
		// correctIndex=
	}

	public void removeAnswer(View view) {
		Toast.makeText(this, "You clicked \u002D!", Toast.LENGTH_SHORT).show();
	}
	public void addAnswer(View view) {
		GridLayout nextGrid = new GridLayout(this);
		EditText nextAnswer = new EditText(this);
		Button nextCorrect = new Button(this);
		Button nextRemove = new Button(this); 

		nextCorrect.setText("\u2718");
		//	nextCorrect.setOnClickListener(setAnswer());

		nextRemove.setText("\u002D");
		//	nextRemove.setOnClickListener(removeAnswer());



		container.addView(nextAnswer);
		container.addView(nextGrid);
		nextGrid.addView(nextCorrect);
		nextGrid.addView(nextRemove);
		

	}

}
