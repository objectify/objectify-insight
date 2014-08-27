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

	/** Maximum number of unique buckets before we flush */
	@Getter @Setter
	private int sizeThreshold = 5000;

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
	 * Collect some statistics. The bucket will either be claimed (object ownership) or be merged with
	 * an equivalent bucket. Don't modify the bucket or expect its contents to remain immutable afterwards.
	 *
	 * @param bucket
	 */
	public synchronized void collect(Bucket bucket) {
		Bucket already = buckets().get(bucket.getBucketKey());
		if (already == null) {
			buckets().put(bucket.getBucketKey(), bucket);
		} else {
			already.merge(bucket);
		}

		if (buckets().size() >= sizeThreshold)
			flush();
	}

	/** Flush the accumulated statistics and reset the collection. */
	private void flush() {
		flusher.flush(buckets().values());
		lazyBuckets = null;
	}

	/**
	 * Only present for testing and debugging
	 */
	public synchronized Bucket getBucket(BucketKey bucketKey) {
		return buckets().get(bucketKey);
	}
}
