package epfl.sweng.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;

import com.jayway.android.robotium.solo.Solo;

import epfl.sweng.R;
import epfl.sweng.authentication.AuthenticationActivity;
import epfl.sweng.authentication.StoreCredential;
import epfl.sweng.testing.TestCoordinator;
import epfl.sweng.testing.TestCoordinator.TTChecks;
import epfl.sweng.testing.TestingTransaction;

public class AuthenticationActivityTest extends ActivityInstrumentationTestCase2<AuthenticationActivity> {
	private Solo solo;
	private String token = "917c2be62cb949b6b47022123b4d0f8e";
	
	public AuthenticationActivityTest() {
		super(AuthenticationActivity.class);
	}

	@Override
	protected void setUp() {
		solo = new Solo(getInstrumentation());
	}

	public void testAuthentification() {
		getActivityAndWaitFor(TTChecks.AUTHENTICATION_ACTIVITY_SHOWN);

		EditText username = (EditText) solo.getView(R.id.gaspar_username);
		EditText pwd = (EditText) solo.getView(R.id.gaspar_password);

		solo.enterText(username, "aa");
		solo.enterText(pwd, "bb");
		Button authentication = (Button) solo.getView(R.id.log_in_tekila);
		solo.clickOnView(authentication);
		solo.sleep(3000);
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