package alba.solr.test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.solr.SolrTestCaseJ4;
import org.junit.BeforeClass;

/**
 * Unit test for simple App.
 */


public class AppTest extends SolrTestCaseJ4
{

	@BeforeClass
	public static void beforeClass() throws Exception {
		initCore("solr-functions-solrconfig.xml","solr-functions-schema.xml");
	}
	
	private void createBaseIndex() {
		clearIndex();
		// add a doc with two fields
		assertU(adoc("id", "1", "label_str", "Hello"));
		assertU(commit());
	}

	@org.junit.Test
	public void ensureTestClassIsPresent() throws Exception {


		File jarToAdd = new File("./target/test-classes/");

		try {
			new URLClassLoader(((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs()) {
				@Override
				public void addURL(URL url) {
					super.addURL(url);
				}
			}.addURL(jarToAdd.toURI().toURL());
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			//logger.error("malformed url", e1);
			e1.printStackTrace();
		}


		Class<?> c = Class.forName("alba.solr.test.AlbaTestClass", true, ClassLoader.getSystemClassLoader());

		assert(c != null);
	}



	/**
	 * 
	 * /select?q=id:1&fl=id,label,a:call(hello,field=label_str)
	 * 
	 * @throws Exception 
	 */
	@org.junit.Test
	public void testApp() throws Exception {
		clearIndex();
		// add a doc with two fields
		assertU(adoc("id", "1", "label_str", "World"));
		assertU(commit());

		// test a simple function call
		assertJQ(req("q", "id:1", "fl", "a:call(hello,field=label_str)")
				, "/response/docs/[0]=={'a':'Hello World'}");
		System.out.println("ok.");
	}

	
	// each function is tested with both literal/constant values and field params
	@org.junit.Test
	public void testS1_literal() throws Exception {
		createBaseIndex();
		
		System.out.print("testing a String function with a constant String parameter... ");
		assertJQ(req("q", "id:1", "fl", "a:call(s1,s=\"ThisIsAString\")")
				, "/response/docs/[0]=={'a':'ThisIsAString'}");
		System.out.println("ok.");

	}
	
	@org.junit.Test
	public void testS1_field() throws Exception {
		createBaseIndex();
		System.out.print("testing a String function with a field as String parameter... ");
		assertJQ(req("q", "id:1", "fl", "a:call(s1,s=label_str)")
				, "/response/docs/[0]=={'a':'World'}");
		System.out.println("ok.");
	}
	
	@org.junit.Test	
	public void test_int_1_const() throws Exception {
		createBaseIndex();
		System.out.print("testing a int function with a constant as int parameter... ");
		assertJQ(req("q", "id:1", "fl", "a:call(i1,i=3)")
				, "/response/docs/[0]=={'a':3}");
		System.out.println("ok.");
	}
	
	
	
	/******** float functions ***********/
	
	@org.junit.Test	
	public void test_float_1_const() throws Exception {
		createBaseIndex();
		System.out.print("testing a float function with a constant as float parameter... ");
		assertJQ(req("q", "id:1", "fl", "a:call(f1,f=1.15)")
				, "/response/docs/[0]=={'a':1}");
		System.out.println("ok.");
	}
	
	/************************************/
	
}
