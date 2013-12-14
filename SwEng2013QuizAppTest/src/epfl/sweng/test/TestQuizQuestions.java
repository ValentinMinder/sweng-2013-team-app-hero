package epfl.sweng.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;

import junit.framework.TestCase;
import epfl.sweng.quizquestions.QuizQuestion;

public class TestQuizQuestions extends TestCase {

	private QuizQuestion createNewQuizQuestion() {
		
		String question = "Coucou";
		ArrayList<String> answers = new ArrayList<String>();
		int solutionIndex = 1;
		String tags = "tags glkjd";
		String owner = "owner";
		int id = 1000;

		QuizQuestion q;
		answers.add("answers 1");
		answers.add("answ 2");
		
		q = new QuizQuestion(question, answers, solutionIndex,
				new HashSet<String>(Arrays.asList(tags.split("\\W+"))), id,
				owner);

		return q;
	}

	public void testSetQuestion() {
		/*
		 * Check that the QuizQuestion.setQuestion() method satisfies the
		 * postcondition below.
		 * 
		 * preconditions: question != null and question.size() <= 500 and there
		 * exists c in question such that !Character.isWhitespace(c)
		 * 
		 * postcondition: getQuestion() returns question
		 */

		/* To be implemented in exercise 5 */

		QuizQuestion q = createNewQuizQuestion();
		String newQ = "Newe question";

		q.setQuestion(newQ);

		assertTrue(q.getQuestion().equals(newQ));
	}

	public void testSetOwner() {
		/*
		 * Check that the QuizQuestion.setOwner() method satisfies the
		 * postcondition below.
		 * 
		 * precondition: owner != null and owner.size() <= 20 and there exists c
		 * in owner such that !Character.isWhitespace(c)
		 * 
		 * 
		 * postcondition: getOwner() returns owner
		 */

		/* To be implemented in exercise 5 */
		
		QuizQuestion q = createNewQuizQuestion();

		String newO = "new Onwer";
		q.setOwner(newO);

		assertTrue(newO.equals(q.getOwner()));
	}

	public void testSetAnswers() {
		/*
		 * Check that the QuizQuestion.setAnswers() method satisfies the
		 * postcondition below.
		 * 
		 * preconditions: answers != null and answers.size() >= 2 and
		 * answers.size() <= 10 and 0 <= solutionIndex < answers.size() and for
		 * all a in answers: a.size() <= 500 and there exists c in a such that
		 * !Character.isWhitespace(c)
		 * 
		 * postconditions: getAnswers() returns the list of answers and
		 * getCorrectAnswer() returns answers[i]
		 */

		/* To be implemented in exercise 5 */

		QuizQuestion q = createNewQuizQuestion();

		ArrayList<String> ans = new ArrayList<String>();
		int solutionIndex = 1;
		ans.add("ans 1");
		ans.add("ans 2");

		q.setAnswers(ans, solutionIndex);

		boolean eqAnswers = true;
		List<String> getA = q.getAnswers();
		for (String s : getA) {
			if (!ans.contains(s)) {
				eqAnswers = false;
				break;
			}
		}

		assertTrue(eqAnswers);
		String correct = getA.get(q.getSolutionIndex());
		assertTrue(correct.equals(ans.get(solutionIndex)));

	}

	public void testSetTags() {
		/*
		 * Check that the QuizQuestion.setTags() method satisfies the
		 * postcondition below.
		 * 
		 * preconditions: tags != null and tags.size() >= 1 and for all t in
		 * tags: t.size() <= 20 and there exists c in t such that
		 * !Character.isWhitespace(c)
		 * 
		 * postcondition: getTags() returns {tag | tag is in tags}
		 */

		/* To be implemented in exercise 5 */
		QuizQuestion q = createNewQuizQuestion();

		String ttags = "test atgs";
		Set<String> newTags = new HashSet<String>(Arrays.asList(ttags
				.split("\\W+")));
		q.setTags(newTags);

		Set<String> getted = q.getTags();

		for (String s : newTags) {
			if (!getted.contains(s)) {
				assertTrue(false);
			}
		}
		
		assertTrue(true);
	}

	public void testJSONConstruct() {
		QuizQuestion q = createNewQuizQuestion();
		QuizQuestion q2 = null;
		
		try {
			q2 = new QuizQuestion(q.toPostEntity());
		} catch (JSONException e) {
			Logger.getLogger("epfl.sweng.test").log(Level.INFO, "Fail in test",
					e);
		}

		assertTrue(q.equals(q2));
	}

	public void testAudit() {
		QuizQuestion q = createNewQuizQuestion();
		assertTrue(q.auditErrors() == 0);
	}
}
