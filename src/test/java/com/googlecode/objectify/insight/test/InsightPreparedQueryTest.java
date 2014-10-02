package com.googlecode.objectify.insight.test;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Transaction;
import com.googlecode.objectify.insight.BucketFactory;
import com.googlecode.objectify.insight.Codepointer;
import com.googlecode.objectify.insight.Collector;
import com.googlecode.objectify.insight.InsightAsyncDatastoreService;
import com.googlecode.objectify.insight.Recorder;
import com.googlecode.objectify.insight.test.util.FakeQueryResultList;
import com.googlecode.objectify.insight.test.util.FakeQueryResultList.QueryResult;
import com.googlecode.objectify.insight.test.util.TestBase;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 */
public class InsightPreparedQueryTest extends TestBase {

	private QueryResult ENTITIES;
	private Query QUERY;

	private InsightAsyncDatastoreService service;
	private BucketFactory bucketFactory;

	@Mock private Collector collector;
	@Mock private AsyncDatastoreService rawService;
	@Mock private PreparedQuery rawPq;
	@Mock private Codepointer codepointer;

	@BeforeMethod
	public void setUpFixture() throws Exception {
		bucketFactory = constantTimeBucketFactory();

		when(rawService.prepare(any(Query.class))).thenReturn(rawPq);
		when(rawService.prepare(any(Transaction.class), any(Query.class))).thenReturn(rawPq);

		when(codepointer.getCodepoint()).thenReturn("here");

		// Constants, but need to wait until the gae apienvironment is set up
		List<Entity> entities = new ArrayList<>();
		entities.add(new Entity("Thing", 123L));
		ENTITIES = FakeQueryResultList.create(entities);

		QUERY = new Query("Thing", KeyFactory.createKey("Parent", 567L));

		Recorder recorder = new Recorder(bucketFactory, collector, codepointer);
		recorder.setRecordAll(true);
		service = new InsightAsyncDatastoreService(rawService, recorder);
	}

	private void iterate(Iterable<?> iterable) {
		iterate(iterable.iterator());
	}
	private void iterate(Iterator<?> iterator) {
		while (iterator.hasNext())
			iterator.next();
	}

	/** We can get rid of a lot of boilerplate */
	private void runTest(Runnable work) {
		runInNamespace("ns", work);
		verify(collector).collect(bucketFactory.forQuery("here", "ns", "Thing", QUERY.toString(), 1));
	}

	@Test
	public void queryIsCollected() throws Exception {
		when(rawPq.asIterable()).thenReturn(ENTITIES);

		runTest(new Runnable() {
			@Override
			public void run() {
				Iterable<Entity> it = service.prepare(QUERY).asIterable();
				iterate(it);
				iterate(it);
			}
		});
	}

	@Test
	public void queryWithTxnIsCollected() throws Exception {
		when(rawPq.asIterable()).thenReturn(ENTITIES);

		runTest(new Runnable() {
			@Override
			public void run() {
				Iterable<Entity> iterable = service.prepare(null, QUERY).asIterable();
				iterate(iterable);
				iterate(iterable);
			}
		});
	}

	@Test
	public void asQueryResultIterableIsCollected() throws Exception {
		when(rawPq.asQueryResultIterable()).thenReturn(ENTITIES);

		runTest(new Runnable() {
			@Override
			public void run() {
				QueryResultIterable<Entity> iterable = service.prepare(QUERY).asQueryResultIterable();
				iterate(iterable);
				iterate(iterable);
			}
		});
	}

	@Test
	public void asQueryResultIterableWithOptionsIsCollected() throws Exception {
		when(rawPq.asQueryResultIterable(any(FetchOptions.class))).thenReturn(ENTITIES);

		runTest(new Runnable() {
			@Override
			public void run() {
				QueryResultIterable<Entity> iterable = service.prepare(QUERY).asQueryResultIterable(Builder.withDefaults());
				iterate(iterable);
				iterate(iterable);
			}
		});
	}

	@Test
	public void asIterableWithOptionsIsCollected() throws Exception {
		when(rawPq.asIterable(any(FetchOptions.class))).thenReturn(ENTITIES);

		runTest(new Runnable() {
			@Override
			public void run() {
				Iterable<Entity> iterable = service.prepare(QUERY).asIterable(Builder.withDefaults());
				iterate(iterable);
				iterate(iterable);
			}
		});
	}

