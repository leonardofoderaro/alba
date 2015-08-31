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
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.Bits;
import org.apache.solr.search.DelegatingCollector;
import org.apache.solr.search.ExtendedQueryBase;
import org.apache.solr.search.PostFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicQuery extends ExtendedQueryBase implements PostFilter {

	private CallableFunction function;

	private ValueSource vs;

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private Map<String, Object> params;

	private Object[] functionParams;

	private ValueSource functionParamValueSource;
	
	private Map<Object, CachedResult> functionResultsCache;


	public DynamicQuery(ValueSource vs, boolean cache, CallableFunction function, Object[] functionParams, Map<Object, CachedResult> cachedResults) {
		// TODO what happens if we set it to true? 
		setCache(false);
		this.vs = vs;
		this.function = function;
		this.functionParams = functionParams;
		this.functionResultsCache = cachedResults;
	}


	@Override
	public boolean equals(Object o) {
		return this == o ;
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(this) + this.vs.hashCode();
	}

	@Override
	public int getCost() {
		// We make sure that the cost is at least 100 to be a post filter
		return Math.max(super.getCost(), 100);
	}

	
	@Override
	public DelegatingCollector getFilterCollector(IndexSearcher idxS) {

		Map fcontext = ValueSource.newContext(idxS);

		DynamicDelegatingCollector ddg = new DynamicDelegatingCollector(this, fcontext, function, functionParams, functionResultsCache);

		ddg.setFunction(function, functionParams, functionParamValueSource);
		return ddg;


	}

	
	// this call is needed only when cache == true, so for now it's better to keep it off
	// TODO move it to a separate branch
	@Override
	public Weight createWeight(IndexSearcher searcher, boolean needsScores) throws IOException {
		return new FunctionWeight(this, searcher, function, functionParams); // new FunctionWeight(this);
	}

	
	public CallableFunction getFunction() {
		return this.function;
	}

	public long getFunctionResult(int docNumber) {
		// TODO Auto-generated method stub
		return function.getResult(docNumber);
		//return 0;
	}

	public ValueSource getValueSource() {
		// TODO Auto-generated method stub
		return this.vs;
	}

	public void setParams(Map<String, Object> params) {
		// TODO Auto-generated method stub
		this.params = params;
	}

	public Map<String, Object> getParams() {
		return this.params;
	}


	// this would be use only with cache == true
	protected class FunctionWeight extends Weight {
		protected IndexSearcher searcher;
		protected float queryNorm;
		protected float queryWeight;
		private CallableFunction function;
		private Object[] functionParams;
		private DynamicQuery query;
		
		protected FunctionWeight(DynamicQuery query, IndexSearcher searcher, CallableFunction function, Object[] functionParams) {
			super(query);
			this.searcher = searcher;
			this.function = function;
			this.functionParams = functionParams;
			this.query = query;
		}

		@Override
		public void extractTerms(Set<Term> terms) {
		  //not needed?
		}

		@Override
		public Explanation explain(LeafReaderContext context, int doc)
				throws IOException {
			//TOO BAD.. how to do it?
			return null;
		}

		@Override
		public float getValueForNormalization() throws IOException {
			// TODO Auto-generated method stub
			queryWeight = getBoost();
			return queryWeight * queryWeight;
		}

		@Override
		public void normalize(float norm, float topLevelBoost) {
			// TODO Auto-generated method stub
		      this.queryNorm = norm * topLevelBoost;
		      queryWeight *= this.queryNorm;
		}

		@Override
		public Scorer scorer(LeafReaderContext context, Bits acceptDocs)
				throws IOException {
			// TODO Auto-generated method stub
			DynamicScorer sc = new DynamicScorer(context.reader(), function, functionParams, this.query);
			return sc;
		}

	}

}