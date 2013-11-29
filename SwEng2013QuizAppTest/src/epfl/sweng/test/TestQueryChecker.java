package epfl.sweng.test;

import epfl.sweng.query.QueryChecker;
import junit.framework.TestCase;

public class TestQueryChecker extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testCheckLengthTooLong() {
		String test = "";
		for (int i = 1; i < 600; ++i) {
			test += i;
		}

		QueryChecker q = QueryChecker.getInstance();
		q.setQuery(test);
		assertTrue("Length condition is not respected", !q.checkQuery());
	}
	
	public void TestCheckSpecialChars() {
		QueryChecker q = QueryChecker.getInstance();
		String query = "(a+b)";

		q.setQuery(query);
		assertTrue("No special chars", q.checkQuery());

		query = "(ab%)";
		q.setQuery(query);
		assertTrue("Special chars", !q.checkQuery());

		query = "(a+b* c)";
		q.setQuery(query);
		assertTrue("No special chars 2", q.checkQuery());

	}
	
	public void tesCheckAtLeatOneAlphaNum() {
		QueryChecker q = QueryChecker.getInstance();
		String query = "a+b";

		q.setQuery(query);
		assertTrue("At least one alphanum", q.checkQuery());

		query = "+";
		q.setQuery(query);
		assertTrue("No alphanum", !q.checkQuery());
	}
	
	public void testCheckNested() {
		QueryChecker q = QueryChecker.getInstance();
		String query = "(a + d j + b ( f + a))";

		q.setQuery(query);
		assertTrue("Correctly nested", q.checkQuery());

		query = "((b + g ) (g + d * b + a )) d + ( )";
		q.setQuery(query);
		assertTrue("Correctly nested 2", q.checkQuery());

		query = ") (";
		q.setQuery(query);
		assertTrue("Badly nested", !q.checkQuery());

		query = "( ))";
		q.setQuery(query);
		assertTrue("Badly nested 2", !q.checkQuery());

	}
	
	public void testCheckExpressions() {
		QueryChecker q = QueryChecker.getInstance();
		String[] queries = { "a+b", "a+b+c", "(a*b)+c", "(b+c)*c+d",
				"((a+b)+c)*d a", "a b", "a+b+(b*d)", "a+b (c + d) 1",
				"(a+b)+(c+d)", "(a+(a+(c d))+a+d)", "(acd)", "(abcd)", "abcd",
				"(ab) (dc + hx )* ad", " ((a b) +abd * ((OMG) + WTF)) ",
				"a1b2Cf " };
		for (String s : queries) {
			q.setQuery(s);
			assertTrue(s, q.checkQuery());
		}

		String[] wrongQueries = { "+", "a+", "()(", "(cA+c)+", "(a++b)",
				"(abi + * sd) + 1", "a+b(c+d)", "(a+b)(a+b)", "((a+b)(A1+b))",
				"(abc + dkg) (aa (bc)", "a2b3 (r2d2))", "a²", "a_b" };
		for (String s : wrongQueries) {

			q.setQuery(s);
			assertTrue(s, !q.checkQuery());
		}

	}

}
