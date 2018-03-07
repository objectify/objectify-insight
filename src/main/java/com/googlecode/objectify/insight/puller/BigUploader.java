package com.googlecode.objectify.insight.puller;

import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.model.TableDataInsertAllRequest;
import com.google.api.services.bigquery.model.TableDataInsertAllRequest.Rows;
import com.google.api.services.bigquery.model.TableDataInsertAllResponse;
import com.google.api.services.bigquery.model.TableRow;
import com.google.common.collect.Lists;
import com.googlecode.objectify.insight.Bucket;
import lombok.extern.java.Log;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Does the work of leasing tasks from the task queue, aggregating again, and pushing
 * the result to BigQuery.
 */
@Log
public class BigUploader {

	private final Bigquery bigquery;
	private final InsightDataset insightDataset;
	private final TablePicker tablePicker;

	@Inject
	public BigUploader(Bigquery bigquery, InsightDataset insightDataset, TablePicker tablePicker) {
		this.bigquery = bigquery;
		this.insightDataset = insightDataset;
		this.tablePicker = tablePicker;
	}

	public void upload(Collection<Bucket> buckets) {
		log.finer("Uploading " + buckets.size() + " buckets to bigquery");

		// Seriously, Google, you f'd up the naming of 'Rows'. The date handling is atrocious.
		// And what's with the whole new JSON layer, including annotations? This library is crap.

		List<Rows> rows = new ArrayList<Rows>();

		for (Bucket bucket : buckets) {
			TableRow row = new TableRow();
			row.set("uploaded", System.currentTimeMillis() / 1000f);	// unix timestamp
			row.set("codepoint", bucket.getKey().getCodepoint());
			row.set("namespace", bucket.getKey().getNamespace());
			row.set("module", bucket.getKey().getModule());
			row.set("version", bucket.getKey().getVersion());
			row.set("kind", bucket.getKey().getKind());
			row.set("op", bucket.getKey().getOp());
			row.set("query", bucket.getKey().getQuery());
			row.set("time", bucket.getKey().getTime() / 1000f);	// unix timestamp
			row.set("reads", bucket.getReads());
			row.set("writes", bucket.getWrites());

			TableDataInsertAllRequest.Rows rowWrapper = new TableDataInsertAllRequest.Rows();

			// As much as we would like to do this there isn't really any kind of stable hash because we
			// are constantly aggregating. If we really want this, we will have to stop aggregating at the
			// task level (the thing that retries).
			//rowWrapper.setInsertId(timestamp);

			rowWrapper.setJson(row);

			rows.add(rowWrapper);
		}

		// BQ can handle maximum 10000 rows per request
		// https://cloud.google.com/bigquery/quotas#streaming_inserts
		// The suggested batch size is 500 but I would like to avoid partitioning the rows if it is possible
		// so I don't have to worry about buckets that are partially written to BQ
		for (List<Rows> partition: Lists.partition(rows, 10000)) {
			TableDataInsertAllRequest request = new TableDataInsertAllRequest().setRows(partition);
			request.setIgnoreUnknownValues(true);
	
			String tableId = tablePicker.pick();
	
			try {
				TableDataInsertAllResponse response = bigquery
						.tabledata()
						.insertAll(insightDataset.projectId(), insightDataset.datasetId(), tableId, request)
						.execute();
	
				if (response.getInsertErrors() != null && !response.getInsertErrors().isEmpty()) {
					throw new RuntimeException("There were errors! " + response.getInsertErrors());
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
