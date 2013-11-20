package epfl.sweng.caching;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.content.Context;
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
	private ArrayList<QuizQuestion> outBox;

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
		// this.myCacheQuizQuestion = new ArrayList<QuizQuestion>();
		this.outBox = new ArrayList<QuizQuestion>();
		this.failBox = new ArrayList<QuizQuestion>();
		myCacheToProxyPrivateTasks = innerCacheToProxyPrivateTasks;
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
	 */
	public String getRandomQuestionFromCache() {
		File dir = new File(contextApplication.getFilesDir() + File.separator
				+ NAME_DIRECTORY_QUESTIONS);
		// File dir = contextApplication.getDir(NAME_DIRECTORY_QUESTIONS,
		// Context.MODE_PRIVATE);

		String[] children = dir.list();
		if (children != null) {
			int size = children.length;
			if (size != 0) {
				int index = (int) (Math.random() * size);
				String hashQuestion = children[index];

				try {
					FileInputStream fis = new FileInputStream(
							contextApplication.getFilesDir() + File.separator
									+ NAME_DIRECTORY_QUESTIONS + File.separator
									+ hashQuestion);

					StringBuffer fileContent = new StringBuffer("");

					byte[] buffer = new byte[SIZE_BUFFER];

					while (fis.read(buffer) != -1) {
						fileContent.append(new String(buffer));
					}

					fis.close();

					return fileContent.toString();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
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
				+ File.separator + NAME_DIRECTORY_TAGS + File.separator
				+ tag);
		Set<String> setHash = null;

		if (!file.exists()) {
			setHash = new HashSet<String>();
		} else {
			try {
				FileInputStream fis = new FileInputStream(file);
				InputStream buffer = new BufferedInputStream(fis);
				ObjectInput input = new ObjectInputStream(buffer);
				// deserialize the List
				setHash = (Set<String>) input.readObject();
				input.close();
				buffer.close();
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
		
		if (!setHash.contains(tag)) {
			setHash.add(tag);
			try {
				FileOutputStream fileOutput = new FileOutputStream(file, false);
				OutputStream buffer = new BufferedOutputStream(fileOutput);
				ObjectOutput output = new ObjectOutputStream(buffer);
			    output.writeObject(setHash);
				output.close();
				buffer.close();
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
		return outBox.add(myQuizQuestion);
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
		if (outBox.size() == 0) { // && failBox.size() == 0) {
			myCacheToProxyPrivateTasks.goOnlineResponse(true);
		} else {
			int k = outBox.size();
			myCacheToProxyPrivateTasks.setASyncCounter(k);
			boolean flag = true;
			for (int i = 0; flag && i < k; ++i) {
				QuizQuestion question = outBox.get(0);
				outBox.remove(0);
				myCacheToProxyPrivateTasks.sendQuestion(question);
			}
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
		outBox.addAll(0, failBox);
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