	@Test
	public void asListWithOptionsIsCollected() throws Exception {
		when(rawPq.asList(any(FetchOptions.class))).thenReturn(ENTITIES);

		runTest(new Runnable() {
			@Override
			public void run() {
				List<Entity> list = service.prepare(QUERY).asList(Builder.withDefaults());
				iterate(list);
				iterate(list);
			}
		});
	}

	@Test
	public void asQueryResultListWithOptionsIsCollected() throws Exception {
		when(rawPq.asQueryResultList(any(FetchOptions.class))).thenReturn(ENTITIES);

		runTest(new Runnable() {
			@Override
			public void run() {
				QueryResultList<Entity> list = service.prepare(QUERY).asQueryResultList(Builder.withDefaults());
				iterate(list);
				iterate(list);
			}
		});
	}

	@Test
	public void asIteratorIsCollected() throws Exception {
		when(rawPq.asIterator()).thenReturn(ENTITIES.iterator());

		runTest(new Runnable() {
			@Override
			public void run() {
				iterate(service.prepare(QUERY).asIterator());
			}
		});
	}

	@Test
	public void asIteratorWithOptionsIsCollected() throws Exception {
		when(rawPq.asIterator(any(FetchOptions.class))).thenReturn(ENTITIES.iterator());

		runTest(new Runnable() {
			@Override
			public void run() {
				iterate(service.prepare(QUERY).asIterator(FetchOptions.Builder.withDefaults()));
			}
		});
	}

	@Test
	public void asQueryResultIteratorIsCollected() throws Exception {
		when(rawPq.asQueryResultIterator()).thenReturn(ENTITIES.iterator());

		runTest(new Runnable() {
			@Override
			public void run() {
				iterate(service.prepare(QUERY).asQueryResultIterator());
			}
		});
	}

	@Test
	public void asQueryResultIteratorWithOptionsIsCollected() throws Exception {
		when(rawPq.asQueryResultIterator(any(FetchOptions.class))).thenReturn(ENTITIES.iterator());

		runTest(new Runnable() {
			@Override
			public void run() {
				iterate(service.prepare(QUERY).asQueryResultIterator(FetchOptions.Builder.withDefaults()));
			}
		});
	}

	@Test
	public void asSingleEntityIsCollected() throws Exception {
		when(rawPq.asSingleEntity()).thenReturn(ENTITIES.iterator().next());

		runTest(new Runnable() {
			@Override
			public void run() {
				service.prepare(QUERY).asSingleEntity();
			}
		});
	}

	@Test
	public void listToArrayIsCollected() throws Exception {
		when(rawPq.asList(any(FetchOptions.class))).thenReturn(ENTITIES);

		runTest(new Runnable() {
			@Override
			public void run() {
				List<Entity> list = service.prepare(QUERY).asList(Builder.withDefaults());
				list.toArray();
				list.toArray();
			}
		});
	}

	@Test
	public void queryResultListToArrayIsCollected() throws Exception {
		when(rawPq.asQueryResultList(any(FetchOptions.class))).thenReturn(ENTITIES);

		runTest(new Runnable() {
			@Override
			public void run() {
				List<Entity> list = service.prepare(QUERY).asQueryResultList(Builder.withDefaults());
				list.toArray();
				list.toArray();
			}
		});
	}

	@Test
	public void listToArray2IsCollected() throws Exception {
		when(rawPq.asList(any(FetchOptions.class))).thenReturn(ENTITIES);

		runTest(new Runnable() {
			@Override
			public void run() {
				List<Entity> list = service.prepare(QUERY).asList(Builder.withDefaults());
				list.toArray(new Entity[0]);
				list.toArray(new Entity[0]);
			}
		});
	}

	@Test
	public void queryResultListToArray2IsCollected() throws Exception {
		when(rawPq.asQueryResultList(any(FetchOptions.class))).thenReturn(ENTITIES);

		runTest(new Runnable() {
			@Override
			public void run() {
				List<Entity> list = service.prepare(QUERY).asQueryResultList(Builder.withDefaults());
				list.toArray(new Entity[0]);
				list.toArray(new Entity[0]);
			}
		});
	}

}
