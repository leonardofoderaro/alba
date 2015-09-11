package alba.components;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;


/*
 * an example search component. probably it can be removed.
 */
public class MySearchComponent extends SearchComponent {

	@Override
	public void prepare(ResponseBuilder rb) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void process(ResponseBuilder rb) throws IOException {
		// TODO Auto-generated method stub
		
		Map<String, String> r = new HashMap<String, String>();
		r.put("a", "b");
		r.put("x", "y");
		
		rb.rsp.add("msc", r);
		
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "xxx";
	}

}
