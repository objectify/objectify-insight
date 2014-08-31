package com.googlecode.objectify.insight.puller;

import com.googlecode.objectify.insight.Bucket;
import com.googlecode.objectify.insight.BucketKey;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates buckets together.
 */
public class BucketAggregator {
	/** */
	Map<BucketKey, Bucket> bucketMap = new HashMap<>();

	/** */
	public void aggregate(List<Bucket> buckets) {
		for (Bucket bucket : buckets) {
			Bucket alreadyHere = bucketMap.get(bucket.getKey());

			if (alreadyHere == null) {
				alreadyHere = new Bucket(bucket.getKey());
				bucketMap.put(bucket.getKey(), alreadyHere);
			}

			alreadyHere.merge(bucket);
		}
	}

	/** */
	public Collection<Bucket> getBuckets() {
		return bucketMap.values();
	}
}
