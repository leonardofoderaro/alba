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

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.BoolDocValues;
import org.apache.lucene.queries.function.docvalues.DoubleDocValues;
import org.apache.lucene.queries.function.docvalues.FloatDocValues;
import org.apache.lucene.queries.function.docvalues.IntDocValues;
import org.apache.lucene.queries.function.docvalues.StrDocValues;
import org.apache.lucene.queries.function.valuesource.LiteralValueSource;
import org.apache.lucene.queries.function.valuesource.MultiFunction;
import org.apache.solr.search.FunctionQParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alba.solr.annotations.Param;
import alba.solr.core.CallableFunction;
import alba.solr.core.DynamicValueSourceParser;


public  class FunctionExecutor extends MultiFunction {

	private Map<String, ValueSource> values;
	private Map<String, CallableFunction> functions;
	private CallableFunction function;

	WrappedIntDocValues wrappedIntDocValues;

	List<Object> fnParams;

	private HashMap<Class<?>, String> mappings;

	private ValueSource vs;

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	// annotated params must be explictly passed in a function call
	private List<ParamField> annotatedParams;

	// non-annotated params are filled and passed automagically
	private List<ParamField> nonAnnotatedParams;

	private Map<String, Object> autoVars;
	private FunctionQParser fp;
	private DynamicValueSourceParser caller;



	public FunctionExecutor(Map<String, ValueSource> values, List<ValueSource>sourceList, FunctionQParser fp, DynamicValueSourceParser dynamicValueSourceParser) {
		super(sourceList);

		this.values = values;

		fnParams = new ArrayList<Object>();

		this.fp = fp;

		mappings = new HashMap<Class<?>, String>();
		mappings.put(String.class, "strVal");
		
		mappings.put(Integer.class, "intVal");
		mappings.put(int.class, "intVal");
		
		mappings.put(Boolean.class, "boolVal");
		mappings.put(boolean.class, "boolVal");
		
		mappings.put(Double.class, "doubleVal");
		mappings.put(double.class, "doubleVal");
		
		mappings.put(Float.class, "floatVal");
		mappings.put(float.class, "floatVal");
		
		mappings.put(Long.class, "longVal");
		mappings.put(long.class, "longVal");

		vs = new LiteralValueSource("");
		wrappedIntDocValues = new WrappedIntDocValues(vs);


		autoVars = new HashMap<String, Object>();
		autoVars.put("org.apache.solr.request.SolrQueryRequest", this.fp.getReq());
		
		this.caller = dynamicValueSourceParser;


	}

	@Override
	public int hashCode() {
		return super.hashCode() + values.hashCode();
	}

	@Override
	protected String name() {
		// TODO Auto-generated method stub
		return "str multi";
	}

	@Override
	public FunctionValues getValues(Map context, LeafReaderContext readerContext)
			throws IOException {
		final Map<String, FunctionValues> valsMap = this.valsMap(sources, context, readerContext);

	    final CallableFunction fn = this.function;
	    
		FunctionExecutor host = this;

		if ((this.function.getReturnType() == Boolean.class) || (this.function.getReturnType() == boolean.class)) {
			logger.error("calling boolean function!");
			return new BoolDocValues(this.sources.get(0)) {

				@Override
				public boolean boolVal(int doc) {
					// TODO Auto-generated method stub
					Object[] objParams = host.populateObjParams(valsMap, doc);
					return host.getBooleanResult(fn, objParams);
				}
			};	
		} else 	if (this.function.getReturnType() == Double.class) {
			return new DoubleDocValues(this.sources.get(0)) {

				@Override
				public double doubleVal(int doc) {
					// TODO Auto-generated method stub
					return 12.0d;
				}
			};	
		} else if (this.function.getReturnType() == String.class) {
			return new StrDocValues(this.vs) {
				@Override
				public String strVal(int doc) {
					Object[] objParams = host.populateObjParams(valsMap, doc);
					return host.getStringResult(fn, objParams);
				}
			}; 	
		} else if (this.function.getReturnType() == Float.class) {
			return new FloatDocValues(this.sources.get(0)) {

				@Override
				public float floatVal(int doc) {
					// TODO Auto-generated method stub
					Object[] objParams = host.populateObjParams(valsMap, doc);
					return host.getFloatResult(fn, objParams, doc);
				}
			};
		} else if ((this.function.getReturnType() == Integer.class)) {
			// with this snippet of code we could avoid to instantiate a new object of type IntDocValues
			// simply by using the existing instance of WrappedIntDocValues.
			// but.. this cause a crash :(
			// wrappedIntDocValues.setFunction(fn);
			// wrappedIntDocValues.setExpectedParams(this.expectedParams);
			// return wrappedIntDocValues;

			// so for now, just go on with the good old new instance of IntDocValues

			return new IntDocValues(this.sources.get(0)) {

				@Override
				public int intVal(int doc) {
					// TODO Auto-generated method stub

					Object[] objParams = host.populateObjParams(valsMap, doc);
					return host.getIntegerResult(fn, objParams, doc);

				}


			};  
		} 

		//apparently we dind't find an appropriate DocValues for this function.
		//it could be a function which should generate two or more additional fields in the resulting docs
		//but HOW can we do that??? 
		//for now, just return a null, so no field will be added.
		host.ping2caller();
		
		logger.error("I don't know how to deal with class " + this.function.getReturnType() + ", check FunctionExecutor.java");

		/*
		 * instanziare e restituire x forza un DocValues altrimenti non valorizza i parametri!!
		 * poi in doctransformer togliere questo campo (capire come!)
		 */
		IntDocValues dd = new IntDocValues(this.vs) {

			@Override
			public int intVal(int doc) {
				// TODO Auto-generated method stub

				Object[] objParams = host.populateObjParams(valsMap, doc);
				try {
					host.function.getMethod().invoke(fn.getInstance(), objParams);
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					// TODO Auto-generated catch block
					logger.error("argggg", e);
				}
				return 0; //we're not going to use this value

			}


		}; 
		return dd; // new StrFunctionValue(null);

	}

