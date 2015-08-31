package alba.solr.core;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.SolrQueryResponse;

public class AlbaResponseWriterBase implements QueryResponseWriter {
	
	private CallableFunction function;

	public AlbaResponseWriterBase(CallableFunction cf) {
		this.function = cf;
	}

	@Override
	public void write(Writer writer, SolrQueryRequest request,
			SolrQueryResponse response) throws IOException {
		
		
		Object[] params = new Object[3];
		
		params[0] = writer;
		params[1] = request;
		params[2] = response;
		
		try {
			this.function.getMethod().invoke(this.function.getInstance(), params);
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

}
