package epfl.sweng.test;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.http.HttpStatus;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.jayway.android.robotium.solo.Solo;

import epfl.sweng.R;
import epfl.sweng.caching.Cache;
import epfl.sweng.caching.CacheException;
import epfl.sweng.editquestions.EditQuestionActivity;
import epfl.sweng.patterns.ProxyHttpClient;
import epfl.sweng.quizquestions.QuizQuestion;
import epfl.sweng.servercomm.SwengHttpClientFactory;
import epfl.sweng.test.minimalmock.MockHttpClient;
import epfl.sweng.testing.TestCoordinator;
import epfl.sweng.testing.TestCoordinator.TTChecks;
import epfl.sweng.testing.TestingTransaction;

public class EditQuestionActivityTest extends ActivityInstrumentationTestCase2<EditQuestionActivity> {
	private Solo solo;
	public static final int ID1 = 2000;
	public static final int ID2 = 2001;
	public static final int DODO = 1000;
	public static final int REM = 1000;
	private MockHttpClient mockHttpClient;
	
	
	public EditQuestionActivityTest() {
		super(EditQuestionActivity.class);
	}

	@Override
	protected void setUp() {
		solo = new Solo(getInstrumentation());
		this.mockHttpClient = new MockHttpClient();
		SwengHttpClientFactory.setInstance(this.mockHttpClient);
	}

	public void testQuestionEdited(){
		
//		getActivityAndWaitFor(TTChecks.MAIN_ACTIVITY_SHOWN);
//		solo.clickOnView(solo.getView(R.id.button1));
		
		getActivityAndWaitFor(TTChecks.EDIT_QUESTIONS_SHOWN);
		
		solo.sleep(DODO*4);
		
		Button add = (Button) solo.getView(R.id.add);
		solo.clickOnView(add);
		
		Button correct = (Button) solo.getView(0);
		//Button notCorrect = (Button) solo.getView(1);
		solo.sleep(DODO);
		assertTrue("correct Typo before clicking", correct.getText().equals("✘") /*&& notCorrect.getText().equals("✘")*/);
		
		solo.clickOnView(correct);
		solo.sleep(DODO);
		assertTrue("Correct typo", correct.getText().equals("✔") /*&& notCorrect.getText().equals("✘")*/);
		
		Button submit = (Button) solo.getView(R.id.submit_question);
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
		ArrayList<String> answer = new ArrayList<String>();
		answer.add("correct One");
		answer.add("wrong One");
		HashSet<String> tags1 = new HashSet<String>();
		tags1.add("test");
		
		
		QuizQuestion Q = new QuizQuestion(s, answer,0, tags1, 0, "moi");
		Cache.setDirectoryFiles(getActivity().getApplicationContext().getFilesDir().getAbsolutePath());
		this.mockHttpClient.pushCannedResponse("POST (?:https?://[^/]+|[^/]+)?/+sweng-quiz.appspot.com/quizquestions\\b", HttpStatus.SC_OK, Q.toPostEntity(), "application/json");
		
		solo.sleep(DODO);
		
		solo.clickOnView(submit);
		getActivityAndWaitFor(TTChecks.NEW_QUESTION_SUBMITTED);

		solo.sleep(DODO*10);
		Button remove = (Button) solo.getView(REM);
		solo.clickOnView(remove);
		//getActivityAndWaitFor(TTChecks.QUESTION_EDITED);
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