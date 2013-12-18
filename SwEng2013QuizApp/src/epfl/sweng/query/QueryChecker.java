package epfl.sweng.query;

import java.util.EmptyStackException;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

final public class QueryChecker {
	private static QueryChecker instance = null;
	private String query;
	private static String patternIncluded = "^[[a-zA-Z0-9]\\s()*+]+$";
	private final static int MAXLENGTH = 500;

	private QueryChecker() {
		this.query = null;
	}
	

	private boolean checkNested() {
		Stack<String> stack = new Stack<String>();
		for (int i = 0; i < query.length(); ++i) {
			switch (query.charAt(i)) {
				case '(':
					stack.push("(");
					break;
				case ')':
					try {
						stack.pop();
					} catch (EmptyStackException e) {
						Logger.getLogger("epfl.sweng.query").log(Level.INFO,
							"checkNested Empty Stack", e);
						return false;
					}
					break;
				default:
					break;
			}
		}
		return stack.size() == 0;
	}

	/**
	 * should only be called by checkQuery once the pre-condition are
		// checked
	 * @return
	 */
	private boolean checkValid() {

		// remove useless space, replace useful one by the * operator
		String pattern = "([[a-zA-Z0-9]])(\\s+)([\\w])";
		String queryClone = query;
		queryClone = queryClone.replaceAll(pattern, "$1*$3");
		pattern = "([[a-zA-Z0-9]])(\\s+)([(])";
		queryClone = queryClone.replaceAll(pattern, "$1*$3");
		pattern = "([)])(\\s+)([a-zA-Z0-9])";
		queryClone = queryClone.replaceAll(pattern, "$1*$3");
		pattern = "([)])(\\s+)([(])";
		queryClone = queryClone.replaceAll(pattern, "$1*$3");
		queryClone = queryClone.replaceAll("\\s+", "");

		// beginSub, endSub delimiter of the queries delimited by parenthesis
		// countParenthesisFound count how many nested level of parenthesis we
		// are
		// overall is the return boolean
		// parenthesisBlockAtLeft indicates whether we already checked a block
		// delimited by
		// parenthesis
		// firstParenthesisFound indicates whether we already checked a block or
		// not
		int beginSub = 0;
		int countParenthesisFound = 0;
		int endSub = 0;
		boolean overall = true;
		boolean parenthesisBlockAtLeft = false;
		boolean firstParenthesisFound = false;

		for (int i = 0; i < queryClone.length(); ++i) {
			char c = queryClone.charAt(i);
			// System.out.println("Handling char " + c);
			if (c == '(') {
				beginSub = i;
				countParenthesisFound++;

				if (i > 0 && queryClone.charAt(i - 1) == ')') {
					return false;
				}
				// if this is the first time we encounter a parenthesis, and we
				// are not at the beginning
				// of the query, we need to first check the first part
				if (!firstParenthesisFound
						&& i > 0
						&& (queryClone.charAt(i - 1) == '+' || queryClone
								.charAt(i - 1) == '*')) {
					boolean result = checkBlock(queryClone.substring(0, i - 1));
					overall = overall && result;
				} else if (!firstParenthesisFound && i > 0) {
					// we have something like a+b(d+e)
					return false;
				}
				firstParenthesisFound = true;

			} else if (c == ')') {
				endSub = i;
				countParenthesisFound--;

				String subQuery = queryClone.substring(beginSub + 1, endSub);
				boolean result = checkBlock(subQuery);
				if (result) {
					StringBuffer buff = new StringBuffer();
					buff.append("(");
					for (int j = 0; j < subQuery.length(); ++j) {
						buff.append("z");
					}
					buff.append(")");
					queryClone = queryClone.substring(0, beginSub)
							+ buff.toString()
							+ queryClone.substring(endSub + 1);
					// System.out.println("new query clone " + queryClone);
				} else {
					return false;
				}

				parenthesisBlockAtLeft = true;
				if (countParenthesisFound > 0 && beginSub > 0) {
					beginSub = queryClone.lastIndexOf('(', beginSub - 1);
				}
			}
		}

		// final check, if we didn't encounter any parenthesis or there remain a
		// part which
		// is not enclosed by parenthesis, we need to check it too
		if (endSub == 0) {
			overall = overall && checkBlock(queryClone);
		} else if (endSub < queryClone.length() - 1) {
			// System.out.println("final check");
			String subQuery = queryClone.substring(endSub + 1);
			if (parenthesisBlockAtLeft) {
				subQuery = "a" + subQuery;
			}
			overall = overall && checkBlock(subQuery);
		}

		// System.out.println(query);
		return overall;
	}

	private boolean checkBlock(String subQuery) {
		if (subQuery.length() == 0) {
			return true;
		} else {
			// System.out.println("Checking subblock : " + subQuery);
			boolean leftOperand = false;
			boolean rightOperand = false;
			boolean operator = false;
			// this is a basic block, ie no more parenthesis
			for (int i = 0; i < subQuery.length(); ++i) {
				char c = subQuery.charAt(i);
				if (c == '+' || c == '*') {
					if (!leftOperand) {
						// we encountered an operator without left member, wrong
						return false;
					}
					if (leftOperand && rightOperand) {
						// we have something like a+b+... so the right operand
						// is not sure to be still true
						rightOperand = false;
					}
					operator = true;

					if (i > 0 && subQuery.charAt(i - 1) == '+'
							|| subQuery.charAt(i - 1) == '*') {
						return false;
					}
				} else {
					if (!operator) {
						leftOperand = true;
					} else {
						rightOperand = true;
					}
				}
			}
			return (leftOperand && rightOperand && operator)
					|| (leftOperand && !operator);
		}
	}

	public void setQuery(String que) {
		this.query = que;
	}

	public static QueryChecker getInstance() {
		if (instance == null) {
			instance = new QueryChecker();
		}
		return instance;
	}

	public boolean checkQuery() {
		if (query == null || query.length() > MAXLENGTH) {
			return false;
		}
		// check that it includes only autorised character
		if (!query.matches(patternIncluded)) {
			return false;
		}
		// check it contains AT LEAST one alphanum character
		String patternAlphaNum = ".*[a-zA-Z0-9]+.*";
		if (!query.matches(patternAlphaNum)) {
			return false;
		}

		if (!checkNested()) {
			return false;
		}

		if (!checkValid()) {
			return false;
		}

		return true;
	}

}
