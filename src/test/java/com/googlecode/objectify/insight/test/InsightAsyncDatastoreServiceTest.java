package com.googlecode.objectify.insight.test;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.googlecode.objectify.insight.BucketFactory;
import com.googlecode.objectify.insight.CodePointer;
import com.googlecode.objectify.insight.Collector;
import com.googlecode.objectify.insight.InsightAsyncDatastoreService;
import com.googlecode.objectify.insight.InsightPreparedQuery;
import com.googlecode.objectify.insight.Recorder;
import com.googlecode.objectify.insight.test.util.TestBase;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.Arrays;
import java.util.Collections;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the nonquery methods. Query methods are tested in InsightPreparedQueryTest.
 */
public class InsightAsyncDatastoreServiceTest extends TestBase {

	private InsightAsyncDatastoreService service;
	private BucketFactory bucketFactory;

	@Mock private AsyncDatastoreService raw;
	@Mock private Collector collector;
	@Mock private CodePointer codePointer;

	@BeforeMethod
	public void setUpFixture() throws Exception {
		bucketFactory = constantTimeBucketFactory();

		when(codePointer.getCodePoint()).thenReturn("here");

		Recorder recorder = new Recorder(bucketFactory, collector, codePointer);
		recorder.setRecordAll(true);

		service = new InsightAsyncDatastoreService(raw, recorder);
	}

	@Test
	public void getIsCollected() throws Exception {
		runInNamespace("ns", new Runnable() {
			@Override
			public void run() {
				Key key = KeyFactory.createKey("Thing", 123L);
				service.get(key);
			}
		});

		verify(collector).collect(bucketFactory.forGet("here", "ns", "Thing", 1));
	}

	@Test
	public void getWithTxnIsCollected() throws Exception {
		runInNamespace("ns", new Runnable() {
			@Override
			public void run() {
				Key key = KeyFactory.createKey("Thing", 123L);
				service.get(null, key);
			}
		});

		verify(collector).collect(bucketFactory.forGet("here", "ns", "Thing", 1));
	}

	@Test
	public void getMultiIsCollected() throws Exception {
		runInNamespace("ns", new Runnable() {
			@Override
			public void run() {
				Key key = KeyFactory.createKey("Thing", 123L);
				service.get(Collections.singleton(key));
			}
		});

		verify(collector).collect(bucketFactory.forGet("here", "ns", "Thing", 1));
	}

	@Test
	public void getMultiWithTxnIsCollected() throws Exception {
		runInNamespace("ns", new Runnable() {
			@Override
			public void run() {
				Key key = KeyFactory.createKey("Thing", 123L);
				service.get(null, Collections.singleton(key));
			}
		});

		verify(collector).collect(bucketFactory.forGet("here", "ns", "Thing", 1));
	}

	@Test
	public void putInsertIsCollected() throws Exception {
		runInNamespace("ns", new Runnable() {
			@Override
			public void run() {
				Entity ent = new Entity("Thing");	// no id
				service.put(ent);
			}
		});

		verify(collector).collect(bucketFactory.forPut("here", "ns", "Thing", true, 1));
	}

	@Test
	public void putUpdateIsCollected() throws Exception {
		runInNamespace("ns", new Runnable() {
			@Override
			public void run() {
				Entity ent = new Entity("Thing", 123L);
				service.put(ent);
			}
		});

		verify(collector).collect(bucketFactory.forPut("here", "ns", "Thing", false, 1));
	}

	@Test
	public void putWithTxnInsertIsCollected() throws Exception {
		runInNamespace("ns", new Runnable() {
			@Override
			public void run() {
				Entity ent = new Entity("Thing");	// no id
				service.put(null, ent);
			}
		});

		verify(collector).collect(bucketFactory.forPut("here", "ns", "Thing", true, 1));
	}

	@Test
	public void putWithTxnUpdateIsCollected() throws Exception {
		runInNamespace("ns", new Runnable() {
			@Override
			public void run() {
				Entity ent = new Entity("Thing", 123L);
				service.put(null, ent);
			}
		});

		verify(collector).collect(bucketFactory.forPut("here", "ns", "Thing", false, 1));
	}

	@Test
	public void putMultiIsCollected() throws Exception {
		runInNamespace("ns", new Runnable() {
			@Override
			public void run() {
				service.put(Arrays.asList(new Entity("Thing"), new Entity("Thing", 123L)));
			}
		});

		verify(collector).collect(bucketFactory.forPut("here", "ns", "Thing", true, 1));
		verify(collector).collect(bucketFactory.forPut("here", "ns", "Thing", false, 1));
	}

	@Test
	public void putMultiWithTxnIsCollected() throws Exception {
		runInNamespace("ns", new Runnable() {
			@Override
			public void run() {
				service.put(null, Arrays.asList(new Entity("Thing"), new Entity("Thing", 123L)));
			}
		});

		verify(collector).collect(bucketFactory.forPut("here", "ns", "Thing", true, 1));
		verify(collector).collect(bucketFactory.forPut("here", "ns", "Thing", false, 1));
	}

	@Test
	public void deleteIsCollected() throws Exception {
		runInNamespace("ns", new Runnable() {
			@Override
			public void run() {
				Key key = KeyFactory.createKey("Thing", 123L);
				service.delete(key);
			}
		});

		verify(collector).collect(bucketFactory.forDelete("here", "ns", "Thing", 1));
	}

	@Test
	public void deleteWithTxnIsCollected() throws Exception {
		runInNamespace("ns", new Runnable() {
			@Override
			public void run() {
				Key key = KeyFactory.createKey("Thing", 123L);
				service.delete((Transaction)null, key);
			}
		});

		verify(collector).collect(bucketFactory.forDelete("here", "ns", "Thing", 1));
	}

	@Test
	public void deleteMultiIsCollected() throws Exception {
		runInNamespace("ns", new Runnable() {
			@Override
			public void run() {
				Key key = KeyFactory.createKey("Thing", 123L);
				service.delete(Collections.singleton(key));
			}
		});

		verify(collector).collect(bucketFactory.forDelete("here", "ns", "Thing", 1));
	}

	@Test
	public void deleteMultiWithTxnIsCollected() throws Exception {
		runInNamespace("ns", new Runnable() {
			@Override
			public void run() {
				Key key = KeyFactory.createKey("Thing", 123L);
				service.delete(null, Collections.singleton(key));
			}
		});

		verify(collector).collect(bucketFactory.forDelete("here", "ns", "Thing", 1));
	}

	@Test
	public void keysOnlyQueriesAreNotCollected() throws Exception {
		Query query = new Query();
		query.setKeysOnly();

		PreparedQuery pq = service.prepare(query);

		assertThat(pq, not(instanceOf(InsightPreparedQuery.class)));
	}

	@Test
	public void keysOnlyTxnQueriesAreNotCollected() throws Exception {
		Query query = new Query();
		query.setKeysOnly();

		PreparedQuery pq = service.prepare(null, query);

		assertThat(pq, not(instanceOf(InsightPreparedQuery.class)));
	}
}
