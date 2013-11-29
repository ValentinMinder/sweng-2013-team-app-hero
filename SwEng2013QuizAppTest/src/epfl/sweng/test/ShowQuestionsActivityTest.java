package epfl.sweng.test;

import org.apache.http.HttpStatus;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.ListView;

import com.jayway.android.robotium.solo.Solo;

import epfl.sweng.R;
import epfl.sweng.caching.Cache;
import epfl.sweng.servercomm.SwengHttpClientFactory;
import epfl.sweng.showquestions.ShowQuestionsActivity;
import epfl.sweng.test.minimalmock.MockHttpClient;
import epfl.sweng.testing.TestCoordinator;
import epfl.sweng.testing.TestCoordinator.TTChecks;
import epfl.sweng.testing.TestingTransaction;

public class ShowQuestionsActivityTest extends
ActivityInstrumentationTestCase2<ShowQuestionsActivity> {
	private Solo solo;
	  public static final int DODO = 3000;
      private MockHttpClient httpClient;

      public ShowQuestionsActivityTest() {
              super(ShowQuestionsActivity.class);
      }

      @Override
      protected void setUp() {
              httpClient = new MockHttpClient();
              SwengHttpClientFactory.setInstance(httpClient);
              solo = new Solo(getInstrumentation());
      }
      public void testShowQuestion() {
    	  Cache.setDirectoryFiles(getActivity().getApplicationContext().getFilesDir().getAbsolutePath());
              httpClient.pushCannedResponse(
                              "GET (?:https?://[^/]+|[^/]+)?/+quizquestions/random\\b",
                              HttpStatus.SC_OK,
                              "{\"question\": \"What is the answer to life, the universe, and everything?\","
                                              + " \"answers\": [\"Forty-two\", \"Twenty-seven\"], \"owner\": \"sweng\","
                                              + " \"solutionIndex\": 0, \"tags\": [\"h2g2\", \"trivia\"], \"id\": \"1\" }",
                              "application/json");

              getActivityAndWaitFor(TTChecks.QUESTION_SHOWN);
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