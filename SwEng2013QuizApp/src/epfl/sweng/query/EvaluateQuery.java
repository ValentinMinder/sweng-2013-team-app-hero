package epfl.sweng.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import epfl.sweng.caching.Cache;
import epfl.sweng.caching.CacheException;

public class EvaluateQuery {
	/**
	 * Class under construction, here some guideline about how it works
	 * 
	 * evaluate method evaluate the query, it first construct a hashmap which
	 * map index of a ( to the nesting level of this one then it check each
	 * parenthesis and evaluate it, store it in a arraylist (which actually
	 * store the intermediate results)
	 * 
	 * simulate method is used to test the code (while I don't have access to
	 * data to make test on questions) it just generate a random number of
	 * String in a set corresponding to the parameter with a number next to it
	 * 
	 * max method look for the maximum nesting level in the expression
	 * 
	 * getLevels method actually construct the hashmap(index, nestinglevel)
	 * 
	 * BasicOperation class is where the operation are done (and, or)
	 * 
	 * ResultQuery class store information (to discuss if really necessary to
	 * store operands, operator, we might use it to optimize the query we
	 * already checked ..) and the result associated
	 * 
	 * WARNING: I've just done some BASIC test of some queries, I can't tell if
	 * it really works the way that is should. Some operation are probably not
	 * optimized, the idea was to develop an algorithm to evaluate a query, the
	 * question of performance has not been thought at this point.
	 * @throws CacheException 
	 * 
	 * 
	 * 
	 * 
	 */
	public static HashSet<String> evaluate(String query) throws CacheException {
		System.out.println("Evaluating " + query);
		HashMap<Integer, Integer> parenthesisLevel = getLevels(query);
		ArrayList<HashSet<String>> results = new ArrayList<HashSet<String>>();
		int count = 0;

		while (parenthesisLevel.size() != 0) {
			Entry<Integer, Integer> max = max(parenthesisLevel.entrySet());
			int beginIndex = max.getKey();
			int endIndex = Parenthesis.findCorrespondingClosingParenthesis(
					query.substring(beginIndex), beginIndex) + beginIndex;
			// we now have a basic expression
			String subQuery = query.substring(beginIndex + 1, endIndex);
			String pattern = "([\\w@]+)([+*]+)([\\w@]+)";
			Pattern p = Pattern.compile(pattern);
			Matcher matcher = p.matcher(subQuery);

			String operand1 = "";
			String operand2 = "";
			String operator = "";
			if (matcher.find()) {
				operand1 = matcher.group(1);
				operand2 = matcher.group(3);
				operator = matcher.group(2);
			}
			// the idea is to use the unallowed character @ to represent the
			// fact
			// that we already evaluated this part , the following number is the
			// index in the arraylist of the result ex @0 has a result at
			// results.get(0)
			query = query.substring(0, beginIndex) + ("@" + count)
					+ query.substring(endIndex + 1);

			Set<String> op1Set = null;
			Set<String> op2Set = null;
			Pattern extractPattern = Pattern.compile("(@)(\\d+)");
			Matcher extracter = extractPattern.matcher(operand1);

			if (extracter.find()) {
				op1Set = results.get(Integer.parseInt(extracter.group(2)));
			} else {
				op1Set = Cache.getInstance().getSetTag(operand1);
			}

			extracter = extractPattern.matcher(operand2);

			if (extracter.find()) {
				op2Set = results.get(Integer.parseInt(extracter.group(2)));
			} else {
				op2Set = Cache.getInstance().getSetTag(operand2);
			}

			HashSet<String> setResult = new HashSet<String>(op1Set);
			if (operator.equals("+")) {
				setResult.addAll(op2Set);
			} else {
				setResult.retainAll(op2Set);
			}

			results.add(setResult);

			count++;
			parenthesisLevel = getLevels(query);
		}
		// results.get(count - 1).print();
		if (count != 0) {
			return 	results.get(count - 1);
		} else {
			Log.e("test", query);
			return Cache.getInstance().getSetTag(query);
			// return new ArrayList<String>();
		}
	}

	public static Set<String> simulate(String s) {
		HashSet<String> hash = new HashSet<String>();
		for (int i = 0; i < 10; ++i) {
			hash.add(s + "" + i);
		}
		return hash;
	}

	private static Entry<Integer, Integer> max(
			Set<Entry<Integer, Integer>> entrySet) {
		int maxNested = 0;
		Entry<Integer, Integer> result = null;

		for (Entry<Integer, Integer> e : entrySet) {
			if (e.getValue() > maxNested) {
				maxNested = e.getValue();
				result = e;
			}
		}
		return result;
	}

	public static HashMap<Integer, Integer> getLevels(String query) {
		// associate each index by its nesting level (for parenthesis only),
		// base level = 1
		HashMap<Integer, Integer> levels = new HashMap<Integer, Integer>();
		int count = 0;
		for (int i = 0; i < query.length(); ++i) {
			char c = query.charAt(i);
			if (c == '(') {
				count++;
				levels.put(i, count);
			} else if (c == ')') {
				count--;
			}
		}
		return (HashMap) levels.clone();
	}
}
