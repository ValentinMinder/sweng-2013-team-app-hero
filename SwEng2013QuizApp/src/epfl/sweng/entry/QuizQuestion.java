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
	private long id;
	
	/**
	 * Question field
	 */
	private String question;
	
	/**
	 * List of answers
	 */
	private ArrayList<String> answer;
	
	/**
	 * Index of the solution in the list of answers 
	 */
	private int solutionIndex;
	
	/**
	 * Question tags
	 */
	private ArrayList<String> tags;

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
	
	/**
	 * Formats the question in a JSON object.
	 * 
	 * Example:
	 *  "{" +
	 *  " \"question\":\"What is the answer to life, the universe and everything?\"," +
	 *  " \"answers\": [ \"42\", \"24\" ]," +
	 *  " \"solutionIndex\": 0," +
	 *  " \"tags\": [ \"h2g2\", \"trivia\" ]" +
	 *  " }";
	 * @return a formatted JSON object that fits for posting on sweng website
	 */
	public String toPostEntity() {
		// TODO: it works, but it seems very VERY complicated!
		StringBuffer entity = new StringBuffer();
		entity.append("{" +
			    " \"question\": \"" + question + "\"," +
			    " \"answers\": [");
		entity.append(" \"" + answer.get(0) + " \"");
		for (int i = 1; i < answer.size(); i++) {
			entity.append(", \"" + answer.get(i) + "\"");
		}
		entity.append(" ]," +
			    " \"solutionIndex\": " + solutionIndex + "," +
			    " \"tags\": [");
		for (int i = 1; i < tags.size(); i++) {
			if (i == 0){
				entity.append(" \"" + tags.get(i) + " \"");
			} else {
				entity.append(", \"" + tags.get(i) + "\"");
			}
		}
		entity.append(" ] }");
		return entity.toString();
	}
	

}
