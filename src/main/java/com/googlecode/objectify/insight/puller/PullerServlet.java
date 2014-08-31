package com.googlecode.objectify.insight.puller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Call this servlet from cron once per minute. It will empty the pull queue and then go back to sleep.
 * Tasks are pulled off the pull queue, aggregated, and then pushed to BigQuery.
 */
public class PullerServlet extends HttpServlet {

	private static final long serialVersionUID = 1;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

	}
}
