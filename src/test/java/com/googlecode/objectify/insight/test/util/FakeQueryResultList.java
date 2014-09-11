package com.googlecode.objectify.insight.test.util;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.datastore.QueryResultList;
import com.googlecode.objectify.insight.util.ReflectionUtils;
import lombok.RequiredArgsConstructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * Fake list, implemented as a proxy
 */
@RequiredArgsConstructor
public class FakeQueryResultList implements InvocationHandler {
	public static interface QueryResult extends QueryResultList<Entity>, QueryResultIterable<Entity> {}

	private static final Method ITERATOR_METHOD = ReflectionUtils.getMethod(List.class, "iterator");

	private final List<Entity> list;

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object result = method.invoke(list, args);

		if (method.equals(ITERATOR_METHOD))
			return PassThroughProxy.create(result, QueryResultIterator.class);
		else
			return result;
	}

	public static QueryResult create(List<Entity> list) {
		return (QueryResult)Proxy.newProxyInstance(QueryResult.class.getClassLoader(), new Class[]{QueryResult.class}, new FakeQueryResultList(list));
	}
}
