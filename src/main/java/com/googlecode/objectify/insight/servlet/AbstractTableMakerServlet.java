package com.googlecode.objectify.insight.servlet;

import com.googlecode.objectify.insight.puller.TablePicker;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Call this servlet from cron once per day. It will make sure there are an appropriate set of tables in bigquery.
 * Make sure this servlet starts before the PullerServlet.
 *
 * Extend this if you do not use guice.
 */
abstract public class AbstractTableMakerServlet extends AbstractBigQueryServlet {

	private static final long serialVersionUID = 1;

	/** */
	private TablePicker tablePicker;

	/** Sets up the picker based on the abstract methods */
	@Override
	public void init() throws ServletException {
		tablePicker = new TablePicker(bigquery(), insightDataset());
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		tablePicker.ensureEnoughTables();
	}
}
