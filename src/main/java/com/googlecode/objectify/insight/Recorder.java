package com.googlecode.objectify.insight;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import lombok.Getter;
import lombok.Setter;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

/**
 * Make it easier to record activities. By default records only kinds that you
 * register with recordKind(), but if you setRecordAll(), it will record everything.
 */
@Singleton
public class Recorder {

	@Getter
	private final BucketFactory bucketFactory;

	@Getter
	private final Collector collector;

	private final Set<String> recordKinds = new HashSet<>();

	@Getter @Setter
	private boolean recordAll;

	@Inject
	public Recorder(BucketFactory bucketFactory, Collector collector) {
		this.bucketFactory = bucketFactory;
		this.collector = collector;
	}

	/**
	 * <p>Add a kind to the list of kinds that get recorded. Unless recordAll is set,
	 * only these kinds will be recorded.</p>
	 *
	 * <p>This method is not thread-safe; register all kinds at application startup,
	 * before you begin using the InsightAsyncDatastoreService.</p>
	 */
	public void recordKind(String kind) {
		recordKinds.add(kind);
	}

	/** */
	private boolean shouldRecord(String kind) {
		return recordAll || recordKinds.contains(kind);
	}

	/**
	 */
	public void get(Key key) {
		if (shouldRecord(key.getKind()))
			collector.collect(bucketFactory.forGet(NamespaceManager.get(), key.getKind(), 1));
	}

	/**
	 */
	public void put(Entity entity) {
		if (shouldRecord(entity.getKind()))
			collector.collect(bucketFactory.forPut(NamespaceManager.get(), entity.getKind(), !entity.getKey().isComplete(), 1));
	}

	/**
	 */
	public void delete(Key key) {
		if (shouldRecord(key.getKind()))
			collector.collect(bucketFactory.forDelete(NamespaceManager.get(), key.getKind(), 1));
	}

	/**
	 */
	public void query(Entity entity, String queryString) {
		if (shouldRecord(entity.getKind()))
			collector.collect(bucketFactory.forQuery(NamespaceManager.get(), entity.getKind(), queryString, 1));
	}
}
