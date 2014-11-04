package com.googlecode.objectify.insight;

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
import java.util.regex.Pattern;

/**
 * Identifies codepoints. Also logs codepoints which have never been seen before so that developers
 * can simply grep logs for the codepoint hash. It'll be in the logs somewhere.
 */
@Singleton
@Log
public class Codepointer {

	/** If set true, we will not record code points - they will all be empty strings */
	@Getter @Setter
	private boolean disabled;
	
	/** Generates the stack trace */
	private StackTracer stackTracer = new StackTracer();

	/** Track which ones we've logged already. It's a Set, just map to the key value */
	private ConcurrentHashMap<String, String> logged = new ConcurrentHashMap<>();

	/**
	 * Get the hash of the code point. Also logs the definition of the code point, once per codepoint (per instance).
	 */
	public String getCodepoint() {
		if (disabled)
			return "disabled";

		String stack = stackTracer.stack();
		String digest = digest(stack);

		if (logged.putIfAbsent(digest, digest) == null) {
			log.info("Codepoint " + digest + " is " + stack);
		}

		return digest;
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
