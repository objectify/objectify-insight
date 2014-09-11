package com.googlecode.objectify.insight.test.util;

import lombok.RequiredArgsConstructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Simple pass through proxy, lets us work around the type system
 */
@RequiredArgsConstructor
public class PassThroughProxy implements InvocationHandler {

	private final Object thing;

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return method.invoke(thing, args);
	}

	public static <T> T create(Object thing, Class<T> intf) {
		final Class[] interfaces = { intf };
		return (T)Proxy.newProxyInstance(intf.getClassLoader(), interfaces, new PassThroughProxy(thing));
	}
}
