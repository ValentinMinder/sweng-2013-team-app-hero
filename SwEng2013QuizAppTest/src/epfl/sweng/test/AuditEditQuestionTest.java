package epfl.sweng.test;

import android.test.ActivityInstrumentationTestCase2;
import com.jayway.android.robotium.solo.Solo;
import epfl.sweng.editquestions.EditQuestionActivity;
import epfl.sweng.servercomm.SwengHttpClientFactory;
import epfl.sweng.test.minimalmock.MockHttpClient;
import epfl.sweng.testing.TestCoordinator;
import epfl.sweng.testing.TestCoordinator.TTChecks;
import epfl.sweng.testing.TestingTransaction;

public class AuditEditQuestionTest extends
		ActivityInstrumentationTestCase2<EditQuestionActivity> {

	private Solo solo;

	public static final int ID1 = 2000;
	public static final int ID2 = 2001;
	public static final int DODO = 1000;
	public static final int REM = 1000;
	private MockHttpClient mockHttpClient;

	public AuditEditQuestionTest() {
		super(EditQuestionActivity.class);
	}

	@Override
	protected void setUp() {
		this.mockHttpClient = new MockHttpClient();
		SwengHttpClientFactory.setInstance(this.mockHttpClient);
		solo = new Solo(getInstrumentation());
	}

	public void testAudit() {

		// getActivityAndWaitFor(TTChecks.MAIN_ACTIVITY_SHOWN);
		// solo.clickOnView(solo.getView(R.id.button1));

		getActivityAndWaitFor(TTChecks.EDIT_QUESTIONS_SHOWN);
		assertTrue(getActivity().auditErrors() == 0);
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
