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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.FloatDocValues;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alba.solr.annotations.FunctionQuery;
import alba.solr.annotations.Param;
import alba.solr.core.CallableFunction;


public class FloatFunction extends FloatDocValues implements IDynamicFunction {

	List<Object> consts;
	private FunctionValues[] values;
	private Map<String, String> args;
	private FunctionQParser fp;
	private Map context;
	private LeafReaderContext readerContext;
	
	Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	private Map<String, CallableFunction> functions;
	private String functionName;

	public FloatFunction(ValueSource vs, FunctionValues[] values, List<Object> params, Map<String, String> args, FunctionQParser fp) {
		super(vs);
		// TODO Auto-generated constructor stub
		this.values = values;
		this.consts = consts;
		this.args = args;
		this.fp = fp;

	}

	@Override
	public float floatVal(int doc) {
		// TODO Auto-generated method stub
		Map<String, Object> params = new HashMap<String, Object>();

		for (String s : args.keySet()) {
			if (args.get(s).startsWith("\"")) {
				params.put(s, args.get(s));
			} else if (NumberUtils.isNumber(args.get(s))) {
				Object objVal;
				
				try {
					objVal = Long.parseLong(args.get(s));
				} catch (NumberFormatException nfe1) {
					try {
						objVal = Float.parseFloat(args.get(s));
					} catch (NumberFormatException nfe2) {
						objVal = s;
					}
					
				}
				
				if (objVal != null) {
					params.put(s, objVal);
				} else {
					params.put(s, "N/A");
				}
				
			} else if ("false".equals(args.get(s).toLowerCase())) {
				params.put(s, false);
			} else if ("true".equals(args.get(s).toLowerCase())) {
				params.put(s, true);
			} else {
				SchemaField f = fp.getReq().getSchema().getField(args.get(s));
				
				ValueSource vs  = f.getType().getValueSource(f, fp);

				Object objVal = null;
				
				try {
					objVal = vs.getValues(this.context, this.readerContext).longVal(doc);
					params.put(s, objVal);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					// TODO Log properly
					
					try {
						objVal = vs.getValues(this.context, this.readerContext).floatVal(doc);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
				
						try {
							objVal = vs.getValues(this.context, this.readerContext).strVal(doc);
						} catch (IOException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
					}
					
					
					e.printStackTrace();
				}
				
				if (objVal != null) {
					params.put(s, objVal);
				} else {
					params.put(s, "N/A");
				}
				

			}

		}
		
		CallableFunction cf = functions.get(this.functionName);
		
		if (cf == null) {
			logger.error("unable to get function " + this.functionName);
		}
		
		if (cf != null) {
			List<Object> fnParams =new ArrayList<Object>();
			Parameter[] methodParameters = cf.getMethod().getParameters();
			
			//TODO spostare quanto più codice possibile in fase di inizializzazione
			for (Parameter p : methodParameters) {
				if (p.isAnnotationPresent(Param.class)) {
					Param paramAnnotation = p.getAnnotation(Param.class);
					fnParams.add(params.get(paramAnnotation.name()));
				}
			}
			
			try {
				return  (float)cf.getMethod().invoke(cf.getInstance(), fnParams.toArray());
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				// TODO Auto-generated catch block
				logger.error("errore mentre chiamavo " + cf.getMethod().getName(), e);
				for (Object o : fnParams) {
					logger.error("p " + o.toString());
				}
			}
		}
		
		
		
		return -1f;

	
	}



	public void setContext(Map context) {
		// TODO Auto-generated method stub
		this.context = context;
	}

	public void setReaderContext(LeafReaderContext readerContext) {
		// TODO Auto-generated method stub
		this.readerContext = readerContext;
	}

	public void setFunctions(Map<String, CallableFunction> functions) {
		// TODO Auto-generated method stub
		this.functions = functions;
	}

	public void setFunctionName(String functionName) {
		// TODO Auto-generated method stub
		this.functionName = functionName;
	}

}
