package com.googlecode.objectify.insight;

import com.google.api.client.util.Value;

/**
 * Possible operations we track as a dimension
 */
public enum Operation {
	@Deprecated
	@Value LOAD,	// temporarily here for backwards compatibility; will be removed in next version

	@Value GET,
	@Value QUERY,
	@Value INSERT,
	@Value UPDATE,
	@Value DELETE
}
