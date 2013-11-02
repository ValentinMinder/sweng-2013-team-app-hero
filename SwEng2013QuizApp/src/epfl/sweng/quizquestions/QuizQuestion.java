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
	 * HW4: 2.2.2 audit method.
	 * @return
	 */
	public int auditErrors() {
		int errors = 0;

		// TODO: to check the importance of the numbers of errors returned (inv 2 and 6)...
		// TODO: bullet/invariant 4 is it sufficient or correct regarding the given invariant?
		// TODO: check with jenkins, they are maybe errors in the audit (it's impossible to see them)
		
		// INV n° 1: 0 < len(question.question) <= 500 AND there exists i SUCH THAT !Character.isWhitespace(question.question.charAt(i)) 
		boolean inv1part3 = false;
		for (int i = 0; i < question.length() && !inv1part3; i++) {
			if (!Character.isWhitespace(question.charAt(i))) {
				inv1part3 = true;
			}
		}
		boolean invariant1 = (question.length() > 0) && (question.length() <= 500) && inv1part3;
		if (!invariant1){
			errors ++;
		}
		
		// INV n°2: FOR ALL k from 0 up to len(question.answers), 0 <len(question.answers[k]) <= 500 AND there exists i such that !Character.isWhitespace(question.answer[k].charAt(i))
		for (int k = 0; k < answers.size(); k++) {
			boolean inv2Kpart3 = false;
			String answerK = answers.get(k);
			for (int i = 0; i < answerK.length() && !inv2Kpart3; i++) {
				if (!Character.isWhitespace(answerK.charAt(i))) {
					inv2Kpart3 = true;
				}
			}
			boolean invariant2K = (answerK.length() > 0) && (answerK.length() <= 500) && inv2Kpart3;
			if (!invariant2K){
				errors ++;
			}
		}
		
		// INV N°3: 2 <= len(question.answers) <= 10
		boolean invariant3 = (answers.size() >= 2) && (answers.size() <= 10);
		if (invariant3){
			errors++;
		}
		
		//INV N°4: there exists i such that isMarkedCorrect(question.answers[i])
		boolean invariant4 = (solutionIndex >= 0) && (solutionIndex < answers.size());
		if (invariant4){
			errors++;
		}
		
		//INV N°5: 1 <= len(question.tags) <= 20
		boolean invariant5 = true;
		if (invariant5){
			errors++;
		}
		
		//INV N°6: FOR ALL k from 0 up to len(question.tags), 0 <len(question.tags[k]) <= 20 AND there exists i such that !Character.isWhitespace(question.answers[k].charAt(i))
		// (String[])  cast is permitted because we have Set<String>
		String[] tagArray = (String[]) tags.toArray();
		for (int k = 0; k < tagArray.length; k++) {
			boolean inv6Kpart3 = false;
			String tagK = tagArray[k];
			for (int i = 0; i < tagK.length() && !inv6Kpart3; i++) {
				if (!Character.isWhitespace(tagK.charAt(i))) {
					inv6Kpart3 = true;
				}
			}
			boolean invariant6K = (tagK.length() > 0) && (tagK.length() <= 20) && inv6Kpart3;
			if (!invariant6K){
				errors ++;
			}
		}
		
		return errors;
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
