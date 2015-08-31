package alba.solr.searchcomponents;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alba.components.FilteredShowFileRequestHandler;
import alba.solr.common.StaticResource;
import alba.solr.core.CallableFunction;
import alba.solr.core.Loader;

/*
 * 	// TODO Auto-generated method stub
		rsp.add("Prova", "boooooh");

 */

public class AlbaRequestHandler extends RequestHandlerBase {

	private CallableFunction function;

	private String sectionName;

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private Map<String, CallableFunction> functions;


	public AlbaRequestHandler( Map<String, CallableFunction> functions ) {
		super();
		this.functions = functions;
	}

	public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp)
			throws Exception {
		// TODO Auto-generated method stub

		/* List<SearchComponent> components = new ArrayList<SearchComponent>();

		MySearchComponent msc = new MySearchComponent();

		ResponseBuilder rb = new ResponseBuilder(req, rsp, components);

		msc.process(rb);*/


		//rsp.add("hello", rb.rsp.getValues());

		req.getContext().put(Loader.FUNCTIONS, functions);


		Object params[] = new Object[2];

		params[0] = req;
		params[1] = rsp;

		// what if this method calls rsp.add( .. ) ????
		Object result = this.function.getMethod().invoke(this.function.getInstance(), params);

		if (Map.class.isAssignableFrom(result.getClass())) {
			// if we got a Map, just return it as-is
			rsp.add(this.sectionName, result);
		} else 	// if we got anything else, try to serialize it!
			if (List.class.isAssignableFrom(result.getClass())) {
				for (Object o : (List)result) {
					DocumentObjectBinder dob = new DocumentObjectBinder();
					SolrInputDocument sd = dob.toSolrInputDocument(o);
					SolrDocument dest = ClientUtils.toSolrDocument(sd);

					HashMap<Object, Object> nl = (HashMap<Object, Object>) dest.get("functionDescriptor");

					//rsp.add(nl.get("name").toString(), dest2);

					rsp.add(null, dest);
				}
			} 
		if (StaticResource.class.isAssignableFrom(result.getClass())) {
			FilteredShowFileRequestHandler file = new FilteredShowFileRequestHandler();
			
			file.init(new NamedList()); //to initialize internal variables - but in this way it will NOT get the proper configuration from SolrConfig!
			
			ModifiableSolrParams solrParams = new ModifiableSolrParams(req.getParams());
		
			StaticResource resource = ((StaticResource)result);
			solrParams.set("file", resource.getName());
			//TODO Proper mapping here!!
			//solrParams.set("contentType", "text/xml;charset=utf-8");
			
			solrParams.set("contentType", resource.getContentType());
			req.setParams(solrParams);
			
			file.handleRequest(req, rsp);
			//	logger.error("returning the content of " + );
		}
		else {
			// unable to do any kind of serialization.. just add the result and let the ResponseWriter handle it
			rsp.add(null, result);
		}

	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "bobobobo";
	}

	public void setSectionName(String name) {
		this.sectionName = name;
	}

	public void setFunction(CallableFunction cf) {
		// TODO Auto-generated method stub
		this.function = cf;

	}

	public void setFunctions(HashMap<String, CallableFunction> functions) {
		// TODO Auto-generated method stub
		this.functions = functions;
	}



}
