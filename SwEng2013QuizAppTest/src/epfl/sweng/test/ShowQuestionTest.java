package epfl.sweng.test;

import org.apache.http.HttpStatus;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;
import android.widget.TextView;

import com.jayway.android.robotium.solo.Solo;

import epfl.sweng.R;
import epfl.sweng.editquestions.EditQuestionActivity;
import epfl.sweng.servercomm.SwengHttpClientFactory;
import epfl.sweng.showquestions.ShowQuestionsActivity;
import epfl.sweng.test.minimalmock.MockHttpClient;
import epfl.sweng.testing.TestCoordinator;
import epfl.sweng.testing.TestCoordinator.TTChecks;
import epfl.sweng.testing.TestingTransaction;

public class ShowQuestionTest extends
		ActivityInstrumentationTestCase2<ShowQuestionsActivity> {

	private Solo solo;
	public static final int ID1 = 2000;
	public static final int ID2 = 2001;
	public static final int DODO = 1000;
	public static final int REM = 1000;
	private MockHttpClient mockHttpClient;

	public ShowQuestionTest() {
		super(ShowQuestionsActivity.class);
	}

	@Override
	protected void setUp() {
		this.mockHttpClient = new MockHttpClient();
		SwengHttpClientFactory.setInstance(this.mockHttpClient);
		solo = new Solo(getInstrumentation());
	}

	public void testMalformedJSON() {

		this.mockHttpClient
		.pushCannedResponse(
				"GET (?:https?://[^/]+|[^/]+)?/+sweng-quiz.appspot.com/quizquestions/random\\b",
				HttpStatus.SC_BAD_REQUEST, null, "application/json");
		getActivityAndWaitFor(TTChecks.QUESTION_SHOWN);

		ListView possibleAnswers = (ListView) solo.getView(R.id.multiple_choices);
		TextView questionTitle = (TextView) solo.getView(R.id.displayed_text);
		TextView tags = (TextView) solo.getView(R.id.tags);
		
		assertTrue(possibleAnswers.getAdapter().isEmpty());
		assertTrue("".equals(questionTitle.getText()));
		assertTrue("".equals(tags.getText()));

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
