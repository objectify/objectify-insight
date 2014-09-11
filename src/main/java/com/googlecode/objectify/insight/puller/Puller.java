package com.googlecode.objectify.insight.puller;

import com.google.appengine.api.taskqueue.Queue;
import com.googlecode.objectify.insight.BucketList;
import com.googlecode.objectify.insight.util.QueueHelper;
import com.googlecode.objectify.insight.util.TaskHandleHelper;
import lombok.extern.java.Log;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Does the work of leasing tasks from the task queue, aggregating again, and pushing
 * the result to BigQuery.
 */
@Log
@Singleton
public class Puller {

	/** */
	public static final int DEFAULT_BATCH_SIZE = 100;

	/** Something long enough to be safe */
	public static final int DEFAULT_LEASE_DURATION_SECONDS = 60 * 10;

	/** */
	private final QueueHelper<BucketList> queue;

	/** */
	private final BigUploader bigUploader;

	/** Number of tasks to lease and aggregate per write to BQ */
	private int batchSize = DEFAULT_BATCH_SIZE;

	/** How long to maintain task leases; short values risk duplicates */
	private int leaseDurationSeconds = DEFAULT_LEASE_DURATION_SECONDS;

	/**
	 */
	@Inject
	public Puller(@Named("insight") Queue queue, BigUploader bigUploader) {
		this.queue = new QueueHelper<>(queue, BucketList.class);
		this.bigUploader = bigUploader;
	}

	/**
	 * Repeatedly leases batches of tasks, aggregates them, pushes the result to BQ, and deletes the tasks.
	 * Continues until there are no more tasks to lease or some sort of error is encountered. This method
	 * should be called regularly on a cron schedule (say, once per minute).
	 */
	public void execute() {
		log.finest("Pulling");

		while (true) {
			try {
				if (processOneBatch() < batchSize)
					return;

			} catch (RuntimeException ex) {
				log.log(Level.WARNING, "Exception while processing insight data; aborting for now", ex);
				return;
			}
		}
	}

	/**
	 * @return the # of tasks actually processed
	 */
	private int processOneBatch() {
		List<TaskHandleHelper<BucketList>> handles = queue.lease(leaseDurationSeconds, TimeUnit.SECONDS, batchSize);

		if (!handles.isEmpty()) {
			log.finer("Leased " + handles.size() + " bucketlist tasks");

			BucketAggregator aggregator = new BucketAggregator();

			for (TaskHandleHelper<BucketList> handle : handles) {
				aggregator.aggregate(handle.getPayload().getBuckets());
			}

			bigUploader.upload(aggregator.getBuckets());

			queue.delete(handles);
		}

		return handles.size();
	}
}
