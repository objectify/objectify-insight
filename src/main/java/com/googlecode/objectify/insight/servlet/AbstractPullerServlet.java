package com.googlecode.objectify.insight.servlet;

import com.google.api.services.bigquery.Bigquery;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.googlecode.objectify.insight.Flusher;
import com.googlecode.objectify.insight.puller.BigUploader;
import com.googlecode.objectify.insight.puller.InsightDataset;
import com.googlecode.objectify.insight.puller.Puller;
import com.googlecode.objectify.insight.puller.TablePicker;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Call this servlet from cron once per minute. It will empty the pull queue and then go back to sleep.
 * Tasks are pulled off the pull queue, aggregated, and then pushed to BigQuery.
 *
 * Extend this if you do not use guice.
 */
abstract public class AbstractPullerServlet extends AbstractBigQueryServlet {

	private static final long serialVersionUID = 1;

	/** */
	private Puller puller;

	/** This is what guice is for */
	@Override
	public void init() throws ServletException {
		InsightDataset insightDataset = insightDataset();
		Queue queue = QueueFactory.getQueue(queueName());
		Bigquery bigquery = bigquery();
		TablePicker tablePicker = new TablePicker(bigquery, insightDataset);
		BigUploader bigUploader = new BigUploader(bigquery, insightDataset, tablePicker);

		puller = new Puller(queue, bigUploader);
	}

	/**
	 * Override this to change the name of the queue we pull from. Be sure to change the value in the Flusher as well.
	 */
	protected String queueName() {
		return Flusher.DEFAULT_QUEUE;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		puller.execute();
	}
}
