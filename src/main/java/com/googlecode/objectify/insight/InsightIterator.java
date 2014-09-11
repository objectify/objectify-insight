package com.googlecode.objectify.insight;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.insight.util.ReflectionUtils;
import lombok.RequiredArgsConstructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * Dynamic proxy which covers any kind of iterator we might run across.
 */
@RequiredArgsConstructor
public class InsightIterator implements InvocationHandler {

	public interface Interface extends QueryResultIterator<Entity>, ListIterator<Entity> {}

	private static final Method NEXT_METHOD = ReflectionUtils.getMethod(Iterator.class, "next");
	private static final Method PREVIOUS_METHOD = ReflectionUtils.getMethod(ListIterator.class, "previous");

	private final Object raw;

	private final InsightCollector collector;

	private final String query;

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object result = method.invoke(raw, args);

		if (method.equals(NEXT_METHOD) || method.equals(PREVIOUS_METHOD)) {
			collector.collect(Bucket.forQuery((Entity)result, query));
		}

		return result;
	}

	public static Interface create(Iterator<Entity> raw, InsightCollector collector, String query) {
		return (Interface)Proxy.newProxyInstance(
				Interface.class.getClassLoader(),
				new Class[]{Interface.class},
				new InsightIterator(raw, collector, query));
	}
}
