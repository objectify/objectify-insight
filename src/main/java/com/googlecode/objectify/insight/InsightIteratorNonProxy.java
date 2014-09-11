package com.googlecode.objectify.insight;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.QueryResultIterator;
import lombok.RequiredArgsConstructor;
import java.util.Iterator;
import java.util.List;

/**
 * This is being kept around in case we discover performance issues with the dynamic proxy.
 */
@Deprecated
@RequiredArgsConstructor
public class InsightIteratorNonProxy implements QueryResultIterator<Entity> {

	private final Iterator<Entity> raw;

	private final InsightCollector collector;

	private final String query;

	@Override
	public List<Index> getIndexList() {
		return ((QueryResultIterator<Entity>)raw).getIndexList();
	}

	@Override
	public Cursor getCursor() {
		return ((QueryResultIterator<Entity>)raw).getCursor();
	}

	@Override
	public boolean hasNext() {
		return raw.hasNext();
	}

	@Override
	public Entity next() {
		Entity ent = raw.next();
		collector.collect(Bucket.forQuery(ent, query));
		return ent;
	}

	@Override
	public void remove() {
		raw.remove();
	}
}
