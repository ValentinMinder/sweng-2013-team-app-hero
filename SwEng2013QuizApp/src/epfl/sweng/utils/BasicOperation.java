package epfl.sweng.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BasicOperation {
	public static List<String> and(Set<String> op1, Set<String> op2) {
		ArrayList<String> results = new ArrayList<String>();
		for (String s : op1) {
			if (op2.contains(s)) {
				results.add(s);
			}
		}
		return results;
	}

	public static List<String> or(Set<String> op1, Set<String> op2) {
		HashSet<String> tempSet = new HashSet<String>(op1);
		tempSet.addAll(op2);
		return new ArrayList<String>(tempSet);
	}
}
