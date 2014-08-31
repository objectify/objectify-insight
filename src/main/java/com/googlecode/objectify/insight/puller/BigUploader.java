package com.googlecode.objectify.insight.puller;

import com.google.api.services.bigquery.Bigquery;
import com.googlecode.objectify.insight.Bucket;
import lombok.RequiredArgsConstructor;
import java.util.Collection;

/**
 * Does the work of leasing tasks from the task queue, aggregating again, and pushing
 * the result to BigQuery.
 */
@RequiredArgsConstructor
public class BigUploader {

	private final Bigquery bigquery;
	private final String projectId;
	private final String datasetId;

	/** Make sure we have a week's worth of tables */
	public void ensureEnoughTables() {
		//bigquery.tables().foo();
	}

	public void upload(Collection<Bucket> buckets) {
	}
}
