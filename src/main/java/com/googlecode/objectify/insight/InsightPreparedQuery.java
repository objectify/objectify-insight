package com.googlecode.objectify.insight;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.datastore.QueryResultList;
import lombok.RequiredArgsConstructor;
import java.util.Iterator;
import java.util.List;

/**
 */
@RequiredArgsConstructor
public class InsightPreparedQuery implements PreparedQuery {

	private final PreparedQuery raw;

	private final Recorder recorder;

	private final String query;

	@Override
	public List<Entity> asList(FetchOptions fetchOptions) {
		return new InsightList(raw.asList(fetchOptions), recorder, query);
	}

	@Override
	public QueryResultList<Entity> asQueryResultList(FetchOptions fetchOptions) {
		return new InsightList(raw.asQueryResultList(fetchOptions), recorder, query);
	}

	@Override
	public Iterable<Entity> asIterable(FetchOptions fetchOptions) {
		return new InsightIterable(raw.asIterable(fetchOptions), recorder, query);
	}

	@Override
	public QueryResultIterable<Entity> asQueryResultIterable(FetchOptions fetchOptions) {
		return new InsightQueryResultIterable(raw.asQueryResultIterable(fetchOptions), recorder, query);
	}

	@Override
	public Iterable<Entity> asIterable() {
		return new InsightIterable(raw.asIterable(), recorder, query);
	}

	@Override
	public QueryResultIterable<Entity> asQueryResultIterable() {
		return new InsightQueryResultIterable(raw.asQueryResultIterable(), recorder, query);
	}

	@Override
	public Iterator<Entity> asIterator(FetchOptions fetchOptions) {
		return InsightIterator.create(raw.asIterator(fetchOptions), recorder, query);
	}

	@Override
	public Iterator<Entity> asIterator() {
		return InsightIterator.create(raw.asIterator(), recorder, query);
	}

	@Override
	public QueryResultIterator<Entity> asQueryResultIterator(FetchOptions fetchOptions) {
		return InsightIterator.create(raw.asQueryResultIterator(fetchOptions), recorder, query);
	}

	@Override
	public QueryResultIterator<Entity> asQueryResultIterator() {
		return InsightIterator.create(raw.asQueryResultIterator(), recorder, query);
	}

	@Override
	public Entity asSingleEntity() throws TooManyResultsException {
		Entity ent = raw.asSingleEntity();
		recorder.query(ent, query);
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
