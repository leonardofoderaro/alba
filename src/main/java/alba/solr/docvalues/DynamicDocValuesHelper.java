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
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.FunctionQParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alba.solr.annotations.Param;
import alba.solr.core.CallableFunction;

public class DynamicDocValuesHelper {
	List<Object> consts;
	private FunctionValues[] values;
	private Map<String, String> args;
	private FunctionQParser fp;
	private Map context;
	private LeafReaderContext readerContext;
	
	Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	private Map<String, CallableFunction> functions;
	private String functionName;
	
    public DynamicDocValuesHelper(ValueSource vs, Map<String, String> args, FunctionQParser fp) {
		// TODO Auto-generated constructor stub
		//this.values = values;
		//this.consts = consts;
		this.args = args;
		this.fp = fp;
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
	
	public Object eval(int doc) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// TODO Auto-generated method stub
		
		/*if (doc < 0 || doc > this.readerContext.reader().maxDoc()) {
			return null;
		}*/
		
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
				} catch (IOException | UnsupportedOperationException e) {
					// TODO Auto-generated catch block
					// TODO Log properly
					
					try {
						objVal = vs.getValues(this.context, this.readerContext).floatVal(doc);
					} catch (IOException | UnsupportedOperationException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
				
						try {
							objVal = vs.getValues(this.context, this.readerContext).strVal(doc);
						} catch (IOException | UnsupportedOperationException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
					}
					
					
					logger.error("error converting values ", e);
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
			
			return cf.getMethod().invoke(cf.getInstance(), fnParams.toArray());
		} else {
			return null;
		}
		
		
	}

	public void setVals(FunctionValues[] vals) {
		// TODO Auto-generated method stub
		this.values = vals;
	}

	public void setParams(List<Object> params) {
		
		
	}
}
