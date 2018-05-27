package utils;

public class CSV {

	static char dq = '\"';
	public static String protect(String in) {
		String out = in.trim();
		if(out.indexOf(dq) != -1) {
			// OK if just at start and end, otherwise trouble
			if(out.charAt(0) == dq && out.charAt(out.length()-1) == dq && out.replaceAll(dq+"", "").length() == out.length()-2) {
				// Retain double quotes
			}
			else {
				System.err.println("Internal double quote in field: " + in);
			}				
		}
		else if(in.indexOf(",") != -1) {
			out = dq + in + dq;
		}
		return out;
	}
}
