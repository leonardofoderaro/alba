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

import java.util.Map;

import org.apache.lucene.queries.function.ValueSource;

import alba.solr.docvalues.FunctionExecutor;

public class FunctionExecutionContext {
	
	private String functionStr;
	
	private Map<String, ValueSource> valueSources;
	
	private CallableFunction function;

	private FunctionExecutor functionExecutor;

	public FunctionExecutionContext(String functionStr,
			Map<String, ValueSource> valueSources, CallableFunction function, FunctionExecutor executor) {
		
		this.functionStr = functionStr;
		
		this.valueSources = valueSources;
		
		this.function = function;
		
		this.functionExecutor = executor;
		
	}
	
	public String getFunctionStr() {
		return functionStr;
	}

	public Map<String, ValueSource> getValueSources() {
		return valueSources;
	}

	public CallableFunction getFunction() {
		return function;
	}
	
	public FunctionExecutor getFunctionExecutor() {
		return this.functionExecutor;
	}

}
