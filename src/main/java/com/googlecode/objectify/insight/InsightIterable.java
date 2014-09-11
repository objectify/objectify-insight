package com.googlecode.objectify.insight;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.QueryResultIterator;
import lombok.RequiredArgsConstructor;

/**
 */
@RequiredArgsConstructor
public class InsightIterable implements QueryResultIterable<Entity> {

	private final Iterable<Entity> raw;

	protected final InsightCollector collector;

	protected final String query;

	@Override
	public QueryResultIterator<Entity> iterator() {
		return InsightIterator.create(raw.iterator(), collector, query);
	}
}
