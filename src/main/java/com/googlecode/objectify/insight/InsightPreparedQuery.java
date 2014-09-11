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
		return new InsightList(raw.asList(fetchOptions), collector, query);
	}

	@Override
	public QueryResultList<Entity> asQueryResultList(FetchOptions fetchOptions) {
		return new InsightList(raw.asQueryResultList(fetchOptions), collector, query);
	}

	@Override
	public Iterable<Entity> asIterable(FetchOptions fetchOptions) {
		return new InsightIterable(raw.asIterable(fetchOptions), collector, query);
	}

	@Override
	public QueryResultIterable<Entity> asQueryResultIterable(FetchOptions fetchOptions) {
		return new InsightQueryResultIterable(raw.asQueryResultIterable(fetchOptions), collector, query);
	}

	@Override
	public Iterable<Entity> asIterable() {
		return new InsightIterable(raw.asIterable(), collector, query);
	}

	@Override
	public QueryResultIterable<Entity> asQueryResultIterable() {
		return new InsightQueryResultIterable(raw.asQueryResultIterable(), collector, query);
	}

	@Override
	public Iterator<Entity> asIterator(FetchOptions fetchOptions) {
		return InsightIterator.create(raw.asIterator(fetchOptions), collector, query);
	}

	@Override
	public Iterator<Entity> asIterator() {
		return InsightIterator.create(raw.asIterator(), collector, query);
	}

	@Override
	public QueryResultIterator<Entity> asQueryResultIterator(FetchOptions fetchOptions) {
		return InsightIterator.create(raw.asQueryResultIterator(fetchOptions), collector, query);
	}

	@Override
	public QueryResultIterator<Entity> asQueryResultIterator() {
		return InsightIterator.create(raw.asQueryResultIterator(), collector, query);
	}

	@Override
	public Entity asSingleEntity() throws TooManyResultsException {
		Entity ent = raw.asSingleEntity();
		collector.collect(Bucket.forQuery(ent, query));
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
