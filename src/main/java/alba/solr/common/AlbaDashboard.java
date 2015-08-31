package alba.solr.common;

import java.util.Map;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

import alba.solr.annotations.AlbaPlugin;
import alba.solr.annotations.SolrRequestHandler;
import alba.solr.annotations.SolrSearchComponent;
import alba.solr.core.CallableFunction;
import alba.solr.core.Loader;

@AlbaPlugin
@BaseUrl("/")
public class AlbaDashboard {
	

	@SuppressWarnings("unchecked")
	@SolrRequestHandler(name = "albaDashboard", value = "/alba")
	public StaticResource getFile(SolrQueryRequest req, 
			SolrQueryResponse rsp) {
		
		Map<String, CallableFunction> functions =  (Map<String, CallableFunction>)req.getContext().get(Loader.FUNCTIONS);
		
		rsp.add("functions", functions);
		
		return new StaticResource("index.html");
	}
	
	@SuppressWarnings("unchecked")
	@SolrRequestHandler(name = "editor", value = "/editor")
	public StaticResource getEditor(SolrQueryRequest req, 
			SolrQueryResponse rsp) {
		
		Map<String, CallableFunction> functions =  (Map<String, CallableFunction>)req.getContext().get(Loader.FUNCTIONS);
		
		rsp.add("functions", functions);
		
		return new StaticResource("acex.html");
	}
	
	
	@SuppressWarnings("unchecked")
	@SolrSearchComponent("functions")
	public Map<String, CallableFunction> getFunctions(SolrQueryRequest req, SolrQueryResponse rsp) {
		return (Map<String, CallableFunction>)req.getContext().get(Loader.FUNCTIONS);
	}
	
	
	
}
