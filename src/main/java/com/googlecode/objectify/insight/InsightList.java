package com.googlecode.objectify.insight;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.QueryResultList;
import com.googlecode.objectify.insight.Recorder.QueryBatch;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * This doesn't count every form of access - it only handles iterator() and toArray(). If
 * you use get() to random-access the list, we don't count. However, Objectify only uses
 * iterator() and toArray() so this does the job.
 *
 * Figuring out the actual counts for random-accessing the list would be challenging -
 * the underlying GAE List is an async object itself which does not expose its fetching
 * behavior. At best we could guess. That can be a future project.
 */
public class InsightList extends InsightIterable implements QueryResultList<Entity> {

	private final List<Entity> raw;

	private boolean collected;

	public InsightList(List<Entity> raw, QueryBatch recorderBatch) {
		super(raw, recorderBatch);

		this.raw = raw;
	}

	@Override
	public List<Index> getIndexList() {
		return ((QueryResultList<Entity>)raw).getIndexList();
	}

	@Override
	public Cursor getCursor() {
		return ((QueryResultList<Entity>)raw).getCursor();
	}

	@Override
	public int size() {
		return raw.size();
	}

	@Override
	public boolean isEmpty() {
		return raw.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return raw.contains(o);
	}

	@Override
	public Object[] toArray() {
		Object[] array = raw.toArray();

		if (!collected) {
			collected = true;
			for (Object o : array)
				recorderBatch.query((Entity)o);
		}

		return array;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		T[] array = raw.toArray(a);

		if (!collected) {
			collected = true;
			for (Object o : array)
				recorderBatch.query((Entity)o);
		}

		return array;
	}

	@Override
	public boolean add(Entity entity) {
		return raw.add(entity);
	}

	@Override
	public boolean remove(Object o) {
		return raw.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return raw.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Entity> c) {
		return raw.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Entity> c) {
		return raw.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return raw.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return raw.retainAll(c);
	}

	@Override
	public void clear() {
		raw.clear();
	}

	@Override
	public Entity get(int index) {
		return raw.get(index);
	}

	@Override
	public Entity set(int index, Entity element) {
		return raw.set(index, element);
	}

	@Override
	public void add(int index, Entity element) {
		raw.add(index, element);
	}

	@Override
	public Entity remove(int index) {
		return raw.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return raw.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return raw.lastIndexOf(o);
	}

	@Override
	public ListIterator<Entity> listIterator() {
		ListIterator<Entity> rawIt = raw.listIterator();

		if (!collected) {
			collected = true;
			rawIt = InsightIterator.create(rawIt, recorderBatch);
		}

		return rawIt;
	}

	@Override
	public ListIterator<Entity> listIterator(int index) {
		ListIterator<Entity> rawIt = raw.listIterator(index);

		if (!collected) {
			collected = true;
			rawIt = InsightIterator.create(rawIt, recorderBatch);
		}

		return rawIt;
	}

	@Override
	public List<Entity> subList(int fromIndex, int toIndex) {
		return raw.subList(fromIndex, toIndex);
	}
}
