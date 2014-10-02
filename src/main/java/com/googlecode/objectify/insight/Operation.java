package com.googlecode.objectify.insight;

import com.google.api.client.util.Value;

/**
 * Possible operations we track as a dimension
 */
public enum Operation {
	@Value GET,
	@Value QUERY,
	@Value INSERT,
	@Value UPDATE,
	@Value DELETE
}
