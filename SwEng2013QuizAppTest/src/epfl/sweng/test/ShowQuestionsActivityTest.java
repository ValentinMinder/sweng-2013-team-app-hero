package epfl.sweng.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.jayway.android.robotium.solo.Solo;

import epfl.sweng.R;
import epfl.sweng.authentication.StoreCredential;
import epfl.sweng.showquestions.ShowQuestionsActivity;
import epfl.sweng.testing.TestCoordinator;
import epfl.sweng.testing.TestCoordinator.TTChecks;
import epfl.sweng.testing.TestingTransaction;

public class ShowQuestionsActivityTest extends
ActivityInstrumentationTestCase2<ShowQuestionsActivity> {
	private Solo solo;
	public static final int DODO = 3000;
	//private MockHttpClient httpClient;
	private String token = "68ecb58237a84ef2b2bc8d7737ff918b";


	public ShowQuestionsActivityTest() {
		super(ShowQuestionsActivity.class);
	}

	@Override
	protected void setUp() {
		//		httpClient = new MockHttpClient();
		//		SwengHttpClientFactory.setInstance(httpClient);

		solo = new Solo(getInstrumentation());
	}
	//
	//	public void testShowQuestionSearch() {
	//		httpClient
	//				.pushCannedResponse(
	//						"GET (?:https?://[^/]+|[^/]+)?/+sweng-quiz.appspot.com/search\\b",
	//						HttpStatus.SC_OK,
	//						"{\"query\": \"(banana + garlic) fruit\","
	//								+ "\"from\": \"YG9HB8)H9*-BYb88fdsfsyb(08bfsdybfdsoi4\"}",
	//						"application/json");
	//		//getActivityAndWaitFor(TTChecks.OFFLINE_CHECKBOX_ENABLED);
	//		getActivityAndWaitFor(TTChecks.QUESTION_SHOWN);
	//		solo.sleep(DODO);
	//		ListView answers = (ListView) solo.getView(R.id.multiple_choices);
	//		int i = 0;
	//		TextView correctness;
	//		do {
	//			String answer = (String) answers.getItemAtPosition(i);
	//			solo.clickOnText(answer);
	//			correctness = (TextView) solo.getView(R.id.correctness);
	//			i++;
	//		} while (correctness.toString().equals(R.string.right_answer));
	//		getActivityAndWaitFor(TTChecks.ANSWER_SELECTED);
	//
	//		Button bouton = (Button) solo.getView(R.id.next_question_button);
	//		solo.clickOnView(bouton);
	//		solo.sleep(DODO);
	//		answers = (ListView) solo.getView(R.id.multiple_choices);
	//		String answer = (String) answers.getItemAtPosition(1);
	//		solo.clickOnText(answer);
	//
	//		getActivityAndWaitFor(TTChecks.ANSWER_SELECTED);
	//	}
	//
	public void testShowQuestion() {
		getActivityAndWaitFor(TTChecks.QUESTION_SHOWN);
		StoreCredential.getInstance().storeSessionId(token, getActivity());

		Button nextQuestionButton = (Button) solo.getView(R.id.next_question_button);
		assertFalse("Next question button is disabled",
				nextQuestionButton.isEnabled()); 
		ListView answers = (ListView) solo.getView(R.id.multiple_choices);
		int i = 0;
		solo.sleep(DODO);

		do {
			String answer = (String) answers.getItemAtPosition(i);
			solo.clickOnText(answer);
			getActivityAndWaitFor(TTChecks.ANSWER_SELECTED);
			i++;
			solo.sleep(DODO);
		} while (!nextQuestionButton.isEnabled());

		assertTrue("Next question button is enabled",
				nextQuestionButton.isEnabled()); 
		Button bouton = (Button) solo.getView(R.id.next_question_button);
		solo.clickOnView(bouton);

	}


	//	public void testShowQuestion() {
	//		getActivityAndWaitFor(TTChecks.QUESTION_SHOWN);
	//
	//		solo.sleep(3000); assertTrue("Question is displayed",
	//				solo.searchText("What is the answer to Life, the universe and everything?"
	//						)); assertTrue("Correct answer is displayed",
	//								solo.searchText("Forty-two"));
	//						assertTrue("Incorrect answer is displayed",
	//								solo.searchText("Twenty-seven"));
	//
	//						Button nextQuestionButton = solo.getButton("Next question");
	//						assertFalse("Next question button is disabled",
	//								nextQuestionButton.isEnabled()); }

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