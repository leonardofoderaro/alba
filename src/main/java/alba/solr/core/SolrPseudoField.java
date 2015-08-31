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

import org.apache.lucene.document.Document;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.solr.request.SolrQueryRequest;

import alba.solr.searchcomponents.ISolrLightPlugin;

public abstract class SolrPseudoField extends FunctionValues implements ISolrLightPlugin {

	protected LeafReader reader;
	
	protected Map<String, String> args;

	private SolrQueryRequest solrQueryRequest;
	
	public SolrPseudoField() {
		
	}
	
	public SolrPseudoField(LeafReader reader) {
		this.reader = reader;
		
	}
	
	@Override
	public String toString(int doc) {
		// TODO Auto-generated method stub
		return "kkkk";
	}

	public void setLeafReader(LeafReader reader, Map<String, String> args) {
		// TODO Auto-generated method stub
		this.reader = reader;
		this.args = args;
	}

	public void setArgs(Map<String, String> args) {
		// TODO Auto-generated method stub
		this.args = args;
	}
	
	@Override
	public Object objectVal(int docID) {
		
		// to avoid java.lang.IndexOutOfBoundsException
		/* if ((docID < 0) || (docID > this.reader.maxDoc())) {
			return null;
		}*/
		
		try {
			return evaluate(this.reader.document(docID), docID);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// TODO scrivere su log e restituire null
			return "Error while retrieving " + docID;
		}
	}
	
	public abstract Object evaluate(Document doc, int x);
	
	public Map<String, String> getArgs() {
		return this.args;
	}
	
	public LeafReader getLeafReader() {
		return this.reader;
	}
	
	public void setSolrQueryRequest(SolrQueryRequest solrQueryRequest) {
		this.solrQueryRequest = solrQueryRequest;
	}
	
	public SolrQueryRequest getSolrQueryRequest() {
		return solrQueryRequest;
	}
	
	// floatVal is used by {!frange}
	@Override
	public float floatVal(int x) {
		// performance issues on this? is this too expensive?
		return Float.parseFloat(objectVal(x).toString());
	}
	
	//doubleVal is used by sort
	@Override
	public double doubleVal(int x) {
		// performance issues on this? is this too expensive?
		return Double.parseDouble(objectVal(x).toString());
	}
	
	public void init() {
		
	}
}
