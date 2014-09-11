package com.googlecode.objectify.insight.util;

import java.lang.reflect.Method;

/**
 * Working around more checked exception brain damage
 */
public class ReflectionUtils {
	public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		try {
			return clazz.getMethod(methodName, parameterTypes);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
}
