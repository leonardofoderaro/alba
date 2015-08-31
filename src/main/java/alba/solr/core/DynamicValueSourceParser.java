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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.ConstValueSource;
import org.apache.lucene.queries.function.valuesource.LiteralValueSource;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.ValueSourceParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alba.solr.docvalues.FunctionExecutor;

public class DynamicValueSourceParser extends ValueSourceParser {

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	Map<String, DocValuesDynamicValueSource> dynamicFunctions;

	String[] rawargs = new String[100];

	Map<String,String> args = new HashMap<String,String>();

	DocValuesDynamicValueSource valueSource = new DocValuesDynamicValueSource("id");

	private Map<String, CallableFunction> functions;

	@SuppressWarnings("unchecked")
	public ValueSource parse(FunctionQParser fp) throws SyntaxError {

		String functionName; 

		List<ValueSource> valueSourceList = new ArrayList<ValueSource>();

		functions =(Map<String, CallableFunction>) fp.getReq().getContext().get(Loader.FUNCTIONS);

		Map<String,ValueSource> values = new HashMap<String,ValueSource>();

		int i = 0;
		while (fp.hasMoreArguments()) {
			rawargs[i++] = fp.parseArg();
		}

		functionName = rawargs[0];		

		CallableFunction function = functions.get(functionName);

		//still need this?

		// FunctionExecutionContext cachedEC = (FunctionExecutionContext)fp.getReq().getContext().get(fp.getString() );
		/* if (cachedEC != null) {
			logger.error("reusing executor from cache!");
			return cachedEC.getFunctionExecutor();
		} */


		for(int k=1;k<i;k++) {
			String parts[] = rawargs[k].split("=");
			String name = parts[0];
			String value = parts[1];

			args.put(parts[0], parts[1]);

			if (value.startsWith("\"") && value.endsWith("\"")) {
				//probably quite ineffcient..
				String v = value.replaceAll("^\"", "").replaceAll("\"$", "");
				LiteralValueSource l = new LiteralValueSource(v);
				values.put(name, l);
				valueSourceList.add(l);
			} else if (NumberUtils.isNumber(value)) {
				ConstValueSource cvs = new ConstValueSource(Float.parseFloat(value));
				values.put(name, cvs );
				valueSourceList.add(cvs);
			} else {
				SchemaField f = fp.getReq().getSchema().getField(value);
				ValueSource vs = f.getType().getValueSource(f, fp);
				values.put(name, vs);
				valueSourceList.add(vs);
			}

		}

		FunctionExecutor executor = new FunctionExecutor(values, valueSourceList, fp, this);

		executor.setFunction(functions.get(functionName));


		// still need this????
		FunctionExecutionContext ec = new FunctionExecutionContext(fp.getString(), values, function, executor);
		fp.getReq().getContext().put(fp.getString(), ec);


		return executor;

	}



	public void addCustomFields() {
		// TODO Auto-generated method stub
		logger.error("I'll add custom fields!"); //how??
	}
}