	protected boolean getBooleanResult(CallableFunction fn, Object[] objParams) {
		try {
			boolean result = (boolean) fn.getMethod().invoke(fn.getInstance(), objParams);
			return result;
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			logger.error("unable to call boolean function + " + fn.getMethod().getName(), e);
		}
		
		//should it raise an exception (and then return null in the response for this function??
		return true;
	}

	protected float getFloatResult(CallableFunction fn, Object[] objParams,
			int doc) {
		try {
			float result = (float) fn.getMethod().invoke(fn.getInstance(), objParams);
			return result;
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			logger.error("unable to call float function + " + fn.getMethod().getName(), e);
		}
		return -1;
	}

	private void ping2caller() {
		// TODO Auto-generated method stub
		caller.addCustomFields();
	}

	protected String getStringResult(CallableFunction fn, Object[] objParams) {
		try {
			return (String)fn.getMethod().invoke(fn.getInstance(), objParams);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			logger.error("unable to call String function!", e);
		}
		return "";
	}

	protected int getIntegerResult(CallableFunction fn, Object[] objParams, int doc) {
		try {
			int i = (int) fn.getMethod().invoke(fn.getInstance(), objParams);
			return i;
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			logger.error("unable to call int function + " + fn.getMethod().getName(), e);
		}
		return -1;
	}

	public Object[] populateObjParams(Map<String, FunctionValues> valsMap, int doc) {


		int nParams = annotatedParams.size();
		Object[] objParams = new Object[nParams];

		Object obj;
		String methodName = "";
		String pName = "";
		for (int i=0;i<nParams;i++) {

			methodName = mappings.get(annotatedParams.get(i).type);

			if (methodName != null) {
				
				pName = annotatedParams.get(i).name;

				obj = valsMap.get(annotatedParams.get(i).name);
				
				Method m;
				try {

					if (obj == null) {
						logger.error("OBJ NULL!!!");
					}
					
					m = obj.getClass().getMethod(methodName, int.class);

					// to avoid IllegalAccessException
					// courtesy of http://stackoverflow.com/questions/13681142/can-not-access-a-member-of-class-org-springframework-aop-truepointcut-with-modif
					m.setAccessible(true);
					
					

					objParams[i] = m.invoke(obj, doc);
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NullPointerException e) {
					// TODO Auto-generated catch block
					logger.error("too much reflection will kill you", e);
				}

			} else {
				logger.error("I don't know how to deal with expected param of class " + annotatedParams.get(i).type.getName());
				obj = autoVars.get(annotatedParams.get(i).type.getName());
				objParams[i] = obj;
			}
			
		} 

		return objParams;
	}

	private void setFunctionParams(Map<String, FunctionValues> valsMap, int doc) {
		// TODO Auto-generated method stub
		Parameter[] methodParameters = function.getMethod().getParameters();

		//TODO spostare quanto più codice possibile in fase di inizializzazione
		for (Parameter p : methodParameters) {
			if (p.isAnnotationPresent(Param.class)) {
				Param paramAnnotation = p.getAnnotation(Param.class);
				fnParams.add(valsMap.get(paramAnnotation.name()).strVal(doc));
			}
		}
	}

	private Map<String,FunctionValues> valsMap(List<ValueSource> sources,
			Map context, LeafReaderContext readerContext) throws IOException {
		// TODO Auto-generated method stub
		final Map<String, FunctionValues> valsmap = new HashMap<String, FunctionValues>();

		int i=0;

		for (String s : values.keySet()) {
			valsmap.put(s, values.get(s).getValues(context, readerContext));
		}

		return valsmap;
	}



	public void setFunction(CallableFunction callableFunction) {
		this.function = callableFunction;
		//wrappedIntDocValues.setFunction(function);

		// TODO Auto-generated method stub
		Parameter[] methodParameters = function.getMethod().getParameters();

		annotatedParams = new ArrayList<ParamField>();


		int i = 0;
		//logger.debug("setting function to " + callableFunction.getMethod().getName());
	
		//TODO spostare quanto più codice possibile in fase di inizializzazione
		for (Parameter p : methodParameters) {
			if (p.isAnnotationPresent(Param.class)) {
				Param paramAnnotation = p.getAnnotation(Param.class);
				//fnParams.add(valsMap.get(paramAnnotation.name()).strVal(doc));
				//logger.error("will expect " + paramAnnotation.name() + " as " + p.getType().getName());
				annotatedParams.add(new ParamField(paramAnnotation.name(), p.getType()));
			} else {
				annotatedParams.add(new ParamField(p.getName(), p.getType()));

				//logger.error("non-annotated param. is it special/auto? - " + p.getType());
			}
		}



	}

	/*protected  String func(int doc, FunctionValues vals) {
		return "OOOK " + vals.strVal(doc);
	}*/

	class ParamField {

		String name;
		Class<?> type;



		public ParamField(String name, Class<?> type) {
			this.name = name;
			this.type = type;
		}
	}
}