package com.googlecode.objectify.insight;

import lombok.Data;
import javax.inject.Inject;

/**
 * Buckets are given a timestamp that depends on a configurable value, so we need to
 * make them from a factory.
 */
@Data
public class BucketFactory {

	/** Gives us rounded timestamps */
	private final Clock clock;

	public BucketFactory() {
		this(new Clock());
	}

	@Inject
	public BucketFactory(Clock clock) {
		this.clock = clock;
	}

	/**
	 */
	public Bucket forGet(String namespace, String kind, long readCount) {
		return new Bucket(new BucketKey(namespace, kind, Operation.GET, null, clock.getTime()), readCount, 0);
	}

	/**
	 */
	public Bucket forPut(String namespace, String kind, boolean insert, long writeCount) {
		Operation op = insert ? Operation.INSERT : Operation.UPDATE;
		return new Bucket(new BucketKey(namespace, kind, op, null, clock.getTime()), 0, writeCount);
	}

	/**
	 */
	public Bucket forDelete(String namespace, String kind, long writeCount) {
		return new Bucket(new BucketKey(namespace, kind, Operation.DELETE, null, clock.getTime()), 0, writeCount);
	}

	/**
	 */
	public Bucket forQuery(String namespace, String kind, String queryString, long readCount) {
		return new Bucket(new BucketKey(namespace, kind, Operation.QUERY, queryString, clock.getTime()), readCount, 0);
	}
}
