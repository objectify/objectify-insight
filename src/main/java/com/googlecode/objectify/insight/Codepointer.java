package com.googlecode.objectify.insight;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.io.BaseEncoding;
import com.googlecode.objectify.insight.util.StackTraceUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;

/**
 * Identifies codepoints. Also logs codepoints which have never been seen before so that developers
 * can simply grep logs for the codepoint hash. It'll be in the logs somewhere.
 */
@Singleton
@Log
@Accessors(chain=true)
public class Codepointer {
	
	/** If set true, we will not record code points - they will all be empty strings */
	@Getter @Setter
	private boolean disabled;
	
	@Getter @Setter
	private StackProducer stackProducer = new LegacyStackProducer();
	
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
	
	@VisibleForTesting
	String stack() {
		return stackProducer.getStack();
	}
	
	/** Give a hex encoded digest of the string */
	@VisibleForTesting
	String digest(String str) {
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
	
	public abstract static class StackProducer {
		protected abstract String getStack();
	}
	
	public static class LegacyStackProducer extends StackProducer {
		@Override
		protected String getStack() {
			// It's tempting to getStackTrace() so we can skip all the Insight noise, but that would
			// clone the stacktrace which seems like extra gc work.
			StringWriter stackWriter = new StringWriter(1024);
			new Exception().printStackTrace(new PrintWriter(stackWriter));
			String stack = stackWriter.toString();
			stack = StackTraceUtils.removeMutableEnhancements(stack);
			return stack;
		}
	}
	
	public static abstract class FilteringStackProducer extends StackProducer {
		@Override
		protected String getStack() {
			Exception e = new Exception();
			Iterable<StackTraceElement> stackTrace = filterStack(Arrays.asList(e.getStackTrace()));
			
			StringBuilder sb = new StringBuilder(1024);
			sb.append(e).append("\r\n");
			for (StackTraceElement ste : stackTrace) {
				sb.append("\tat ").append(ste.toString()).append("\r\n");
			}
			return sb.toString();
		}
		
		/**
		 * Implement if you want to modify the stacktrace used for codepoint generation.<br>
		 * Useful for removing meaningless stack trace elements (for example: servlets, filters, etc).
		 */
		protected abstract Iterable<StackTraceElement> filterStack(Iterable<StackTraceElement> stack);
		
		/**
		 * Removes stack trace elements from classes whose name - not simple name (!) - starts with the provided prefix.
		 */
		protected static Predicate<StackTraceElement> removeStackTraceElementsFromPackage(final String classNamePrefix) {
			return new Predicate<StackTraceElement>() {
				@Override
				public boolean apply(StackTraceElement ste) {
					return !ste.getClassName().startsWith(classNamePrefix);
				}
			};
		}
		
		protected static Predicate<StackTraceElement> removeStackTraceElementsWithMutableEnhancements() {
			return new Predicate<StackTraceElement>() {
				@Override
				public boolean apply(StackTraceElement ste) {
					return !StackTraceUtils.containsMutableEnhancements(ste.getClassName());
				}
			};
		}
	}
	
	public static class AdvancedStackProducer extends FilteringStackProducer {
		@Override
		protected Iterable<StackTraceElement> filterStack(Iterable<StackTraceElement> stack) {
			return FluentIterable.from(stack)
					// mutable enhancements
					.filter(removeStackTraceElementsWithMutableEnhancements())
					// basic appengine servlet classes
					.filter(removeStackTraceElementsFromPackage("com.google.apphosting."))
					.filter(removeStackTraceElementsFromPackage("com.google.tracing."))
					.filter(removeStackTraceElementsFromPackage("java.lang.Thread"))
					.filter(removeStackTraceElementsFromPackage("javax.servlet.http."))
					.filter(removeStackTraceElementsFromPackage("org.mortbay.jetty."))
					.filter(removeStackTraceElementsFromPackage("org.eclipse.jetty."))
					// proxy and reflection classes
					.filter(removeStackTraceElementsFromPackage("com.sun.proxy."))
					.filter(removeStackTraceElementsFromPackage("java.lang.reflect."))
					.filter(removeStackTraceElementsFromPackage("java.security."))
					.filter(removeStackTraceElementsFromPackage("sun.reflect."))
					// guice
					.filter(removeStackTraceElementsFromPackage("com.google.inject."))
					// cloud endpoints
					.filter(removeStackTraceElementsFromPackage("com.google.api.server.spi."))
					// gwt rpc
					.filter(removeStackTraceElementsFromPackage("com.google.gwt.user.server.rpc."))
					// objectify, objectify-insight
					.filter(removeStackTraceElementsFromPackage("com.googlecode.objectify."));
		}
	}
}
