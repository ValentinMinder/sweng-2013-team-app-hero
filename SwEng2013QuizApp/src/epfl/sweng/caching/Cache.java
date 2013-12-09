package epfl.sweng.caching;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.util.Log;

import epfl.sweng.patterns.ProxyHttpClient;
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
	private Cache(ICacheToProxyPrivateTasks innerCacheToProxyPrivateTasks)
			throws CacheException {
		myCacheToProxyPrivateTasks = innerCacheToProxyPrivateTasks;

		initCache();
	}

	private void initCache() throws CacheException {
		this.failBox = new ArrayList<QuizQuestion>();

		File dirFiles = new File(directoryFiles);
		if (!dirFiles.exists()) {
			if (!dirFiles.mkdir()) {
				throw new CacheException(
						"Not possible to create directory files : "
								+ directoryFiles);
			}
		}

		File dirUtils = new File(directoryFiles + File.separator
				+ NAME_DIRECTORY_UTILS);
		if (!dirUtils.exists()) {
			if (!dirUtils.mkdir()) {
				throw new CacheException(
						"Not possible to create directory utils : "
								+ dirUtils.getAbsolutePath());
			}
		}

		File dirQuestions = new File(directoryFiles + File.separator
				+ NAME_DIRECTORY_QUESTIONS);
		if (!dirQuestions.exists()) {
			if (!dirQuestions.mkdir()) {
				throw new CacheException(
						"Not possible to create directory questions : "
								+ dirQuestions.getAbsolutePath());
			}
		}

		File dirTags = new File(directoryFiles + File.separator
				+ NAME_DIRECTORY_TAGS);
		if (!dirTags.exists()) {
			if (!dirTags.mkdir()) {
				throw new CacheException(
						"Not possible to create directory tags : "
								+ dirTags.getAbsolutePath());
			}
		}
	}

	/**
	 * Delete the instance (usefull for test)
	 */
	public static void deleteInstance() {
		instance = null;
	}

	public static String getDirectoryFiles() {
		return directoryFiles;
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
			ICacheToProxyPrivateTasks innerCacheToProxyPrivateTasks)
			throws CacheException {
		if (instance == null) {
			instance = new Cache(innerCacheToProxyPrivateTasks);
		}
		return instance.getProxyToCachePrivateTask();

	}

	/**
	 * Returns the singleton cache object, for testing purposes. Creates a proxy
	 * and a cache if not created so far.
	 * 
	 * @return
	 * @throws CacheException
	 */
	public static Cache getInstance() throws CacheException {
		// creates the proxy, and the proxy creates the cache!
		ProxyHttpClient.getInstance();
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

	/**
	 * Fetch a question from the cache.
	 * 
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
	public boolean addQuestionToCache(QuizQuestion myQuizQuestion)
			throws CacheException {
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
	private boolean addQuestionToTagFile(String tag, String hashQuestion)
			throws CacheException {
		File file = new File(directoryFiles + File.separator
				+ NAME_DIRECTORY_TAGS + File.separator + tag.toLowerCase());
		Set<String> setHash = getSetTagWithFile(file);

		if (!setHash.contains(hashQuestion)) {
			setHash.add(hashQuestion);
			// WE HAVE TO DECLARE AN OBJECTOUPUTSTREAM, NOT OBJECTOUTPUT!
			ObjectOutputStream output = null;
			try {
				output = new ObjectOutputStream(new FileOutputStream(file,
						false));
				try {
					output.writeObject(setHash);
				} finally {
					closeSilently(output);
				}
			} catch (IOException e) {
				Logger.getLogger("epfl.sweng.caching").log(Level.INFO,
						"fail to create or write to stream", e);
				throw new CacheException(e);
			}
			return true;
		}
		return false;
	}

	private static void closeSilently(ObjectOutputStream os) {
		try {
			os.close();
		} catch (IOException ex) {
			Logger.getLogger("epfl.sweng.caching").log(Level.INFO,
					"fail to close stream", ex);
		}

	}

	/**
	 * Add a question to the outBox.
	 * 
	 * @param myQuizQuestion
	 *            QuizQuestion to add
	 * @return
	 * @throws CacheException
	 */
	public boolean addQuestionToOutBox(QuizQuestion myQuizQuestion)
			throws CacheException {
		File file = new File(directoryFiles + File.separator
				+ NAME_DIRECTORY_UTILS + File.separator + NAME_FILE_OUTBOX);

		ArrayList<QuizQuestion> outbox = getListOutBoxWithFile(file);
		if (outbox == null) {
			outbox = new ArrayList<QuizQuestion>();
		}

		outbox.add(myQuizQuestion);
		ObjectOutputStream output = null;
		try {
			output = new ObjectOutputStream(new FileOutputStream(file, false));
			try {
				output.writeObject(outbox);
			} finally {
				closeSilently(output);
			}

			return true;
		} catch (IOException e) {
			Logger.getLogger("epfl.sweng.caching").log(Level.INFO,
					"fail to create or write to stream", e);
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
	private ArrayList<QuizQuestion> getListOutBoxWithFile(File file)
			throws CacheException {
		ArrayList<QuizQuestion> outbox = null;

		if (!file.exists()) {
			outbox = new ArrayList<QuizQuestion>();
		} else {
			ObjectInputStream input = null;
			try {
				input = new ObjectInputStream(new FileInputStream(file));
				outbox = (ArrayList<QuizQuestion>) input.readObject();
			} catch (IOException e) {
				Log.e("Error", "fail to create or read to stream, exception : " + e);
				throw new CacheException(e);
			} catch (ClassNotFoundException e) {
				Log.e("Error",
						"ClassNotFoundException in getListOutBoxWithFile, exception : " + e);
				throw new CacheException(e);
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						Log.e("Error",
								"Failing to close input stream in getListOutBoxWithFile, exception : " + e);
					}
				}
			}
		}

		return outbox;
	}

	public String getJSONQuestion(String hashCode) throws CacheException {
		StringBuffer buff = new StringBuffer();
		try {
			FileReader fr = new FileReader(directoryFiles + File.separator
					+ NAME_DIRECTORY_QUESTIONS + File.separator + hashCode);
			BufferedReader buffReader = new BufferedReader(fr);
			String line = buffReader.readLine();
			while (line != null) {
				buff.append(line);
				line = buffReader.readLine();
				if (line != null) {
					buff.append('\n');
				}
			}

			buffReader.close();
			fr.close();
		} catch (IOException e) {
			throw new CacheException(e);
		}

		return buff.toString();
	}

	/**
	 * Save the outbox to the file
	 * 
	 * @param outbox
	 * @return
	 * @author AntoineW
	 * @throws CacheException
	 */
	private boolean saveOutBox(ArrayList<QuizQuestion> outbox)
			throws CacheException {
		File file = new File(directoryFiles + File.separator
				+ NAME_DIRECTORY_UTILS + File.separator + NAME_FILE_OUTBOX);

		ObjectOutputStream output = null;
		try {
			output = new ObjectOutputStream(new FileOutputStream(file, false));

			try {
				output.writeObject(outbox);
			} finally {
				closeSilently(output);
			}

			return true;
		} catch (IOException e) {
			Log.e("Error", "IOException in saveOutBox, exception : " + e);
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
				+ NAME_DIRECTORY_TAGS + File.separator + tag.toLowerCase());
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
			ObjectInputStream input = null;
			try {
				input = new ObjectInputStream(new FileInputStream(file));
				setHash = (HashSet<String>) input.readObject();
			} catch (IOException e) {
				throw new CacheException(e);
			} catch (ClassNotFoundException e) {
				throw new CacheException(e);
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						Log.e("Error", "file to close stream, exception : " + e);
					}
				}
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
		public boolean addQuestionToCache(QuizQuestion myQuizQuestion)
				throws CacheException {
			return instance.addQuestionToCache(myQuizQuestion);
		}

		@Override
		public boolean addQuestionToOutBox(QuizQuestion myQuizQuestion)
				throws CacheException {
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
