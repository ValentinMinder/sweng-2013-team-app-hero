package epfl.sweng.test;

import epfl.sweng.query.Parenthesis;
import junit.framework.TestCase;

public class TestParenthesis extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testCheckRemoveParenthesisOneElement() {
		String query = Parenthesis.removeParenthesisAroundOneElement("((((a))))");
		assertTrue("Just one", query.equals("a"));

		query = Parenthesis.removeParenthesisAroundOneElement("(((a))+((b)))");
		assertTrue("Just two", query.equals("(a+b)"));
		
		query = Parenthesis.removeParenthesisAroundOneElement("((a))+((b))");
		assertTrue("Just two", query.equals("a+b"));

		query = Parenthesis
				.removeParenthesisAroundOneElement("(a)+(b)+(c)+(d)");
		assertTrue("Just four", query.equals("a+b+c+d"));
	}
	
	public void testCheckParenthesis() {
		String query = Parenthesis.parenthesis("");
		assertTrue("Empty", query.equals(""));

		query = Parenthesis.parenthesis("a+b");
		assertTrue("Just OR without parenthesis", query.equals("(a+b)"));

		query = Parenthesis.parenthesis("a+b*c");
		assertTrue("Priority", query.equals("(a+(b*c))"));

		query = Parenthesis.parenthesis("(a+b)*c");
		assertTrue("With parenthesis", query.equals("((a+b)*c)"));
		
		query = Parenthesis.parenthesis("((((a+b))*c))");
		assertTrue("Useless parenthesis", query.equals("((a+b)*c)"));

		query = Parenthesis.parenthesis("(((a)+((b)))*c)");
		assertTrue("Useless parenthesis 2 ", query.equals("((a+b)*c)"));

		query = Parenthesis.parenthesis("((((((((a+b))))*c))))");
		assertTrue("Useless parenthesis 3 ", query.equals("((a+b)*c)"));

		query = Parenthesis.parenthesis("((((((((((a))+((b))))))*c))))");
		assertTrue("Useless parenthesis 4 ", query.equals("((a+b)*c)"));

		query = Parenthesis.parenthesis("(abc+xyz)*hvd");
		assertTrue("With parenthesis and variable with more than one letter",
				query.equals("((abc+xyz)*hvd)"));

		query = Parenthesis.parenthesis("a+(b*c)+(v+d)*c+g+(a*b+c*(g+d))");
		assertTrue("Big query",
				query.equals("((((a+(b*c))+((v+d)*c))+g)+((a*b)+(c*(g+d))))"));
	}

}
