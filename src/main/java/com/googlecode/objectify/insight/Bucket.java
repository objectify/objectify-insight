package com.googlecode.objectify.insight;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One bucket of data we aggregate to.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bucket {
	/** */
	public static Bucket forGet(String namespace, String kind, long readCount) {
		return new Bucket(new BucketKey(namespace, kind, Operation.LOAD, null), readCount, 0);
	}

	private BucketKey key;

	/** Variable data that is aggregated */
	private long reads;
	private long writes;

	/** */
	public Bucket(BucketKey key) {
		this.key = key;
	}

	/** Merge the other bucket into this one */
	public void merge(Bucket other) {
		reads += other.getReads();
		writes += other.getWrites();
	}
}
