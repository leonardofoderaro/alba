package alba.solr.common;

import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.rest.BaseSolrResource;
import org.apache.solr.rest.ManagedResource;
import org.apache.solr.rest.ManagedResourceStorage.StorageIO;

public class AlbaManagedResource extends ManagedResource{

	protected AlbaManagedResource(String resourceId, SolrResourceLoader loader,
			StorageIO storageIO) throws SolrException {
		super(resourceId, loader, storageIO);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onManagedDataLoadedFromStorage(NamedList<?> managedInitArgs,
			Object managedData) throws SolrException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Object applyUpdatesToManagedData(Object updates) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void doDeleteChild(BaseSolrResource endpoint, String childId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doGet(BaseSolrResource endpoint, String childId) {
		SolrQueryResponse response = endpoint.getSolrResponse();
		
		response.add("ooooooooo", "ppppppp");
		
	}

	

}
