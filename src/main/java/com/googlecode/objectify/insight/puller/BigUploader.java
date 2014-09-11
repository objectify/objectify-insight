package com.googlecode.objectify.insight.puller;

import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.model.TableDataInsertAllRequest;
import com.google.api.services.bigquery.model.TableDataInsertAllRequest.Rows;
import com.google.api.services.bigquery.model.TableDataInsertAllResponse;
import com.google.api.services.bigquery.model.TableRow;
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

		// Seriously, Google, you f'd up the naming of 'Rows'.

		List<Rows> rows = new ArrayList<Rows>();

		for (Bucket bucket : buckets) {
			TableRow row = new TableRow();
			row.set("namespace", bucket.getKey().getNamespace());
			row.set("kind", bucket.getKey().getKind());
			row.set("op", bucket.getKey().getOp());
			row.set("query", bucket.getKey().getQuery());
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

		TableDataInsertAllRequest request = new TableDataInsertAllRequest().setRows(rows);

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
