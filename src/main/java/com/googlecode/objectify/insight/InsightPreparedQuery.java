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

	private final InsightCollector collector;

	private final String query;

	@Override
	public List<Entity> asList(FetchOptions fetchOptions) {
		return raw.asList(fetchOptions);
	}

	@Override
	public QueryResultList<Entity> asQueryResultList(FetchOptions fetchOptions) {
		return raw.asQueryResultList(fetchOptions);
	}

	@Override
	public Iterable<Entity> asIterable(FetchOptions fetchOptions) {
		return new InsightIterable(raw.asIterable(fetchOptions), collector, query);
	}

	@Override
	public QueryResultIterable<Entity> asQueryResultIterable(FetchOptions fetchOptions) {
		return raw.asQueryResultIterable(fetchOptions);
	}

	@Override
	public Iterable<Entity> asIterable() {
		return new InsightIterable(raw.asIterable(), collector, query);
	}

	@Override
	public QueryResultIterable<Entity> asQueryResultIterable() {
		return raw.asQueryResultIterable();
	}

	@Override
	public Iterator<Entity> asIterator(FetchOptions fetchOptions) {
		return raw.asIterator(fetchOptions);
	}

	@Override
	public Iterator<Entity> asIterator() {
		return raw.asIterator();
	}

	@Override
	public QueryResultIterator<Entity> asQueryResultIterator(FetchOptions fetchOptions) {
		return raw.asQueryResultIterator(fetchOptions);
	}

	@Override
	public QueryResultIterator<Entity> asQueryResultIterator() {
		return raw.asQueryResultIterator();
	}

	@Override
	public Entity asSingleEntity() throws TooManyResultsException {
		return raw.asSingleEntity();
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
