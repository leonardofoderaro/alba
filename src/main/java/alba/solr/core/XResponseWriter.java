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
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.lucene.document.Document;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.ResponseWriterUtil;
import org.apache.solr.response.ResultContext;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.response.transform.DocTransformer;
import org.apache.solr.response.transform.DocTransformers;
import org.apache.solr.response.transform.TransformContext;
import org.apache.solr.search.DocList;
import org.apache.solr.search.ReturnFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XResponseWriter implements QueryResponseWriter {

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());



	@Override
	public void write(Writer writer, SolrQueryRequest request,
			SolrQueryResponse response) throws IOException {

		SolrDocumentList docList = (SolrDocumentList) response.getValues().get("response");

		SolrDocumentList transformedDocList = new SolrDocumentList();

		DocTransformers transformers = null;

		boolean transformersInitialized = false;

		for (SolrDocument d : docList) {

			if (!transformersInitialized) {

				// if we have only 1 transformer, it will be of class DocTransfomer
				// BUT
				// if we have more than 1 transformer, it will be of class DocTransformers

				Class<?> transfomerClass = response.getReturnFields().getTransformer().getClass();

				if (DocTransformer.class.isAssignableFrom(transfomerClass)) {
					DocTransformer dt = (DocTransformer) response.getReturnFields().getTransformer();
					if (transformers == null) {
						transformers = new DocTransformers();
					}
					transformers.addTransformer(dt);
				} else {
					// found more than 1 transformer, just get them all
					transformers = (DocTransformers)response.getReturnFields().getTransformer();
				}
				
				// we need to to this loop just once
				transformersInitialized = true;

			}
			
			if( transformers != null ) {

				for (int i = 0; i < transformers.size(); i++) {
					DocTransformer dt = transformers.getTransformer(i);

					// ALERT!! functions are seen as transformers, but they aren't!
					// bug or feature?
					if (dt.getClass().isAssignableFrom(DocTransformer.class)) {
						TransformContext context = new TransformContext();
						context.req = request;
						dt.setContext(context);
						dt.transform(d, -1);			
					}
				}

			} 
			
			transformedDocList.add(d);
		}

		List<MyDoc> docs = new DocumentObjectBinder().getBeans(MyDoc.class,docList);

		MyDocs<MyDoc>  mydocs = new MyDocs<MyDoc>();

		mydocs.setDocs(docs);

		JAXBContext jc = null;

		try {
			jc = JAXBContext.newInstance(MyDocs.class);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			logger.error("unable to create jaxb context", e);
		}


		Marshaller m = null;
		try {
			m = jc.createMarshaller();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			logger.error("unable to create marshaller", e);
		}

		try {
			m.marshal(mydocs, writer);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			logger.error("error while marshalling", e);
			writer.close();
		} 
	}

	@Override
	public String getContentType(SolrQueryRequest request,
			SolrQueryResponse response) {
		// TODO Auto-generated method stub
		return "text/xml";
	}

	@Override
	public void init(NamedList args) {
		// TODO Auto-generated method stub

	}
	
	

}
