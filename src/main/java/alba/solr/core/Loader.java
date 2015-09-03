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

import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alba.solr.annotations.AlbaResponseWriter;
import alba.solr.annotations.DocTransformer;
import alba.solr.annotations.FunctionQuery;
import alba.solr.annotations.AlbaPlugin;
import alba.solr.annotations.PostFilter;
import alba.solr.annotations.SolrLightPlugin;
import alba.solr.annotations.SolrRequestHandler;
import alba.solr.annotations.SolrSearchComponent;
import alba.solr.searchcomponents.AlbaRequestHandler;
import alba.solr.searchcomponents.ISolrLightPlugin;
import alba.solr.utils.PackageScanner;

public class Loader extends DynamicSearchComponent {

	public static final Object SEARCHCOMPONENTS = "slp.searchComponents";
	
	public static final Object RESPONSEWRITERS = "slp.responseWriters";
	
	public static final Object FUNCTIONS = "slp.functions";

	public static final Object POSTFILTERS = "slp.postfilters";

	public static final Object DOCTRANSFORMERS = "slp.docTransformers";
	
	public static final Object CACHEDRESULTS = "slp.cachedResults";

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	String packagesToScan = "";

	PackageScanner packageScanner;
	
	private Map<String, AlbaRequestHandler> requestHandlers;

	private Map<String, CallableFunction> searchComponents;

	private Map<String, SolrPseudoField> pseudoFields;

	private Map<String, DocValuesDynamicValueSource> dynamicFunctions;

	private HashMap<String, CallableFunction> functions;

	private HashMap<String, CallableFunction> postFilters;

	private HashMap<String, CallableFunction> docTransformers;
	
	private Map<String, AlbaResponseWriterBase> responseWriters;

	private Map<Object, CachedResult> cachedResults;



	@Override
	public void init() {
		requestHandlers = new HashMap<String, AlbaRequestHandler>();

		searchComponents = new HashMap<String, CallableFunction>();

		functions = new HashMap<String, CallableFunction>();

		postFilters = new HashMap<String, CallableFunction>();

		pseudoFields = new HashMap<String, SolrPseudoField>();

		dynamicFunctions = new HashMap<String, DocValuesDynamicValueSource>();

		packageScanner = new PackageScanner();

		cachedResults = new HashMap<Object, CachedResult>();

		docTransformers = new HashMap<String, CallableFunction>();
		
		responseWriters = new HashMap<String, AlbaResponseWriterBase>();

		NamedList l = this.getInitParams();

		NamedList<String> packagesToScan = (NamedList<String>) this.getInitParams().get("packagesToScan");

		String packageName = "";

		for (int i = 0; i < packagesToScan.size(); i++) {

			packageName = packagesToScan.getVal(i);

			//loadPlugins(packageName);

			loadPlugins2(packageName);
			

		}


	}

