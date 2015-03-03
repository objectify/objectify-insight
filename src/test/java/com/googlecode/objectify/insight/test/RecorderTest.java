package com.googlecode.objectify.insight.test;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.googlecode.objectify.insight.Bucket;
import com.googlecode.objectify.insight.BucketFactory;
import com.googlecode.objectify.insight.Codepointer;
import com.googlecode.objectify.insight.Collector;
import com.googlecode.objectify.insight.InsightAsyncDatastoreService;
import com.googlecode.objectify.insight.Recorder;
import com.googlecode.objectify.insight.test.util.TestBase;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Make sure the recorder is behaving appropriately.
 */
public class RecorderTest extends TestBase {

	private InsightAsyncDatastoreService service;
	private BucketFactory bucketFactory;
	private Recorder recorder;

	@Mock private AsyncDatastoreService raw;
	@Mock private Collector collector;
	@Mock private Codepointer codepointer;

	@BeforeMethod
	public void setUpFixture() throws Exception {
		bucketFactory = constantTimeBucketFactory();

		when(codepointer.getCodepoint()).thenReturn("here");

		recorder = new Recorder(bucketFactory, collector, codepointer);

		service = new InsightAsyncDatastoreService(raw, recorder);
	}

	@Test
	public void noRecording() throws Exception {
		runInNamespace("ns", new Runnable() {
			@Override
			public void run() {
				Key key = KeyFactory.createKey("Thing", 123L);
				service.get(key);
			}
		});

		verify(collector, never()).collect(any(Bucket.class));
	}

	@Test
	public void recordsKind() throws Exception {
		recorder.recordKind("Thing");

		runInNamespace("ns", new Runnable() {
			@Override
			public void run() {
				Key key = KeyFactory.createKey("Thing", 123L);
				service.get(key);
			}
		});

		verify(collector).collect(bucketFactory.forGet("here", "ns", "Thing", 1));
	}
}
