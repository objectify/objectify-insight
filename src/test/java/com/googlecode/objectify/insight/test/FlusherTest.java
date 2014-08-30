package com.googlecode.objectify.insight.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.insight.Bucket;
import com.googlecode.objectify.insight.BucketsPayload;
import com.googlecode.objectify.insight.Flusher;
import com.googlecode.objectify.insight.test.util.TestBase;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 */
public class FlusherTest extends TestBase {

	private Queue queue;
	private Flusher flusher;
	private ArgumentCaptor<TaskOptions> taskCaptor;

	@BeforeMethod
	public void setUpFixture() throws Exception {
		queue = mock(Queue.class);
		flusher = new Flusher(queue);
		taskCaptor = ArgumentCaptor.forClass(TaskOptions.class);
	}

	@Test
	public void flushingBucketsProducesJsonPullTask() throws Exception {
		List<Bucket> buckets = new ArrayList<>();
		buckets.add(Bucket.forGet("ns", "kindA", 123));
		buckets.add(Bucket.forGet("ns", "kindB", 456));

		flusher.flush(buckets);

		verify(queue).addAsync(isNull(Transaction.class), taskCaptor.capture());
		TaskOptions parameter = taskCaptor.getValue();

		byte[] expectedJson = new ObjectMapper().writeValueAsBytes(new BucketsPayload(buckets));

		assertThat(parameter.getPayload(), equalTo(expectedJson));

		// damn, no way to check headers for content type because there is no accessor on TaskOptions
	}

}























