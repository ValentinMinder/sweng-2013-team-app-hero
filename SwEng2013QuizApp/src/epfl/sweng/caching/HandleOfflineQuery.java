package epfl.sweng.caching;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import epfl.sweng.query.EvaluateQuery;

/**
 * Class that handles a request, and stores the result for future use in case of
 * partial request with the "next" and "from" field.
 * 
 * @author valentin
 * 
 */
public final class HandleOfflineQuery {
	public static final int RETURN_ARRAY_SIZE = 10;

	private static HandleOfflineQuery instance;

	private LinkedList<String> previousHashSetAsLinkedList;
	private String previousQuery;
	private String previousToken;

	private Cache cache;

	/**
	 * Constructor of the singleton.
	 * 
	 * @throws CacheException
	 */
	private HandleOfflineQuery() throws CacheException {
		resetPreviousState();
		cache = Cache.getInstance();
	}

	/**
	 * Get instance of the singleton.
	 * 
	 * @return
	 * @throws CacheException
	 */
	public static synchronized HandleOfflineQuery getInstance()
			throws CacheException {
		if (instance == null) {
			instance = new HandleOfflineQuery();
		}
		return instance;
	}

	/**
	 * Reset the previous memorized state of the last query.
	 */
	private void resetPreviousState() {
		previousHashSetAsLinkedList = null;
		previousQuery = null;
		previousToken = null;
	}

	/**
	 * Return the previous token (the hash of the next questions in the search).
	 * 
	 * @return
	 */
	public String getPreviousToken() {
		return previousToken;
	}

	/**
	 * Handle a query and returns the corresponding array of questions.
	 * 
	 * @param query
	 *            original clean query
	 * @param token
	 *            the "from" field in case of partial request
	 * @return
	 * @throws CacheException
	 */
	public ArrayList<String> query(String query, String token)
		throws CacheException {
		if (query.equals(previousQuery) && token.equals(previousToken)) {
			return getArrayOfJSONQuestionsPartial();
		} else {
			resetPreviousState();
			HashSet<String> set = EvaluateQuery.evaluate(query);
			System.out.println("found questions: " + set.size());
			if (set.size() > RETURN_ARRAY_SIZE) {
				previousQuery = query;
				previousHashSetAsLinkedList = new LinkedList<String>(set);
				return getArrayOfJSONQuestionsPartial();
			} else {
				return getArrayOfJSONQuestionsALL(set);
			}
		}
	}

	/**
	 * Return a ArrayList of string corresponding to SOME next questions in JSON
	 * format. Used for a huge quantity of questions. Questions are identified
	 * by the LinkedList of hashCode in the cache.
	 * 
	 * @return
	 * @author AntoineW
	 * @throws CacheException
	 */
	private ArrayList<String> getArrayOfJSONQuestionsPartial()
		throws CacheException {
		ArrayList<String> result = new ArrayList<String>(RETURN_ARRAY_SIZE);
		boolean flag = true;
		for (int i = 0; i < RETURN_ARRAY_SIZE && flag; i++) {
			String hashCode = previousHashSetAsLinkedList.poll();
			if (hashCode != null) {
				result.add(cache.getJSONQuestion(hashCode));
			} else {
				flag = false;
			}
		}
		if (flag) {
			previousToken = previousHashSetAsLinkedList.peek();
		} else {
			previousToken = "null";
		}
		return result;
	}

	/**
	 * Return a ArrayList of string corresponding to ALL the questions (identify
	 * by the set of hashCode in parameter) in JSON format. Used only for a few
	 * questions.
	 * 
	 * @param hashCodes
	 *            a set of hash representing the questions
	 * @return
	 * @author AntoineW
	 * @throws CacheException
	 */
	private ArrayList<String> getArrayOfJSONQuestionsALL(
			HashSet<String> hashCodes) throws CacheException {
		ArrayList<String> result = new ArrayList<String>();
		Iterator<String> itHashCode = hashCodes.iterator();
		while (itHashCode.hasNext()) {
			String hashCode = itHashCode.next();
			result.add(cache.getJSONQuestion(hashCode));
		}
		return result;
	}
}
