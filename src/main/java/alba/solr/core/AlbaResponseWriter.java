package alba.solr.core;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.ResultContext;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.response.transform.DocTransformer;
import org.apache.solr.response.transform.DocTransformers;
import org.apache.solr.response.transform.TransformContext;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.DocList;
import org.apache.solr.search.ReturnFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alba.solr.annotations.ResponseWriter;

public class AlbaResponseWriter implements QueryResponseWriter {

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());


	private IndexSchema schema;

	protected SolrDocumentList docList;


	public static enum SolrDocumentListSerializationStrategy {DEFAULT, CUSTOM};

	private CallableFunction function;
	private SolrDocumentListSerializationStrategy documentListSerializationStrategy;

	private Type customSerializationType;

	public AlbaResponseWriter() {

	}

	public AlbaResponseWriter(CallableFunction cf, SolrDocumentListSerializationStrategy t, Type customSerializationType) {
		this.function = cf;
		this.documentListSerializationStrategy = t;
		this.customSerializationType = customSerializationType;
	}

	@Override
	public void write(Writer writer, SolrQueryRequest request,
			SolrQueryResponse response) throws IOException {

		Object[] params = new Object[4];
		params[0] = writer;
		params[1] = request;
		params[2] = response;

		try {
			params[3] = prepare(request, response);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			logger.error("unable to prepare writer: ", e);
			writer.write("Unable to prepare writer: " + e.getMessage() + " - check your logs for more information");
		}

		callFunction(writer, params);

	}

	private SolrDocumentList prepareSolrDocList(SolrQueryRequest request,
			SolrQueryResponse response) throws IOException {
		//TODO should be place in a constructor?
		this.schema = request.getSchema();

		docList = null;


		//see public final void writeDocuments in TextResponseWriter
		if (response.getValues().get("response").getClass() == ResultContext.class) {
			ResultContext rc = (ResultContext)response.getValues().get("response");

			try {
				docList = getDocuments(rc, response.getReturnFields(), request);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error("unable to get Solr document list", e);
				docList = null;
			}

		} else {
			docList = (SolrDocumentList) response.getValues().get("response");
		}

		SolrDocumentList transformedDocList = new SolrDocumentList();

		DocTransformers transformers = null;

		boolean transformersInitialized = false;

		for (SolrDocument d : docList) {

			if (!transformersInitialized) {

				// if we have only 1 transformer, it will be of class DocTransfomer
				// BUT
				// if we have more than 1 transformer, it will be of class DocTransformers

				if (response.getReturnFields().getTransformer() != null) {
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
		return this.docList;
	}

	public List prepare( SolrQueryRequest request,
			SolrQueryResponse response) throws IOException, ClassNotFoundException {

		prepareSolrDocList(request, response);
		
		switch (this.documentListSerializationStrategy) {
		case DEFAULT:
			return (List) docList;

		case CUSTOM:
			return (List) prepareBindedSolrDocumentList(request, response);
		default: 
			return null;
		}

	}

	private List<?> prepareBindedSolrDocumentList(SolrQueryRequest request,
			SolrQueryResponse response) throws ClassNotFoundException {

		Class<?> beanClass = null;

		// TODO this can be done with a single regex!
		String className = this.customSerializationType.getTypeName().replaceAll("[^<]*<", "");
		className = className.replaceAll(">.*", "");

		beanClass = Class.forName(className);

		List<?> bindedDocs = new DocumentObjectBinder().getBeans(beanClass, docList);


		return bindedDocs;
	}

	private void callFunction(Writer writer, Object[] params) throws IOException {
		try {
			Object obj = this.function.getMethod().invoke(this.function.getInstance(), params);
			
			if (obj != null) {
				ResponseWriter rw = this.function.getMethod().getAnnotation(ResponseWriter.class);
				
				if (rw.responseType().equals("text/xml")) {
					serializeToXML(writer, obj);
				} 
				
				//TODO handle more cases. should they be pluggable?
				
			}
			
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			writer.write(e.getMessage());
		}

	}

	@Override
	public String getContentType(SolrQueryRequest request,
			SolrQueryResponse response) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(NamedList args) {
		// TODO Auto-generated method stub

	}

	// see  public final void writeDocuments(String name, ResultContext res, ReturnFields fields )
	//      in TextResponseWriter
	// TODO REALLY NEED TO TEST THIS PART!
	private SolrDocumentList  getDocuments(ResultContext res, ReturnFields fields, SolrQueryRequest req) throws IOException {
		DocList ids = res.docs;
		TransformContext context = new TransformContext();
		context.query = res.query;
		context.wantsScores = fields.wantsScore() && ids.hasScores();
		context.req = req;

		SolrDocumentList documents = new SolrDocumentList();

		//TODO understand how to deal with scores
		// writeStartDocumentList(name, ids.offset(), ids.size(), ids.matches(), 
		//    context.wantsScores ? new Float(ids.maxScore()) : null );

		DocTransformer transformer = fields.getTransformer();
		context.searcher = req.getSearcher();
		context.iterator = ids.iterator();
		if( transformer != null ) {
			transformer.setContext( context );
		}
		int sz = ids.size();
		Set<String> fnames = fields.getLuceneFieldNames();
		for (int i=0; i<sz; i++) {
			int id = context.iterator.nextDoc();
			Document doc = context.searcher.doc(id, fnames);

			Set<String> fieldNames = this.schema.getFields().keySet();

			SolrDocument d = new SolrDocument();

			for (String fname : fieldNames) {
				Object val =doc.getField(fname);

				if ((val instanceof StoredField) || (val instanceof Field)) {
					Object o = this.schema.getField(fname).getType().toObject((IndexableField) val);

					d.setField(fname, o);
				} 

			}

			if( transformer != null ) {
				transformer.transform( d, id);
			}

			documents.add(d);

		}
		if( transformer != null ) {
			transformer.setContext( null );
		}

		return documents;
	}
	
	
	private void serializeToXML(Writer writer, Object obj) throws IOException {
		
		JAXBContext jc = null;

		try {
			jc = JAXBContext.newInstance(obj.getClass());
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
			m.marshal(obj, writer);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			logger.error("error while marshalling", e);
			writer.close();
		} 
	}


	
	
	/*
	 * 
		List<?> bindedDocs = new DocumentObjectBinder().getBeans(beanClass, docList);
		
		try {
			mydocs = listClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			throw new IOException(e.getMessage());
		}

		for (Method m : mydocs.getClass().getMethods()) {
			if ((m.getParameterCount() == 1) && (m.getParameters()[0].getType() == List.class) && (m.getReturnType().toString().equals("void"))) {
				try {
					m.invoke(mydocs, bindedDocs);
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					// TODO Auto-generated catch block
					throw new IOException("Error invokating method " + m.getName() + ": " + e.getMessage());
				}
			}
		}
		*/


}
