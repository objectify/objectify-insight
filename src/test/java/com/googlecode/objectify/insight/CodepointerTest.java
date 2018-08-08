package com.googlecode.objectify.insight;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.googlecode.objectify.insight.Codepointer.AdvancedStackProducer;
import com.googlecode.objectify.insight.Codepointer.LegacyStackProducer;

/**
 */
public class CodepointerTest {
	
	@Test
	public void codepointingCanBeDisabled() throws Exception {
		Codepointer codepointer = new Codepointer();
		assertThat(codepointer.getCodepoint(), not(equalTo("disabled")));
		codepointer.setDisabled(true);
		assertThat(codepointer.getCodepoint(), equalTo("disabled"));
	}
	
	@Test
	public void testDefaultStackProducer() {
		assertThat(new Codepointer().getStackProducer(), Matchers.instanceOf(LegacyStackProducer.class));
	}
	
	@Test
	public void testCodepointForCompacterStack() {
		final StackTraceElement ste = new StackTraceElement("className", "methodName", "fileName", 1);
		final String compactedStack = Exception.class.getName() + "\r\n" + "\tat " + ste.toString() + "\r\n";
		
		final String codepoint1 = new Codepointer().setStackProducer(new LegacyStackProducer()).getCodepoint();
		final String codepoint2 = new Codepointer().setStackProducer(new AdvancedStackProducer()).getCodepoint();
		final String codepoint3 = new Codepointer().setStackProducer(new AdvancedStackProducer() {
			@Override
			protected Iterable<StackTraceElement> filterStack(Iterable<StackTraceElement> stack) {
				return stack;
			}
		}).getCodepoint();
		final String codepoint4 = new Codepointer().setStackProducer(new AdvancedStackProducer() {
			@Override
			protected Iterable<StackTraceElement> filterStack(Iterable<StackTraceElement> stack) {
				return Lists.newArrayList(ste);
			}
		}).getCodepoint();
		
		assertThat(codepoint3, not(equalTo(codepoint2)));
		
		assertThat(codepoint4, not(equalTo(codepoint1)));
		assertThat(codepoint4, not(equalTo(codepoint2)));
		assertThat(codepoint4, not(equalTo(codepoint3)));
		assertThat(codepoint4, equalTo(new Codepointer().digest(compactedStack)));
	}
	
	@Test
	public void testCompacterStack() {
		final String stack1 = new Codepointer().setStackProducer(new LegacyStackProducer()).stack();
		final String stack2 = new Codepointer().setStackProducer(new AdvancedStackProducer()).stack();
		final String stack3 = new Codepointer().setStackProducer(new AdvancedStackProducer() {
			@Override
			protected Iterable<StackTraceElement> filterStack(Iterable<StackTraceElement> stack) {
				return stack;
			}
		}).stack();
		final String stack4 = new Codepointer().setStackProducer(new AdvancedStackProducer() {
			@Override
			protected Iterable<StackTraceElement> filterStack(Iterable<StackTraceElement> stack) {
				return Iterables.limit(stack, 1);
			}
		}).stack();
		
		assertThat(getStackTraceElementCount(stack1), greaterThan(2));
		assertThat(getStackTraceElementCount(stack2), greaterThan(2));
		assertThat(getStackTraceElementCount(stack3), greaterThan(2));
		assertThat(getStackTraceElementCount(stack4), equalTo(1));
	}

	private int getStackTraceElementCount(final String stack) {
		return stack.split("\tat").length - 1;
	}
	
	@Test
	public void testAdvancedFiltering() {
		final String stack1 = new Codepointer().setStackProducer(new LegacyStackProducer()).stack();
		final String stack2 = new Codepointer().setStackProducer(new AdvancedStackProducer()).stack();
		
		assertThat(stack1, containsString("com.googlecode.objectify."));
		assertThat(stack2, not(containsString("com.googlecode.objectify.")));
	}
}
