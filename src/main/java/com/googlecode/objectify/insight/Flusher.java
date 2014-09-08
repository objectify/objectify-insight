package com.googlecode.objectify.insight;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.googlecode.objectify.insight.util.QueueHelper;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;

/**
 * Writes aggregated statistics to a pull queue.
 */
public class Flusher {

	/** The default pull queue that statistics are flushed to */
	public static final String DEFAULT_QUEUE = "insight";

	/** The queue we use */
	private QueueHelper<BucketList> queue;

	/** Use the default pull queue name */
	public Flusher() {
		this(QueueFactory.getQueue(DEFAULT_QUEUE));
	}

	/** */
	@Inject
	public Flusher(@Named("insight") Queue queue) {
		this.setQueue(queue);
	}

	/**
	 * Change the queue we flush to.
	 */
	public void setQueue(Queue queue) {
		this.queue = new QueueHelper<BucketList>(queue, BucketList.class);
	}

	/**
	 * Write buckets to the relevant pull queue as a single task with a JSON payload.
	 */
	public void flush(Collection<Bucket> buckets) {
		queue.add(new BucketList(buckets));
	}
}
