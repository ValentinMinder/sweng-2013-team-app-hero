package epfl.sweng.test;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;

import com.jayway.android.robotium.solo.Solo;

import epfl.sweng.R;
import epfl.sweng.authentication.StoreCredential;
import epfl.sweng.entry.MainActivity;
import epfl.sweng.showquestions.ShowQuestionsActivity;
import epfl.sweng.testing.TestCoordinator;
import epfl.sweng.testing.TestCoordinator.TTChecks;
import epfl.sweng.testing.TestingTransaction;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
	private Solo solo;
	private String token = "917c2be62cb949b6b47022123b4d0f8e";
	public static final int DODO = 3000;


	public MainActivityTest() {
		super(MainActivity.class);
	}

	@Override
	protected void setUp() {
		solo = new Solo(getInstrumentation());
	}

/*	public void testMain1() {
		getActivityAndWaitFor(TTChecks.MAIN_ACTIVITY_SHOWN);
		solo.clickOnButton(R.string.show_random_question);

	}
	public void testMain2() {
		getActivityAndWaitFor(TTChecks.MAIN_ACTIVITY_SHOWN);
		solo.clickOnButton(R.string.submit_quiz_question);

	}*/
	public void testMain3() {
		getActivityAndWaitFor(TTChecks.MAIN_ACTIVITY_SHOWN);
		solo.sleep(DODO);
		Button butlog = (Button) solo.getView(R.id.button_log);
		solo.clickOnView(butlog);
		getActivityAndWaitFor(TTChecks.LOGGED_OUT);
		
	}
	
	public void testSuccessAuthentification() {
		getActivityAndWaitFor(TTChecks.MAIN_ACTIVITY_SHOWN);
		Button show = (Button) solo.getView(R.id.button2);
		solo.clickOnView(show);
		getActivityAndWaitFor(TTChecks.SEARCH_ACTIVITY_SHOWN);
	}
	public void test3() {
		getActivityAndWaitFor(TTChecks.MAIN_ACTIVITY_SHOWN);
		Button show = (Button) solo.getView(R.id.button1);
		Activity i = getActivity();
		solo.clickOnView(show);
		//getActivityAndWaitFor(TTChecks.EDIT_QUESTIONS_SHOWN);
		
		
		
	}
	public void test4() {
		
		
		getActivityAndWaitFor(TTChecks.MAIN_ACTIVITY_SHOWN);
		Button show = (Button) solo.getView(R.id.button3);
		solo.clickOnView(show);
		getActivityAndWaitFor(TTChecks.EDIT_QUESTIONS_SHOWN);
	}
	

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