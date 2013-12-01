package epfl.sweng.caching;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import epfl.sweng.patterns.ProxyHttpClient;
import epfl.sweng.query.EvaluateQuery;
import epfl.sweng.quizquestions.QuizQuestion;

/**
 * CacheQuizQuestion is a cache: It stores all question already fetched and
 * question to be submitted.
 * <p>
 * It will be set persistent and support fail of the app. Ideally
 * 
 * @author valentin
 * 
 */
public final class Cache {
	private final static String NAME_DIRECTORY_QUESTIONS = "questions";
	private final static String NAME_DIRECTORY_TAGS = "tags";
	private final static String NAME_DIRECTORY_UTILS = "utils";
	private final static String NAME_FILE_OUTBOX = "outbox";

	private static String directoryFiles = null;
	private static Cache instance = null;
	private ICacheToProxyPrivateTasks myCacheToProxyPrivateTasks = null;

	/**
	 * Stores failed to sent questions while trying to resume from offline mode
	 * to online.
	 */
	private ArrayList<QuizQuestion> failBox;

	/**
	 * Private constructor of the singleton pattern.
	 * <p>
	 * There are NO getInstance as ONLY the proxy has an instance of the cache.
	 * Use getProxyToCachePrivateTasks instead.
	 * 
	 * @param innerCacheToProxyPrivateTasks
	 * @throws CacheException 
	 */
	private Cache(ICacheToProxyPrivateTasks innerCacheToProxyPrivateTasks) throws CacheException {
		myCacheToProxyPrivateTasks = innerCacheToProxyPrivateTasks;

		initCache();
	}

	private void initCache() throws CacheException {
		this.failBox = new ArrayList<QuizQuestion>();

		File dirFiles = new File(directoryFiles);
		if (!dirFiles.exists()) {
			if (!dirFiles.mkdir()) {
				throw new CacheException("Not possible to create directory files : " + directoryFiles);
			}
		}
		
		File dirUtils = new File(directoryFiles + File.separator
				+ NAME_DIRECTORY_UTILS);
		if (!dirUtils.exists()) {
			if (!dirUtils.mkdir()) {
				throw new CacheException("Not possible to create directory utils : " + dirUtils.getAbsolutePath());
			}
		}

		File dirQuestions = new File(directoryFiles + File.separator
				+ NAME_DIRECTORY_QUESTIONS);
		if (!dirQuestions.exists()) {
			if (!dirQuestions.mkdir()) {
				throw new CacheException("Not possible to create directory questions : " + dirQuestions.getAbsolutePath());
			}
		}

		File dirTags = new File(directoryFiles + File.separator
				+ NAME_DIRECTORY_TAGS);
		if (!dirTags.exists()) {
			if (!dirTags.mkdir()) {
				throw new CacheException("Not possible to create directory tags : " + dirTags.getAbsolutePath());
			}
		}
	}
	
	/**
	 * Delete the instance (usefull for test)
	 */
	public static void deleteInstance() {
		instance = null;
	}

	/**
	 * Create a unique instance of the cache. Return a ProxyToCachePrivateTasks
	 * for the proxy to interact with the cache. Replace the standard
	 * getInstance, as only the proxy need to be able to speak to cache: the
	 * proxy creates a cache with a private task for the cache to speak to the
	 * proxy, and the cache send back a private task for the proxy to interact
	 * with the cache.
	 * <p>
	 * We've choose this architecture as proxy and cache have different roles,
	 * but are very close, and nobody else should disturb in there private
	 * methods.
	 * 
	 * @param innerCacheToProxyPrivateTasks
	 *            a private task to interact from the cache to the proxy
	 * @return a private task to interact from the proxy to the cache
	 * @throws CacheException 
	 */
	public static synchronized IProxyToCachePrivateTasks getProxyToCachePrivateTasks(
			ICacheToProxyPrivateTasks innerCacheToProxyPrivateTasks) throws CacheException {
		if (instance == null) {
			instance = new Cache(innerCacheToProxyPrivateTasks);
		}
		return instance.getProxyToCachePrivateTask();

	}

