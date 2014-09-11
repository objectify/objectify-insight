package com.googlecode.objectify.insight.test;

import com.googlecode.objectify.insight.Bucket;
import com.googlecode.objectify.insight.BucketFactory;
import com.googlecode.objectify.insight.Flusher;
import com.googlecode.objectify.insight.InsightCollector;
import com.googlecode.objectify.insight.test.util.TestBase;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 */
public class InsightCollectorTest extends TestBase {

	@Mock private Flusher flusher;

	private InsightCollector collector;
	private BucketFactory bucketFactory;

	@BeforeMethod
	public void setUpFixture() throws Exception {
		bucketFactory = constantTimeBucketFactory();
		collector = new InsightCollector(flusher);
	}

	@Test
	public void tooManyDifferentBucketsCausesFlush() throws Exception {
		collector.setSizeThreshold(2);

		Set<Bucket> buckets = new LinkedHashSet<>();
		buckets.add(bucketFactory.forGet("ns", "kindA", 1));
		buckets.add(bucketFactory.forGet("ns", "kindB", 1));

		Iterator<Bucket> it = buckets.iterator();

		collector.collect(it.next());

		verify(flusher, never()).flush(anyCollectionOf(Bucket.class));

		collector.collect(it.next());

		verify(flusher).flush(buckets(buckets));
	}

	@Test
	public void ageCausesFlush() throws Exception {
		collector.setAgeThresholdMillis(100);

		Bucket bucket = bucketFactory.forGet("ns", "kindA", 1);

		collector.collect(bucket);
		collector.collect(bucket);

		verify(flusher, never()).flush(anyCollectionOf(Bucket.class));

		Thread.sleep(101);
		collector.collect(bucket);

		verify(flusher).flush(buckets(bucketFactory.forGet("ns", "kindA", 3)));
	}

	@Test
	public void sameBucketOverAndOverDoesNotCauseFlush() throws Exception {
		collector.setSizeThreshold(2);

		for (int i=0; i<10; i++)
			collector.collect(bucketFactory.forGet("ns", "kind", 1));

		verify(flusher, never()).flush(anyCollectionOf(Bucket.class));
	}

	@Test
	public void sameBucketGetsAggregated() throws Exception {
		collector.setSizeThreshold(2);

		collector.collect(bucketFactory.forGet("ns", "kindA", 1));
		collector.collect(bucketFactory.forGet("ns", "kindA", 2));
		collector.collect(bucketFactory.forGet("ns", "kindA", 3));

		verify(flusher, never()).flush(anyCollectionOf(Bucket.class));

		collector.collect(bucketFactory.forGet("ns", "kindB", 1));

		Set<Bucket> expected = new LinkedHashSet<>();
		expected.add(bucketFactory.forGet("ns", "kindA", 6));
		expected.add(bucketFactory.forGet("ns", "kindB", 1));

		verify(flusher).flush(buckets(expected));
	}
}
