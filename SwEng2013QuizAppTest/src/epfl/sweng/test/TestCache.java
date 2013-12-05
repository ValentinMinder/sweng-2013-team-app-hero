package epfl.sweng.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import android.test.AndroidTestCase;
import epfl.sweng.caching.Cache;
import epfl.sweng.caching.CacheException;
import epfl.sweng.caching.HandleOfflineQuery;
import epfl.sweng.patterns.ProxyHttpClient;
import epfl.sweng.query.EvaluateQuery;
import epfl.sweng.query.Parenthesis;
import epfl.sweng.quizquestions.QuizQuestion;

public class TestCache extends AndroidTestCase {
	private String directoryFiles;
	private QuizQuestion question;
	private String jsonQuestion;
	
	private void deleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				deleteRecursive(child);

		fileOrDirectory.delete();
	}
	
	private void initTest() {
		directoryFiles = getContext().getFilesDir().getAbsolutePath();
			deleteDirectory();

		Cache.deleteInstance();
		ProxyHttpClient.deleteInstance();
		Cache.setDirectoryFiles(directoryFiles);
		
		List<String> answers = new ArrayList<String>();
		answers.add("Test1");
		answers.add("Test2");

		Set<String> tags = new HashSet<String>();
		tags.add("test");

		question = new QuizQuestion("Question ?", answers, 0,
				tags, 0, "owner");
		
		StringBuffer entity = new StringBuffer();
		entity.append("{\n");
		entity.append("\"id\": 0,\n");
		entity.append("\"question\": \"Question ?\"," + " \"answers\": [");
		entity.append(" \"" + answers.get(0) + "\"");
		for (int i = 1; i < answers.size(); i++) {
			entity.append(", \"" + answers.get(i) + "\"");
		}
		
		entity.append(" ]," + " \"solutionIndex\": 0," + " \"tags\": [");

		Iterator<String> itTags = question.getTags().iterator();
		while (itTags.hasNext()) {
			String tag = itTags.next();
			if (itTags.hasNext()) {
				entity.append(" \"" + tag + "\", ");
			} else {
				entity.append("\"" + tag + "\"");
			}
		}
		
		entity.append(" ],");
		entity.append("\n\"owner\": \"owner\"");
		entity.append("\n}");

		jsonQuestion = entity.toString();
	}

	private void deleteDirectory() {
		String directoryFilesQuestions = directoryFiles + File.separator
				+ "questions";
		File directoryQuestions = new File(directoryFilesQuestions);
		deleteRecursive(directoryQuestions);
		
		String directoryFilesTags = directoryFiles + File.separator + "tags";
		File directoryTags = new File(directoryFilesTags);
		deleteRecursive(directoryTags);
		
		String directoryFilesUtils = directoryFiles + File.separator + "utils";
		File directoryUtils = new File(directoryFilesUtils);
		deleteRecursive(directoryUtils);
	}

	protected void setUp() throws Exception {
		super.setUp();
		initTest();
	}

	public void testAddQuestionToCache() {
		try {
			Cache.getInstance().addQuestionToCache(question);
		} catch (CacheException e) {
			Logger.getLogger("epfl.sweng.test").severe(e.getMessage());
			assertTrue("Cache exception for add question to cache", false);
		}

		String hashCode = String.valueOf(question.hashCode());

		Set<String> hashCodesTag = null;
		try {
			hashCodesTag = Cache.getInstance().getSetTag("test");
		} catch (CacheException e) {
			Logger.getLogger("epfl.sweng.test").severe(e.getMessage());
			assertTrue("Cache exception for getSetTag", false);
		}
		
		assertTrue("Wrong size set tag", hashCodesTag.size() == 1);

		Iterator<String> itTags = hashCodesTag.iterator();
		String hashCodeSetFirstQuestion = itTags.next();

		assertTrue("Wrong hash code", hashCodeSetFirstQuestion.equals(hashCode));
		
		String jsonQuestionCache = null;
		try {
			jsonQuestionCache = Cache.getInstance().getJSONQuestion(
					hashCodeSetFirstQuestion);
		} catch (CacheException e) {
			Logger.getLogger("epfl.sweng.test").severe(e.getMessage());
			assertTrue("Cache exception for getJSONQuestion", false);
		}

		assertTrue("JSON string not correspond",
				jsonQuestion.equals(jsonQuestionCache));
		
		HashSet<String> hashCodes = new HashSet<String>();
		hashCodes.add(hashCodeSetFirstQuestion);
		
		List<String> listQuestionsJSON = null;
		try {
			listQuestionsJSON = HandleOfflineQuery.getInstance().getArrayOfJSONQuestionsALL(hashCodes);
		} catch (CacheException e) {
			Logger.getLogger("epfl.sweng.test").severe(e.getMessage());
			assertTrue("Cache exception for getArrayOfJSONQuestions", false);
		}
		
		assertTrue("Wrong size get array of JSON questions",
				listQuestionsJSON.size() == 1);
		
		assertTrue("Wrong string JSON in list of JSON questions",
				listQuestionsJSON.get(0).equals(jsonQuestion));
		
		String questionTemp = null;
		try {
			questionTemp = Cache.getInstance().getRandomQuestionFromCache();
		} catch (CacheException e) {
			Logger.getLogger("epfl.sweng.test").severe(e.getMessage());
			assertTrue("Cache exception for getRandomQuestionFromCache", false);
		}
		
		assertTrue("Wrong string JSON in getRandomQuestionFromCache",
				questionTemp.equals(jsonQuestion));
	}
	

	public void testOutbox() {
		try {
			Cache.getInstance().addQuestionToOutBox(question);
		} catch (CacheException e) {
			Logger.getLogger("epfl.sweng.test").severe(e.getMessage());
			assertTrue("Cache exception for addQuestionToOutBox : " + e.getMessage(), false);
		}
		
		List<QuizQuestion> outbox = null;
		try {
			outbox = Cache.getInstance().getListOutBox();
		} catch (CacheException e) {
			Logger.getLogger("epfl.sweng.test").severe(e.getMessage());
			assertTrue("Cache exception for getListOutBox", false);
		}
		
		assertTrue("Wrong size get list outbox",
				outbox.size() == 1);
		
		assertTrue("Wrong string JSON in list outbox",
				outbox.get(0).toPostEntity().equals(jsonQuestion));
		
		deleteDirectory();
	}
	
	public void testEvaluationOfQuery() {
		deleteDirectory();
		String query = Parenthesis.parenthesis("non2+nonexistenttag");
		try {
			HashSet<String> result = EvaluateQuery.evaluate(query);
			assertTrue(result.size() == 0);
		} catch (CacheException e) {
			Logger.getLogger("epfl.sweng.test").severe(e.getMessage());
		}
		
	}
	
}
