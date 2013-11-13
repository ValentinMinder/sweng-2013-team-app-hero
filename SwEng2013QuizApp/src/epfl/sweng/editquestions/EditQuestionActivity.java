package epfl.sweng.editquestions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
import epfl.sweng.authentication.StoreCredential;
import epfl.sweng.quizquestions.QuizQuestion;
import epfl.sweng.servercomm.ProxyHttpClientFactory;
import epfl.sweng.testing.TestCoordinator;
import epfl.sweng.testing.TestCoordinator.TTChecks;

/**
 * 
 * @author xhanto
 *  This class is used to submit a new question to the server.
 * 
 */
public class EditQuestionActivity extends Activity {

	private final int correctCst = 0;
	private final int removeCst = 1000;
	private final int answerCst = 2000;
	private final int gridCst = 3000;
	private QuizQuestion question;
	private int correctIndex;
	private int removeIndex;
	private int answerIndex;
	private int gridIndex;
	private LinearLayout container;
	private EditText questionField;
	private EditText tagsText;
	private int idIndex = 0;
	private LinkedList<Integer> idList = new LinkedList<Integer>();
	private Button submit;

	
	/**
	 * 
	 */
	private void afficheMoiCeMessageDeDebug(String myString) {
		Toast.makeText(getBaseContext(),
				myString, Toast.LENGTH_SHORT)
				.show();
	}
	
	/**
	 * Method who is called if error occurred on submit
	 */
	private void errorEditQuestion() {
		Toast.makeText(getBaseContext(),
				R.string.not_upload_question, Toast.LENGTH_SHORT)
				.show();
	}
	/**
	 * Method who is called if success on submit
	 */
	private void successEditQuestion() {
		Toast.makeText(getBaseContext(),
				R.string.question_submitted, Toast.LENGTH_SHORT)
				.show();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_question);
		
		initUI();

