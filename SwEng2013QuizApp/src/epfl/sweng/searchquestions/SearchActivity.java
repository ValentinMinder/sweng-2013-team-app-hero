package epfl.sweng.searchquestions;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import epfl.sweng.R;
import epfl.sweng.testing.TestCoordinator;
import epfl.sweng.testing.TestCoordinator.TTChecks;

public class SearchActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
	}

	public void search(View view) {
		
		
		
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		EditText editQuery = (EditText) findViewById(R.id.searchText);
		editQuery.addTextChangedListener(textListener);

		getMenuInflater().inflate(R.menu.search, menu);
		return true;
	}
	
	
	
	private TextWatcher textListener = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			TestCoordinator.check(TTChecks.QUERY_EDITED);
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

}
