package com.googlecode.objectify.insight.puller;

/**
 * The "address" of the dataset used to store insight data. If you're using guice, you must
 * bind this to a real implementation.
 */
public interface InsightDataset {
	String projectId();
	String datasetId();
}
