package epfl.sweng.caching;

import epfl.sweng.quizquestions.QuizQuestion;

public interface IProxyToCachePrivateTasks {
	boolean addQuestionToCache(QuizQuestion myQuizQuestion);
	boolean addQuestionToOutBox(QuizQuestion myQuizQuestion);
	boolean sendOutBox();
	void addToFailBox(QuizQuestion myQuestion);
	boolean getSentStatus();
	String getRandomQuestionFromCache();
}
