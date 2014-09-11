package com.googlecode.objectify.insight;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.QueryResultIterator;
import lombok.RequiredArgsConstructor;

/**
 */
@RequiredArgsConstructor
public class InsightQueryResultIterable implements QueryResultIterable<Entity> {

	private final QueryResultIterable<Entity> raw;

	protected final Recorder recorder;

	protected final String query;

	private boolean collected;

	@Override
	public QueryResultIterator<Entity> iterator() {
		if (collected) {
			return raw.iterator();
		} else {
			collected = true;
			return InsightIterator.create(raw.iterator(), recorder, query);
		}
	}
}
