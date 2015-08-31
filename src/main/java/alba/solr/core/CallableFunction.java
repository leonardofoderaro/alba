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

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.common.util.Hash;

import alba.solr.annotations.FunctionQuery;
import alba.solr.annotations.Param;

public class CallableFunction {
	
	private String name;
	private Object instance;
	private Method method;
	private Class<?> returnType;
	private Map<Integer,Integer> results;

	public CallableFunction(String name, Object o, Method m, Class<?> returnType) {
		// TODO Auto-generated constructor stub
		this.instance = o;
		this.method = m;
		this.returnType = returnType;
		this.name = name;
		this.results = new HashMap<Integer, Integer>();
	}
	
	public Object getInstance() {
		return instance;
	}

	public Method getMethod() {
		return method;
	}
	
	@Field
	public void setFunctionDescriptor(String method) {
		// not needed
		
	}
	
	public Map<String, Object> getFunctionDescriptor() {
		Map<String, Object> methodInfo = new HashMap<String, Object>();
		
		methodInfo.put("class", this.getInstance().getClass().getName());
		methodInfo.put("name", this.name);
		
		methodInfo.put("methodName", this.getMethod().getName());
		
		FunctionQuery functionAnnotation = this.getMethod().getAnnotation(FunctionQuery.class);
		
		if (functionAnnotation != null) {
			methodInfo.put("description", functionAnnotation.description());
		}
		
		List<Map<String, String>> params = new ArrayList<Map<String,String>>();
		
		for (Parameter p : this.getMethod().getParameters()) {
			Map<String, String> paramDetail = new HashMap<String, String>();
			
			paramDetail.put(p.getName(), p.getType().getName());
			
			Param paramAnnotation = p.getAnnotation(Param.class);
			if (paramAnnotation != null) {
				paramDetail.put("description", paramAnnotation.description());
			}
			
			
			params.add(paramDetail);
			
		}
		
		methodInfo.put("params", params);
		
		
		//List<Map<String, String>> annotations = new ArrayList<Map<String,String>>();
		
		
		return methodInfo;
		
	}

	
	public Class<?> getReturnType() {
		return returnType;
	}

	public long getResult(int docNumber) {
		// TODO Auto-generated method stub
		return this.results.get(docNumber);
	}

	public void setResult(int doc, int i) {
		// TODO Auto-generated method stub
		//this.results.put(doc,i);
	}

	public Map<Integer, Integer> getResults() {
		// TODO Auto-generated method stub
		return results;
	}
	
	public void setResults(Map<Integer, Integer> results) {
		this.results = results;
	}

}
