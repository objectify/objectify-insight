package com.googlecode.objectify.insight;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StackTracer {

	private static Pattern pattern = Pattern.compile("^(.*(?:EnhancerBy|FastClassBy).*\\${2})([a-zA-Z0-9]+)(\\${0,2}\\.?.*\\(<generated>\\))$", Pattern.MULTILINE);

	public String stack() {
		// It's tempting to getStackTrace() so we can skip all the Insight noise, but that would
		// clone the stacktrace which seems like extra gc work.
		StringWriter stackWriter = new StringWriter(1024);
		new Exception().printStackTrace(new PrintWriter(stackWriter));
		String stack = stackWriter.toString();
		
		Matcher m = pattern.matcher(stack);
		if (m.find()) {
		    // replace first number with "number" and second number with the first
		    stack = m.replaceAll("$1$3");
		}
		
		return stack;
	}
	
}
