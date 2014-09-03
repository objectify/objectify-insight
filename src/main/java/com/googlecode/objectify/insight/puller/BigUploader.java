package com.googlecode.objectify.insight.puller;

import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.model.TableDataInsertAllRequest;
import com.google.api.services.bigquery.model.TableDataInsertAllRequest.Rows;
import com.google.api.services.bigquery.model.TableDataInsertAllResponse;
import com.google.api.services.bigquery.model.TableRow;
import com.googlecode.objectify.insight.Bucket;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Does the work of leasing tasks from the task queue, aggregating again, and pushing
 * the result to BigQuery.
 */
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

		// Seriously, Google, you f'd up the naming of 'Rows'.

		List<Rows> rows = new ArrayList<Rows>();

		for (Bucket bucket : buckets) {
			TableRow row = new TableRow();
			row.set("namespace", bucket.getKey().getNamespace());
			row.set("kind", bucket.getKey().getKind());
			row.set("op", bucket.getKey());
			row.set("query", bucket.getKey().getQuery());
			row.set("reads", bucket.getReads());
			row.set("writes", bucket.getWrites());

			TableDataInsertAllRequest.Rows rowWrapper = new TableDataInsertAllRequest.Rows();

			// TODO: make the insertid the stable md5 hash of the bucket
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
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
