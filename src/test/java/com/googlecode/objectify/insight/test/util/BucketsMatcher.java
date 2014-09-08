package com.googlecode.objectify.insight.test.util;

import com.googlecode.objectify.insight.Bucket;
import lombok.RequiredArgsConstructor;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Hamcrest matcher for exactly matching a collection of buckets which are matched by their key.
 * Read/write counts are checked.
 */
@RequiredArgsConstructor
public class BucketsMatcher extends ArgumentMatcher<Collection<Bucket>> {
	private final Set<Bucket> patternSet;

	public BucketsMatcher(Collection<Bucket> pattern) {
		patternSet = new LinkedHashSet<>(pattern);
	}

	@Override
	public boolean matches(Object o) {
		@SuppressWarnings("unchecked")
		Collection<Bucket> other = (Collection<Bucket>)o;
		Set<Bucket> otherSet = new LinkedHashSet<>(other);

		return otherSet.equals(patternSet);
	}

	@Override
	public void describeTo(Description description) {
		description.appendText(patternSet.toString());
	}
}
