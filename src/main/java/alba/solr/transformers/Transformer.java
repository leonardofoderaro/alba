package alba.solr.transformers;

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
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.transform.TransformerFactory;
import org.apache.solr.response.transform.TransformerWithContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alba.solr.core.CallableFunction;
import alba.solr.core.Loader;


public class Transformer extends TransformerFactory {

	class PluggableDocTransformer extends TransformerWithContext {

		Map<Object, Object> context;
		
		Logger logger = LoggerFactory.getLogger(this.getClass().getName());
		
		final CallableFunction function;

		public PluggableDocTransformer( CallableFunction function )
		{
			this.function = function;
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub

			return "PluggableTransformer";
		}	

		@Override
		public void transform(SolrDocument doc, int docid) throws IOException {
			// TODO Auto-generated method stub
			
			Map<String, Object> results = new HashMap<String, Object>();

			try {
				this.function.getMethod().invoke(this.function.getInstance(), doc);

			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				// TODO Auto-generated catch block
				logger.error("error while invoking " + function.getMethod().getName(), e);
			}

	
		}

	}

	private Map<String, CallableFunction> transformers;

	@SuppressWarnings("unchecked")
	@Override
	public TransformerWithContext create(String field, SolrParams params,
			SolrQueryRequest req) {
		// TODO Auto-generated method stub
		Logger logger = LoggerFactory.getLogger(this.getClass().getName());
		
		transformers = (Map<String, CallableFunction>)req.getContext().get(Loader.DOCTRANSFORMERS);
		
		String name = params.get("name");
		
		if (name == null) {
			logger.error("no param name found for transformer " + this.getClass().getName());
			return null;
		}
		
		
		CallableFunction function = transformers.get(name);
		
		if (function == null) {
			logger.error("no mapped function found for transformer " + name);
		}
		

		
		PluggableDocTransformer docTransformer = new PluggableDocTransformer(function);

		docTransformer.context = req.getContext();

		return docTransformer;
	}
	
	


}
