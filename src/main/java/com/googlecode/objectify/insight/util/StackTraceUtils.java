package com.googlecode.objectify.insight.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StackTraceUtils {
	
	private static final String ENHANCER_BY = "EnhancerBy";
	private static final String FAST_CLASS_BY = "FastClassBy";
	
	// http://regex101.com/r/aC1pS0/5
	private static final Pattern pattern = Pattern.compile("((?:" + ENHANCER_BY + "|" + FAST_CLASS_BY + ")\\w+)(\\${2})(\\w+)(\\${0,2}\\.?)");
	
	public static String removeMutableEnhancements(String oldStack) {
		// much faster than pattern.matcher
		if (!containsMutableEnhancements(oldStack)) {
			return oldStack;
		}
		
		String stack = oldStack;
		Matcher m = pattern.matcher(stack);
		if (m.find()) {
			// replace first number with "number" and second number with the first
			stack = m.replaceAll("$1$2$4");
		}
		return stack;
	}
	
	public static boolean containsMutableEnhancements(String s) {
		return s.contains(ENHANCER_BY) || s.contains(FAST_CLASS_BY);
	}
	
}
