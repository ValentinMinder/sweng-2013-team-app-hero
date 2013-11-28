package epfl.sweng.test;

import java.util.ArrayList;
import java.util.HashSet;

import junit.framework.TestCase;
import epfl.sweng.quizquestions.QuizQuestion;


public class QuizQuestionTest extends TestCase {

	
	ArrayList<String> answer = new ArrayList<String>();
	
	HashSet<String> tagSet = new HashSet<String>();

	QuizQuestion question;
	
	
    public void testSetQuestion() {
        
    	
    	answer.add("fine");
    	answer.add("not fine");
    	String quest = "How am I ?";
    	
    	tagSet.add("tag");
    	tagSet.add("tag2");
    	
    	question = new QuizQuestion("How are you ?", answer, 0, tagSet, 0, "Me");
    	
    	question.setQuestion(quest);
    	assertTrue(question.getQuestion().equals(quest));
    	
    }

    public void testSetOwner() {
      
    	
    	question = new QuizQuestion("How are you ?", answer, 0, tagSet, 0, "Me");

    	String owner = "You";
    	
    	question.setOwner(owner);
    	assertTrue(question.getOwner().equals(owner));

    }

    public void testSetAnswers() {
       
    	
    	question = new QuizQuestion("Hello ?", answer, 0, tagSet, 0, "Me");
    	ArrayList<String> answers = new ArrayList<String>();
    	answers.add("Yes");
    	answers.add("No");
    	answers.add("Maybe");
    	
    	int solutionIndex = 1; // "No"
    	
    	question.setAnswers(answers, solutionIndex);
    	
    	assertTrue(question.getAnswers().equals(answers));
    	assertTrue(question.getSolutionIndex()==solutionIndex);
    	   	
    }

    public void testSetTags() {
       
    	question = new QuizQuestion("Hello ?", answer, 0, tagSet, 0, "Me");

    	HashSet<String> tags = new HashSet<String>();
    	tags.add("tag1");
    	tags.add("taaaaaaaaaaaaaaaags");
    	
    	question.setTags(tags);
    	
    	assertTrue(question.getTags().containsAll(tags));
    	
    	
    }
}