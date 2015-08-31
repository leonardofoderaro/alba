package alba.solr.docvalues;

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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.ValueSourceScorer;
import org.apache.lucene.queries.function.docvalues.IntDocValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alba.solr.core.CallableFunction;
import alba.solr.docvalues.FunctionExecutor.ParamField;

public class WrappedIntDocValues extends IntDocValues {

	List<Object> fnParams;
	Parameter[] methodParameters;
	private CallableFunction function;
	private List<ParamField> expectedParams;
	private Map<String, ValueSource> valueSourcesMap;

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	private Map<String, FunctionValues> functionValuesMap;


	public WrappedIntDocValues(ValueSource vs) {
		super(vs);
		// TODO Auto-generated constructor stub

		fnParams =new ArrayList<Object>();

	}

	@Override
	public int intVal(int doc) {
		// TODO Auto-generated method stub
		logger.error("evaluate fn for doc = " + doc);

		//logger.error("reading doc " + doc);

		int nParams = expectedParams.size();

		Object[] objParams = new Object[nParams];

		for (int i=0;i<nParams;i++) {
			objParams[i] = functionValuesMap.get(expectedParams.get(i).name).strVal(doc);
		}

		methodParameters = function.getMethod().getParameters();

		try {
			return (int) function.getMethod().invoke(function.getInstance(), objParams);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			logger.error("error on method call", e);
		}
		return -1;

		//TODO spostare quanto più codice possibile in fase di inizializzazione
		/*	for (Parameter p : methodParameters) {
			if (p.isAnnotationPresent(Param.class)) {
				Param paramAnnotation = p.getAnnotation(Param.class);
				fnParams.add(valsMap.get(paramAnnotation.name()).strVal(doc));
			}
		}

		try {
			return (int) fn.getMethod().invoke(fn.getInstance(), fnParams.toArray());
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		//return -1;
	}

	public void setFunction(CallableFunction fn) {
		// TODO Auto-generated method stub
		this.function = fn;

	}

	public void setExpectedParams(List<ParamField> expectedParams) {
		// TODO Auto-generated method stub
		this.expectedParams = expectedParams;
	}

	public void setValueSourcesMap(Map<String, ValueSource> values) {
		// TODO Auto-generated method stub
		this.valueSourcesMap = values;
	}

	public void functionValuesMap(Map<String, FunctionValues> valsMap) {
		// TODO Auto-generated method stub
		this.functionValuesMap = valsMap;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + function.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		WrappedIntDocValues that = (WrappedIntDocValues)o;
		return this.equals(that);
	}

	@Override
	public ValueSourceScorer getScorer(IndexReader reader) {
		return super.getScorer(reader);
	}
}
