package com.googlecode.objectify.insight;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * One bucket of data we aggregate to.
 */
@Data
@EqualsAndHashCode(of={"namespace", "kind", "operation", "query"})
@AllArgsConstructor
public class Bucket {

	/** */
	public static Bucket forGet(String namespace, String kind, long readCount) {
		return new Bucket(namespace, kind, Operation.LOAD, null, readCount, 0);
	}

	private final String namespace;
	private final String kind;
	private final Operation operation;
	private final String query;

	/** Variable data that is aggregated */
	private long reads;
	private long writes;

	/** Merge the other bucket into this one */
	public void merge(Bucket other) {
		reads += other.getReads();
		writes += other.getWrites();
	}
}
