package epfl.sweng.servercomm;

public class UtilsHttpResponse {

	// SEE http://fr.wikipedia.org/wiki/Liste_des_codes_HTTP for details
	public final static int OK = 200;
	public final static String OK_MSG = "OK";
	public final static int CREATED = 201;
	public final static String CREATED_MSG = "CREATED";
	public final static int UNAUTHORIZED = 401;
	public final static String UNAUTHORIZED_MSG = "Unauthorized";
	public final static int METHOD_NOT_ALLOWED = 405;
	public final static String METHOD_NOT_ALLOWED_MSG = "Method Not Allowed";
	public final static int INTERNAL_SERVER_ERROR = 500;
	public final static String INTERNAL_SERVER_ERROR_MSG = "Internal Server Errror";

}
