package epfl.sweng.caching;

import java.util.ArrayList;
import epfl.sweng.quizquestions.QuizQuestion;

/** 
 * CacheQuizQuestion is a cache: It stores 
 * all question already fetched and question to be submitted.
 * <p>
 * It will be set persistent and support fail of the app.
 * Ideally
 * @author valentin
 *
 */
public class Cache {
	private static Cache instance = null;
	private ICacheToProxyPrivateTasks myCacheToProxyPrivateTasks = null;
	
	/**
	 * All the question cached.
	 */
	private ArrayList<QuizQuestion> myCacheQuizQuestion;
	
	/**
	 * Question to be sent while in offline mode.
	 */
	private ArrayList<QuizQuestion> outBox;
	
	/**
	 * Stores failed to sent questions while trying to resume from offline mode to online.
	 */
	private ArrayList<QuizQuestion> failBox;

	/**
	 * Private constructor of the singleton pattern.
	 * <p>
	 * There are NO getInstance as ONLY the proxy has an instance of the cache.
	 * Use getProxyToCachePrivateTasks instead.
	 * @param innerCacheToProxyPrivateTasks
	 */
	private Cache(ICacheToProxyPrivateTasks innerCacheToProxyPrivateTasks) {
		this.myCacheQuizQuestion = new ArrayList<QuizQuestion>();
		this.outBox = new ArrayList<QuizQuestion>();
		this.failBox = new ArrayList<QuizQuestion>();
		myCacheToProxyPrivateTasks = innerCacheToProxyPrivateTasks;
	}
	
	/**
	 * Create a unique instance of the cache. Return a ProxyToCachePrivateTasks for the proxy 
	 * to interact with the cache. Replace the standard getInstance, as only the proxy need to
	 * be able to speak to cache: the proxy creates a cache with a private task for the cache to
	 * speak to the proxy, and the cache send back a private task for the proxy to interact with 
	 * the cache. 
	 * <p>
	 * We've choose this architecture as proxy and cache have different roles, but are very close, 
	 * and nobody else should disturb in there private methods.
	 * @param innerCacheToProxyPrivateTasks a private task to interact from the cache to the proxy
	 * @return a private task to interact from the proxy to the cache
	 */
	public static synchronized IProxyToCachePrivateTasks getProxyToCachePrivateTasks(
			ICacheToProxyPrivateTasks innerCacheToProxyPrivateTasks) {
		if (instance == null){
			instance = new Cache(innerCacheToProxyPrivateTasks);
		}
		return instance.getProxyToCachePrivateTask();
		
	}
	
	/**
	 * Creates a ProxyToCachePrivateTask.
	 * @return
	 */
	private IProxyToCachePrivateTasks getProxyToCachePrivateTask (){
		return new InnerProxyToCachePrivateTask();
	}

	/**
	 * Fetch a question from the cache.
	 */
	public String getRandomQuestionFromCache() {
		if (!myCacheQuizQuestion.isEmpty()) {
			int size = myCacheQuizQuestion.size();
			int index = (int) (Math.random() * size);
			return myCacheQuizQuestion.get(index).toPostEntity();
		}
		// offline and empty cache
		return null;
	}

	/**
	 * Add a question to the cache.
	 * 
	 * @param myQuizQuestion
	 *            QuizQuestion to add
	 * @return
	 */
	private boolean addQuestionToCache(QuizQuestion myQuizQuestion) {
		return myCacheQuizQuestion.add(myQuizQuestion);
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
	 * For each question, it ask the proxy to send the question to the real subject.
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
	 * @param myQuestion
	 */
	private void addToFailBox(QuizQuestion myQuestion) {
		failBox.add(myQuestion);
	}
	
	/**
	 * Checks if all questions have been sent.
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
	 * @author valentin
	 *
	 */
	private class InnerProxyToCachePrivateTask implements IProxyToCachePrivateTasks {
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
