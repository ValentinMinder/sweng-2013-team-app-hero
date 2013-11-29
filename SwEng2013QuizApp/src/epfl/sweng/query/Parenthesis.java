package epfl.sweng.query;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Parenthesis {
	public static String parenthesis(String query) {

		String queryClone = replaceSpace(query);
		System.out.println("Parenthesing ... " + queryClone);

		queryClone = removeParenthesisAroundOneElement(queryClone);
		String subAnd = parenthesisOperator('*', queryClone);
		String subOr = parenthesisOperator('+', subAnd);

		return removeUselessParenthesis(subOr);
	}

	public static String removeUselessParenthesis(String query) {
		// TODO check (delete, edit..)
		String queryClone = query;
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i < queryClone.length(); ++i) {
			char c = query.charAt(i);
			if (c == '(') {
				int closing = findCorrespondingClosingParenthesis(
						queryClone.substring(i), 0)
						+ i;

				if (closing != i && map.get(i - 1) != null
						&& map.get(i - 1) == closing + 1) {
					queryClone = queryClone.substring(0, i)
							+ queryClone.substring(i + 1, closing)
							+ queryClone.substring(closing + 1);
					map.remove(i);

					// Update all value
					Set<Integer> keys = map.keySet();
					Iterator<Integer> it = keys.iterator();
					while (it.hasNext()) {
						Integer key = it.next();
						map.put(key, map.get(key) - 2);
					}

					i -= 1;
				} else {
					map.put(i, closing);
				}
			}
		}
		return queryClone;
	}

	public static int findCorrespondingClosingParenthesis(String query,
			int index) {
		int count = 0;
		for (int i = 0; i < query.length(); ++i) {
			char c = query.charAt(i);
			if (c == '(') {
				count++;
			} else if (c == ')') {
				count--;
				if (count == 0) {
					return i;
				}
			}
		}
		return index;
	}

	public static String removeParenthesisAroundOneElement(String query) {
		String queryClone = query;

		int i = 0;
		while (i < queryClone.length()) {
			char firstParenthesis = ' ';
			char secondParenthesis = ' ';
			char cChar = queryClone.charAt(i);
			int invariant = i;

			while (i < queryClone.length() - 1 && !Character.isLetter(cChar)
					&& !Character.isDigit(cChar)) {
				i++;
				cChar = queryClone.charAt(i);
			}

			int firstCharVariable = i;

			if (firstCharVariable > 0) {
				firstParenthesis = queryClone.charAt(firstCharVariable - 1);
				if (firstParenthesis == '(') {
					while (i < queryClone.length() - 1
							&& (Character.isLetter(cChar) || Character
									.isDigit(cChar))) {
						i++;
						cChar = queryClone.charAt(i);
					}

					if (i < queryClone.length()) {
						secondParenthesis = cChar;
						while (firstParenthesis == '('
								&& secondParenthesis == ')') {

							queryClone = queryClone.substring(0,
									firstCharVariable - 1)
									+ queryClone
											.substring(firstCharVariable, i)
									+ queryClone.substring(i + 1);

							/*
							 * String s1 = queryClone.substring(0,
							 * firstCharVariable - 1); String s2 =
							 * queryClone.substring(firstCharVariable, i);
							 * String s3 = queryClone.substring(i + 1);
							 * queryClone = s1 + s2 + s3;
							 */

							firstCharVariable -= 1;
							i -= 1;
							if (firstCharVariable > 0
									&& i < queryClone.length()) {
								firstParenthesis = queryClone
										.charAt(firstCharVariable - 1);
								secondParenthesis = queryClone.charAt(i);
							} else {
								firstParenthesis = ' ';
							}
						}
					}
				}
			}

			if (i == invariant) {
				i++;
			}
		}

		return queryClone;
	}

	private static String parenthesisOperator(char op, String query) {
		int operatorIndex = 0;
		int lastOperatorIndex = 0;
		String subQuery = query;
		lastOperatorIndex = operatorIndex;
		operatorIndex = subQuery.indexOf(op, lastOperatorIndex);

		while (operatorIndex != -1) {
			int blockLeft = findBlockLeft(subQuery.substring(0, operatorIndex));
			if (blockLeft != -1) {
				int blockRight = findBlockRight(subQuery
						.substring(operatorIndex + 1));
				if (blockRight != -1) {
					int right = blockRight + operatorIndex + 1;
					int left = blockLeft;
					lastOperatorIndex = right + 1;
					// normal case
					if (right + 1 < subQuery.length() - 1 && left != 0) {
						subQuery = subQuery.substring(0, left) + "("
								+ subQuery.substring(left, right + 1) + ")"
								+ subQuery.substring(right + 1);
						// left at min value but not right
					} else if (left == 0 && right + 1 < subQuery.length() - 1) {
						subQuery = "(" + subQuery.substring(0, right + 1) + ")"
								+ subQuery.substring(right + 1);
						// left at min value and right at max value
					} else if (left == 0) {
						subQuery = "(" + subQuery + ")";
						// left not at min value but right at max value
					} else {
						subQuery = subQuery.substring(0, left) + "("
								+ subQuery.substring(left) + ")";
					}
				} else {
					lastOperatorIndex++;
				}
			} else {
				lastOperatorIndex++;
			}

			operatorIndex = subQuery.indexOf(op, lastOperatorIndex);
		}
		return subQuery;
	}

	private static String replaceSpace(String query) {
		String queryClone = query;
		for (int i = 0; i < query.length(); ++i) {
			String pattern = "([\\w])(\\s+)([\\w])";
			queryClone = queryClone.replaceAll(pattern, "$1*$3");
			pattern = "([\\w])(\\s+)([(])";
			queryClone = queryClone.replaceAll(pattern, "$1*$3");
			pattern = "([)])(\\s+)(\\w)";
			queryClone = queryClone.replaceAll(pattern, "$1*$3");
			pattern = "([)])(\\s+)([(])";
			queryClone = queryClone.replaceAll(pattern, "$1*$3");
		}
		// remove all space at the end only... avoid multiple space inline like "a b c"
		queryClone = queryClone.replaceAll("\\s+", "");
		return queryClone;
	}

	private static int findCorrespondingOpeningParenthesis(String query,
			int index) {
		int count = 0;
		for (int i = query.length() - 1; i >= 0; --i) {
			char c = query.charAt(i);
			if (c == ')') {
				count++;
			} else if (c == '(') {
				count--;
				if (count == 0) {
					return i;
				}
			}
		}
		return index;
	}

	private static int findBlockLeft(String query) {
		if (query.charAt(query.length() - 1) == ')') {
			return findCorrespondingOpeningParenthesis(query,
					query.length() - 1);
		}

		int i = query.length() - 1;
		for (i = query.length() - 1; i >= 0; --i) {
			char c = query.charAt(i);
			if (c == '+' || c == '*') {
				return i + 1;
			}
			// if (c == '(' && i < query.length() - 1) {
			// return -1;
			// }
		}

		return 0;
	}

	private static int findBlockRight(String query) {
		if (query.charAt(0) == '(') {
			return findCorrespondingClosingParenthesis(query, 0);
		}

		int i = 0;
		for (i = 0; i < query.length(); ++i) {
			char c = query.charAt(i);
			if (c == '+' || c == '*') {
				return i - 1 >= 0 ? i - 1 : -1;
			}
			// if (c == ')' && i > 0) {
			// return -1;
			// }
		}

		return query.length() - 1;
	}

}
