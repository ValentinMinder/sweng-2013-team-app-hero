package epfl.sweng.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import org.apache.http.HttpStatus;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.jayway.android.robotium.solo.Solo;

import epfl.sweng.R;
import epfl.sweng.authentication.AuthenticationActivity;
import epfl.sweng.caching.Cache;
import epfl.sweng.caching.CacheException;
import epfl.sweng.patterns.ProxyHttpClient;
import epfl.sweng.quizquestions.QuizQuestion;
import epfl.sweng.servercomm.SwengHttpClientFactory;
import epfl.sweng.test.minimalmock.MockHttpClient;
import epfl.sweng.testing.TestCoordinator;
import epfl.sweng.testing.TestCoordinator.TTChecks;
import epfl.sweng.testing.TestingTransaction;

public class AuthenticationActivityTest extends
		ActivityInstrumentationTestCase2<AuthenticationActivity> {
	public static final int DODO = 3000;
	public static final int ID1 = 2000;
	public static final int ID2 = 2001;
	private String directoryFiles;
	private Solo solo;
	private String token = "68ecb58237a84ef2b2bc8d7737ff918b";
	private MockHttpClient mockHttpClient;

	public AuthenticationActivityTest() {
		super(AuthenticationActivity.class);
	}
	
	private void deleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				deleteRecursive(child);

		fileOrDirectory.delete();
	}
	
	private void initTest() {
		directoryFiles = this.getInstrumentation().getTargetContext().getApplicationContext().getFilesDir().getAbsolutePath();
			deleteDirectory();

		Cache.deleteInstance();
		ProxyHttpClient.deleteInstance();
		Cache.setDirectoryFiles(directoryFiles);
	}
	
	private void deleteDirectory() {
		String directoryFilesQuestions = directoryFiles + File.separator
				+ "questions";
		File directoryQuestions = new File(directoryFilesQuestions);
		deleteRecursive(directoryQuestions);
		
		String directoryFilesTags = directoryFiles + File.separator + "tags";
		File directoryTags = new File(directoryFilesTags);
		deleteRecursive(directoryTags);
		
		String directoryFilesUtils = directoryFiles + File.separator + "utils";
		File directoryUtils = new File(directoryFilesUtils);
		deleteRecursive(directoryUtils);
	}

	@Override
	protected void setUp() {
		this.mockHttpClient = new MockHttpClient();
		SwengHttpClientFactory.setInstance(this.mockHttpClient);
		solo = new Solo(getInstrumentation());
		/*Cache.deleteInstance();
		ProxyHttpClient.deleteInstance();*/
	}

	public void testAuthentificationFail() {
		getActivityAndWaitFor(TTChecks.AUTHENTICATION_ACTIVITY_SHOWN);

		this.mockHttpClient
				.pushCannedResponse(
						"GET (?:https?://[^/]+|[^/]+)?/+sweng-quiz.appspot.com/login\\b",
						HttpStatus.SC_BAD_REQUEST, null, "application/json");

		EditText username = (EditText) solo.getView(R.id.gaspar_username);
		EditText pwd = (EditText) solo.getView(R.id.gaspar_password);

		solo.enterText(username, "aa");
		solo.enterText(pwd, "bb");
		Button authentication = (Button) solo.getView(R.id.log_in_tekila);
		solo.clickOnView(authentication);
		solo.sleep(3000);

		assertTrue("Authentication not fail",
				solo.searchText(solo.getString(R.string.authentication_failed)));
	}

	public void testAuthentificationSuccessfullAndLogout() {
		getActivityAndWaitFor(TTChecks.AUTHENTICATION_ACTIVITY_SHOWN);

		this.mockHttpClient
				.pushCannedResponse(
						"GET (?:https?://[^/]+|[^/]+)?/+sweng-quiz.appspot.com/login\\b",
						HttpStatus.SC_OK, "{\"token\": \"" + token + "\" }",
						"application/json");

		this.mockHttpClient
				.pushCannedResponse(
						"POST (?:https?://[^/]+|[^/]+)?/+sweng-quiz.appspot.com/login\\b",
						HttpStatus.SC_OK, "{\"session\": \"" + token + "\" }",
						"application/json");

		this.mockHttpClient
				.pushCannedResponse(
						"POST (?:https?://[^/]+|[^/]+)?/+tequila.epfl.ch/cgi-bin/tequila/login\\b",
						HttpStatus.SC_MOVED_TEMPORARILY, null,
						"application/json");

		solo.sleep(DODO);

		EditText username = (EditText) solo.getView(R.id.gaspar_username);
		EditText pwd = (EditText) solo.getView(R.id.gaspar_password);
		Button authentication = (Button) solo.getView(R.id.log_in_tekila);

		solo.enterText(username, "aa");
		solo.enterText(pwd, "bb");
		solo.clickOnView(authentication);

		getActivityAndWaitFor(TTChecks.MAIN_ACTIVITY_SHOWN);

		Button show_random_question = (Button) solo.getView(R.id.button2);
		Button submit_quiz_question = (Button) solo.getView(R.id.button1);
		Button search_quiz = (Button) solo.getView(R.id.button3);

		assertTrue("Button show random question not enabled",
				show_random_question.isEnabled());
		assertTrue("Button submit quiz question not enabled",
				submit_quiz_question.isEnabled());
		assertTrue("Button search quiz not enabled", search_quiz.isEnabled());

		// Show question
		this.mockHttpClient
				.pushCannedResponse(
						"GET (?:https?://[^/]+|[^/]+)?/+sweng-quiz.appspot.com/quizquestions/random\\b",
						HttpStatus.SC_OK,
						"{\"question\": \"What is the answer to life, the universe, and everything?\","
								+ " \"answers\": [\"Forty-two\", \"Twenty-seven\"], \"owner\": \"sweng\","
								+ " \"solutionIndex\": 0, \"tags\": [\"h2g2\", \"trivia\"], \"id\": \"1\" }",
						"application/json");

		solo.clickOnView(show_random_question);
		getActivityAndWaitFor(TTChecks.QUESTION_SHOWN);
		
		Button nextQuestionButton = (Button) solo
				.getView(R.id.next_question_button);
		
		solo.clickOnText("Twenty-seven");
		getActivityAndWaitFor(TTChecks.ANSWER_SELECTED);
		
		assertFalse("Next question button is enabled",
				nextQuestionButton.isEnabled());
		
		solo.clickOnText("Forty-two");
		getActivityAndWaitFor(TTChecks.ANSWER_SELECTED);

		assertTrue("Next question button is disabled",
				nextQuestionButton.isEnabled());
		
		this.mockHttpClient
		.pushCannedResponse(
				"GET (?:https?://[^/]+|[^/]+)?/+sweng-quiz.appspot.com/quizquestions/random\\b",
				HttpStatus.SC_OK,
				"{\"question\": \"Question?\","
						+ " \"answers\": [\"answer1\", \"answer2\"], \"owner\": \"sweng\","
						+ " \"solutionIndex\": 0, \"tags\": [\"h2g2\", \"trivia\"], \"id\": \"1\" }",
				"application/json");
		
		solo.clickOnView(nextQuestionButton);
		
		getActivityAndWaitFor(TTChecks.QUESTION_SHOWN);
		
		solo.goBack();
		solo.sleep(DODO);

		CheckBox offline = (CheckBox) solo.getView(R.id.offline);
		solo.clickOnView(offline);

		getActivityAndWaitFor(TTChecks.OFFLINE_CHECKBOX_ENABLED);
		
		// Submit question
		solo.clickOnView(submit_quiz_question);
		getActivityAndWaitFor(TTChecks.EDIT_QUESTIONS_SHOWN);

		solo.sleep(DODO * 4);

		Button add = (Button) solo.getView(R.id.add);
		solo.clickOnView(add);

		Button correct = (Button) solo.getView(0);
		// Button notCorrect = (Button) solo.getView(1);
		solo.sleep(DODO);
		assertTrue("correct Typo before clicking", correct.getText()
				.equals("✘") /* && notCorrect.getText().equals("✘") */);

		solo.clickOnView(correct);
		solo.sleep(DODO);
		assertTrue("Correct typo", correct.getText().equals("✔") /*
																 * &&
																 * notCorrect.
																 * getText
																 * ().equals
																 * ("✘")
																 */);

		Button submit = (Button) solo.getView(R.id.submit_question);
		assertFalse("Submit is disabled", submit.isEnabled());

		String s = "test question, please ignore";
		EditText question = (EditText) solo.getView(R.id.type_question);
		solo.enterText(question, s);

		solo.sleep(DODO);
		assertTrue(
				"title edited",
				question.getText().toString()
						.equals("test question, please ignore"));

		EditText tags = (EditText) solo.getView(R.id.tags);
		EditText ans1 = (EditText) solo.getView(ID1);
		EditText ans2 = (EditText) solo.getView(ID2);

		solo.enterText(ans1, "correct One");
		solo.enterText(ans2, "wrong One");

		solo.sleep(DODO);
		assertFalse("Submit is disabled", submit.isEnabled());

		solo.enterText(tags, "test");

		solo.sleep(DODO);
		assertTrue("Submit is disabled", submit.isEnabled());

		solo.enterText(tags, "");
		solo.sleep(DODO);
		assertFalse("Submit is disabled", submit.isEnabled());

		solo.enterText(tags, "test");
		assertTrue("Submit is disabled", submit.isEnabled());
		solo.sleep(DODO);
		ArrayList<String> answer = new ArrayList<String>();
		answer.add("correct One");
		answer.add("wrong One");
		HashSet<String> tags1 = new HashSet<String>();
		tags1.add("test");

		solo.sleep(DODO);

		solo.clickOnView(submit);
		getActivityAndWaitFor(TTChecks.NEW_QUESTION_SUBMITTED);

		solo.goBack();
		solo.sleep(DODO);

		try {
			assertTrue("Question not stored in cache", Cache.getInstance()
					.getListOutBox().size() == 1);
		} catch (CacheException e) {
			assertTrue("Cache exception for getListOutBox", false);
		}
		
		//Display question stored but not submitted
		solo.clickOnView(show_random_question);
		getActivityAndWaitFor(TTChecks.QUESTION_SHOWN);
		
		solo.goBack();
		solo.sleep(DODO);
		
		QuizQuestion Q = new QuizQuestion(s, answer, 0, tags1, 0, "moi");
		this.mockHttpClient
				.pushCannedResponse(
						"POST (?:https?://[^/]+|[^/]+)?/+sweng-quiz.appspot.com/quizquestions\\b",
						HttpStatus.SC_CREATED, Q.toPostEntity(), "application/json");

		solo.clickOnView(offline);
		
		getActivityAndWaitFor(TTChecks.OFFLINE_CHECKBOX_DISABLED);

		solo.sleep(DODO);

		try {
			assertTrue("Question not submitted", Cache.getInstance()
					.getListOutBox().size() == 0);
		} catch (CacheException e) {
			assertTrue("Cache exception for getListOutBox", false);
		}
		
		
		solo.clickOnView(search_quiz);
		getActivityAndWaitFor(TTChecks.SEARCH_ACTIVITY_SHOWN);

		solo.sleep(DODO);
		
		//TEMP START
		String querry = "fruit";
		EditText querryText = (EditText) solo.getView(R.id.searchText);
		solo.enterText((EditText) querryText, querry);
		solo.sleep(DODO);	
		
		assertTrue("button is enabled", solo.getButton("Search").isEnabled());
		
		String jsonQuestion = "{ \"id\": \"7654765\", \"owner\": \"fruitninja\", " +
				"\"question\": \"How many calories are in a banana?\"," +
			      "\"answers\": [ \"Just enough\", \"Too many\" ]," +
			      "\"solutionIndex\": 0," +
			      "\"tags\": [ \"fruit\", \"banana\", \"trivia\" ] }";
		
		String response = "{ \"questions\": [ " + jsonQuestion + " ]," +
			  "\"next\": \"YG9HB8)H9*-BYb88fdsfsyb(08bfsdybfdsoi4\"	} ";
		
		this.mockHttpClient
		.pushCannedResponse(
				"POST (?:https?://[^/]+|[^/]+)?/+sweng-quiz.appspot.com/search\\b",
				HttpStatus.SC_OK, response, "application/json");
		
		solo.clickOnButton("Search");
		
		getActivityAndWaitFor(TTChecks.QUESTION_SHOWN);
		
		assertTrue("Wrong question", solo.searchText("How many calories are in a banana?"));
		
		Button nextQuestion = (Button) solo
				.getView(R.id.next_question_button);

		solo.clickOnText("Just enough");
		getActivityAndWaitFor(TTChecks.ANSWER_SELECTED);
		
		assertTrue("Next question button is disabled",
				nextQuestion.isEnabled());
		
		String jsonQuestion2 = "{ \"id\": \"7654765\", \"owner\": \"fruitninja\", " +
				"\"question\": \"How many calories are in a apple?\"," +
			      "\"answers\": [ \"Just enough\", \"Too many\" ]," +
			      "\"solutionIndex\": 0," +
			      "\"tags\": [ \"fruit\", \"apple\", \"trivia\" ] }";
		
		String response2 = "{ \"questions\": [ " + jsonQuestion2 + " ]," +
			  "\"next\": \"YG9HB8)H9*-BYb88fdsfsyb(08bfsdybfdsoi4\"	} ";
		
		this.mockHttpClient
		.pushCannedResponse(
				"POST (?:https?://[^/]+|[^/]+)?/+sweng-quiz.appspot.com/search\\b",
				HttpStatus.SC_OK, response2, "application/json");
		
		solo.clickOnView(nextQuestion);
		
		getActivityAndWaitFor(TTChecks.QUESTION_SHOWN);
		
		assertTrue("Wrong question", solo.searchText("How many calories are in a apple?"));
		
		//TEMP END
		
		solo.goBack();
		solo.sleep(DODO);
		
		solo.goBack();
		solo.sleep(DODO);
		
		solo.clickOnView(offline);
		getActivityAndWaitFor(TTChecks.OFFLINE_CHECKBOX_ENABLED);
		
		solo.clickOnView(search_quiz);
		getActivityAndWaitFor(TTChecks.SEARCH_ACTIVITY_SHOWN);

		solo.sleep(DODO);
		
		querryText = (EditText) solo.getView(R.id.searchText);
		solo.enterText((EditText) querryText, querry);
		solo.sleep(DODO);
		
		solo.clickOnButton("Search");
		
		getActivityAndWaitFor(TTChecks.QUESTION_SHOWN);
		
		assertTrue("Wrong question", solo.searchText("How many calories are in a banana?"));
		
		solo.goBack();
		solo.sleep(DODO);
		
		solo.goBack();
		solo.sleep(DODO);
		
		solo.clickOnView(offline);
		
		getActivityAndWaitFor(TTChecks.OFFLINE_CHECKBOX_DISABLED);
		
		
		Button logout = (Button) solo.getView(R.id.button_log);
		solo.clickOnView(logout);

		getActivityAndWaitFor(TTChecks.LOGGED_OUT);

		assertFalse("Button show random question enabled",
				show_random_question.isEnabled());
		assertFalse("Button submit quiz question enabled",
				submit_quiz_question.isEnabled());
		assertFalse("Button search quiz enabled", search_quiz.isEnabled());
		
		solo.clickOnView(logout);
		getActivityAndWaitFor(TTChecks.AUTHENTICATION_ACTIVITY_SHOWN);
	}

	private void getActivityAndWaitFor(final TestCoordinator.TTChecks expected) {
		TestCoordinator.run(getInstrumentation(), new TestingTransaction() {
			@Override
			public void initiate() {
				getActivity();
			}

			@Override
			public void verify(TestCoordinator.TTChecks notification) {
				assertEquals(String.format(
						"Expected notification %s, but received %s", expected,
						notification), expected, notification);
			}

			@Override
			public String toString() {
				return String.format("getActivityAndWaitFor(%s)", expected);
			}
		});
	}

}