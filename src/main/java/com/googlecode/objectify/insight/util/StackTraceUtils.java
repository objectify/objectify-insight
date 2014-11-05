package com.googlecode.objectify.insight.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StackTraceUtils {

	// http://regex101.com/r/aC1pS0/5
	private static Pattern pattern = Pattern.compile("((?:EnhancerBy|FastClassBy)\\w+)(\\${2})(\\w+)(\\${0,2}\\.?)");

	public static String removeMutableEnhancements(String oldStack) {
		String stack = oldStack;
		Matcher m = pattern.matcher(stack);
		if (m.find()) {
		    // replace first number with "number" and second number with the first
			stack = m.replaceAll("$1$2$4");
		}
		return stack;
	}

}
