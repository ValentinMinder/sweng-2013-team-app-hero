package epfl.sweng.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.jayway.android.robotium.solo.Solo;

import epfl.sweng.R;
import epfl.sweng.showquestions.ShowQuestionsActivity;
import epfl.sweng.testing.TestingTransaction;
import epfl.sweng.testing.TestingTransactions;
import epfl.sweng.testing.TestingTransactions.TTChecks;

public class ShowQuestionsActivityTest extends
ActivityInstrumentationTestCase2<ShowQuestionsActivity> {
	private Solo solo;

	public ShowQuestionsActivityTest() {
		super(ShowQuestionsActivity.class);
	}

	@Override
	protected void setUp() {
		solo = new Solo(getInstrumentation());
	}
	public void testShowQuestion(){
		getActivityAndWaitFor(TTChecks.QUESTION_SHOWN);
		solo.sleep(3000);	
		ListView answers = (ListView) solo.getView(R.id.multiple_choices);
		//Button nextQuesiton = (Button) solo.getButton("Next question");
		int i = 0;
		TextView correctness;
		do{
			String answer = (String) answers.getItemAtPosition(i);
			solo.clickOnText(answer);
			correctness = (TextView) solo.getView(R.id.correctness);
			i++;
		} while(correctness.toString().equals(R.string.right_answer));
		getActivityAndWaitFor(TTChecks.ANSWER_SELECTED);

	}
	/*public void testShowQuestion() {
		getActivityAndWaitFor(TTChecks.QUESTION_SHOWN);

		solo.sleep(3000);	
		assertTrue("Question is displayed", solo.searchText("What is the answer to Life, the universe and everything?"));
		assertTrue("Correct answer is displayed", solo.searchText("Forty-two"));
		assertTrue("Incorrect answer is displayed", solo.searchText("Twenty-seven"));

		Button nextQuestionButton = solo.getButton("Next question");
		assertFalse("Next question button is disabled", nextQuestionButton.isEnabled());
	}
*/
	private void getActivityAndWaitFor(final TestingTransactions.TTChecks expected) {
		TestingTransactions.run(getInstrumentation(), new TestingTransaction() {
			@Override
			public void initiate() {
				getActivity();
			}

			@Override
			public void verify(TestingTransactions.TTChecks notification) {
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