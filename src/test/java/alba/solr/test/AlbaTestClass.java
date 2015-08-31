package alba.solr.test;

import alba.solr.annotations.FunctionQuery;
import alba.solr.annotations.Param;
import alba.solr.annotations.AlbaPlugin;


@AlbaPlugin
public class AlbaTestClass {
	
	@FunctionQuery(name="hellox", description="odifjas")
	public String hi(@Param(name="s", description="param") String s) {
		return "Hello, " + s + "!";
	}
	
	
	/*
	 * input:
	 *   s1 = String
	 *   
	 * returns:
	 *   String: the same value passed as parameter
	 */
	@FunctionQuery(name="s1", description="odifjas")
	public String s1(@Param(name="s", description="odifjas") String s) {
		return s;
	}
	
	/*
	 * input:
	 *   i1 = int
	 *   
	 * returns:
	 *   Integer: the same value passed as parameter
	 */
	@FunctionQuery(name="i1", description="odifjas")
	public Integer i1(@Param(name="i", description="odifjas") Integer i) {
		return i;
	}
	
	/*
	 * input:
	 *   i1 = int
	 *   
	 * returns:
	 *   Integer: the same value passed as parameter
	 */
	@FunctionQuery(name="f1", description="odifjas")
	public Float f1(@Param(name="i", description="odifjas") Float f) {
		return f;
	}
	
}
