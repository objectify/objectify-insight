package com.googlecode.objectify.insight.test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.testng.annotations.Test;

import com.googlecode.objectify.insight.Codepointer;

/**
 */
public class CodepointerTest {

	private static String nonEnhanced = "at com.googlecode.objectify.insight.StackTracer.stack(StackTracer.java:16)\r\n"
			+ "\tat com.googlecode.objectify.insight.StackTracerTest.stack(StackTracerTest.java:12)\r\n"
			+ "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\r\n"
			+ "\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)\r\n"
			+ "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\r\n"
			+ "\tat java.lang.reflect.Method.invoke(Method.java:606)";
	
	private static String enhanced = "at com.googlecode.objectify.insight.Codepointer.getCodepoint(Codepointer.java:40)\r\n"
			+ "\tat com.googlecode.objectify.insight.test.CodepointTestContext.codepoint(CodepointTestContext.java:17)\r\n"
			+ "\tat com.googlecode.objectify.insight.test.CodepointTestContext$$EnhancerByCGLIB$$4b065412.CGLIB$codepoint$0(<generated>)\r\n"
			+ "\tat com.googlecode.objectify.insight.test.CodepointTestContext$$EnhancerByCGLIB$$4b065412$$FastClassByCGLIB$$b6c1cce6.invoke(<generated>)\r\n"
			+ "\tat net.sf.cglib.proxy.MethodProxy.invokeSuper(MethodProxy.java:228)\r\n"
			+ "\tat com.googlecode.objectify.insight.test.CodepointerTest$2.intercept(CodepointerTest.java:86)\r\n"
			+ "\tat com.googlecode.objectify.insight.test.CodepointTestContext$$EnhancerByCGLIB$$4b065412.codepoint(<generated>)\r\n"
			+ "\tat com.googlecode.objectify.insight.test.CodepointerTest.enhancedClassesShouldGenerateSameCodepointHash(CodepointerTest.java:46)\r\n"
			+ "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\r\n"
			+ "\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)\r\n"
			+ "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\r\n"
			+ "\tat java.lang.reflect.Method.invoke(Method.java:606)";
	
	private static String unEnhanced = "at com.googlecode.objectify.insight.Codepointer.getCodepoint(Codepointer.java:40)\r\n"
			+ "\tat com.googlecode.objectify.insight.test.CodepointTestContext.codepoint(CodepointTestContext.java:17)\r\n"
			+ "\tat com.googlecode.objectify.insight.test.CodepointTestContext$$EnhancerByCGLIB$$.CGLIB$codepoint$0(<generated>)\r\n"
			+ "\tat com.googlecode.objectify.insight.test.CodepointTestContext$$EnhancerByCGLIB$$$$FastClassByCGLIB$$.invoke(<generated>)\r\n"
			+ "\tat net.sf.cglib.proxy.MethodProxy.invokeSuper(MethodProxy.java:228)\r\n\tat com.googlecode.objectify.insight.test.CodepointerTest$2.intercept(CodepointerTest.java:86)\r\n"
			+ "\tat com.googlecode.objectify.insight.test.CodepointTestContext$$EnhancerByCGLIB$$.codepoint(<generated>)\r\n"
			+ "\tat com.googlecode.objectify.insight.test.CodepointerTest.enhancedClassesShouldGenerateSameCodepointHash(CodepointerTest.java:46)\r\n"
			+ "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\r\n"
			+ "\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)\r\n"
			+ "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\r\n"
			+ "\tat java.lang.reflect.Method.invoke(Method.java:606)";
	
	@Test
	public void codepointingCanBeDisabled() throws Exception {
		Codepointer codepointer = new Codepointer();
		assertThat(codepointer.getCodepoint(), not(equalTo("disabled")));
		codepointer.setDisabled(true);
		assertThat(codepointer.getCodepoint(), equalTo("disabled"));
	}

	@Test
	public void nonEnhancedStackTraceShouldRemainTheSame() {
		Codepointer codepointer = new Codepointer();
		assertThat(codepointer.removeMutableEnhancements(nonEnhanced), equalTo(nonEnhanced));
	}
	
	@Test
	public void enhancedStacktraceShouldHaveDynamicPartsRemoved() {
		Codepointer codepointer = new Codepointer();
		assertThat(codepointer.removeMutableEnhancements(enhanced), equalTo(unEnhanced));
	}

}