		TestCoordinator.check(TTChecks.EDIT_QUESTIONS_SHOWN);

	}

	/**
	 * Method to set enable a button who is put in parameter.
	 * 
	 * @param sub
	 *            the button who will be set enable if the function audit
	 *            returns 0.
	 */
	private void submitControler(Button sub) {
		if (auditErrors() == 0) {
			sub.setEnabled(true);
		} else {
			sub.setEnabled(false);
		}
	}

	private int auditEditTexts() {
		int editErrors=0;

		//Bullet 1: The widget where the user can enter the question exists.
		//It has its hint set to “Type in the question’s text body”. 
		//The widget has its visibility property set to VISIBLE.

		// != 0 because VISIBLE = 0
		if (questionField == null || questionField.getVisibility() != View.VISIBLE 
				|| !(questionField.getHint().equals(R.string.type_question))) {
			editErrors++;
			System.out.println("bu1_editt");

		}

		//Bullet 2: There exist zero or more EditText widgets to enter answers. 
		//These have their hint set to “Type in the answer”. 
		//Their visibility properties are set to VISIBLE.
		boolean  zeroOrMore = answerIndex>=answerCst;

		if (idList.size() != 0) {
			for (int i = 0; i < idList.size(); i++) {
				EditText editCheck = (EditText) findViewById(idList.get(i) + answerCst);
				if (!zeroOrMore || !editCheck.getHint().equals(R.string.type_answer) 
						|| editCheck.getVisibility() != View.VISIBLE) {
					editErrors++;
					System.out.println("bu2_editt");
				}
			}
		}

		//Bullet 3: The widget where the user can enter tags exists. 
		//This widget has its hint set to “Type in the question’s tags”. 
		//The widget has its visibility property set to VISIBLE.

		// != 0 because VISIBLE = 0
		if (tagsText == null || tagsText.getVisibility() != View.VISIBLE 
				|| !(tagsText.getHint().equals(R.string.type_tags))) {
			editErrors++;
			System.out.println("bu3_editt");

		}
		return editErrors;
	}

	private int auditButtons() {
		int buttonErrors = 0;
		Button addButton = (Button) findViewById(R.id.add);

		//Bullet 1: A button exists to add a new answer. 
		//It has its text set to “+”, and its visibility set to VISIBLE.

		if (addButton == null || addButton.getVisibility() != View.VISIBLE
				|| !(addButton.getText().equals("\u002B"))) {
			buttonErrors++;
			System.out.println("bu1_butt");

		}

		//Bullet 2: A button exists to submit the queston. 
		// It has its text set to “Submit”, and its visibility set to VISIBLE.
		if (submit != null || submit.getVisibility() != View.VISIBLE
				|| !(submit.getText().equals(R.string.submit_question))) {
			buttonErrors++;
			System.out.println("bu2_butt");

		}

		//Bullet 3: For every answer, there is a button to remove that answer. 
		//This button has its text set to “-”, and its visibility set to VISIBLE.
		boolean  remToAns = removeIndex-removeCst == answerIndex-answerCst;

		if (idList.size() != 0) {
			for (int i = 0; i < idList.size(); i++) {
				Button removeCheck = (Button) findViewById(idList.get(i) + removeCst);
				if (!remToAns || !removeCheck.getText().equals("\u002D") ||
						removeCheck.getVisibility() != View.VISIBLE) {
					buttonErrors++;
					System.out.println("bu3_butt");

				}
			}
		}
		//Bullet 4: For every answer, there is a button to toggle its correctness. 
		//This button has its text set to “✘” or “✔”, and its visibility set to VISIBLE.
		boolean  togToAns = correctIndex-correctCst == answerIndex-answerCst;
		if (idList.size() != 0) {
			for (int i = 0; i < idList.size(); i++) {
				Button correctCheck = (Button) findViewById(idList.get(i));
				if (!togToAns 
						|| !((correctCheck.getText().equals("\u2714"))  // correct
								|| (correctCheck.getText().equals("\u2718"))) // wrong
								|| correctCheck.getVisibility() != View.VISIBLE) {
					buttonErrors++;
					System.out.println("bu4_butt");

				}
			}
		}


		return buttonErrors;
	}

	private int auditAnswers() {
		int answerErrors=0;
		int correctCount = 0;
		// Bullet 1: There is at most one correct answer 
		//(that is, at most one correctness button has its text set to “✔”).
		for (int i = 0; i < idList.size(); i++) {
			Button correctCheck = (Button) findViewById(idList.get(i));			
			if (correctCheck.getText().equals("\u2714")) {
				correctCount++;
			}
		}
		if (correctCount != 1) {
			answerErrors++;
			System.out.println("bu1_ans");

		}

		return answerErrors;
	}

	private int auditSubmitButton() {
		
		if (audit() == 0) {
			return 0;
		} else {
			System.out.println("err_sub");

			return 1;
		}
		
		
	}
	
	public int auditErrors() {
		
		return auditAnswers()+auditButtons()+auditEditTexts()+auditSubmitButton();
		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_question, menu);
		return true;
	}

	public void initUI() {
		correctIndex = correctCst;
		removeIndex = removeCst;
		answerIndex = answerCst;
		gridIndex = gridCst;
		idIndex = 0;

		container = (LinearLayout) findViewById(R.id.container);
		container.removeAllViews();

		submit = (Button) findViewById(R.id.submit_question);

		questionField = (EditText) findViewById(R.id.type_question);
		questionField.removeTextChangedListener(textListener);
		questionField.setText("");
		questionField.addTextChangedListener(textListener);
		// julien tracking change
		tagsText = (EditText) findViewById(R.id.tags);
		tagsText.removeTextChangedListener(textListener);
		tagsText.setText("");
		tagsText.addTextChangedListener(textListener);

		submit.setEnabled(false);

		GridLayout grid = new GridLayout(this);
		EditText answer = new EditText(this);
		Button correct = new Button(this);
		Button remove = new Button(this);

		grid.setId(gridIndex);

		answer.removeTextChangedListener(textListener);
		answer.setId(answerIndex);
		answer.setHint(R.string.type_answer);
		// julien: track change
		answer.addTextChangedListener(textListener);

		correct.setOnClickListener(null);
		correct.setText(R.string.wrong_answer);
		correct.setId(correctIndex);
		correct.setOnClickListener(answerHandler);

		remove.setOnClickListener(null);
		remove.setText(R.string.minus);
		remove.setId(removeIndex);
		remove.setOnClickListener(removeHandler);

		container.addView(answer);
		container.addView(grid);

		grid.addView(correct);
		grid.addView(remove);

		idList.clear();
		idList.add(idIndex);

		correctIndex++;
		removeIndex++;
		answerIndex++;
		gridIndex++;
		idIndex++;
	}

	/**
	 * Listener that handle a click on the button to set a question correct.
	 */
	private View.OnClickListener answerHandler = new View.OnClickListener() {

		@Override
		public void onClick(View view) {

			for (int i = 0; i < idList.size(); i++) {
				Button allFalse = (Button) findViewById(idList.get(i));
				allFalse.setText(R.string.wrong_answer);
			}

			((Button) findViewById(view.getId()))
			.setText(R.string.right_answer);
			submitControler(submit);
			TestCoordinator.check(TTChecks.QUESTION_EDITED);

		}
	};
	/**
	 * Listener that handle a click on the remove button
	 */
	private View.OnClickListener removeHandler = new View.OnClickListener() {

		@Override
		public void onClick(View view) {

			int idToRemove = view.getId();
			GridLayout delGrid = (GridLayout) findViewById(idToRemove
					+ answerCst);
			EditText delAnswer = (EditText) findViewById(idToRemove + removeCst);
			delGrid.removeAllViews();
			container.removeView(delGrid);
			container.removeView(delAnswer);
			idList.remove((Integer) (idToRemove - removeCst));
			submitControler(submit);
			TestCoordinator.check(TTChecks.QUESTION_EDITED);

		}
	};
	/**
	 * Listener that react and check if the question is correctly constructed
	 * when a text is written on an answer field.
	 */
	private TextWatcher textListener = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			submitControler(submit);
			TestCoordinator.check(TTChecks.QUESTION_EDITED);
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
	 * Method to add an answer field, a button to set it correct and a button to
	 * remove it.
	 * 
	 * @param view
	 */
	// View a faire
	public void addAnswer(View view) {
		GridLayout nextGrid = new GridLayout(this);
		EditText nextAnswer = new EditText(this);
		Button nextCorrect = new Button(this);
		Button nextRemove = new Button(this);

		nextGrid.setId(gridIndex);
		nextAnswer.setId(answerIndex);
		nextAnswer.setHint(R.string.type_answer);
		nextAnswer.addTextChangedListener(textListener);

		nextCorrect.setText(R.string.wrong_answer);
		nextCorrect.setId(correctIndex);
		nextCorrect.setOnClickListener(answerHandler);

		nextRemove.setText(R.string.minus);
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

		TestCoordinator.check(TTChecks.QUESTION_EDITED);
	}

	/**
	 * Method to create and submit a question.
	 * 
	 * @param view
	 */
	// View a faire
	public void submitQuestion(View view) {
		EditText editQuestion = (EditText) findViewById(R.id.type_question);
		EditText tagText = (EditText) findViewById(R.id.tags);
		ArrayList<String> answers = new ArrayList<String>();

		int solutionIndex = -1;
		String questionBody = editQuestion.getText().toString();
		String tagString = tagText.getText().toString();

		Set<String> tags = new HashSet<String>(Arrays.asList(tagString
				.split("\\W+")));

		for (int i = 0; i < idList.size(); i++) {
			EditText ans = (EditText) findViewById(idList.get(i) + answerCst);
			String ansString = ans.getText().toString();
			answers.add(ansString);

			Button correct = (Button) findViewById(idList.get(i));
			if (correct.getText().equals("\u2714")) {
				solutionIndex = i;
			}
		}

		question = new QuizQuestion(questionBody, answers,
				solutionIndex, tags, 0, "OWNER");
		/*QuizQuestion question = new QuizQuestion(0, questionBody, answers,
				solutionIndex, tags);*/
		submitQuestion(question.toPostEntity());
	}

	/**
	 * Method audit to count the number of errors in the question (returns 0 if
	 * none)
	 * 
	 * @return
	 */
	public int audit() {
		int checkErrors = 0;
		boolean oneTrue = false;
		EditText editQuestion = (EditText) findViewById(R.id.type_question);
		String questionText = editQuestion.getText().toString();

		EditText editTags = (EditText) findViewById(R.id.tags);
		String tagsToString = editTags.getText().toString();
		if (tagsToString.trim().length() == 0) {
			checkErrors++;
		}

		if (idList.size() < 2 || questionText.trim().length() == 0) {
			checkErrors++;
		}

		for (int i = 0; i < idList.size(); i++) {
			Button isCorrect = (Button) findViewById(idList.get(i));			
			if (isCorrect.getText().equals("\u2714")) {
				oneTrue = true;
			}

			EditText isFull = (EditText) findViewById(idList.get(i) + answerCst);
			if (isFull.getText().toString().trim().length() == 0) {
				checkErrors++;
			}
		}

		if (!oneTrue) {
			checkErrors++;
		}

		return checkErrors;

	}

	/**
	 * This method submit the question to the server.
	 * 
	 * In fact, it checks the connection and ask an async task to submit the
	 * question
	 * 
	 * @param questionAsEntity
	 *            question already formatted as entity
	 */
	private void submitQuestion(String questionAsEntity) {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		// Test network connection
		if (networkInfo != null && networkInfo.isConnected()) {
			try {
				new SubmitQuestionTask().execute(questionAsEntity).get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		} else {
			Toast.makeText(getBaseContext(), R.string.no_network,
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * 
	 * Task to submit a question
	 * 
	 * @author Valentin
	 * 
	 */
	private class SubmitQuestionTask extends AsyncTask<String, Void, String> {

		/**
		 * Execute and retrieve the answer from the website.
		 */
		@Override
		protected String doInBackground(String... questionElement) {
			String serverURL = "https://sweng-quiz.appspot.com/";
			HttpPost post = new HttpPost(serverURL + "quizquestions/");
			post.setHeader("Content-type", "application/json");
			post.setHeader(
					"Authorization",
					"Tequila "
							+ StoreCredential.getInstance().getSessionId(
									getApplicationContext()));

			try {
				post.setEntity(new StringEntity(questionElement[0]));

				HttpResponse response = ProxyHttpClientFactory
						.getInstance().execute(post);

				//afficheMoiCeMessageDeDebug("status: " + response.getStatusLine().getStatusCode());
				//afficheMoiCeMessageDeDebug("msg: " + response.getStatusLine().getReasonPhrase());

				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
					HttpEntity result = response.getEntity();
					if (result != null) {
						return EntityUtils.toString(result);
					} else {
						//TODO to fix, just for the test and eventually finding the bug
						return " ";
					}
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (HttpResponseException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}

		/**
		 * Execute and retrieve the answer from the website.
		 */
		protected void onPostExecute(String result) {
			// if result is null, server something else than a 2xx status.
			if (result != null) {
				// result contain a JSON object representing the question if success on submit
				successEditQuestion();
			} else {
				errorEditQuestion();
			}			
			TestCoordinator.check(TTChecks.NEW_QUESTION_SUBMITTED);
			initUI();
		}

	}



}
