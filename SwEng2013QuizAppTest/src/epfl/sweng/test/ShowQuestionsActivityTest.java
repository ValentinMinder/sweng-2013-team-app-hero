package epfl.sweng.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;

import com.jayway.android.robotium.solo.Solo;

import epfl.sweng.showquestions.ShowQuestionsActivity;
import epfl.sweng.testing.TestingTransaction;
import epfl.sweng.testing.TestingTransactions;
import epfl.sweng.testing.TestingTransactions.TTChecks;

public class ShowQuestionsActivityTest extends
ActivityInstrumentationTestCase2<ShowQuestionsActivity> {
	  private Solo solo;

	  public ShowQuestionsActivityTest() {
	    super(ShowQuestionsActivity.class);
	  }

	  @Override
	  protected void setUp() {
	    solo = new Solo(getInstrumentation());
	  }

	  public void testShowQuestion() {
	    getActivityAndWaitFor(TTChecks.QUESTION_SHOWN);
	    assertTrue("Question is displayed", solo.searchText("What is the answer to Life, the universe and everything?"));
	    assertTrue("Correct answer is displayed", solo.searchText("Forty-two"));
	    assertTrue("Incorrect answer is displayed", solo.searchText("Twenty-seven"));

	    Button nextQuestionButton = solo.getButton("Next question");
	    assertFalse("Next question button is disabled", nextQuestionButton.isEnabled());
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