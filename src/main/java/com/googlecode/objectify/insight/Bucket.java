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
	/**
	 */
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
