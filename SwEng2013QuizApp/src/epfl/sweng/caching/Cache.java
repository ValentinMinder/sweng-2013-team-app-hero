package epfl.sweng.caching;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.content.Context;
import android.util.Log;
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
	private final static int SIZE_BUFFER = 1024;

	private static Context contextApplication = null;

	private static Cache instance = null;
	private ICacheToProxyPrivateTasks myCacheToProxyPrivateTasks = null;

	/**
	 * All the question cached.
	 */
	// private ArrayList<QuizQuestion> myCacheQuizQuestion;

	/**
	 * Question to be sent while in offline mode.
	 */
	// private ArrayList<QuizQuestion> outBox;

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
	 */
	private Cache(ICacheToProxyPrivateTasks innerCacheToProxyPrivateTasks) {
		myCacheToProxyPrivateTasks = innerCacheToProxyPrivateTasks;

		initCache();
	}

	private Cache() {
		initCache();
	}

	private void initCache() {
		this.failBox = new ArrayList<QuizQuestion>();

		File dirUtils = new File(contextApplication.getFilesDir()
				+ File.separator + NAME_DIRECTORY_UTILS);
		if (!dirUtils.exists()) {
			dirUtils.mkdir();
		}

		File dirQuestions = new File(contextApplication.getFilesDir()
				+ File.separator + NAME_DIRECTORY_QUESTIONS);
		if (!dirQuestions.exists()) {
			dirQuestions.mkdir();
		}

		File dirTags = new File(contextApplication.getFilesDir()
				+ File.separator + NAME_DIRECTORY_TAGS);
		if (!dirTags.exists()) {
			dirTags.mkdir();
		}
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
	 */
	public static synchronized IProxyToCachePrivateTasks getProxyToCachePrivateTasks(
			ICacheToProxyPrivateTasks innerCacheToProxyPrivateTasks) {
		if (instance == null) {
			instance = new Cache(innerCacheToProxyPrivateTasks);
		}
		return instance.getProxyToCachePrivateTask();

	}

	public static Cache getInstance() {
		if (instance == null) {
			instance = new Cache();
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
	 * Return a ArrayList of string corresponding to all the questions (identify
	 * by the set of hashCode in parameter) in JSON format
	 * 
	 * @param hashCodes
	 * @return
	 * @author AntoineW
	 */
	public ArrayList<String> getArrayOfJSONQuestions(Set<String> hashCodes) {
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
	 */
	public String getRandomQuestionFromCache() {
		File dir = new File(contextApplication.getFilesDir() + File.separator
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

	public static void setContextApplication(Context context) {
		contextApplication = context;
	}

	/**
	 * Add a question to the cache.
	 * 
	 * @param myQuizQuestion
	 *            QuizQuestion to add
	 * @return
	 */
	private boolean addQuestionToCache(QuizQuestion myQuizQuestion) {
		String hashQuestion = Integer.toString(myQuizQuestion.hashCode());
		String jsonQuestion = myQuizQuestion.toPostEntity();
		File file = new File(contextApplication.getFilesDir().getAbsoluteFile()
				+ File.separator + NAME_DIRECTORY_QUESTIONS + File.separator
				+ hashQuestion);
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
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
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
	 */
	private boolean addQuestionToTagFile(String tag, String hashQuestion) {
		File file = new File(contextApplication.getFilesDir().getAbsoluteFile()
				+ File.separator + NAME_DIRECTORY_TAGS + File.separator + tag);
		Set<String> setHash = getSetTagWithFile(file);

		if (!setHash.contains(tag)) {
			setHash.add(tag);
			try {
				FileOutputStream fileOutput = new FileOutputStream(file, false);
				ObjectOutput output = new ObjectOutputStream(fileOutput);
				output.writeObject(setHash);
				output.close();
				fileOutput.close();
				return true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
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
	 */
	private boolean addQuestionToOutBox(QuizQuestion myQuizQuestion) {
		return addQuestionToOutBoxFile(myQuizQuestion);
	}

	/**
	 * Add a question to the outbox
	 * 
	 * @param question
	 * @return
	 * @author AntoineW
	 */
	private boolean addQuestionToOutBoxFile(QuizQuestion question) {
		File file = new File(contextApplication.getFilesDir().getAbsoluteFile()
				+ File.separator + NAME_DIRECTORY_UTILS + File.separator
				+ NAME_FILE_OUTBOX);

		ArrayList<QuizQuestion> outbox = getListOutBoxWithFile(file);
		if (outbox == null) {
			outbox = new ArrayList<QuizQuestion>();
		}

		outbox.add(question);
		try {
			FileOutputStream fileOutput = new FileOutputStream(file, false);
			ObjectOutput output = new ObjectOutputStream(fileOutput);
			output.writeObject(outbox);
			output.close();
			fileOutput.close();

			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Return the list questions to be sent while in offline mode.
	 * 
	 * @return
	 * @author AntoineW
	 */
	public ArrayList<QuizQuestion> getListOutBox() {
		File file = new File(contextApplication.getFilesDir().getAbsoluteFile()
				+ File.separator + NAME_DIRECTORY_UTILS + File.separator
				+ NAME_FILE_OUTBOX);

		return getListOutBoxWithFile(file);
	}

	/**
	 * Return the list questions to be sent while in offline mode.
	 * 
	 * @return
	 * @author AntoineW
	 */
	private ArrayList<QuizQuestion> getListOutBoxWithFile(File file) {
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
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		return outbox;
	}

	private String getJSONQuestion(String hashCode) {
		String result = null;
		try {
			FileInputStream fis = new FileInputStream(
					contextApplication.getFilesDir() + File.separator
							+ NAME_DIRECTORY_QUESTIONS + File.separator
							+ hashCode);

			StringBuffer fileContent = new StringBuffer("");

			byte[] buffer = new byte[SIZE_BUFFER];

			while (fis.read(buffer) != -1) {
				fileContent.append(new String(buffer));
			}

			fis.close();

			result = fileContent.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Save the outbox to the file
	 * 
	 * @param outbox
	 * @return
	 * @author AntoineW
	 */
	private boolean saveOutBox(ArrayList<QuizQuestion> outbox) {
		File file = new File(contextApplication.getFilesDir().getAbsoluteFile()
				+ File.separator + NAME_DIRECTORY_UTILS + File.separator
				+ NAME_FILE_OUTBOX);

		try {
			FileOutputStream fileOutput = new FileOutputStream(file, false);
			ObjectOutput output = new ObjectOutputStream(fileOutput);
			output.writeObject(outbox);
			output.close();
			fileOutput.close();

			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Return the set of hashCode corresponding to questions are in cache and
	 * corresponding to the tag
	 * 
	 * @param tag
	 * @return
	 * @author AntoineW
	 */
	public Set<String> getSetTag(String tag) {
		File file = new File(contextApplication.getFilesDir().getAbsoluteFile()
				+ File.separator + NAME_DIRECTORY_TAGS + File.separator + tag);
		Set<String> setHash = getSetTagWithFile(file);

		return setHash;
	}

	/**
	 * Return a set of string that is in a file
	 * 
	 * @param tag
	 * @param file
	 * @return
	 * @author AntoineW
	 */
	private Set<String> getSetTagWithFile(File file) {
		Set<String> setHash = null;
		Log.e("test", "start getSetTagWithFile");
		if (!file.exists()) {
			Log.e("test", "File not exist");
			setHash = new HashSet<String>();
		} else {
			Log.e("test", "File exist");
			try {
				FileInputStream fis = new FileInputStream(file);
				ObjectInput input = new ObjectInputStream(fis);
				// deserialize the List
				setHash = (HashSet<String>) input.readObject();
				input.close();
				fis.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		Log.e("test", "getSetTagWithFile size set : " + setHash.size());
		return setHash;
	}

	/**
	 * Send the outBox to the real subject.
	 * <p>
	 * For each question, it ask the proxy to send the question to the real
	 * subject.
	 * 
	 * @return
	 */
	private boolean sendOutBox() {
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
	 */
	private boolean getSentStatus() {
		boolean status = failBox.size() == 0;
		ArrayList<QuizQuestion> outBox = getListOutBox();
		outBox.addAll(0, failBox);
		saveOutBox(outBox);
		failBox.clear();
		System.out.println("sent status" + status);
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
		public boolean addQuestionToCache(QuizQuestion myQuizQuestion) {
			return instance.addQuestionToCache(myQuizQuestion);
		}

		@Override
		public boolean addQuestionToOutBox(QuizQuestion myQuizQuestion) {
			return instance.addQuestionToOutBox(myQuizQuestion);
		}

		@Override
		public boolean sendOutBox() {
			return instance.sendOutBox();
		}

		@Override
		public void addToFailBox(QuizQuestion myQuestion) {
			instance.addToFailBox(myQuestion);
		}

		@Override
		public boolean getSentStatus() {
			return instance.getSentStatus();
		}

		@Override
		public String getRandomQuestionFromCache() {
			return instance.getRandomQuestionFromCache();
		}

	}
}
