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

import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.QueryComponent;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alba.solr.searchcomponents.ISolrLightPlugin;

public abstract class DynamicSearchComponent extends SearchComponent implements ISolrLightPlugin {
	

	@SuppressWarnings("rawtypes")
	private NamedList initParams;
	
	
	private ResponseBuilder responseBuilder;
	
	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Override
	public void prepare(ResponseBuilder rb) throws IOException {
		// TODO Auto-generated method stub
		this.responseBuilder = rb;
		prepare();
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "test";
	}
	
	@Override
	public void init(@SuppressWarnings("rawtypes") NamedList args) {
		super.init(args);
		this.initParams = args;
		init();
	}
	
	@Override
	public void process(ResponseBuilder rb) throws IOException {
		this.responseBuilder = rb;
		process();
	}
	
	public NamedList getInitParams() {
		return this.initParams;
	}
	
	public ResponseBuilder getResponseBuilder() {
		return responseBuilder;
	}
	
	public abstract void process();
	
	public abstract void init();
	
	public abstract void prepare();
	

}
