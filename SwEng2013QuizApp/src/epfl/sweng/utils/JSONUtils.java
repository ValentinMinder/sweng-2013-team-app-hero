package epfl.sweng.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

public class JSONUtils {
	
	/**
	 * Convert an object JSONArray to an ArrayList
	 * @param jsonArray JSONArray you want to convert
	 * @return ArrayList corresponding to the parameter
	 * @throws JSONException
	 */
	public static ArrayList<String> convertJSONArrayToArrayListString(JSONArray jsonArray) throws JSONException {
		ArrayList<String> arrayReturn = new ArrayList<String>();
		if (jsonArray != null) {
			for (int i = 0; i < jsonArray.length(); i++) {
				arrayReturn.add(jsonArray.get(i).toString());
			}
		}

		return arrayReturn;
	}
}