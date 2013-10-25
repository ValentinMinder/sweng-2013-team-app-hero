package epfl.sweng.quizquestions;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import epfl.sweng.utils.JSONUtils;

/**
 * 
 * Defines a quiz question.
 * 
 * @author xhanto
 *
 */
public class QuizQuestion {
	
	/**
	 * Question id
	 */
	private int id;
	
	/**
	 * Question field
	 */
	private String question;
	
	/**
	 * List of answers
	 */
	private List<String> answer;
	
	/**
	 * Index of the solution in the list of answers 
	 */
	private int solutionIndex;
	
	/**
	 * Question's owner
	 */
	private String owner;
	
	/**
	 * Question tags
	 */
	private Set<String> tags;
	
	public QuizQuestion(final String jsonInput) throws JSONException  {
		JSONObject jsonQuestion = new JSONObject(jsonInput);
		
		id = jsonQuestion.getInt("id");
		question = jsonQuestion.getString("question");
		answer = JSONUtils.convertJSONArrayToArrayListString(jsonQuestion.getJSONArray("answers"));
		solutionIndex = jsonQuestion.getInt("solutionIndex");
		tags = JSONUtils.convertJSONArraySetString(jsonQuestion.getJSONArray("tags"));
    }

	public QuizQuestion(final String question, final List<String> answers, final int
	        solutionIndex, final Set<String> tags, final int id, final String owner) {
		this.question=question;
		this.answer=answers;
		this.solutionIndex=solutionIndex;
		this.tags=tags;
	}
	
	public int getId() {
		return id;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public String getQuestion() {
		return question;
	}

	public List<String> getAnswer() {
		return answer;
	}

	public int getSolutionIndex() {
		return solutionIndex;
	}

	public Set<String> getTags() {
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
		
		Iterator<String> itTags = tags.iterator();
		while (itTags.hasNext()) {
			String tag = itTags.next();
			if (itTags.hasNext()) {
				entity.append(" \"" + tag + "\", ");
			} else {
				entity.append("\"" + tag + "\"");
			}
		}
		entity.append(" ] }");
		return entity.toString();
	}
	

}
