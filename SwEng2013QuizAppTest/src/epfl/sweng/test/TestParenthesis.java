package epfl.sweng.test;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import epfl.sweng.utils.Parenthesis;

public class TestParenthesis {
	@Test
	public void checkParenthesis() {
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

		query = Parenthesis.parenthesis("(abc+xyz)*hvd");
		assertTrue("With parenthesis and variables with more than one letter",
				query.equals("((abc+xyz)*hvd)"));

		query = Parenthesis.parenthesis("a+(b*c)+(v+d)*c+g+(a*b+c*(g+d))");
		assertTrue("Big query",
				query.equals("((((a+(b*c))+((v+d)*c))+g)+((a*b)+(c*(g+d))))"));
	}
}
