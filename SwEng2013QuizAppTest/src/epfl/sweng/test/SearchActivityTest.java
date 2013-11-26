package epfl.sweng.test;

import com.jayway.android.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;
import epfl.sweng.searchquestions.SearchActivity;
import epfl.sweng.servercomm.SwengHttpClientFactory;
import epfl.sweng.test.minimalmock.MockHttpClient;
import epfl.sweng.testing.TestCoordinator;
import epfl.sweng.testing.TestCoordinator.TTChecks;
import epfl.sweng.testing.TestingTransaction;
import epfl.sweng.R;


public class SearchActivityTest extends ActivityInstrumentationTestCase2<SearchActivity> {
	private Solo solo;
	private MockHttpClient httpClient;
	public static final int DODO = 3000;
	
	public SearchActivityTest() {
		super(SearchActivity.class);
	}
	protected void setUp() throws Exception {
		httpClient = new MockHttpClient();
		SwengHttpClientFactory.setInstance(httpClient);
		solo = new Solo(getInstrumentation());
	}

	public void testSearch() {
		getActivityAndWaitFor(TTChecks.SEARCH_ACTIVITY_SHOWN);
		solo.sleep(DODO);
		String querry = "fruit";
		
		solo.enterText((EditText) solo.getView(R.id.searchText), querry);
		//getActivityAndWaitFor(TTChecks.QUERY_EDITED);
		solo.sleep(DODO);
		
		solo.clickOnButton("Search");
		
		
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
