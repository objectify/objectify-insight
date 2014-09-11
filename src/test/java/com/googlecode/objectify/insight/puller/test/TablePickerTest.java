package com.googlecode.objectify.insight.puller.test;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.model.Table;
import com.googlecode.objectify.insight.puller.InsightDataset;
import com.googlecode.objectify.insight.puller.TablePicker;
import com.googlecode.objectify.insight.test.util.TestBase;
import org.mockito.MockitoAnnotations.Mock;
import org.testng.annotations.Test;
import java.text.SimpleDateFormat;
import java.util.Date;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 */
public class TablePickerTest extends TestBase {

	@Mock Bigquery bigquery;
	@Mock GoogleJsonResponseException googleJsonResponseException;

	@Test
	public void pickerPicksTheRightName() throws Exception {
		TablePicker picker = new TablePicker(null, null);

		String picked = picker.pick();

		String expected = "OBJSTATS_" + new SimpleDateFormat("YYYY_MM_dd").format(new Date());

		assertThat(picked, equalTo(expected));
	}

	/**
	 * Because GoogleJsonResponseException is full of final methods and can't be constructed,
	 * this is basically untestable.
	 */
	//@Test
	public void ensuringTablesWorksEvenIfTablesExist() throws Exception {
		when(googleJsonResponseException.getStatusCode()).thenReturn(409);
		when(bigquery.tables().insert(any(String.class), any(String.class), any(Table.class))).thenThrow(googleJsonResponseException);

		TablePicker picker = new TablePicker(bigquery, new InsightDataset() {
			@Override
			public String projectId() {
				return "foo";
			}

			@Override
			public String datasetId() {
				return "bar";
			}
		});

		// It's enough just to make sure this doesn't produce a higher level exception
		picker.ensureEnoughTables();
	}
}
