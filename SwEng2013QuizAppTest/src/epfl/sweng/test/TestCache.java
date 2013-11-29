package epfl.sweng.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.test.AndroidTestCase;

import epfl.sweng.caching.Cache;
import epfl.sweng.caching.CacheException;
import epfl.sweng.quizquestions.QuizQuestion;

public class TestCache extends AndroidTestCase {
	private String directoryFiles;

	private void deleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				deleteRecursive(child);

		fileOrDirectory.delete();
	}

	private void deleteDirectory() {
		String directoryFilesQuestions = directoryFiles + File.separator
				+ "questions";
		String directoryFilesTags = directoryFiles + File.separator + "tags";
		File directoryQuestions = new File(directoryFilesQuestions);
		File directoryTags = new File(directoryFilesTags);
		deleteRecursive(directoryQuestions);
		deleteRecursive(directoryTags);
	}

	protected void setUp() throws Exception {
		super.setUp();
		directoryFiles = getContext().getFilesDir().getAbsolutePath();
		deleteDirectory();

		Cache.setDirectoryFiles(directoryFiles);
	}

	protected void tearDown() throws Exception {
		deleteDirectory();
	}

	public void testAddQuestionToCache() {
		List<String> answers = new ArrayList<String>();
		answers.add("Test1");
		answers.add("Test2");

		Set<String> tags = new HashSet<String>();
		tags.add("test");

		QuizQuestion questionTest = new QuizQuestion("Question ?", answers, 0,
				tags, 0, "owner");
		try {
			Cache.getInstance().addQuestionToCache(questionTest);
		} catch (CacheException e) {
			assertTrue("Cache exception for add question to cache", false);
		}

		String hashCode = String.valueOf(questionTest.hashCode());

		Set<String> hashCodesTag = null;
		try {
			hashCodesTag = Cache.getInstance().getSetTag("test");
		} catch (CacheException e) {
			assertTrue("Cache exception for getSetTag", false);
		}
		
		assertTrue("Wrong size set tag", hashCodesTag.size() == 1);

		Iterator<String> itTags = hashCodesTag.iterator();
		String hashCodeSetFirstQuestion = itTags.next();

		assertTrue("Wrong hash code", hashCodeSetFirstQuestion.equals(hashCode));

		StringBuffer entity = new StringBuffer();
		entity.append("{\n");
		entity.append("\"id\": 0,\n");
		entity.append("\"question\": \"Question ?\"," + " \"answers\": [");
		entity.append(" \"" + answers.get(0) + "\"");
		for (int i = 1; i < answers.size(); i++) {
			entity.append(", \"" + answers.get(i) + "\"");
		}
		
		entity.append(" ]," + " \"solutionIndex\": 0," + " \"tags\": [");

		itTags = tags.iterator();
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

		String jsonQuestion = entity.toString();
		String jsonQuestionCache = null;
		try {
			jsonQuestionCache = Cache.getInstance().getJSONQuestion(
					hashCodeSetFirstQuestion);
		} catch (CacheException e) {
			assertTrue("Cache exception for getJSONQuestion", false);
		}

		assertTrue("JSON string not correspond",
				jsonQuestion.equals(jsonQuestionCache));
		
		Set<String> hashCodes = new HashSet<String>();
		hashCodes.add(hashCodeSetFirstQuestion);
		
		List<String> listQuestionsJSON = null;
		try {
			listQuestionsJSON = Cache.getInstance().getArrayOfJSONQuestions(hashCodes);
		} catch (CacheException e) {
			assertTrue("Cache exception for getArrayOfJSONQuestions", false);
		}
		
		assertTrue("Wrong size get array of JSON questions",
				listQuestionsJSON.size() == 1);
		
		assertTrue("Wrong string JSON in list of JSON questions",
				listQuestionsJSON.get(0).equals(jsonQuestion));
	}
	
	public void testOutbox() {
		
	}
	
	public void testFailbox() {
		
	}
	
	
}
