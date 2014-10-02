package com.googlecode.objectify.insight;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.insight.Recorder.QueryBatch;
import lombok.RequiredArgsConstructor;

/**
 */
@RequiredArgsConstructor
public class InsightQueryResultIterable implements QueryResultIterable<Entity> {

	private final QueryResultIterable<Entity> raw;

	protected final QueryBatch recorderBatch;

	private boolean collected;

	@Override
	public QueryResultIterator<Entity> iterator() {
		if (collected) {
			return raw.iterator();
		} else {
			collected = true;
			return InsightIterator.create(raw.iterator(), recorderBatch);
		}
	}
}
