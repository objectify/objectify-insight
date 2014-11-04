package com.googlecode.objectify.insight.test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Scanner;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

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

	@Test
	public void nonEnhancedClassesShouldGenerateSameCodepointHash()
			throws Exception {
		CodepointTestContext context = new CodepointTestContext();
		CodepointTestContext context2 = new CodepointTestContext();
		assertThat(context.codepoint(), equalTo(context2.codepoint()));
	}
	
	@Test
	public void enhancedClassesShouldGenerateSameCodepointHash()
			throws Exception {
		CodepointTestContext enhancedContext = cglibEnhanced();
		CodepointTestContext enhancedContext2 = cglibEnhanced();
		assertThat(enhancedContext.codepoint(), equalTo(enhancedContext2.codepoint()));
	}

	@Test
	public void t()
			throws Exception {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome +
                File.separator + "bin" +
                File.separator + "java";	    
        String classpath = System.getProperty("java.class.path");
	    String className = CodepointTestContext.class.getCanonicalName();
		
        ProcessBuilder builder = new ProcessBuilder(
	                javaBin, "-cp", classpath, className);

        Process process = builder.start();
        inheritIO(process.getInputStream(), System.out);
        process.waitFor();
        process.exitValue();
	}
	
	private static void inheritIO(final InputStream src, final PrintStream dest) {
	    new Thread(new Runnable() {
	        public void run() {
	            Scanner sc = new Scanner(src);
	            while (sc.hasNextLine()) {
	                dest.println(sc.nextLine());
	            }
	        }
	    }).start();
	}
	
	private CodepointTestContext cglibEnhanced() {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(CodepointTestContext.class);
		enhancer.setCallback(new MethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args,
					MethodProxy proxy) throws Throwable {
				return proxy.invokeSuper(obj, args);
			}
		});	
	    CodepointTestContext proxy = (CodepointTestContext) enhancer.create();
		return proxy;
	}

}