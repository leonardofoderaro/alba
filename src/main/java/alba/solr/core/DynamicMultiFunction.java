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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.MultiFunction;
import org.apache.solr.search.FunctionQParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alba.solr.docvalues.DynamicDocValuesHelper;
import alba.solr.docvalues.IntegerFunction;
import alba.solr.docvalues.StringFunction;
import alba.solr.searchcomponents.ISolrLightPlugin;

public class DynamicMultiFunction extends MultiFunction implements ISolrLightPlugin {

	FunctionValues functionValues;

	List<Object> consts = new ArrayList<Object>();

	private Map<String, String> args;

	private FunctionQParser fp;

	private Map<String, CallableFunction> functions;

	private String functionName;

	DynamicDocValuesHelper helper;

	CallableFunction cf;

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());


	//TODO anziche' una lista dev'essere una mappa?
	public DynamicMultiFunction(List<ValueSource> sources, List<Object> consts, FunctionQParser fp, Map<String, String> args, Map<String, CallableFunction> functions) {
		super(sources);
		this.consts = consts;
		this.args = args;
		this.fp = fp;
		this.functions = functions;

		helper = new DynamicDocValuesHelper(this, args, fp);


		helper.setFunctions(functions);




		// TODO Auto-generated constructor stub
	}

	@Override
	protected String name() {
		// TODO Auto-generated method stub
		return "multi";
	}

	//public abstract FunctionValues getFunctionValues();

	public static FunctionValues[] valsArr(List<ValueSource> sources, Map fcontext, LeafReaderContext readerContext) throws IOException {
		final FunctionValues[] valsArr = new FunctionValues[sources.size()];
		int i=0;
		for (ValueSource source : sources) {
			if (source != null) {
				valsArr[i] = source.getValues(fcontext, readerContext);
			} else {
				valsArr[i] = null;
			}

			i++;

		}
		return valsArr;
	}

	public void setFunctionName(String functionName) {
		// TODO Auto-generated method stub
		this.functionName = functionName;
	}

	@Override
	public FunctionValues getValues(Map context, LeafReaderContext readerContext)
			throws IOException {
		// TODO Auto-generated method stub
		FunctionValues[] vals =  DynamicMultiFunction.valsArr(sources, context, readerContext);

		//FunctionValues fv = this.getFunctionValues();

		List<Object> params = new ArrayList<Object>();

		int i = 0;
		for (i=0; i < vals.length; i++) {
			if (vals[i] != null) {
				params.add(vals[i]);
			} else {
				params.add(this.consts.get(i));
			}
		}



		helper.setVals(vals);

		helper.setContext(context);
		helper.setReaderContext(readerContext);

		helper.setFunctionName(this.functionName);

		cf = functions.get(this.functionName);

		if (cf != null) {
			if ( cf.getReturnType() == String.class) {
				return new StringFunction(this, helper);
			}

			if ( cf.getReturnType() == Integer.class) {
				return new IntegerFunction(this, helper);
			}
		}

		return null;

	}

}
