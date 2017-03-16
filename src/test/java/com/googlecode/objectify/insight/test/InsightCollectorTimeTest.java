package com.googlecode.objectify.insight.test;

import com.googlecode.objectify.insight.Bucket;
import com.googlecode.objectify.insight.BucketFactory;
import com.googlecode.objectify.insight.Clock;
import com.googlecode.objectify.insight.Collector;
import com.googlecode.objectify.insight.Flusher;
import com.googlecode.objectify.insight.test.util.TestBase;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.LinkedHashSet;
import java.util.Set;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 */
public class InsightCollectorTimeTest extends TestBase {

	@Mock private Flusher flusher;
	@Mock private Clock clock;

	private BucketFactory bucketFactory;

	private Collector collector;

	@BeforeMethod
	public void setUpFixture() throws Exception {
		when(clock.getTime()).thenReturn(100L, 200L);

		bucketFactory = new BucketFactory(clock, "module", "version");
		collector = new Collector(flusher);
	}

	/**
	 */
	@Test
	public void differentTimeBucketsAreSplit() throws Exception {
		collector.setSizeThreshold(2);

		Set<Bucket> expected = new LinkedHashSet<>();
		expected.add(bucketFactory.forGet("here", "ns", "kindA", 1));
		expected.add(bucketFactory.forGet("here", "ns", "kindA", 2));

		for (Bucket bucket: expected)
			collector.collect(bucket);

		verify(flusher).flush(buckets(expected));
	}
}
