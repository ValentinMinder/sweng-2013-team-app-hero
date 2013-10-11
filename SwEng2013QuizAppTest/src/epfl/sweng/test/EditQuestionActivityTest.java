package epfl.sweng.test;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import epfl.sweng.R;
import epfl.sweng.editquestions.EditQuestionActivity;
import epfl.sweng.testing.TestingTransaction;
import epfl.sweng.testing.TestingTransactions;
import epfl.sweng.testing.TestingTransactions.TTChecks;

public class EditQuestionActivityTest extends
ActivityInstrumentationTestCase2<EditQuestionActivity> {
	private Solo solo;

	public EditQuestionActivityTest() {
		super(EditQuestionActivity.class);
	}

	@Override
	  protected void setUp() {
	    solo = new Solo(getInstrumentation());
	  }

	public void testEditQuestion() {
		getActivityAndWaitFor(TTChecks.EDIT_QUESTIONS_SHOWN);
		solo.clickOnButton(R.id.submit_question);
		
		
	
	}


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