package epfl.sweng.caching;

import epfl.sweng.quizquestions.QuizQuestion;

public interface ICacheToProxyPrivateTasks {
	void setProxyToCachePrivateTasks(IProxyToCachePrivateTasks myProxyToCachePrivateTasksS);
	void goOnlineResponse(boolean bool);
	int sendQuestion(QuizQuestion question);
	void setASyncCounter(int k);
}
