package epfl.sweng.test;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import epfl.sweng.servercomm.SwengHttpClientFactory;
import epfl.sweng.showquestions.ShowQuestionsActivity;
import epfl.sweng.testing.TestingTransaction;
import epfl.sweng.testing.TestingTransactions;
import epfl.sweng.testing.TestingTransactions.TTChecks;

public class MockHttpClientTest extends ActivityInstrumentationTestCase2<ShowQuestionsActivity> {

    protected static final String RANDOM_QUESTION_BUTTON_LABEL = "Show a random question";
    
    private MockHttpClient httpClient;
    private Solo solo;

    public MockHttpClientTest() {
        super(ShowQuestionsActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        httpClient = new MockHttpClient();
        SwengHttpClientFactory.setInstance(httpClient);
        
        solo = new Solo(getInstrumentation());
    }
    
    public void testFetchQuestion() {
        httpClient.pushCannedResponse(
                "GET (?:https?://[^/]+|[^/]+)?/+quizquestions/random\\b",
                200,
                "{\"question\": \"What is the answer to life, the universe, and everything?\", \"answers\": [\"Forty-two\", \"Twenty-seven\"], \"owner\": \"sweng\", \"solutionIndex\": 0, \"tags\": [\"h2g2\", \"trivia\"], \"id\": \"1\" }",
                "application/json");
        
        getActivityAndWaitFor(TTChecks.QUESTION_SHOWN);
        assertTrue("Question must be displayed", solo.searchText("What is the answer to life, the universe, and everything?"));
        assertTrue("Correct answer must be displayed", solo.searchText("Forty-two"));
        assertTrue("Incorrect answer must be displayed", solo.searchText("Twenty-seven"));
    }

    protected void getActivityAndWaitFor(final TestingTransactions.TTChecks expected) {
        TestingTransactions.run(getInstrumentation(), new TestingTransaction() {
            @Override
            public void initiate() {
                getActivity();
            }

            @Override
            public void verify(TestingTransactions.TTChecks notification) {
                assertEquals(String.format("Expected notification %s, but received %s", expected, notification),
                        expected, notification);
            }

            @Override
            public String toString() {
                return String.format("getActivityAndWaitFor(%s)", expected);
            }
        });
    }

}
