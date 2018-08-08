package com.googlecode.objectify.insight.puller;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.testing.json.GoogleJsonResponseExceptionFactoryTesting;
import com.google.api.client.testing.json.MockJsonFactory;
import com.google.api.services.bigquery.model.Table;
import com.googlecode.objectify.insight.puller.InsightDataset;
import com.googlecode.objectify.insight.puller.TablePicker;
import com.googlecode.objectify.insight.puller.TablePicker.BigqueryHandler;
import com.googlecode.objectify.insight.test.util.TestBase; 
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 */
public class TablePickerTest extends TestBase {

	@Mock BigqueryHandler bigqueryHandler;
	@Mock InsightDataset insightDataset;

	private TablePicker picker;

	@BeforeMethod
	public void before() {
		when(insightDataset.projectId()).thenReturn("foo");
		when(insightDataset.datasetId()).thenReturn("bar");

		picker = new TablePicker(bigqueryHandler, insightDataset);
	}

	@Test
	public void pickerPicksTheRightName() throws Exception {
		String picked = picker.pick();

		String expected = "OBJSTATS_" + new SimpleDateFormat("yyyyMMdd").format(new Date());

		assertThat(picked, equalTo(expected));
	}

	@Test
	public void ensuringTablesWorksWithoutExistingTables() throws Exception {
		whenTablesGet().thenThrow(notFoundGoogleJsonResponseException());
		whenTablesInsert().thenReturn(new Table());
		whenTablesUpdate().thenThrow(notFoundGoogleJsonResponseException());

		// It's enough just to make sure this doesn't produce a higher level exception
		picker.ensureEnoughTables();
	}

	@Test
	public void ensuringTablesWorksEvenIfTablesExist() throws Exception {
		whenTablesGet().thenReturn(new Table());
		whenTablesInsert().thenThrow(duplicateGoogleJsonResponseException());
		whenTablesUpdate().thenReturn(new Table());

		// It's enough just to make sure this doesn't produce a higher level exception
		picker.ensureEnoughTables();
	}

	@Test(expectedExceptions = GoogleJsonResponseException.class)
	public void ensuringTablesFailsIfNonExistingTablesCantBeInserted() throws Exception {
		whenTablesGet().thenThrow(notFoundGoogleJsonResponseException());
		whenTablesInsert().thenThrow(unknownGoogleJsonResponseException());

		// It's enough just to make sure this doesn't produce a higher level exception
		picker.ensureEnoughTables();
	}

	@Test(expectedExceptions = GoogleJsonResponseException.class)
	public void ensuringTablesFailsIfExistingTablesCantBeUpdated() throws Exception {
		whenTablesGet().thenReturn(new Table());
		whenTablesUpdate().thenThrow(unknownGoogleJsonResponseException());

		// It's enough just to make sure this doesn't produce a higher level exception
		picker.ensureEnoughTables();
	}

	private OngoingStubbing<Table> whenTablesGet() throws IOException {
		return when(bigqueryHandler.tablesGet(any(InsightDataset.class), any(String.class)));
	}

	private OngoingStubbing<Table> whenTablesInsert() throws IOException {
		return when(bigqueryHandler.tablesInsert(any(InsightDataset.class), any(Table.class)));
	}

	private OngoingStubbing<Table> whenTablesUpdate() throws IOException {
		return when(bigqueryHandler.tablesUpdate(any(InsightDataset.class), any(String.class), any(Table.class)));
	}

	private GoogleJsonResponseException notFoundGoogleJsonResponseException() throws IOException {
		return googleJsonResponseException(404, "notFound");
	}

	private GoogleJsonResponseException duplicateGoogleJsonResponseException() throws IOException {
		return googleJsonResponseException(409, "duplicate");
	}

	private GoogleJsonResponseException unknownGoogleJsonResponseException() throws IOException {
		return googleJsonResponseException(500, "unknown");
	}

	private GoogleJsonResponseException googleJsonResponseException(int httpCode, String reasonPhrase) throws IOException {
		return GoogleJsonResponseExceptionFactoryTesting.newMock(new MockJsonFactory(), httpCode, reasonPhrase);
	}
}
