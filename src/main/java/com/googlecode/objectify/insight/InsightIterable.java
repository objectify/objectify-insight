package com.googlecode.objectify.insight;

import com.google.appengine.api.datastore.Entity;
import com.googlecode.objectify.insight.Recorder.QueryBatch;
import lombok.RequiredArgsConstructor;
import java.util.Iterator;

/**
 */
@RequiredArgsConstructor
public class InsightIterable implements Iterable<Entity> {

	private final Iterable<Entity> raw;

	protected final QueryBatch recorderBatch;

	private boolean collected;

	@Override
	public Iterator<Entity> iterator() {
		if (collected) {
			return raw.iterator();
		} else {
			collected = true;
			return InsightIterator.create(raw.iterator(), recorderBatch);
		}
	}
}
