package com.googlecode.objectify.insight.servlet;

import com.google.api.services.bigquery.Bigquery;
import com.googlecode.objectify.insight.puller.InsightDataset;
import javax.servlet.http.HttpServlet;

/**
 * Base servlet for nonguice servlets that use bigquery. Users must extend this class and implement
 * the methods that provide Bigquery information.
 */
abstract public class AbstractBigQueryServlet extends HttpServlet {

	private static final long serialVersionUID = 1;

	/**
	 * Implement this to provide the projectId and datasetId where we will store data.
	 */
	abstract protected InsightDataset insightDataset();

	/**
	 * Implement this to provide the authenticated bigquery object.
	 */
	abstract protected Bigquery bigquery();
}
