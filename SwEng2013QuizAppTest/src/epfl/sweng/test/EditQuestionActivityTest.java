package epfl.sweng.test;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
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
	public static final int DODO = 1000;
	public static final int REM = 1000;
	
	
	public EditQuestionActivityTest() {
		super(EditQuestionActivity.class);
	}

	@Override
	protected void setUp() {
		solo = new Solo(getInstrumentation());
	}

	public void testQuestionEdited(){
		getActivityAndWaitFor(TTChecks.EDIT_QUESTIONS_SHOWN);
		solo.sleep(DODO*3);
		Button submit = (Button) solo.getView(R.id.submit_question);
		assertFalse("Submit is disabled", submit.isEnabled());
		
		Button add = (Button) solo.getView(R.id.add);
		solo.clickOnView(add);
		
		
		Button correct = (Button) solo.getView(0);
		//Button notCorrect = (Button) solo.getView(1);
		solo.sleep(DODO);
		assertTrue("correct Typo before clicking", correct.getText().equals("✘") /*&& notCorrect.getText().equals("✘")*/);
		
		solo.clickOnView(correct);
		solo.sleep(DODO);
		assertTrue("Correct typo", correct.getText().equals("✔") /*&& notCorrect.getText().equals("✘")*/);
		
		assertFalse("Submit is disabled", submit.isEnabled());
		
		
		String s = "test question, please ignore";
		EditText question = (EditText) solo.getView(R.id.type_question);
		solo.enterText(question,s );
		Log.e("Test",s+" = " +question.getText() + " " + question.getText().equals(s));
		solo.sleep(DODO);
		assertTrue("title edited", question.getText().toString().equals("test question, please ignore"));
		
		
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
		solo.clickOnView(submit);
		getActivityAndWaitFor(TTChecks.NEW_QUESTION_SUBMITTED);
		solo.sleep(DODO*10);
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