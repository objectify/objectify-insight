package com.googlecode.objectify.insight;

/**
 * Aggregates statistics and flushes them to a pull queue as necessary. Thread-safe. You should
 * create just one of these per application and pass it into all InsightAsyncDatastoreService instances.
 */
public class InsightCollector {

	/**
	 * Collect some statistics. The bucket will be merged with any equivalent buckets.
	 * @param bucket
	 */
	public synchronized void collect(Bucket bucket) {

	}
}
