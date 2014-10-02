package com.googlecode.objectify.insight;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.datastore.QueryResultList;
import com.googlecode.objectify.insight.Recorder.QueryBatch;
import lombok.RequiredArgsConstructor;
import java.util.Iterator;
import java.util.List;

/**
 */
@RequiredArgsConstructor
public class InsightPreparedQuery implements PreparedQuery {

	private final PreparedQuery raw;

	private final QueryBatch recorderBatch;

	@Override
	public List<Entity> asList(FetchOptions fetchOptions) {
		return new InsightList(raw.asList(fetchOptions), recorderBatch);
	}

	@Override
	public QueryResultList<Entity> asQueryResultList(FetchOptions fetchOptions) {
		return new InsightList(raw.asQueryResultList(fetchOptions), recorderBatch);
	}

	@Override
	public Iterable<Entity> asIterable(FetchOptions fetchOptions) {
		return new InsightIterable(raw.asIterable(fetchOptions), recorderBatch);
	}

	@Override
	public QueryResultIterable<Entity> asQueryResultIterable(FetchOptions fetchOptions) {
		return new InsightQueryResultIterable(raw.asQueryResultIterable(fetchOptions), recorderBatch);
	}

	@Override
	public Iterable<Entity> asIterable() {
		return new InsightIterable(raw.asIterable(), recorderBatch);
	}

	@Override
	public QueryResultIterable<Entity> asQueryResultIterable() {
		return new InsightQueryResultIterable(raw.asQueryResultIterable(), recorderBatch);
	}

	@Override
	public Iterator<Entity> asIterator(FetchOptions fetchOptions) {
		return InsightIterator.create(raw.asIterator(fetchOptions), recorderBatch);
	}

	@Override
	public Iterator<Entity> asIterator() {
		return InsightIterator.create(raw.asIterator(), recorderBatch);
	}

	@Override
	public QueryResultIterator<Entity> asQueryResultIterator(FetchOptions fetchOptions) {
		return InsightIterator.create(raw.asQueryResultIterator(fetchOptions), recorderBatch);
	}

	@Override
	public QueryResultIterator<Entity> asQueryResultIterator() {
		return InsightIterator.create(raw.asQueryResultIterator(), recorderBatch);
	}

	@Override
	public Entity asSingleEntity() throws TooManyResultsException {
		Entity ent = raw.asSingleEntity();
		recorderBatch.query(ent);
		return ent;
	}

	@Override
	public int countEntities(FetchOptions fetchOptions) {
		return raw.countEntities(fetchOptions);
	}

	@Override
	public int countEntities() {
		return raw.countEntities();
	}
}
