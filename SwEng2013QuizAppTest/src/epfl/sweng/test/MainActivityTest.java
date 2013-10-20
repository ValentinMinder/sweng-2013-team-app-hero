package epfl.sweng.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;

import com.jayway.android.robotium.solo.Solo;

import epfl.sweng.R;
import epfl.sweng.entry.MainActivity;
import epfl.sweng.testing.TestCoordinator;
import epfl.sweng.testing.TestCoordinator.TTChecks;
import epfl.sweng.testing.TestingTransaction;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
	private Solo solo;

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
		Button butlog = (Button) solo.getView(R.id.button_log);
		solo.clickOnView(butlog);
		getActivityAndWaitFor(TTChecks.AUTHENTICATION_ACTIVITY_SHOWN);
	}
	


	private void getActivityAndWaitFor(final TestCoordinator.TTChecks expected) {
		TestCoordinator.run(getInstrumentation(), new TestingTransaction() {
			@Override
			public void initiate() {
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