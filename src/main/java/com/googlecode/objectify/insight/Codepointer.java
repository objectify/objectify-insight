package com.googlecode.objectify.insight;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.BaseEncoding;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import javax.inject.Singleton;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Identifies codepoints. Also logs codepoints which have never been seen before so that developers
 * can simply grep logs for the codepoint hash. It'll be in the logs somewhere.
 */
@Singleton
@Log
public class Codepointer {

	// http://regex101.com/r/aC1pS0/5
	private static Pattern pattern = Pattern.compile("((?:EnhancerBy|FastClassBy)\\w+)(\\${2})(\\w+)(\\${0,2}\\.?)");

	/** If set true, we will not record code points - they will all be empty strings */
	@Getter @Setter
	private boolean disabled;
	
	/** Track which ones we've logged already. It's a Set, just map to the key value */
	private ConcurrentHashMap<String, String> logged = new ConcurrentHashMap<>();

	/**
	 * Get the hash of the code point. Also logs the definition of the code point, once per codepoint (per instance).
	 */
	public String getCodepoint() {
		if (disabled)
			return "disabled";

		String stack = stack();
		String digest = digest(stack);

		if (logged.putIfAbsent(digest, digest) == null) {
			log.info("Codepoint " + digest + " is " + stack);
		}

		return digest;
	}

	private String stack() {
		// It's tempting to getStackTrace() so we can skip all the Insight noise, but that would
		// clone the stacktrace which seems like extra gc work.
		StringWriter stackWriter = new StringWriter(1024);
		new Exception().printStackTrace(new PrintWriter(stackWriter));
		String stack = stackWriter.toString();
		stack = removeMutableEnhancements(stack);
		return stack;
	}

	@VisibleForTesting
	public String removeMutableEnhancements(String oldStack) {
		String stack = oldStack;
		Matcher m = pattern.matcher(stack);
		if (m.find()) {
		    // replace first number with "number" and second number with the first
			stack = m.replaceAll("$1$2$4");
		}
		return stack;
	}
	
	/** Give a hex encoded digest of the string */
	private String digest(String str) {
		// Checked exceptions are retarded

		final MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Impossible", e);
		}

		try {
			return BaseEncoding.base16().encode(md.digest(str.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Impossible", e);
		}
	}
}
