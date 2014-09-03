package com.googlecode.objectify.insight.puller;

import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.model.Table;
import javax.inject.Inject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Manages tables on our behalf. Makes sure we have enough tables to pick from.
 */
public class TablePicker {
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat("YYYY-MM-dd");

	/** Number of days ahead to create tables. */
	private static final int DAYS_AHEAD = 7;

	/** */
	private static final long MILLIS_PER_DAY = 1000 * 60 * 60 * 24;

	private final Bigquery bigquery;
	private final InsightDataset insightDataset;

	@Inject
	public TablePicker(Bigquery bigquery, InsightDataset insightDataset) {
		this.bigquery = bigquery;
		this.insightDataset = insightDataset;
	}

	/** */
	public String pick() {
		return tableIdFor(new Date());
	}

	/**
	 * Make sure we have a week's worth of tables. This should be called periodically via cron - more than once a week.
	 */
	public void ensureEnoughTables() {
		long now = System.currentTimeMillis();

		for (int i = 0; i < DAYS_AHEAD; i++) {
			String tableId = tableIdFor(new Date(now + MILLIS_PER_DAY * i));
			ensureTable(tableId);
		}
	}

	/** */
	private String tableIdFor(Date date) {
		return "OBJSTATS_" + FORMAT.format(date);
	}

	/** */
	private void ensureTable(String tableId) {
	}

}
