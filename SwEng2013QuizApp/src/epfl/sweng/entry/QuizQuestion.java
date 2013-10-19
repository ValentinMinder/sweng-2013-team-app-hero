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

	public QuizQuestion(long idQ, String quest, ArrayList<String> answ, int solutionInd, ArrayList<String> tagsList) {
		this.question=quest;
		this.answer=answ;
		this.solutionIndex=solutionInd;
		this.tags=tagsList;
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
		StringBuffer entity = new StringBuffer();
		entity.append("{" +
			    " \"question\": \"" + question + "\"," +
			    " \"answers\": [");
		entity.append(" \"" + answer.get(0) + "\"");
		for (int i = 1; i < answer.size(); i++) {
			entity.append(", \"" + answer.get(i) + "\"");
		}
		entity.append(" ]," +
			    " \"solutionIndex\": " + solutionIndex + "," +
			    " \"tags\": [");
		for (int i = 0; i < tags.size(); i++) {
			if (i == 0) {
				entity.append(" \"" + tags.get(i) + "\"");
			} else {
				entity.append(", \"" + tags.get(i) + "\"");
			}
		}
		entity.append(" ] }");
		return entity.toString();
	}
	

}
