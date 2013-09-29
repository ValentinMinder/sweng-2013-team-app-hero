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
	private LinearLayout container;
	private GridLayout grid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_question);
		container = (LinearLayout) findViewById(R.id.container);
		grid = (GridLayout) findViewById(R.id.grid);
		editQuestion = (EditText) findViewById(R.id.type_question);
		editAnswer = (EditText) findViewById(R.id.type_answer);
		correct = (Button) findViewById(R.id.correct);
		remove = (Button) findViewById(R.id.remove);
		add = (Button) findViewById(R.id.add);
		answers.add(editAnswer);
		correctButtons.add(correct);


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_question, menu);
		return true;
	}

	public void setAnswer(View view) {
		//correct.setText("\u2714");
		// correctIndex=
	}

	public void removeAnswer(View view) {
		Toast.makeText(this, "You clicked \u002D!", Toast.LENGTH_SHORT).show();
	}
	public void addAnswer(View view) {
		Button test = new Button(this);
		test.setId(1);
		test.setText("essai");
		grid.addView(test);
		Toast.makeText(this, "You clicked \u002B!", Toast.LENGTH_SHORT).show();

	}

}
