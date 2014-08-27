package com.googlecode.objectify.insight.test.util;

import com.googlecode.objectify.insight.Bucket;
import lombok.RequiredArgsConstructor;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

/**
 * Hamcrest matcher for exactly matching a bucket, including read/write counts (which are
 * not normally compared in equals/hashcode so that buckets can be hashed).
 */
@RequiredArgsConstructor
public class BucketMatcher extends ArgumentMatcher<Bucket> {
	private final Bucket pattern;

	@Override
	public boolean matches(Object o) {
		Bucket other = (Bucket)o;

		return other.equals(pattern)
				&& other.getReads() == pattern.getReads()
				&& other.getWrites() == pattern.getWrites();
	}

	@Override
	public void describeTo(Description description) {
		description.appendText(pattern.toString());
	}
}
