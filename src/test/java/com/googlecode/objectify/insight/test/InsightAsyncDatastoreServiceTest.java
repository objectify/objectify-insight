package com.googlecode.objectify.insight.test;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.googlecode.objectify.insight.Bucket;
import com.googlecode.objectify.insight.InsightAsyncDatastoreService;
import com.googlecode.objectify.insight.InsightCollector;
import com.googlecode.objectify.insight.test.util.TestBase;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.mockito.Mockito.verify;

/**
 */
public class InsightAsyncDatastoreServiceTest extends TestBase {

	private InsightAsyncDatastoreService service;

	@Mock private AsyncDatastoreService raw;
	@Mock private InsightCollector collector;

	@BeforeMethod
	public void setUpFixture() throws Exception {
		service = new InsightAsyncDatastoreService(raw, collector);
	}

	@Test
	public void getKeyIsCollected() throws Exception {
		runInNamespace("ns", new Runnable() {
			@Override
			public void run() {
				Key key = KeyFactory.createKey("Thing", 123L);
				service.get(key);
			}
		});

		verify(collector).collect(Bucket.forGet("ns", "Thing", 1));
	}

}
