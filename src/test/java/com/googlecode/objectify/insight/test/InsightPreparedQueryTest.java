package com.googlecode.objectify.insight.test;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.googlecode.objectify.insight.Bucket;
import com.googlecode.objectify.insight.InsightAsyncDatastoreService;
import com.googlecode.objectify.insight.InsightCollector;
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

	@Mock private InsightCollector collector;
	@Mock private AsyncDatastoreService rawService;
	@Mock private PreparedQuery rawPq;

	@BeforeMethod
	public void setUpFixture() throws Exception {
		when(rawService.prepare(any(Query.class))).thenReturn(rawPq);
		when(rawService.prepare(any(Transaction.class), any(Query.class))).thenReturn(rawPq);

		// Constants, but need to wait until the gae apienvironment is set up
		List<Entity> entities = new ArrayList<>();
		entities.add(new Entity("Thing", 123L));
		ENTITIES = FakeQueryResultList.create(entities);

		QUERY = new Query("Thing", KeyFactory.createKey("Parent", 567L));

		service = new InsightAsyncDatastoreService(rawService, collector);
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
		verify(collector).collect(Bucket.forQuery("ns", "Thing", QUERY.toString(), 1));
	}

	@Test
	public void queryIsCollected() throws Exception {
		when(rawPq.asIterable()).thenReturn(ENTITIES);

		runTest(new Runnable() {
			@Override
			public void run() {
				iterate(service.prepare(QUERY).asIterable());
			}
		});
	}

	@Test
	public void queryWithTxnIsCollected() throws Exception {
		when(rawPq.asIterable()).thenReturn(ENTITIES);

		runTest(new Runnable() {
			@Override
			public void run() {
				iterate(service.prepare(null, QUERY).asIterable());
			}
		});
	}

	@Test
	public void asQueryResultIterableIsCollected() throws Exception {
		when(rawPq.asQueryResultIterable()).thenReturn(ENTITIES);

		runTest(new Runnable() {
			@Override
			public void run() {
				iterate(service.prepare(QUERY).asQueryResultIterable());
			}
		});
	}

	@Test
	public void asQueryResultIterableWithOptionsIsCollected() throws Exception {
		when(rawPq.asQueryResultIterable(any(FetchOptions.class))).thenReturn(ENTITIES);

		runTest(new Runnable() {
			@Override
			public void run() {
				iterate(service.prepare(QUERY).asQueryResultIterable(FetchOptions.Builder.withDefaults()));
			}
		});
	}

	@Test
	public void asIterableWithOptionsIsCollected() throws Exception {
		when(rawPq.asIterable(any(FetchOptions.class))).thenReturn(ENTITIES);

		runTest(new Runnable() {
			@Override
			public void run() {
				iterate(service.prepare(QUERY).asIterable(FetchOptions.Builder.withDefaults()));
			}
		});
	}

	@Test
	public void asListWithOptionsIsCollected() throws Exception {
		when(rawPq.asList(any(FetchOptions.class))).thenReturn(ENTITIES);

		runTest(new Runnable() {
			@Override
			public void run() {
				iterate(service.prepare(QUERY).asList(FetchOptions.Builder.withDefaults()));
			}
		});
	}

	@Test
	public void asQueryResultListWithOptionsIsCollected() throws Exception {
		when(rawPq.asQueryResultList(any(FetchOptions.class))).thenReturn(ENTITIES);

		runTest(new Runnable() {
			@Override
			public void run() {
				iterate(service.prepare(QUERY).asQueryResultList(FetchOptions.Builder.withDefaults()));
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

}
