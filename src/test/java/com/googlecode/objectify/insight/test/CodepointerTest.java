package com.googlecode.objectify.insight.test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.testng.annotations.Test;

import com.googlecode.objectify.insight.Codepointer;

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
