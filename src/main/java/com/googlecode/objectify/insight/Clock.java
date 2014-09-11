package com.googlecode.objectify.insight;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import javax.inject.Singleton;

/**
 * Gets the current time, rounded to whatever arbitrary boundry we want.
 */
@Log
@Singleton
@Data
public class Clock {

	/**
	 * Aggregate events into time buckets with this granularity. What this effectively means
	 * is that all timestamps are rounded by this amount.
	 */
	@Getter @Setter
	private long granularityMillis = 60 * 1000;

	/**
	 * Get the current time value rounded to the specified boundary
	 */
	public long getTime() {
		long time = System.currentTimeMillis();
		return time - time % granularityMillis;
	}
	
}
