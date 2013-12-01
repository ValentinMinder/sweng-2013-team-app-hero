package epfl.sweng.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;

import com.jayway.android.robotium.solo.Solo;

import epfl.sweng.R;
import epfl.sweng.authentication.StoreCredential;
import epfl.sweng.entry.MainActivity;
import epfl.sweng.servercomm.SwengHttpClientFactory;
import epfl.sweng.test.minimalmock.MockHttpClient;
import epfl.sweng.testing.TestCoordinator;
import epfl.sweng.testing.TestCoordinator.TTChecks;
import epfl.sweng.testing.TestingTransaction;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
	private Solo solo;
	private String token = "917c2be62cb949b6b47022123b4d0f8e";
	public static final int DODO = 3000;
	private MockHttpClient mockHttpClient;

	public MainActivityTest() {
		super(MainActivity.class);
	}

	@Override
	protected void setUp() {
		this.mockHttpClient = new MockHttpClient();
		SwengHttpClientFactory.setInstance(this.mockHttpClient);
		solo = new Solo(getInstrumentation());
		StoreCredential.getInstance().removeSessionId(getActivity().getApplicationContext());
	}
	
	public void testButtonNotEnabled() {
		getActivityAndWaitFor(TTChecks.MAIN_ACTIVITY_SHOWN);
		
		Button show_random_question = (Button) solo.getView(R.id.button2);
		Button submit_quiz_question = (Button) solo.getView(R.id.button1);
		Button search_quiz = (Button) solo.getView(R.id.button3);
		
		assertFalse("Button show random question enabled", show_random_question.isEnabled());
		assertFalse("Button submit quiz question enabled", submit_quiz_question.isEnabled());
		assertFalse("Button search quiz enabled", search_quiz.isEnabled());
	}

	/*public void testMainShowQuestion() {
		//getActivityAndWaitFor(TTChecks.MAIN_ACTIVITY_SHOWN);
		this.mockHttpClient
		.pushCannedResponse(
				"GET (?:https?://[^/]+|[^/]+)?/+sweng-quiz.appspot.com/quizquestions/random\\b",
				HttpStatus.SC_OK,
				"{\"question\": \"What is the answer to life, the universe, and everything?\","
						+ " \"answers\": [\"Forty-two\", \"Twenty-seven\"], \"owner\": \"sweng\","
						+ " \"solutionIndex\": 0, \"tags\": [\"h2g2\", \"trivia\"], \"id\": \"1\" }",
				"application/json");
		Button show = (Button) solo.getView(R.id.button2);
		solo.clickOnView(show);
		getActivityAndWaitFor(TTChecks.QUESTION_SHOWN);
	}
	
	public void testMainEditQuestion() {
		//getActivityAndWaitFor(TTChecks.MAIN_ACTIVITY_SHOWN);
		Button show = (Button) solo.getView(R.id.button1);
		solo.clickOnView(show);
		getActivityAndWaitFor(TTChecks.EDIT_QUESTIONS_SHOWN);
	}
	
	public void testMainSearch() {
		//getActivityAndWaitFor(TTChecks.MAIN_ACTIVITY_SHOWN);
		Button show = (Button) solo.getView(R.id.button3);
		solo.clickOnView(show);
		getActivityAndWaitFor(TTChecks.EDIT_QUESTIONS_SHOWN);
	}*/
	
	/*public void test3() {
		solo.sleep(DODO);
		getActivityAndWaitFor(TTChecks.MAIN_ACTIVITY_SHOWN);
		solo.sleep(DODO*3);
		Button show = (Button) solo.getView(R.id.button1);
		solo.clickOnView(show);
		
		getActivityAndWaitFor(TTChecks.EDIT_QUESTIONS_SHOWN);
		
	
	}*/
	
//	public void test4() {
//		
//		
//		getActivityAndWaitFor(TTChecks.MAIN_ACTIVITY_SHOWN);
//		Button show = (Button) solo.getView(R.id.button3);
//		solo.clickOnView(show);
//		getActivityAndWaitFor(TTChecks.EDIT_QUESTIONS_SHOWN);
//	}
	

	private void getActivityAndWaitFor(final TestCoordinator.TTChecks expected) {
		TestCoordinator.run(getInstrumentation(), new TestingTransaction() {
			@Override
			public void initiate() {
				StoreCredential.getInstance().storeSessionId(token, getActivity().getApplicationContext());
				getActivity();
			}
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