package com.googlecode.objectify.insight;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
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

	@Getter
	private final Codepointer codepointer;

	private final Set<String> recordKinds = new HashSet<>();

	@Getter @Setter
	private boolean recordAll;

	@Inject
	public Recorder(BucketFactory bucketFactory, Collector collector, Codepointer codepointer) {
		this.bucketFactory = bucketFactory;
		this.collector = collector;
		this.codepointer = codepointer;
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

	/** Create a new batch for recording */
	public Batch batch() {
		return new Batch();
	}

	/** Create a new batch for recording query data */
	public QueryBatch query(Query query) {
		return new QueryBatch(query);
	}

	/**
	 * A session of recording associated with a particular code point.
	 */
	public class Batch {

		protected final String namespace;
		protected final String codePoint;

		Batch() {
			namespace = NamespaceManager.get();
			codePoint = codepointer.getCodepoint();
		}

		/**
		 */
		public void get(Key key) {
			if (shouldRecord(key.getKind()))
				collector.collect(bucketFactory.forGet(codePoint, namespace, key.getKind(), 1));
		}

		/**
		 */
		public void put(Entity entity) {
			if (shouldRecord(entity.getKind()))
				collector.collect(bucketFactory.forPut(codePoint, namespace, entity.getKind(), !entity.getKey().isComplete(), 1));
		}

		/**
		 */
		public void delete(Key key) {
			if (shouldRecord(key.getKind()))
				collector.collect(bucketFactory.forDelete(codePoint, namespace, key.getKind(), 1));
		}
	}

	public class QueryBatch extends Batch {
		protected final String query;

		/** */
		QueryBatch(Query q) {
			query = q.toString();
		}

		/**
		 */
		public void query(Entity entity) {
			if (shouldRecord(entity.getKind()))
				collector.collect(bucketFactory.forQuery(codePoint, namespace, entity.getKind(), query, 1));
		}
	}
}
