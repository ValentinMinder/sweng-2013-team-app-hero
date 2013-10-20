package epfl.sweng.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;

import com.jayway.android.robotium.solo.Solo;

import epfl.sweng.R;
import epfl.sweng.editquestions.EditQuestionActivity;
import epfl.sweng.testing.TestCoordinator;
import epfl.sweng.testing.TestCoordinator.TTChecks;
import epfl.sweng.testing.TestingTransaction;

public class EditQuestionActivityTest extends ActivityInstrumentationTestCase2<EditQuestionActivity> {
	private Solo solo;
	public static final int ID1 = 2000;
	public static final int ID2 = 2001;
	public static final int DODO = 10000;
	public static final int REM = 1000;
	public EditQuestionActivityTest() {
		super(EditQuestionActivity.class);
	}

	@Override
	protected void setUp() {
		solo = new Solo(getInstrumentation());
	}

	public void testEditQuestion() {
		getActivityAndWaitFor(TTChecks.EDIT_QUESTIONS_SHOWN);
		Button submit = (Button) solo.getView(R.id.submit_question);
		assertFalse("Submit is disabled", submit.isEnabled());
		Button add = (Button) solo.getView(R.id.add);
		solo.clickOnView(add);

		Button correct = (Button) solo.getView(0);
		EditText question = (EditText) solo.getView(R.id.type_question);
		solo.enterText(question, "test question, please ignore");
		EditText tags = (EditText) solo.getView(R.id.tags);
		EditText ans1 = (EditText) solo.getView(ID1);
		EditText ans2 = (EditText) solo.getView(ID2);
		assertFalse("Submit is disabled", submit.isEnabled());

		solo.enterText(ans1, "Reponse1");

		solo.enterText(ans2, "Reponse2");

		solo.enterText(tags, "a, b, c");

		solo.clickOnView(correct);
		submit = (Button) solo.getView(R.id.submit_question);
		solo.clickOnButton("Submit");
		solo.clickOnButton("Submit");
		solo.sleep(DODO);
		Button remove = (Button) solo.getView(REM);
		solo.clickOnView(remove);
		getActivityAndWaitFor(TTChecks.QUESTION_EDITED);


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