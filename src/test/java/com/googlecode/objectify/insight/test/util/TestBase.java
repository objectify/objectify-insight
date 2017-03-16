/*
 */

package com.googlecode.objectify.insight.test.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.common.base.Supplier;
import com.googlecode.objectify.insight.Bucket;
import com.googlecode.objectify.insight.BucketFactory;
import com.googlecode.objectify.insight.Clock;
import com.googlecode.objectify.insight.Flusher;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import java.util.Collection;
import java.util.Collections;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * All tests should extend this class to set up the GAE environment.
 * @see <a href="http://code.google.com/appengine/docs/java/howto/unittesting.html">Unit Testing in Appengine</a>
 *
 * Also sets up any Mockito annotated fields.
 *
 * @author Jeff Schnitzer <jeff@infohazard.org>
 */
public class TestBase
{
	/** */
	private final LocalServiceTestHelper helper =
			new LocalServiceTestHelper(
					// Our tests assume strong consistency
					new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy(),
					new LocalTaskQueueTestConfig());
	/** */
	@BeforeMethod
	public void setUp() {
		this.helper.setUp();
		MockitoAnnotations.initMocks(this);
	}

	/** */
	@AfterMethod
	public void tearDown() {
		this.helper.tearDown();
	}

	/** */
	protected void runInNamespace(String namespace, Runnable runnable) {
		String oldNamespace = NamespaceManager.get();
		try {
			NamespaceManager.set(namespace);

			runnable.run();
		} finally {
			NamespaceManager.set(oldNamespace);
		}
	}

	/** */
	protected <T> T runInNamespace(String namespace, Supplier<T> supplier) {
		String oldNamespace = NamespaceManager.get();
		try {
			NamespaceManager.set(namespace);

			return supplier.get();
		} finally {
			NamespaceManager.set(oldNamespace);
		}
	}

	/** Little bit of boilerplate that makes the tests read better */
	protected Collection<Bucket> buckets(Collection<Bucket> matching) {
		return argThat(new BucketsMatcher(matching));
	}

	/** Little bit of boilerplate that makes the tests read better */
	protected Collection<Bucket> buckets(Bucket singletonSetContent) {
		return buckets(Collections.singleton(singletonSetContent));
	}

	/** Convenience method */
	protected byte[] jsonify(Object object) throws JsonProcessingException {
		return new ObjectMapper().writeValueAsBytes(object);
	}

	/**
	 * Make a task handle which holds the jsonified payload. Task name is an arbitrary unique string.
	 */
	protected TaskHandle makeTaskHandle(Object payload) throws JsonProcessingException {
		return new TaskHandle(
				TaskOptions.Builder.withPayload(jsonify(payload), "application/json").taskName(makeUniqueString()),
				Flusher.DEFAULT_QUEUE);
	}

	protected String makeUniqueString() {
		return new Object().toString().split("@")[1];
	}


	/** Useful for making stable tests */
	protected BucketFactory constantTimeBucketFactory() {
		Clock clock = mock(Clock.class);
		when(clock.getTime()).thenReturn(100L);	// just a stable value
		return new BucketFactory(clock, "module", "version");
	}

}
