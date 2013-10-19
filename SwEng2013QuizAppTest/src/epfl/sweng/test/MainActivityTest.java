package epfl.sweng.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import com.jayway.android.robotium.solo.Solo;
import epfl.sweng.R;
import epfl.sweng.entry.MainActivity;
import epfl.sweng.testing.TestingTransaction;
import epfl.sweng.testing.TestingTransactions;
import epfl.sweng.testing.TestingTransactions.TTChecks;

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