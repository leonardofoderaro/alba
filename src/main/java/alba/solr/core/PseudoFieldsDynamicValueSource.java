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

import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.LongDocValues;
import org.apache.lucene.queries.function.valuesource.LongFieldSource;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.mutable.MutableValue;
import org.apache.lucene.util.mutable.MutableValueLong;
import org.apache.solr.request.SolrQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PseudoFieldsDynamicValueSource extends ValueSource {

	private SolrPseudoField pseudoField;

	private Map<String, String> args;

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private SolrQueryRequest solrQueryRequest;

	public PseudoFieldsDynamicValueSource(SolrPseudoField pseudoField, Map<String, String> args) {
		// TODO Auto-generated constructor stub
		this.pseudoField = pseudoField;
		this.args = args;	
	}

	public FunctionValues getValues(Map context, LeafReaderContext readerContext)
			throws IOException {


		pseudoField.setLeafReader(readerContext.reader(), this.args);

		//TODO. params are positional, make them named!
		pseudoField.setArgs(this.args);

		pseudoField.setSolrQueryRequest(this.solrQueryRequest);

		pseudoField.init();

		return pseudoField;

	}




	@Override
	public boolean equals(Object o) {
		return false;
		
		//if (o.getClass() != this.getClass()) return false;
		//PseudoFieldsDynamicValueSource other = (PseudoFieldsDynamicValueSource) o;
		//return this.pseudoField.equals(other.pseudoField);
	}

	@Override
	public int hashCode() {
		int h = getClass().hashCode();

		return h + pseudoField.hashCode();
	}

	@Override
	public String description() {
		// TODO Auto-generated method stub
		return "xxpxpxp";
	}

	public void setSolrQueryRequest(SolrQueryRequest req) {
		// TODO Auto-generated method stub
		this.solrQueryRequest = req;
	}



}
