package com.googlecode.objectify.insight;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import lombok.Data;
import javax.inject.Inject;

/**
 * Make it easier to record activities
 */
@Data
public class Recorder {

	private final BucketFactory bucketFactory;
	private final Collector collector;

	@Inject
	public Recorder(BucketFactory bucketFactory, Collector collector) {
		this.bucketFactory = bucketFactory;
		this.collector = collector;
	}

	/**
	 */
	public void get(Key key) {
		collector.collect(bucketFactory.forGet(NamespaceManager.get(), key.getKind(), 1));
	}

	/**
	 */
	public void put(Entity entity) {
		collector.collect(bucketFactory.forPut(NamespaceManager.get(), entity.getKind(), !entity.getKey().isComplete(), 1));
	}

	/**
	 */
	public void delete(Key key) {
		collector.collect(bucketFactory.forDelete(NamespaceManager.get(), key.getKind(), 1));
	}

	/**
	 */
	public void query(Entity entity, String queryString) {
		collector.collect(bucketFactory.forQuery(NamespaceManager.get(), entity.getKind(), queryString, 1));
	}
}