	private void loadPlugins2(String packageName) {
		// TODO Auto-generated method stub

		List<Class<?>> classes = packageScanner.scanPackage(packageName, null);

		if (classes == null) {
			logger.error("nothing to load for the package " + packageName + ". are jars present?");
			return;
		}

		for (Class<?> c : classes) {
			
				loadMethods(c, c.getAnnotation(AlbaPlugin.class), functions);

			AlbaPlugin sdpAnnotation = (AlbaPlugin) c.getAnnotation(AlbaPlugin.class);


			if (sdpAnnotation != null) {

		
				Constructor constructor;
				try {
					constructor = c.getConstructor(null);
					Object o = constructor.newInstance(null);

					for (Method m : o.getClass().getMethods()) {
						

						FunctionQuery sdf = (FunctionQuery) m.getAnnotation(FunctionQuery.class);
						if (sdf != null) {
							CallableFunction cf = new CallableFunction(sdf.name(), o, m, m.getReturnType());
							functions.put(sdf.name(), cf);
						}

						PostFilter sdpf = (PostFilter) m.getAnnotation(PostFilter.class);
						if (sdpf != null) {
							CallableFunction cf = new CallableFunction(sdpf.name(), o, m, m.getReturnType());
							postFilters.put(sdpf.name(), cf);
						}

						DocTransformer dt = (DocTransformer) m.getAnnotation(DocTransformer.class);
						if (dt != null) {
							CallableFunction cf = new CallableFunction(dt.name(), o, m, m.getReturnType());
							docTransformers.put(dt.name(), cf);
						}
						
						
						SolrSearchComponent searchComponentAnnotation = (SolrSearchComponent) m.getAnnotation(SolrSearchComponent.class);
						if (searchComponentAnnotation != null) {
							
							CallableFunction cf = new CallableFunction(searchComponentAnnotation.value(), o, m, m.getReturnType());
							
							Parameter params[] = m.getParameters();
							if (params.length < 2) {
								logger.error("Methods annotated with SolrSearchComponents must accept at least two params: SolrQueryRequest and SolrQueryResponse.");
							} else {
								if ((params[0].getType() != SolrQueryRequest.class) ||
								    (params[0].getType() != SolrQueryRequest.class)) {
									logger.error("Methods annotated with SolrSearchComponents must accept at least two params: SolrQueryRequest and SolrQueryResponse.");												
								} else {
									searchComponents.put(searchComponentAnnotation.value(), cf);
									
								}
							}
							
						}
						
						
						SolrRequestHandler requestHandlerComponent = (SolrRequestHandler) m.getAnnotation(SolrRequestHandler.class);
						if (requestHandlerComponent != null) {
							
							CallableFunction cf = new CallableFunction(requestHandlerComponent.name(), o, m, m.getReturnType());
							
							Parameter params[] = m.getParameters();
							
							if (params.length < 2) {
								logger.error("Methods annotated with SolrRequestHandler must accept at least two params: SolrQueryRequest and SolrQueryResponse.");
							} else {
								if ((params[0].getType() != SolrQueryRequest.class) ||
								    (params[1].getType() != SolrQueryResponse.class)) {
									logger.error("Methods annotated with SolrRequestHandler must accept at least two params: SolrQueryRequest and SolrQueryResponse.");												
								} else {
									AlbaRequestHandler arh = new AlbaRequestHandler(null);
									arh.setFunction(cf);
									arh.setSectionName(requestHandlerComponent.name());

									requestHandlers.put(requestHandlerComponent.value(), arh);
								}
							}
							
						}
						
						AlbaResponseWriter albaRWAnnotation = (AlbaResponseWriter) m.getAnnotation(AlbaResponseWriter.class);
						if (albaRWAnnotation != null) {
							CallableFunction cf = new CallableFunction(albaRWAnnotation.value(), o, m, m.getReturnType());
							
							Parameter params[] = m.getParameters();
							
							if (params.length < 3) {
								logger.error("Methods annotated with AlbaResponseWriter must accept at least three params: Writer, SolrQueryRequest and SolrQueryResponse.");
							} else {
								if ((params[0].getType() != Writer.class)  ||
								    (params[1].getType() != SolrQueryRequest.class) ||
								    (params[2].getType() != SolrQueryResponse.class)) {
									logger.error("Methods annotated with AlbaResponseWriter must accept at least three params: Writer, SolrQueryRequest and SolrQueryResponse.");												
								} else {
									
									//TODO it should be possible to choose a different class instead of AlbaResponseWriterBase
									AlbaResponseWriterBase responseWriter = new AlbaResponseWriterBase(cf);
								
									responseWriters.put(albaRWAnnotation.value(), responseWriter);
								}
							}
							
						}

					}

				} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					// TODO Auto-generated catch block
					logger.error("error while instantiating " + c.getName(), e);
				}
			} 



		}

	}

	private void loadMethods(Class<?> c, Annotation ann,
			HashMap<String, CallableFunction> functions) {

		//Annotation sdpAnnotation =  (Annotation) c.getAnnotation(a.getClass());



		if (ann != null) {
			Constructor constructor;
			try {
				constructor = c.getConstructor(null);
				Object o = constructor.newInstance(null);

				for (Method m : o.getClass().getMethods()) {
					FunctionQuery sdf = (FunctionQuery) m.getAnnotation(FunctionQuery.class);
					if (sdf != null) {

						// we shouldn't allow to register primitive types as functions,
						// because we wouldn't be able to return null in case of error
						if (m.getReturnType().isPrimitive()) {
							logger.error("function " + m.getName() + " can't return a primitive type. Please use an Object instead.");
						} else {

							CallableFunction cf = new CallableFunction(sdf.name(),o, m, m.getReturnType());

							functions.put(sdf.name(), cf);
						}
					}
				}

			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				logger.error("error while instantiating " + c.getName(), e);
			}
		}

	}

	@Override
	public void process() {

	}


	@Override
	public void prepare() {
		// TODO Auto-generated method stub

		this.getResponseBuilder().req.getContext().put(FUNCTIONS, functions);

		this.getResponseBuilder().req.getContext().put(POSTFILTERS, postFilters);

		this.getResponseBuilder().req.getContext().put(CACHEDRESULTS, cachedResults);

		this.getResponseBuilder().req.getContext().put(DOCTRANSFORMERS, docTransformers);
		
		this.getResponseBuilder().req.getContext().put(SEARCHCOMPONENTS, searchComponents);
		
		for (String s : requestHandlers.keySet()) {
			AlbaRequestHandler arh = requestHandlers.get(s);
			arh.setFunctions(functions);
			this.getResponseBuilder().req.getCore().registerRequestHandler(s, arh);
		}
		
		for (String s : responseWriters.keySet()) {
			AlbaResponseWriterBase arh = responseWriters.get(s);
			// do we need this?? maybe we need for custom transformers?
			//arh.setFunctions(functions);
			this.getResponseBuilder().req.getCore().registerResponseWriter(s, arh);
		}
		
		

	}


	//TODO can be deleted???
	private void loadPlugins(String packageName) {

		List<Class<?>> classes = packageScanner.scanPackage(packageName, ISolrLightPlugin.class);

		for (Class<?> c : classes) {

			SolrLightPlugin slpAnnotation = (SolrLightPlugin) c.getAnnotation(SolrLightPlugin.class);

			if (slpAnnotation != null) {
				logger.error("found annotations for " + c.getName());

				if (SolrPseudoField.class.isAssignableFrom(c)) {
					try {
						SolrPseudoField p = (SolrPseudoField) c.newInstance();
						this.pseudoFields.put(slpAnnotation.name(), p);
					} catch (InstantiationException | IllegalAccessException e) {
						// TODO Auto-generated catch block
						logger.error("unable to load function " + slpAnnotation.name(), e);
					}

					logger.error("loading function values " + c.getName());
				}

				// pseudo fields using DocValues
				if (DocValuesDynamicValueSource.class.isAssignableFrom(c)) {
					try {
						DocValuesDynamicValueSource p = (DocValuesDynamicValueSource) c.newInstance();
						this.dynamicFunctions.put(slpAnnotation.name(), p);
					} catch (InstantiationException | IllegalAccessException e) {
						// TODO Auto-generated catch block
						logger.error("unable to load function " + slpAnnotation.name(), e);
					}

					logger.error("loading doc values function values '" + slpAnnotation.name() + "' - " + c.getName());
				}




			}




		}



	}



	@Override
	public String getDescription() {
		return "AlbaPlugins Loader";
	}


	@Override
	public NamedList<String> getStatistics() {
		NamedList<String> nl = new NamedList<String>();
		nl.add("prova", "abc");
		return nl;
	}




}



