package com.googlecode.objectify.insight.servlet;

import com.googlecode.objectify.insight.puller.TablePicker;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Call this servlet from cron once per day. It will make sure there are an appropriate set of tables in bigquery.
 * Make sure this servlet starts before the PullerServlet.
 */
public class GuiceTableMakerServlet extends HttpServlet {

	private static final long serialVersionUID = 1;

	/** */
	private final TablePicker tablePicker;

	/** */
	@Inject
	public GuiceTableMakerServlet(TablePicker tablePicker) {
		this.tablePicker = tablePicker;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		tablePicker.ensureEnoughTables();
	}
}
