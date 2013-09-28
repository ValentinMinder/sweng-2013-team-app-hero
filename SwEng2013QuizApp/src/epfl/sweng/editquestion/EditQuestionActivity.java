package epfl.sweng.editquestion;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.EditText;
import epfl.sweng.R;
/**
 * 
 * @author xhanto
 *
 */
public class EditQuestionActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_question);
        EditText editText = (EditText) findViewById(R.id.edit_text);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_question, menu);
		return true;
	}

}
