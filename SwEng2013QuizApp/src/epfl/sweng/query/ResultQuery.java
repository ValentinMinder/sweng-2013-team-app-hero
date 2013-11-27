package epfl.sweng.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResultQuery {
	private String operand1;
	private String operand2;
	private String operator;
	private Set<String> results;

	public ResultQuery(String operand1, String operand2, String operator) {
		super();
		this.operand1 = operand1;
		this.operand2 = operand2;
		this.operator = operator;
		this.results = new HashSet<String>();
	}

	public void addResult(Set<String> l) {
		results.addAll(l);
	}

	public void print() {
		System.out.println("Results of query");
		for (String s : results) {
			System.out.println(s);
		}
	}

	public Set<String> getResult() {
		return results;
	}
}
