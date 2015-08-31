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

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.valuesource.FieldCacheSource;
import org.apache.lucene.util.mutable.MutableValueLong;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.FunctionQParser;

import alba.solr.searchcomponents.ISolrLightPlugin;

public class DocValuesDynamicValueSource extends FieldCacheSource implements ISolrLightPlugin {

	private FunctionQParser functionQueryParser;


	public DocValuesDynamicValueSource(String string, FunctionQParser fp) {
		super(string);
		this.functionQueryParser = fp;
	}
	
	public DocValuesDynamicValueSource(String field) {
		super(field);
		// TODO Auto-generated constructor stub
	}

	public long externalToLong(String extVal) {
		return Long.parseLong(extVal);
	}

	public Object longToObject(long val) {
		return val;
	}

	public String longToString(long val) {
		return longToObject(val).toString();
	}


	@Override
	public FunctionValues getValues(Map context, LeafReaderContext readerContext) throws IOException {
		SchemaField field = this.functionQueryParser.getReq().getSchema().getField("id");
		
		return field.getType().getValueSource(field, this.functionQueryParser).getValues(context, readerContext);
		
		/*
		final NumericDocValues arr = DocValues.getNumeric(readerContext.reader(), "id");
		final Bits valid = DocValues.getDocsWithField(readerContext.reader(), "id");

		return new LongDocValues(this) {
			@Override
			public long longVal(int doc) {
				return arr.get(doc);
			
			}
			
			@Override
			public double doubleVal(int doc) {
				return arr.get(doc);
			}
			

			@Override
			public boolean exists(int doc) {
				return arr.get(doc) != 0 || valid.get(doc);
			}

			@Override
			public Object objectVal(int doc) {
				return valid.get(doc) ? longToObject(arr.get(doc)) : null;
			}

			@Override
			public String strVal(int doc) {
				return valid.get(doc) ? longToString(arr.get(doc)) : null;
			}

			@Override
			protected long externalToLong(String extVal) {
				return DocValuesDynamicValueSource.this.externalToLong(extVal);
			}

			@Override
			public ValueFiller getValueFiller() {
				return new ValueFiller() {
					private final MutableValueLong mval = newMutableValueLong();

					@Override
					public MutableValue getValue() {
						return mval;
					}

					@Override
					public void fillValue(int doc) {
						mval.value = arr.get(doc);
						mval.exists = mval.value != 0 || valid.get(doc);
					}
				};
			}

		}; */
	}



	@Override
	public String description() {
		// TODO Auto-generated method stub
		return "prova descrizione";
	}

	
	protected MutableValueLong newMutableValueLong() {
	    return new MutableValueLong();
	  }

	
}
