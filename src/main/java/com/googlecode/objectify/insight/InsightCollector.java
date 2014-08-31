package com.googlecode.objectify.insight;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Aggregates statistics and flushes them to a pull queue as necessary. Thread-safe. You should
 * create just one of these per application and pass it into all InsightAsyncDatastoreService instances.
 */
@RequiredArgsConstructor
public class InsightCollector {

	/** Where we flush statistics when we cross thresholds */
	private final Flusher flusher;

	/** Date at which the first bucket as added; null if empty */
	private Date oldest;

	/**
	 * Maximum number of unique buckets before we flush. Since all buckets get JSONified into a single
	 * queue task, the hard limit for this is determined by the max size of a task (1 MB).
	 */
	@Getter @Setter
	private int sizeThreshold = 1000;

	/** Maximum age of a bucket before we flush */
	@Getter @Setter
	private long ageThresholdMillis = 30 * 1000;

	/**
	 * Buckets we aggregate into; this is recreated every flush and instantiated lazily.
	 * Use the buckets() method internally.
	 */
	private Map<BucketKey, Bucket> lazyBuckets;

	/**
	 * Use the standard Flusher
	 */
	public InsightCollector() {
		this(new Flusher());
	}

	/** Use this instead of referencing the lazy var explicitly */
	private Map<BucketKey, Bucket> buckets() {
		if (lazyBuckets == null)
			lazyBuckets = new LinkedHashMap<>(sizeThreshold + sizeThreshold / 3);	// what guava uses for best guess

		return lazyBuckets;
	}

	/**
	 * Collect some statistics. The bucket will be merged with an equivalent bucket.
	 *
	 * @param bucket
	 */
	public synchronized void collect(Bucket bucket) {
		if (oldest == null)
			oldest = new Date();

		Bucket existing = buckets().get(bucket.getKey());
		if (existing == null) {
			existing = new Bucket(bucket.getKey());
			buckets().put(existing.getKey(), existing);
		}

		existing.merge(bucket);

		// Check time before size because flush() wipes out time
		if (oldest.getTime() + ageThresholdMillis <= System.currentTimeMillis())
			flush();

		if (buckets().size() >= sizeThreshold)
			flush();
	}

	/** Flush the accumulated statistics and reset the collection. */
	private void flush() {
		flusher.flush(buckets().values());
		lazyBuckets = null;
		oldest = null;
	}

	/**
	 * Only present for testing and debugging
	 */
	public synchronized Bucket getBucket(BucketKey bucketKey) {
		return buckets().get(bucketKey);
	}
}
