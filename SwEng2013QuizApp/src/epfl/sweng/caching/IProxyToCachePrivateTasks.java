package epfl.sweng.caching;

import epfl.sweng.quizquestions.QuizQuestion;

public interface IProxyToCachePrivateTasks {
	boolean addQuestionToCache(QuizQuestion myQuizQuestion) throws CacheException;
	boolean addQuestionToOutBox(QuizQuestion myQuizQuestion) throws CacheException;
	boolean sendOutBox() throws CacheException;
	void addToFailBox(QuizQuestion myQuestion);
	boolean getSentStatus() throws CacheException;
	String getRandomQuestionFromCache() throws CacheException;
}
