package epfl.sweng.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;

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
		//Button submit = solo.getButton(solo.getString(R.string.plus));
		Button submit = (Button) solo.getView(R.id.submit_question);
		assertFalse("Submit is disabled",submit.isEnabled());
		solo.clickOnView((Button) solo.getView(R.id.add));
		solo.clickOnView((Button) solo.getView(R.id.add));
		solo.enterText(R.id.type_question, "Test Question");
		solo.enterText(2000, "Reponse1");
		solo.enterText(2001, "Reponse2");
		solo.enterText(R.id.tags, "a, b, c");
		solo.clickOnView((Button)solo.getView(1));
		solo.clickOnView((Button)solo.getView(1003));
		solo.clickOnView(submit);

		
		
		
	
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