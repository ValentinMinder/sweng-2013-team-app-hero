package epfl.sweng.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.CheckBox;
import android.widget.EditText;

import com.jayway.android.robotium.solo.Solo;

import epfl.sweng.R;
import epfl.sweng.authentication.StoreCredential;
import epfl.sweng.caching.CacheException;
import epfl.sweng.patterns.ProxyHttpClient;
import epfl.sweng.searchquestions.SearchActivity;
import epfl.sweng.servercomm.SwengHttpClientFactory;
import epfl.sweng.test.minimalmock.MockHttpClient;
import epfl.sweng.testing.TestCoordinator;
import epfl.sweng.testing.TestCoordinator.TTChecks;
import epfl.sweng.testing.TestingTransaction;


public class SearchActivityTest extends ActivityInstrumentationTestCase2<SearchActivity> {
	private Solo solo;
	private MockHttpClient httpClient;
	public static final int DODO = 3000;
	private String token = "68ecb58237a84ef2b2bc8d7737ff918b";
	
	public SearchActivityTest() {
		super(SearchActivity.class);
	}
	protected void setUp() throws Exception {
		httpClient = new MockHttpClient();
		SwengHttpClientFactory.setInstance(httpClient);
		solo = new Solo(getInstrumentation());
	}

	public void testSearch() {
		solo.sleep(DODO);
		getActivityAndWaitFor(TTChecks.SEARCH_ACTIVITY_SHOWN);
		assertFalse("button is not enabled", solo.getButton("Search").isEnabled());
		
		
		String querry = "fruit";
		EditText querryText = (EditText) solo.getView(R.id.searchText);
		solo.enterText((EditText) querryText, querry);
		solo.sleep(DODO);
		assertTrue("querry edited", "fruit".toString().equals(querryText.getText()));
		
		
		//getActivityAndWaitFor(TTChecks.QUERY_EDITED);
		assertTrue("button is enabled", solo.getButton("Search").isEnabled());
		
		
		StoreCredential.getInstance().storeSessionId(token, getActivity());
		solo.sleep(DODO);
		//solo.clickOnButton("Search");
		solo.sleep(DODO);
		
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
