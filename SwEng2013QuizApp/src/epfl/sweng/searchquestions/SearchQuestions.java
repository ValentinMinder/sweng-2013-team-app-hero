package epfl.sweng.searchquestions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;

import android.os.AsyncTask;
import epfl.sweng.patterns.ProxyHttpClient;
import epfl.sweng.quizquestions.QuizQuestion;

public class SearchQuestions {
    private static SearchQuestions instance = null;
    private String nextID = null;
    private String request = null;
    private LinkedList<QuizQuestion> cachedRequestArray = null;

    private SearchQuestions(String requestS) {
	cachedRequestArray = new LinkedList<QuizQuestion>();
	this.request = requestS;
    }

    public static synchronized SearchQuestions getInstance(String requestS) {
	if (instance == null) {
	    instance = new SearchQuestions(requestS);
	}
	return instance;
    }

    public QuizQuestion getNextQuizQuestion(String sessionID) {
	if (cachedRequestArray.isEmpty()) {
	    // TODO retrieve from server, and fill the array
	    new GetQuestionTask().execute(sessionID);

	}
	// if we have a remaining array of question.
	if (!cachedRequestArray.isEmpty()) {
	    return cachedRequestArray.peekFirst();
	}
	// if the array was empty and the server didn't get any more question.
	return null;
    }

    public void setRequest(String requestS) {
	this.request = requestS;
    }

    private class GetQuestionTask extends AsyncTask<String, Void, HttpResponse> {

	/**
	 * Execute and retrieve the answer from the website.
	 */
	@Override
	protected HttpResponse doInBackground(String... questionElement) {
	    HttpPost post = new HttpPost(
		    "https://sweng-quiz.appspot.com/search");
	    post.setHeader("Authorization", "Tequila " + questionElement[0]);
	    String jsonQuery = "{\n\"query\": \"" + request + "\"\n}";
	    try {
		post.setEntity(new StringEntity(jsonQuery));
		HttpResponse response = ProxyHttpClient.getInstance().execute(
			post);
		return response;
	    } catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (ClientProtocolException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	    return null;

	}

	/**
	 * Execute and retrieve the answer from the website.
	 */
	protected void onPostExecute(HttpResponse httpResponse) {
	    if (httpResponse == null
		    || httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
		return;
	    }

	    String content = httpResponse.getEntity().toString();
	    System.out.println(content);
	    try {
		JSONArray array = new JSONArray(content);
		JSONArray questionArray = array.getJSONArray(0);
		
		for (int i = 0; i < questionArray.length(); ++i) {
		    cachedRequestArray.add(new QuizQuestion(questionArray.getString(i)));
		    System.out.println(questionArray.getString(i));
		}
		
	    } catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
    }
}
