package alba.solr.common;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alba.solr.annotations.AlbaResponseWriter;
import alba.solr.annotations.DocTransformer;
import alba.solr.annotations.FunctionQuery;
import alba.solr.annotations.Param;
import alba.solr.annotations.AlbaPlugin;
import alba.solr.annotations.PostFilter;
import alba.solr.annotations.SolrRequestHandler;
import alba.solr.annotations.SolrSearchComponent;
import alba.solr.core.CallableFunction;
import alba.solr.core.Loader;
import alba.solr.core.MyDoc;


@AlbaPlugin
public class MySimpleFloatFunction {

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());


	public MySimpleFloatFunction() {

	}

	@FunctionQuery(name="len", description="field length")
	public Integer len(@Param(name="field", description="the string (or the field) to use") String field) {
		return field.length();
	}
	
	@DocTransformer(name="reverse")
	public void reverse(SolrDocument doc) {
		String label = doc.getFieldValue("label").toString();
		StringBuffer sb = new StringBuffer(label);
		doc.setField("label", sb.reverse().toString());
	}

	@DocTransformer(name="addchild")
	public void addChild(SolrDocument doc) {
		SolrDocument child = new SolrDocument();
		child.setField("field", "child doc value");

		doc.addChildDocument(child);
	}
	
	@DocTransformer(name="ucase")
	public void ucase(SolrDocument doc, @Param(name="field", description="the field") String field) {
		doc.setField(field, doc.getFieldValue(field).toString().toUpperCase());
	}
	
	
	/*
	
	@Function(name="aaa", description="dcoicodij")
	public String hi() {
		return "hi";
	}

	@Function(name="hello",
			description="A simple function which adds the string 'Hello ' before the string passed as param")
	public String sayhi(@Param(name="field", 
	description="the string to use") String field) {
		return "Hello " + field;
	}

	@Function(name="yeah", description = "test")
	public Float test(@Param(name="id", description = "param id") long id,
			@Param(name="multiplier", description = "mult") float multiplier) {
		return (float)id / multiplier;
	}
	
	
	@Function(name="concat")
	public String concat(@Param(name="field1") String field1,
			@Param(name="field2") String field2) {

		return field1 + ", " + field2 + "!";
	}

	@Function(name="info")
	public String getInfo(SolrQueryRequest req) {
		return req.getSchema().getField("label").getName();
	}

	@Function(name="uppercase")
	public String uppercase(@Param(name="field") String field) {
		return field.toUpperCase();
	}



	@Function(name="contains")
	public boolean match(@Param(name="s1") String s1,
			@Param(name="s2") String s2) {
		logger.error("s1 = " + s1);
		logger.error("s2 = " + s2);

		return ((s1 != null) && (s1.contains(s2)));
	}

	@PostFilter(name="minlength")
	public boolean evaluateLen(Integer len, //no annotation on the first param, it comes from the function
			@Param(name="l") Integer minlen) {
		return (len >= minlen);
	}


	@Function(name="random")
	public Integer random() {
		return (int)(Math.random()*100);
	}

	@Function(name="boolFunc")
	public Boolean ops() {
		return false;
	}

	@Function(name="noop")
	public Integer noop() {
		return 0;
	}

	@Function(name="testInt")
	public Integer testInt(@Param(name="field") Integer i) {
		return i+2;
	}

	@DocTransformer(name="createFields")
	public void test(SolrDocument doc) {

		doc.setField("abc", 12);
		doc.setField("idcopy", (long)doc.getFieldValue("id"));
		doc.setField("available", this.get((long)doc.getFieldValue("id")));

		SolrDocument child = new SolrDocument();
		child.setField("field1", "prova!");

		doc.addChildDocument(child);

		List<Integer> i = new ArrayList<Integer>();
		i.add(12);
		i.add(21);
		i.add(15);

		CachedObjectKey k = new CachedObjectKey("ddd", 13);

		doc.setField("lista", i);
		doc.setField("key", k);

		Map<String, String> names = new HashMap<String, String>();
		names.put("prova", "ciao");

		doc.setField("mappa", names);

	}

	@Function(name="get")
	public Integer get(@Param(name="field") Long i) {
		String s = jedis.get(i.toString());
		if (s != null) {
			return Integer.parseInt(s);

		} else {
			jedis.set(i.toString(), (Math.random() > 0.5) ? "1" : "0");
			return 0;
		}

	} */

	@PostFilter(name="available")
	public boolean available(Long flag) {

		return (flag % 2 == 0);

		/*
		String o = jedis.get(flag.toString());

		if (o == null) {
			String newVal = (Math.random() > 0.5) ? "1" : "0";
			jedis.set(flag.toString(), newVal);
			return "1".equals(newVal);
		} else {
			return "1".equals(o);
		}
		 */


	}




	@SolrRequestHandler(name="mydocs", value="/abcde")
	@firstComponents({"prova", "functions", "test"})
	@lastComponents({"last1", "last2", "last3"})
	public Map<Object, Object> requestHandler(SolrQueryRequest req, 
			SolrQueryResponse rsp) {

		Map<Object, Object> results = new HashMap<Object, Object>();

		results.put("key1", "value1");
		results.put("key2", "value2");
		results.put("key3", "value3");
		results.put("key4", "value4");


		return results;
	}


	@SolrRequestHandler(name="functions", value="/functions.alba")
	public List<CallableFunction> functions(SolrQueryRequest req, 
			SolrQueryResponse rsp) {

		List<CallableFunction> list = new ArrayList<CallableFunction>();

		Map<String, CallableFunction> functions = (Map<String, CallableFunction>) req.getContext().get(Loader.FUNCTIONS);

		if (functions == null) {
			logger.error("functions null!");
		}

		for (String s : functions.keySet()) {
			list.add(functions.get(s));
		}

		return list;

	}



	@SolrRequestHandler(name="mydocs", value="/prova")
	@firstComponents({"prova", "functions", "test"})
	@lastComponents({"last1", "last2", "last3"})
	public List<MyDoc> requestHandlerList(SolrQueryRequest req, 
			SolrQueryResponse rsp) {

		List<MyDoc> list = new ArrayList<MyDoc>();

		MyDoc doc1 = new MyDoc();
		doc1.abc = 1;
		doc1.label = "cdsihds";
		list.add(doc1);

		MyDoc doc2 = new MyDoc();
		doc2.abc = 2;
		doc2.label = "apoccp";
		list.add(doc2);

		return list;

	}


	@SolrRequestHandler(name="mydocs", value="/prova2")
	@firstComponents({"prova", "functions", "test"})
	@lastComponents({"last1", "last2", "last3"})
	public List<SolrDocument> requestHandlerList2(SolrQueryRequest req, 
			SolrQueryResponse rsp) {

		List<SolrDocument> list = new ArrayList<SolrDocument>();

		SolrDocument doc1 = new SolrDocument();
		doc1.setField("testo", "ciao");

		SolrDocument doc2 = new SolrDocument();
		doc2.setField("abc", "prova");

		list.add(doc1);
		list.add(doc2);

		return list;

	}



	@SolrSearchComponent("prova")
	public Map<Object, Object> searchComponent(SolrQueryRequest req, 
			SolrQueryResponse rsp) {
		Map<Object, Object> result = new HashMap<Object, Object>();

		result.put("prova", "ciao");

		return result;
	}

	@DocTransformer(name="transform")
	public void test(SolrDocument doc) {
		doc.setField("abc", 12);
	}
	



	@AlbaResponseWriter(value="alba", responseType="text/plain") 
	public void myResponseWriter (Writer writer, SolrQueryRequest request,
			SolrQueryResponse response) throws IOException {
		writer.write("<h1>hello world!</h1>\n\r");

		writer.write(response.getValues().toString());

	}


	public static void main(String[] args) {
		MyDoc doc = new MyDoc();
		doc.label = "ciao";

		DocumentObjectBinder dob = new DocumentObjectBinder();

		SolrInputDocument sd = dob.toSolrInputDocument(doc);

		System.out.println(sd.toString());

	}

}
