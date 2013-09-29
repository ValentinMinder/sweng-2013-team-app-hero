package epfl.sweng.entry;

import java.util.ArrayList;

/**
 * 
 * Defines a quiz question.
 * 
 * @author xhanto
 *
 */
public class QuizQuestion {
	/**
	 * Question identifier
	 */
	long id;
	/**
	 * Question field
	 */
	String question;
	/**
	 * List of answers
	 */
	ArrayList<String> answer;
	/**
	 * Index of the solution in the list of answers 
	 */
	int solutionIndex;
	/**
	 * Question tags
	 */
	ArrayList<String> tags;

	public QuizQuestion(long id, String question, ArrayList<String> answer, int solutionIndex, ArrayList<String> tags) {
		this.id=id;
		this.question=question;
		this.answer=answer;
		this.solutionIndex=solutionIndex;
		this.tags=tags;
	}

	public long getId() {
		return id;
	}

	public String getQuestion() {
		return question;
	}

	public ArrayList<String> getAnswer() {
		return answer;
	}

	public int getSolutionIndex() {
		return solutionIndex;
	}

	public ArrayList<String> getTags() {
		return tags;
	}

	@Override
	public String toString() {
		return "QuizQuestion [id=" + id + ", question=" + question
				+ ", answer=" + answer + ", solutionIndex=" + solutionIndex
				+ ", tags=" + tags + "]";
	}
	

}
