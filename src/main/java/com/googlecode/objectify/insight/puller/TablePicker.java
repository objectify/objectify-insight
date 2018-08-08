package com.googlecode.objectify.insight.puller;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.model.Table;
import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.common.annotations.VisibleForTesting;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.java.Log;
import javax.inject.Inject;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Manages tables on our behalf. Makes sure we have enough tables to pick from.
 */
@Log
public class TablePicker {
	/** Number of days ahead to create tables. */
	private static final int DAYS_AHEAD = 7;

	/** */
	private static final long MILLIS_PER_DAY = 1000 * 60 * 60 * 24;

	private final BigqueryHandler bigqueryHandler;
	private final InsightDataset insightDataset;

	/**
	 * Default format is SimpleDateFormat("'OBJSTATS_'yyyyMMdd") which will produce
	 * values like OBJSTATS_20150114. If you set the format, be sure to include any
	 * desired table name prefix.
	 */
	@Getter @Setter
	private DateFormat format = new SimpleDateFormat("'OBJSTATS_'yyyyMMdd");

	@Inject
	public TablePicker(Bigquery bigquery, InsightDataset insightDataset) {
		this(new DefaultBigqueryHandler(bigquery), insightDataset);
	}
	
	@VisibleForTesting
	TablePicker(BigqueryHandler bigqueryHandler, InsightDataset insightDataset) {
		this.bigqueryHandler = bigqueryHandler;
		this.insightDataset = insightDataset;
	}

	/** */
	public String pick() {
		return tableIdFor(new Date());
	}

	/**
	 * Make sure we have a week's worth of tables. This should be called periodically via cron - more than once a week.
	 */
	public void ensureEnoughTables() throws IOException {
		log.finer("Ensuring sufficient tables for " + DAYS_AHEAD + " days");

		long now = System.currentTimeMillis();

		for (int i = 0; i < DAYS_AHEAD; i++) {
			String tableId = tableIdFor(new Date(now + MILLIS_PER_DAY * i));
			ensureTable(tableId);
		}
	}

	/** */
	private String tableIdFor(Date date) {
		return format.format(date);
	}

	/** */
	private void ensureTable(String tableId) throws IOException {
		log.finest("Ensuring table exists: " + tableId);

		TableReference reference = new TableReference();
		reference.setProjectId(insightDataset.projectId());
		reference.setDatasetId(insightDataset.datasetId());
		reference.setTableId(tableId);

		Table table = new Table();
		table.setTableReference(reference);
		table.setSchema(schema());

		if (getTable(tableId) == null) {
			bigqueryHandler.tablesInsert(insightDataset, table);
		} else {
			bigqueryHandler.tablesUpdate(insightDataset, tableId, table);
		}
	}

	private TableSchema schema() {
		List<TableFieldSchema> fields = new ArrayList<>();

		fields.add(tableFieldSchema("uploaded", "TIMESTAMP"));
		fields.add(tableFieldSchema("codepoint", "STRING"));
		fields.add(tableFieldSchema("namespace", "STRING"));
		fields.add(tableFieldSchema("module", "STRING"));
		fields.add(tableFieldSchema("version", "STRING"));
		fields.add(tableFieldSchema("kind", "STRING"));
		fields.add(tableFieldSchema("op", "STRING"));
		fields.add(tableFieldSchema("query", "STRING"));
		fields.add(tableFieldSchema("time", "TIMESTAMP"));

		fields.add(tableFieldSchema("reads", "INTEGER"));
		fields.add(tableFieldSchema("writes", "INTEGER"));

		TableSchema schema = new TableSchema();
		schema.setFields(fields);

		return schema;
	}

	private TableFieldSchema tableFieldSchema(String name, String type) {
		TableFieldSchema field = new TableFieldSchema();
		field.setName(name);
		field.setType(type);
		return field;
	}
	
	private Table getTable(String tableId) throws IOException {
		try {
			return bigqueryHandler.tablesGet(insightDataset, tableId);
		} catch (GoogleJsonResponseException e) {
			if (e.getStatusCode() == 404) {
				log.finest("Table " + tableId + " is missing.");
				return null;
			} else {
				throw e;
			}
		}
	}
	
	@VisibleForTesting
	interface BigqueryHandler {
		Table tablesGet(InsightDataset insightDataset, String tableId) throws IOException;
		Table tablesInsert(InsightDataset insightDataset, Table table) throws IOException;
		Table tablesUpdate(InsightDataset insightDataset, String tableId, Table table) throws IOException;
	}
	
	@RequiredArgsConstructor
	private static class DefaultBigqueryHandler implements BigqueryHandler {
		
		private final Bigquery bigquery;
		
		@Override
		public Table tablesGet(InsightDataset insightDataset, String tableId) throws IOException {
			return bigquery.tables().get(insightDataset.projectId(), insightDataset.datasetId(), tableId).execute();
		}
		
		@Override
		public Table tablesInsert(InsightDataset insightDataset, Table table) throws IOException {
			return bigquery.tables().insert(insightDataset.projectId(), insightDataset.datasetId(), table).execute();
		}
		
		@Override
		public Table tablesUpdate(InsightDataset insightDataset, String tableId, Table table) throws IOException {
			return bigquery.tables().update(insightDataset.projectId(), insightDataset.datasetId(), tableId, table).execute();
		}
	}
}
