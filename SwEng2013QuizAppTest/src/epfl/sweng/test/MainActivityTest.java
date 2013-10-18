package epfl.sweng.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;

import com.jayway.android.robotium.solo.Solo;

import epfl.sweng.R;
import epfl.sweng.entry.MainActivity;
import epfl.sweng.testing.TestingTransaction;
import epfl.sweng.testing.TestingTransactions;
import epfl.sweng.testing.TestingTransactions.TTChecks;

public class MainActivityTest extends
ActivityInstrumentationTestCase2<MainActivity> {
	private Solo solo;

	public MainActivityTest() {
		super(MainActivity.class);
	}

	@Override
	protected void setUp() {
		solo = new Solo(getInstrumentation());
	}

	public void testMain1() {
		getActivityAndWaitFor(TTChecks.MAIN_ACTIVITY_SHOWN);
		Button button1 = (Button) solo.getView(R.id.button1);
		solo.clickOnView(button1);

	}
	public void testMain2() {
		getActivityAndWaitFor(TTChecks.MAIN_ACTIVITY_SHOWN);
		Button button2 = (Button) solo.getView(R.id.button2);
		solo.clickOnView(button2);

	}
	public void testMain3() {
		getActivityAndWaitFor(TTChecks.MAIN_ACTIVITY_SHOWN);
		Button butlog = (Button) solo.getView(R.id.button_log);
		solo.clickOnView(butlog);

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