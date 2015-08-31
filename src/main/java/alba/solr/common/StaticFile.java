package alba.solr.common;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

import alba.solr.annotations.AlbaPlugin;
import alba.solr.annotations.SolrRequestHandler;

@AlbaPlugin
public class StaticFile {

	@SolrRequestHandler(name = "static", value = "/foo-init")
	public void getFile(SolrQueryRequest req, 
			SolrQueryResponse rsp) {
		
		
		
	}
	
}
