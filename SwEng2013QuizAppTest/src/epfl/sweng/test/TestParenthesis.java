package epfl.sweng.test;

import epfl.sweng.query.Parenthesis;
import junit.framework.TestCase;

public class TestParenthesis extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testCheckRemoveParenthesisOneElement() {
		String query = Parenthesis.removeParenthesisAroundOneElement("((((a))))");
		assertTrue("Just one", "a".equals(query));

		query = Parenthesis.removeParenthesisAroundOneElement("(((a))+((b)))");
		assertTrue("Just two", "(a+b)".equals(query));
		
		query = Parenthesis.removeParenthesisAroundOneElement("((a))+((b))");
		assertTrue("Just two", "a+b".equals(query));

		query = Parenthesis
				.removeParenthesisAroundOneElement("(a)+(b)+(c)+(d)");
		assertTrue("Just four", "a+b+c+d".equals(query));
	}
	
	public void testCheckParenthesis() {
		String query = Parenthesis.parenthesis("");
		assertTrue("Empty", "".equals(query));

		query = Parenthesis.parenthesis("a+b");
		assertTrue("Just OR without parenthesis", "(a+b)".equals(query));

		query = Parenthesis.parenthesis("a+b*c");
		assertTrue("Priority", "(a+(b*c))".equals(query));

		query = Parenthesis.parenthesis("(a+b)*c");
		assertTrue("With parenthesis","((a+b)*c)".equals( query));
		
		query = Parenthesis.parenthesis("((((a+b))*c))");
		assertTrue("Useless parenthesis", "((a+b)*c)".equals(query));

		query = Parenthesis.parenthesis("(((a)+((b)))*c)");
		assertTrue("Useless parenthesis 2 ", "((a+b)*c)".equals(query));

		query = Parenthesis.parenthesis("((((((((a+b))))*c))))");
		assertTrue("Useless parenthesis 3 " + query, "((a+b)*c)".equals(query));

		query = Parenthesis.parenthesis("((((((((((a))+((b))))))*c))))");
		assertTrue("Useless parenthesis 4 ", "((a+b)*c)".equals(query));

		query = Parenthesis.parenthesis("(abc+xyz)*hvd");
		assertTrue("With parenthesis and variable with more than one letter",
				"((abc+xyz)*hvd)".equals(query));

		query = Parenthesis.parenthesis("a+(b*c)+(v+d)*c+g+(a*b+c*(g+d))");
		assertTrue("Big query",
				"((((a+(b*c))+((v+d)*c))+g)+((a*b)+(c*(g+d))))".equals(query));
		
		// handling multiple spaces
		query = Parenthesis.parenthesis("a b");
		assertTrue("handling implicit spaces: 2 var",
				"(a*b)".equals(query));
		
		query = Parenthesis.parenthesis("a b c");
		assertTrue("handling implicit spaces: 3 var, 2 spaces",
				"((a*b)*c)".equals(query));
		
		query = Parenthesis.parenthesis("a b+c");
		assertTrue("handling implicit spaces: 3 var, 1 space 1",
				"((a*b)+c)".equals(query));
		
		query = Parenthesis.parenthesis("a+b c");
		assertTrue("handling implicit spaces: 3 var, 1 space 2",
				"(a+(b*c))".equals(query));
		
		query = Parenthesis.parenthesis("a*b c");
		assertTrue("handling implicit spaces: 3 var, 1 space 3",
				"((a*b)*c)".equals(query));
		
		query = Parenthesis.parenthesis("a b c d");
		assertTrue("handling implicit spaces: 4 var, 3 spaces",
				"(((a*b)*c)*d)".equals(query));
		
		query = Parenthesis.parenthesis("a b+c d");
		assertTrue("handling implicit spaces: 4 var, 2 spaces",
				"((a*b)+(c*d))".equals(query));
		
		// handling composed queries
		query = Parenthesis.parenthesis("a+b c+d");
		assertTrue("composed queries 4 var, 2 blocks, +*+, no parent",
				"((a+(b*c))+d)".equals(query));
		
		query = Parenthesis.parenthesis("a+b (c+d)");
		assertTrue("composed queries 4 var, 2 blocks, +*+, parent last group",
				"(a+(b*(c+d)))".equals(query));
		
		query = Parenthesis.parenthesis("(a+b) c+d");
		assertTrue("composed queries 4 var, 2 blocks, +*+, parent first group",
				"(((a+b)*c)+d)".equals(query));
		
		query = Parenthesis.parenthesis("(a+b) (c+d)");
		assertTrue("composed queries 4 var, 2 blocks, +*+, parent both groups",
				"((a+b)*(c+d))".equals(query));
		
		query = Parenthesis.parenthesis("a b+c d");
		assertTrue("composed queries 4 var, 2 blocks, *+*, no parent",
				"((a*b)+(c*d))".equals(query));
		
		query = Parenthesis.parenthesis("a b+(c d)");
		assertTrue("composed queries 4 var, 2 blocks, *+*, parent last group",
				"((a*b)+(c*d))".equals(query));
		
		query = Parenthesis.parenthesis("(a b)+c d");
		assertTrue("composed queries 4 var, 2 blocks, *+*, parent first group",
				"((a*b)+(c*d))".equals(query));
		
		query = Parenthesis.parenthesis("(a b)+(c d)");
		assertTrue("composed queries 4 var, 2 blocks, *+*, parent both groups",
				"((a*b)+(c*d))".equals(query));
		
	}

}
