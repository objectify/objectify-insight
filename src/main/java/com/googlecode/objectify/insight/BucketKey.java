package com.googlecode.objectify.insight;

import lombok.Data;

/**
 * Immutable key that identifies a bucket
 */
@Data
public class BucketKey {
	private final String namespace;
	private final String kind;
	private final Operation operation;
	private final String query;
}
