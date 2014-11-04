package com.googlecode.objectify.insight.test;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import com.googlecode.objectify.insight.Codepointer;

public class CodepointTestContext {
	
	public CodepointTestContext() {}
	
	public String codepoint() {		
		Codepointer codepointer = new Codepointer();
		return codepointer.getCodepoint();
	}
	
	private static CodepointTestContext cglibEnhanced() {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(CodepointTestContext.class);
		enhancer.setCallback(new MethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args,
					MethodProxy proxy) throws Throwable {
				return proxy.invokeSuper(obj, args);
			}
		});	
	    CodepointTestContext proxy = (CodepointTestContext) enhancer.create();
		return proxy;
	}	
	
	public static void main(String[] args) {
		CodepointTestContext context = cglibEnhanced();
		System.out.println(context.codepoint());
	}
}