	/**
	 * Returns the singleton cache object, for testing purposes.
	 * Creates a proxy and a cache if not created so far.
	 * @return
	 * @throws CacheException
	 */
	public static Cache getInstance() throws CacheException {
		// creates the proxy, and the proxy creates the cache!
		ProxyHttpClient.getInstance();
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (instance == null) {
			throw new CacheException("Proxy only should instance the cache!");
		}

		return instance;
	}

	/**
	 * Creates a ProxyToCachePrivateTask.
	 * 
	 * @return
	 */
	private IProxyToCachePrivateTasks getProxyToCachePrivateTask() {
		return new InnerProxyToCachePrivateTask();
	}

	private LinkedList<String> previousHashSetAsLinkedList;
	private String previousQuery;
	private String previousToken;
	public static final int RETURN_ARRAY_SIZE = 10;
	private void resetPreviousState() {
		previousHashSetAsLinkedList = null;
		previousQuery = null;
		previousToken = null;
	}
	
	/**
	 * Return the previous token (the hash of the next questions in the search).
	 * @return
	 */
	public String getPreviousToken() {
		return previousToken;
	}
	
	public ArrayList<String> getArrayOfJSONQuestions (String query, String token) throws CacheException {
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
	 * Return a ArrayList of string corresponding to some next questions in JSON format.
	 * Used for a huge quantity of questions.
	 * Questions are identified by the LinkedList of hashCode in the cache.
	 * 
	 * @return
	 * @author AntoineW
	 * @throws CacheException 
	 */
	private ArrayList<String> getArrayOfJSONQuestionsPartial () throws CacheException {
		ArrayList<String> result = new ArrayList<String>(RETURN_ARRAY_SIZE);
		boolean flag = true;
		for (int i = 0; i < RETURN_ARRAY_SIZE && flag; i++) {
			String hashCode = previousHashSetAsLinkedList.poll();
			if (hashCode != null) {
				result.add(getJSONQuestion(hashCode));
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
	 * Return a ArrayList of string corresponding to all the questions (identify
	 * by the set of hashCode in parameter) in JSON format.
	 * Used only for a few questions.
	 * 
	 * @param hashCodes a set of hash representing the questions
	 * @return
	 * @author AntoineW
	 * @throws CacheException 
	 */
	private ArrayList<String> getArrayOfJSONQuestionsALL(HashSet<String> hashCodes) throws CacheException {
		ArrayList<String> result = new ArrayList<String>();
		Iterator<String> itHashCode = hashCodes.iterator();
		while (itHashCode.hasNext()) {
			String hashCode = itHashCode.next();
			result.add(getJSONQuestion(hashCode));
		}
		return result;
	}

	/**
	 * Fetch a question from the cache.
	 * @throws CacheException 
	 */
	public String getRandomQuestionFromCache() throws CacheException {
		File dir = new File(directoryFiles + File.separator
				+ NAME_DIRECTORY_QUESTIONS);

		String[] children = dir.list();
		if (children != null) {
			int size = children.length;
			if (size != 0) {
				int index = (int) (Math.random() * size);
				String hashQuestion = children[index];

				return getJSONQuestion(hashQuestion);
			}
		}

		// offline and empty cache
		return null;
	}

	public static void setDirectoryFiles(String directory) {
		directoryFiles = directory;
	}

	/**
	 * Add a question to the cache.
	 * 
	 * @param myQuizQuestion
	 *            QuizQuestion to add
	 * @return
	 * @throws CacheException 
	 */
	public boolean addQuestionToCache(QuizQuestion myQuizQuestion) throws CacheException {
		String hashQuestion = Integer.toString(myQuizQuestion.hashCode());
		String jsonQuestion = myQuizQuestion.toPostEntity();
		File file = new File(directoryFiles + File.separator
				+ NAME_DIRECTORY_QUESTIONS + File.separator + hashQuestion);
		if (!file.exists()) {
			try {
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(jsonQuestion.getBytes());
				fos.close();

				Iterator<String> itTag = myQuizQuestion.getTags().iterator();
				while (itTag.hasNext()) {
					String tag = itTag.next();
					addQuestionToTagFile(tag, hashQuestion);
				}

				return true;
			} catch (IOException e) {
				throw new CacheException(e);
			}
		}

		return false;
	}

	/**
	 * Add the hashcode of a question to a file that corresponds to a tag and
	 * that contains a Set of hashcode (corresponding to all the questions that
	 * contains that tag)
	 * 
	 * @param tag
	 * @param hashQuestion
	 * @return
	 * @author AntoineW
	 * @throws CacheException 
	 */
	private boolean addQuestionToTagFile(String tag, String hashQuestion) throws CacheException {
		File file = new File(directoryFiles + File.separator
				+ NAME_DIRECTORY_TAGS + File.separator + tag);
		Set<String> setHash = getSetTagWithFile(file);

		if (!setHash.contains(hashQuestion)) {
			setHash.add(hashQuestion);
			try {
				FileOutputStream fileOutput = new FileOutputStream(file, false);
				ObjectOutput output = new ObjectOutputStream(fileOutput);
				output.writeObject(setHash);
				output.close();
				fileOutput.close();
				return true;
			} catch (IOException e) {
				throw new CacheException(e);
			}
		}

		return false;
	}

	/**
	 * Add a question to the outBox.
	 * 
	 * @param myQuizQuestion
	 *            QuizQuestion to add
	 * @return
	 * @throws CacheException 
	 */
	public boolean addQuestionToOutBox(QuizQuestion myQuizQuestion) throws CacheException {
		File file = new File(directoryFiles + File.separator
				+ NAME_DIRECTORY_UTILS + File.separator + NAME_FILE_OUTBOX);

		ArrayList<QuizQuestion> outbox = getListOutBoxWithFile(file);
		if (outbox == null) {
			outbox = new ArrayList<QuizQuestion>();
		}

		outbox.add(myQuizQuestion);
		try {
			FileOutputStream fileOutput = new FileOutputStream(file, false);
			ObjectOutput output = new ObjectOutputStream(fileOutput);
			output.writeObject(outbox);
			output.close();
			fileOutput.close();

			return true;
		} catch (IOException e) {
			throw new CacheException(e);
		}
	}

	/**
	 * Return the list questions to be sent while in offline mode.
	 * 
	 * @return
	 * @author AntoineW
	 * @throws CacheException 
	 */
	public ArrayList<QuizQuestion> getListOutBox() throws CacheException {
		File file = new File(directoryFiles + File.separator
				+ NAME_DIRECTORY_UTILS + File.separator + NAME_FILE_OUTBOX);

		return getListOutBoxWithFile(file);
	}

	/**
	 * Return the list questions to be sent while in offline mode.
	 * 
	 * @return
	 * @author AntoineW
	 * @throws CacheException 
	 */
	private ArrayList<QuizQuestion> getListOutBoxWithFile(File file) throws CacheException {
		ArrayList<QuizQuestion> outbox = null;

		if (!file.exists()) {
			outbox = new ArrayList<QuizQuestion>();
		} else {
			try {
				FileInputStream fis = new FileInputStream(file);
				ObjectInput input = new ObjectInputStream(fis);
				outbox = (ArrayList<QuizQuestion>) input.readObject();
				input.close();
				fis.close();
			} catch (IOException e) {
				throw new CacheException(e);
			} catch (ClassNotFoundException e) {
				throw new CacheException(e);
			}
		}

		return outbox;
	}

	public String getJSONQuestion(String hashCode) throws CacheException {
		String result = "";
		try {
			FileReader fr = new FileReader(
					directoryFiles + File.separator + NAME_DIRECTORY_QUESTIONS
					+ File.separator + hashCode);
			BufferedReader buffReader = new BufferedReader(fr);
			String line = buffReader.readLine();
			while (line != null) {
				result += line;
				line = buffReader.readLine();
				if (line != null) {
					result += '\n';
				}
			}

			buffReader.close();
			fr.close();
		} catch (IOException e) {
			throw new CacheException(e);
		}

		return result;
	}

	/**
	 * Save the outbox to the file
	 * 
	 * @param outbox
	 * @return
	 * @author AntoineW
	 * @throws CacheException 
	 */
	private boolean saveOutBox(ArrayList<QuizQuestion> outbox) throws CacheException {
		File file = new File(directoryFiles + File.separator
				+ NAME_DIRECTORY_UTILS + File.separator + NAME_FILE_OUTBOX);

		try {
			FileOutputStream fileOutput = new FileOutputStream(file, false);
			ObjectOutput output = new ObjectOutputStream(fileOutput);
			output.writeObject(outbox);
			output.close();
			fileOutput.close();

			return true;
		} catch (IOException e) {
			throw new CacheException(e);
		}
	}

	/**
	 * Return the set of hashCode corresponding to questions are in cache and
	 * corresponding to the tag
	 * 
	 * @param tag
	 * @return
	 * @author AntoineW
	 * @throws CacheException 
	 */
	public HashSet<String> getSetTag(String tag) throws CacheException {
		File file = new File(directoryFiles + File.separator
				+ NAME_DIRECTORY_TAGS + File.separator + tag);
		HashSet<String> setHash = getSetTagWithFile(file);

		return setHash;
	}

	/**
	 * Return a set of string that is in a file
	 * 
	 * @param tag
	 * @param file
	 * @return
	 * @author AntoineW
	 * @throws CacheException 
	 */
	private HashSet<String> getSetTagWithFile(File file) throws CacheException {
		HashSet<String> setHash = null;
		if (!file.exists()) {
			setHash = new HashSet<String>();
		} else {
			try {
				FileInputStream fis = new FileInputStream(file);
				ObjectInput input = new ObjectInputStream(fis);
				
				setHash = (HashSet<String>) input.readObject();
				input.close();
				fis.close();
			} catch (IOException e) {
				throw new CacheException(e);
			} catch (ClassNotFoundException e) {
				throw new CacheException(e);
			}
		}

		return setHash;
	}

	/**
	 * Send the outBox to the real subject.
	 * <p>
	 * For each question, it ask the proxy to send the question to the real
	 * subject.
	 * 
	 * @return
	 * @throws CacheException 
	 */
	private boolean sendOutBox() throws CacheException {
		ArrayList<QuizQuestion> outbox = getListOutBox();

		if (outbox.size() == 0) { // && failBox.size() == 0) {
			myCacheToProxyPrivateTasks.goOnlineResponse(true);
		} else {
			int k = outbox.size();
			myCacheToProxyPrivateTasks.setASyncCounter(k);
			boolean flag = true;
			for (int i = 0; flag && i < k; ++i) {
				QuizQuestion question = outbox.get(0);
				outbox.remove(0);
				myCacheToProxyPrivateTasks.sendQuestion(question);
			}

			saveOutBox(outbox);
		}
		return false;
	}

	/**
	 * Add a question to the failBox.
	 * 
	 * @param myQuestion
	 */
	private void addToFailBox(QuizQuestion myQuestion) {
		failBox.add(myQuestion);
	}

	/**
	 * Checks if all questions have been sent.
	 * 
	 * @return true if not question was in the failBox.
	 * @throws CacheException 
	 */
	private boolean getSentStatus() throws CacheException {
		boolean status = failBox.size() == 0;
		ArrayList<QuizQuestion> outBox = getListOutBox();
		outBox.addAll(0, failBox);
		saveOutBox(outBox);
		failBox.clear();

		return status;
	}

	/**
	 * Private class to interact from the proxy to the cache.
	 * <p>
	 * Created to ensure that nobody else than the proxy calls these methods.
	 * 
	 * @author valentin
	 * 
	 */
	private class InnerProxyToCachePrivateTask implements
			IProxyToCachePrivateTasks {
		@Override
		public boolean addQuestionToCache(QuizQuestion myQuizQuestion) throws CacheException {
			return instance.addQuestionToCache(myQuizQuestion);
		}

		@Override
		public boolean addQuestionToOutBox(QuizQuestion myQuizQuestion) throws CacheException {
			return instance.addQuestionToOutBox(myQuizQuestion);
		}

		@Override
		public boolean sendOutBox() throws CacheException {
			return instance.sendOutBox();
		}

		@Override
		public void addToFailBox(QuizQuestion myQuestion) {
			instance.addToFailBox(myQuestion);
		}

		@Override
		public boolean getSentStatus() throws CacheException {
			return instance.getSentStatus();
		}

		@Override
		public String getRandomQuestionFromCache() throws CacheException {
			return instance.getRandomQuestionFromCache();
		}

	}
}
