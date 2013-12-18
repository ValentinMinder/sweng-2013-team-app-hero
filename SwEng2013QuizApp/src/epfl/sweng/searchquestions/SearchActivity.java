package epfl.sweng.searchquestions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import epfl.sweng.R;
import epfl.sweng.query.QueryChecker;
import epfl.sweng.showquestions.ShowQuestionsActivity;
import epfl.sweng.testing.TestCoordinator;
import epfl.sweng.testing.TestCoordinator.TTChecks;

public class SearchActivity extends Activity {

	private EditText editQuery;
	private Button searchButton;
	private QueryChecker queryChecker = QueryChecker.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		editQuery = (EditText) findViewById(R.id.searchText);
		editQuery.addTextChangedListener(textListener);
		searchButton = (Button) findViewById(R.id.searchButton);
		searchButton.setEnabled(false);
		TestCoordinator.check(TTChecks.SEARCH_ACTIVITY_SHOWN);
	}

	public void search(View view) {

		String search = editQuery.getText().toString();

		Intent showQuestionIntent = new Intent(this,
				ShowQuestionsActivity.class);
		showQuestionIntent.putExtra("Type", "Search"); // hardcoded car il faut
														// des strings, pas des
														// int!
		showQuestionIntent.putExtra("Request", search);
		startActivity(showQuestionIntent);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.search, menu);
		return true;
	}

	private TextWatcher textListener = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {

			searchController(searchButton);

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

	private int auditSearchButton() {
		String query = editQuery.getText().toString();
		queryChecker.setQuery(query);
		if (queryChecker.checkQuery()) {
			return 0;
		} else {
			return 1;
		}
	}

	private void searchController(Button search) {
		if (auditSearchButton() == 0) {
			search.setEnabled(true);
		} else {
			search.setEnabled(false);
		}
	}

}
