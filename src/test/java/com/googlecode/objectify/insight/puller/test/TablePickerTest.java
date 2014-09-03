package com.googlecode.objectify.insight.puller.test;

import com.googlecode.objectify.insight.puller.TablePicker;
import org.testng.annotations.Test;
import java.text.SimpleDateFormat;
import java.util.Date;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 */
public class TablePickerTest {

	@Test
	public void pickerPicksTheRightName() throws Exception {
		TablePicker picker = new TablePicker(null, null);

		String picked = picker.pick();

		String expected = "OBJSTATS_" + new SimpleDateFormat("YYYY-MM-dd").format(new Date());

		assertThat(picked, equalTo(expected));
	}
}
