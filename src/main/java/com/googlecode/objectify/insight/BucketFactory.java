package com.googlecode.objectify.insight;

import lombok.Data;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.appengine.api.modules.ModulesServiceFactory;

/**
 * Buckets are given a timestamp that depends on a configurable value, so we need to
 * make them from a factory.
 */
@Data
public class BucketFactory {

	/** Gives us rounded timestamps */
	private final Clock clock;
	private final String module;
	private final String version;

	public BucketFactory() {
		this(new Clock(), ModulesServiceFactory.getModulesService().getCurrentModule(), ModulesServiceFactory.getModulesService().getCurrentVersion());
	}

	@Inject
	public BucketFactory(Clock clock, @Named("module") String module, @Named("version") String version) {
		this.clock = clock;
		this.module = module;
		this.version = version;
	}

	/**
	 */
	public Bucket forGet(String codePoint, String namespace, String kind, long readCount) {
		return new Bucket(new BucketKey(codePoint, namespace, module, version, kind, Operation.GET, null, clock.getTime()), readCount, 0);
	}

	/**
	 */
	public Bucket forPut(String codePoint, String namespace, String kind, boolean insert, long writeCount) {
		Operation op = insert ? Operation.INSERT : Operation.UPDATE;
		return new Bucket(new BucketKey(codePoint, namespace, module, version, kind, op, null, clock.getTime()), 0, writeCount);
	}

	/**
	 */
	public Bucket forDelete(String codePoint, String namespace, String kind, long writeCount) {
		return new Bucket(new BucketKey(codePoint, namespace, module, version, kind, Operation.DELETE, null, clock.getTime()), 0, writeCount);
	}

	/**
	 */
	public Bucket forQuery(String codePoint, String namespace, String kind, String queryString, long readCount) {
		return new Bucket(new BucketKey(codePoint, namespace, module, version, kind, Operation.QUERY, queryString, clock.getTime()), readCount, 0);
	}
}
