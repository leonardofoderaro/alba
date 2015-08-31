package alba.solr.core;

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
import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.FunctionQParserPlugin;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SyntaxError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicQueryParser extends QParserPlugin {

	Map<String, CallableFunction> postFilters;

	Map<String, CallableFunction> functions;

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private Map<Object, CachedResult> cachedResults;

	@Override
	public QParser createParser(String qstr, SolrParams localParams,
			SolrParams params, SolrQueryRequest req) {

		postFilters = (Map<String, CallableFunction>) req.getContext().get(Loader.POSTFILTERS);

		cachedResults = (Map<Object, CachedResult>) req.getContext().get(Loader.CACHEDRESULTS);

		CallableFunction function = postFilters.get(localParams.get("name"));


		return new QParser(qstr, localParams, params, req) {

			private ValueSource functionParamValueSource;

			@Override
			public Query parse() throws SyntaxError {
			
				ValueSource vs = null;
				
				Map<String, Object> params = new HashMap<String, Object>();
				
				String funcStr = localParams.get(QueryParsing.V, null);
	
				
		    	int nParams = 1;
				
				if ((function != null) && (function.getMethod() != null)) {
					 nParams = function.getMethod().getParameterCount();
				}
				
				boolean cache = false;
				
		    	Object functionParams[] = new Object[nParams];
		    	
		    	int i = 1;  //in the 0th positions there's the parametric function result (as ValueSource)
				Iterator<String> it = localParams.getParameterNamesIterator();
				while (it.hasNext()) {
					String p = it.next();
					
					
					/* does it make sense to be able to switch on/off the cache? what would it imply? 
					if ("cache".equals(p)) {
						cache = ("1".equals(localParams.get(p)));
					}
					*/
					
					if (!"v".equals(p) && !"cache".equals(p) && !"type".equals(p) && !"name".equals(p)) {
						params.put(p, localParams.get(p));
						
						Class<?> expectedType = function.getMethod().getParameters()[i].getType();
						if (expectedType == Integer.class) {
						  functionParams[i] = Integer.parseInt(localParams.get(p));
						} else {
						  logger.error("param " + i + " should be of type " +expectedType + " but I don't know how to parse it." );
						  // right place for magic params? like passing the request & so on.. ?
						}
					
						i++;
					}				
				}
				
				
				if ((funcStr != null) && (funcStr != "")) {
					Query funcQ = subQuery(funcStr, FunctionQParserPlugin.NAME).getQuery();
					
					//if (funcQ instanceof FunctionQuery) {  //what else could be?
					vs = ((FunctionQuery)funcQ).getValueSource();		
					functionParamValueSource = vs; //todo must call getValues when using it!
					
				} else {
					logger.error("!!!! no function defined for the postfilter???");
				}
				
				DynamicQuery dq = new DynamicQuery(vs, cache, function, functionParams, cachedResults);
			
				dq.setParams(params);
				
				return dq;
			}
		};
	}

	@Override
	public void init(NamedList args) {
		// Emtpy on purpose
	}
	
	@Override
	public String getDescription() {
		return "xxxx";
	}

}
