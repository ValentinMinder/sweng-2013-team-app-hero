package epfl.sweng.test.testing;

import epfl.sweng.entry.MainActivity;
import epfl.sweng.test.mock.SwengActivityInstrumentationTestCase;
import epfl.sweng.testing.Toast;

public class ToastTest extends SwengActivityInstrumentationTestCase<MainActivity> {

    public ToastTest() {
        super(MainActivity.class);
    }

    public void testCanCreateToasts() {
        getActivity();
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), "foo", Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(), "bar", Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(), "baz", Toast.LENGTH_SHORT).show();
            }
        });
        
        Toast.waitForToastsToBeUpdated();
        assertTrue("Must display the first toast", solo.searchText("foo"));
        assertTrue("Must display the second toast", solo.searchText("bar"));
        assertTrue("Must display the third toast", solo.searchText("baz"));
        
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Toast.cancelAllToasts();
            }
        });

        Toast.waitForToastsToBeUpdated();
        assertFalse("Must cancel the first toast", solo.searchText("foo"));
        assertFalse("Must cancel the second toast", solo.searchText("bar"));
        assertFalse("Must cancel the third toast", solo.searchText("baz"));
    }

}
