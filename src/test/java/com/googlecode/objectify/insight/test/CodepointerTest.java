package com.googlecode.objectify.insight.test;

import com.googlecode.objectify.insight.Codepointer;
import org.testng.annotations.Test;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 */
public class CodepointerTest {

	@Test
	public void codepointingCanBeDisabled() throws Exception {
		Codepointer codepointer = new Codepointer();

		assertThat(codepointer.getCodepoint(), not(equalTo("disabled")));

		codepointer.setDisabled(true);

		assertThat(codepointer.getCodepoint(), equalTo("disabled"));
	}

}























