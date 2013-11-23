package epfl.sweng.quizquestions;

import java.io.Serializable;
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
public class QuizQuestion implements Serializable {

	private static final int TAGS_SIZE = 20;
	
	private static final int ANSWER_LENGTH = 500;
	
	private static final int ANSWERS_SIZE = 10;
	
	/**
	 * Question id
	 */
	private long id;

	/**
	 * Question field
	 */
	private String question;

	/**
	 * List of answers Valou: refactoring answer to answers to be consistent.
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

	public QuizQuestion(final String jsonInput) throws JSONException {
		JSONObject jsonQuestion = new JSONObject(jsonInput);

		// une question pas encore envoyee au serveur n'a pas de owner ni id
		try {
			id = jsonQuestion.getLong("id");
			owner = jsonQuestion.getString("owner");
		} catch (JSONException e) {
			id = -1;
			owner = "anonymous";
		}
		
		question = jsonQuestion.getString("question");
		answers = JSONUtils.convertJSONArrayToArrayListString(jsonQuestion
				.getJSONArray("answers"));
		solutionIndex = jsonQuestion.getInt("solutionIndex");
		tags = JSONUtils.convertJSONArraySetString(jsonQuestion
				.getJSONArray("tags"));
	}

	public QuizQuestion(final String question, final List<String> answers,
			final int solutionIndex, final Set<String> tags, final int id,
			final String owner) {
		this.question = question;
		this.answers = answers;
		this.solutionIndex = solutionIndex;
		this.tags = tags;
		this.id = id;
		this.owner = owner;
	}

	public long getId() {
		return id;
	}

	public void setId(int idQ) {
		this.id = idQ;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String questionS) {
		this.question = questionS;
	}

	public List<String> getAnswers() {
		return answers;
	}

	public void setAnswers(List<String> answersS) {
		this.answers = answersS;
	}
	
	public void setAnswers(List<String> answersS, int solutionIndexS) {
		this.answers = answersS;
		this.solutionIndex = solutionIndexS;
	}

	public int getSolutionIndex() {
		return solutionIndex;
	}

	public void setSolutionIndex(int solutionIndexS) {
		this.solutionIndex = solutionIndexS;
	}

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tagsS) {
		this.tags = tagsS;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String ownerS) {
		this.owner = ownerS;
	}

	/**
	 * HW4: 2.2.2 audit method.
	 * 
	 * @return
	 */
	public int auditErrors() {
		int errors = 0;

		// INV n°1: 0 < len(question.question) <= 500 AND there exists i SUCH
		// THAT !Character.isWhitespace(question.question.charAt(i))
		boolean inv1part3 = false;
		for (int i = 0; i < question.length() && !inv1part3; i++) {
			if (!Character.isWhitespace(question.charAt(i))) {
				inv1part3 = true;
			}
		}
		boolean invariant1 = (question.length() > 0)
				&& (question.length() <= ANSWER_LENGTH) && inv1part3;
		if (!invariant1) {
			errors++;
		}

		// INV n°2: FOR ALL k from 0 up to len(question.answers), 0
		// <len(question.answers[k]) <= 500 AND there exists i such that
		// !Character.isWhitespace(question.answer[k].charAt(i))
		for (int k = 0; k < answers.size(); k++) {
			boolean inv2Kpart3 = false;
			String answerK = answers.get(k);
			for (int i = 0; i < answerK.length() && !inv2Kpart3; i++) {
				if (!Character.isWhitespace(answerK.charAt(i))) {
					inv2Kpart3 = true;
				}
			}
			boolean invariant2K = (answerK.length() > 0)
					&& (answerK.length() <= ANSWER_LENGTH) && inv2Kpart3;
			if (!invariant2K) {
				errors++;
			}
		}

		// INV N°3: 2 <= len(question.answers) <= 10
		boolean invariant3 = (answers.size() >= 2) && (answers.size() <= ANSWERS_SIZE);
		if (!invariant3) {
			errors++;
		}

		// INV N°4: there exists i such that
		// isMarkedCorrect(question.answers[i])
		boolean invariant4 = (solutionIndex >= 0)
				&& (solutionIndex < answers.size());
		if (!invariant4) {
			errors++;
		}

		// INV N°5: 1 <= len(question.tags) <= 20
		boolean invariant5 = (tags.size() >= 1) && (tags.size() <= TAGS_SIZE);
		if (!invariant5) {
			errors++;
		}

		// INV N°6: FOR ALL k from 0 up to len(question.tags), 0
		// <len(question.tags[k]) <= 20 AND there exists i such that
		// !Character.isWhitespace(question.answers[k].charAt(i))
		// (String[]) cast is permitted because we have Set<String>
		Object[] tagArray = tags.toArray();
		for (int k = 0; k < tagArray.length; k++) {
			boolean inv6Kpart3 = false;
			String tagK = (String) tagArray[k];
			for (int i = 0; i < tagK.length() && !inv6Kpart3; i++) {
				if (!Character.isWhitespace(tagK.charAt(i))) {
					inv6Kpart3 = true;
				}
			}
			boolean invariant6K = (tagK.length() > 0) && (tagK.length() <= TAGS_SIZE)
					&& inv6Kpart3;
			if (!invariant6K) {
				errors++;
			}
		}

		return errors;
	}

	/**
	 * Formats the question in a JSON object.
	 * 
	 * Example: "{" +
	 * " \"question\":\"What is the answer to life, the universe and everything?\","
	 * + " \"answers\": [ \"42\", \"24\" ]," + " \"solutionIndex\": 0," +
	 * " \"tags\": [ \"h2g2\", \"trivia\" ]" + " }";
	 * 
	 * @return a formatted JSON object that fits for posting on sweng website
	 */
	public String toPostEntity() {
		StringBuffer entity = new StringBuffer();
		entity.append("{\n");
		entity.append("\"id\": " + id + ",\n");
		entity.append("\"question\": \"" + question + "\","
				+ " \"answers\": [");
		entity.append(" \"" + answers.get(0) + "\"");
		for (int i = 1; i < answers.size(); i++) {
			entity.append(", \"" + answers.get(i) + "\"");
		}
		entity.append(" ]," + " \"solutionIndex\": " + solutionIndex + ","
				+ " \"tags\": [");

		Iterator<String> itTags = tags.iterator();
		while (itTags.hasNext()) {
			String tag = itTags.next();
			if (itTags.hasNext()) {
				entity.append(" \"" + tag + "\", ");
			} else {
				entity.append("\"" + tag + "\"");
			}
		}
		entity.append(" ],");
		entity.append("\n\"owner\": \"" + owner + "\"");
		entity.append("\n}");
		return entity.toString();
	}

}
