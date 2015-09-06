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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.search.DelegatingCollector;
import org.apache.solr.search.LRUCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DynamicDelegatingCollector extends DelegatingCollector {

	DynamicQuery dynamicQuery;

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private CallableFunction function;

	private Object[] functionParams;

	private ValueSource functionParamValueSource;

	private Map fcontext;

	private Map<Object, CachedResult> functionResultsCache;


	public DynamicDelegatingCollector(DynamicQuery dynamicQuery, Map fcontext, CallableFunction function2, Object[] functionParams2, Map<Object, CachedResult> functionResultsCache) {
		// TODO Auto-generated constructor stub

		this.dynamicQuery = dynamicQuery;
		this.fcontext = fcontext;
		this.functionResultsCache = functionResultsCache;

	}

	@Override
	protected void doSetNextReader(LeafReaderContext context) throws IOException {
		super.doSetNextReader(context);
	}
	

	@Override
	public void collect(int docNumber) throws IOException {
		boolean result = false;
		
		
		// can we cache this one?
		functionParams[0] = this.dynamicQuery.getValueSource().getValues(null, this.context).objectVal(docNumber);
		
		
		try {
			// can we cache this, too?
		
			result = (boolean)this.function.getMethod().invoke(this.function.getInstance(), functionParams);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			logger.error("error calling " + this.function.getMethod().getName(), e);
		}
		
		// ideally, we'd need some sort of cache to ask for this result 
		// instead of doing the two expensive calls above
		if (result) {
			super.collect(docNumber);
		}
		
	}

	public void setFunction(CallableFunction function, Object[] functionParams, ValueSource functionParamValueSource) {
		// TODO Auto-generated method stub
		this.function = function;
		this.functionParams = functionParams;
		this.functionParamValueSource = functionParamValueSource;
	}
}
