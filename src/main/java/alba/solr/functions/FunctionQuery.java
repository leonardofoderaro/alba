package alba.solr.functions;

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
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.handler.component.ResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alba.common.interfaces.ILightFunctionQuery;

public abstract class  FunctionQuery extends FunctionValues implements ILightFunctionQuery {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private LeafReader readerContext;
	
	private String fieldName;
	
	private ResponseBuilder responseBuilder;

	private Document doc;
	
	protected String[] args;

	private Map<Object, Object> context;
	
	private DocValues[] docValues;
	
	NumericDocValues docVals;

	public FunctionQuery(LeafReader reader) {
		this.readerContext = reader;
		
	}
	
	public FunctionQuery() {
		//
	}
	
	public void setLeafReader(LeafReader reader, String[] args) {
		
		this.readerContext = reader;
		
		//i = 0;
		for (String arg : args) {
			//docValues[i] = 
			
		}
		
		try {
		//	this.docVals[0] =
					DocValues.getNumeric(this.readerContext, "id");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			this.docVals = null;
			logger.error("error while readering docvals!", e);
		}
		
	}

	public Object objectVal(int docID) {
	/*	if (docID > readerContext.maxDoc()) {
			logger.error("docID must be > 0 and < maxDoc()");
			return null;
		} */
		
		//return docID;
		
		
			
			return floatVal(docID);
			//return new Float(dv.get(docID));
			
			//doc = readerContext.document(docID);
			//return evaluate(doc);
		
			// TODO Auto-generated catch block
				
	}
	
	@Override
	public float floatVal(int doc) { 
	
		if (this.docVals != null) {
			return (float)this.docVals.get(doc);
		} else {
			return -1;
		}
			
	}
	
	@Override
	public ValueSource getValueSource() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setFieldName(String fieldName) {
		// TODO Auto-generated method stub
		this.fieldName = fieldName;
		
	}

	public void setArgs(String[] args) {
		// TODO Auto-generated method stub
		this.args = args;
	}
	
	@Override
	public long longVal(int doc) {
		NumericDocValues dv;
		try {
			dv = DocValues.getNumeric(this.readerContext, "id");
			return dv.get(doc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return -1;
	}
	
	@Override
	public Object initialize(ResponseBuilder rb) {
		
		this.responseBuilder = rb;
		
		return null;
	}

	@Override
	public String toString(int doc) {
		// TODO Auto-generated method stub
		return null;
	}

	/* 
	public void setContext(Map<Object, Object> context) {
		// TODO Auto-generated method stub
		this.context = context;
	}
	
	
	public Map<Object, Object> getContext() {
		return context;
	}
	*/

	
	public ResponseBuilder getResponseBuilder() {
		return this.responseBuilder;
	}
	
	public LeafReader getLeafReader() {
		return this.readerContext;
	}
	
	

}
