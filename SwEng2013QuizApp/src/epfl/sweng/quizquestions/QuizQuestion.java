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
	 * Valou: refactoring answer to answers to be consistent.
	 */
	private List<String> answers;
	
	/**
	 * Index of the solution in the list of answers 
	 */
	private int solutionIndex;
	
	/**
	 * Question tags
	 */
	private Set<String> tags;
	
	/**
	 * Question's owner
	 */
	private String owner;
	
	public QuizQuestion(final String jsonInput) throws JSONException  {
		JSONObject jsonQuestion = new JSONObject(jsonInput);
		
		id = jsonQuestion.getInt("id");
		question = jsonQuestion.getString("question");
		answers = JSONUtils.convertJSONArrayToArrayListString(jsonQuestion.getJSONArray("answers"));
		solutionIndex = jsonQuestion.getInt("solutionIndex");
		tags = JSONUtils.convertJSONArraySetString(jsonQuestion.getJSONArray("tags"));
		owner = jsonQuestion.getString("owner");
    }

	public QuizQuestion(final String question, final List<String> answers, final int
	        solutionIndex, final Set<String> tags, final int id, final String owner) {
		this.question=question;
		this.answers=answers;
		this.solutionIndex=solutionIndex;
		this.tags=tags;
		this.id = id;
		this.owner = owner;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public List<String> getAnswers() {
		return answers;
	}

	public void setAnswers(List<String> answers) {
		this.answers = answers;
	}

	public int getSolutionIndex() {
		return solutionIndex;
	}

	public void setSolutionIndex(int solutionIndex) {
		this.solutionIndex = solutionIndex;
	}

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
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
		entity.append(" \"" + answers.get(0) + "\"");
		for (int i = 1; i < answers.size(); i++) {
			entity.append(", \"" + answers.get(i) + "\"");
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
