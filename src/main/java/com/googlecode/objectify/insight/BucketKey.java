package com.googlecode.objectify.insight;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Immutable key that identifies a bucket (one vertex of the hypercube).
 * Wish everything could be final but then Jackson can't deserialize it.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BucketKey {
	private String namespace;
	private String kind;
	private Operation op;
	private String query;

	/**
	 * Time is a currentTimeMillis() rounded to a block edge for aggregation.
	 */
	private long time;
}
