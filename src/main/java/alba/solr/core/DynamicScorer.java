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

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.util.Bits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicScorer extends Scorer {


	protected final LeafReader reader;
	private int doc = -1;
	protected final int maxDoc;
	//protected final FunctionValues values;
	protected boolean checkDeletes;
	private final Bits liveDocs;
	private CallableFunction function;
	private Object[] functionParams;
	
	Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	private DynamicQuery query;


	public DynamicScorer(IndexReader reader, CallableFunction function, Object[] functionParams, DynamicQuery query) {
		super(null);
		logger.error("created dynamic scorer!");
		
		this.query = query;
		this.reader = (LeafReader)reader;
		this.function = function;
		this.maxDoc = reader.maxDoc();
		//this.values = values;
		setCheckDeletes(true);
		this.liveDocs = MultiFields.getLiveDocs(reader);
		this.functionParams = functionParams;
	}

	public IndexReader getReader() {
		return reader;
	}

	public void setCheckDeletes(boolean checkDeletes) {
		this.checkDeletes = checkDeletes && reader.hasDeletions();
	}

	public boolean matches(int doc) {
		return (!checkDeletes || liveDocs.get(doc)) && matchesValue(doc);
	}

	public boolean matchesValue(int doc) {
		try {
			return this.query.getValueSource().getValues(null, reader.getContext()).boolVal(doc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("matchesValue " + doc, e);
			return false;
		}
	
		
	}

	@Override
	public int docID() {
		return doc;
	}

	@Override
	public int nextDoc() throws IOException {
		for (; ;) {
			doc++;
			if (doc >= maxDoc) return doc = NO_MORE_DOCS;
			if (matches(doc)) return doc;
		}
	}

	@Override
	public int advance(int target) throws IOException {
		// also works fine when target==NO_MORE_DOCS
		doc = target - 1;
		return nextDoc();
	}

	@Override
	public float score() throws IOException {
		return 1;
		//  return values.floatVal(doc);
	}

	@Override
	public int freq() throws IOException {
		return 1;
	}

	@Override
	public long cost() {
		return maxDoc;
	}
}