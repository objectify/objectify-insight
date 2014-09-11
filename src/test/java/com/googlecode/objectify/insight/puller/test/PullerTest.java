package com.googlecode.objectify.insight.puller.test;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.googlecode.objectify.insight.Bucket;
import com.googlecode.objectify.insight.BucketFactory;
import com.googlecode.objectify.insight.BucketList;
import com.googlecode.objectify.insight.puller.BigUploader;
import com.googlecode.objectify.insight.puller.Puller;
import com.googlecode.objectify.insight.test.util.TestBase;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 */
public class PullerTest extends TestBase {

	@Mock private Queue queue;
	@Mock private BigUploader bigUploader;

	private BucketFactory bucketFactory;
	private Bucket bucket1;
	private Bucket bucket2;
	private Bucket bucket3;
	private Bucket bucket4;
	private BucketList bucketList1;
	private BucketList bucketList2;
	private Puller puller;

	@BeforeMethod
	public void setUpFixture() throws Exception {
		bucketFactory = new BucketFactory();

		bucket1 = bucketFactory.forGet("ns", "kindA", 11);
		bucket2 = bucketFactory.forGet("ns", "kindB", 22);
		bucket3 = bucketFactory.forGet("ns", "kindA", 33);
		bucket4 = bucketFactory.forGet("ns", "kindB", 44);

		bucketList1 = new BucketList(Arrays.asList(bucket1, bucket2));
		bucketList2 = new BucketList(Arrays.asList(bucket3, bucket4));

		puller = new Puller(queue, bigUploader);
	}

	@Test
	public void uploadsAggregatedLeasedTasks() throws Exception {

		TaskHandle taskHandle1 = makeTaskHandle(bucketList1);
		TaskHandle taskHandle2 = makeTaskHandle(bucketList2);

		when(queue.leaseTasks(Puller.DEFAULT_LEASE_DURATION_SECONDS, TimeUnit.SECONDS, Puller.DEFAULT_BATCH_SIZE))
				.thenReturn(Arrays.asList(taskHandle1, taskHandle2));

		puller.execute();

		verify(queue).deleteTaskAsync(Arrays.asList(taskHandle1, taskHandle2));

		verify(bigUploader).upload(buckets(Arrays.asList(
				bucketFactory.forGet("ns", "kindA", 44),
				bucketFactory.forGet("ns", "kindB", 66)
		)));
	}
}
