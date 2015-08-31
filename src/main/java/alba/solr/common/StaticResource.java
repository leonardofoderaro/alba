package alba.solr.common;

import java.util.HashMap;

public class StaticResource {
	private String name;
	
	private HashMap<String, String>  mimeTypes;
	
	//paths are relative to /configs/{coreName}/"
	public StaticResource(String name) {
		this.name = name;
		
		// is there a better way to initialize this map?
		mimeTypes = new HashMap<String, String>();
		mimeTypes.put("html", "text/html;charset=utf-8");
		mimeTypes.put("txt", "text/plain;charset=utf-8");
		mimeTypes.put("js", "application/javascript;charset=utf-8");
		
	}

	public String getName() {
		return this.name;
	}

	public String getContentType() {
		String extension = this.name.replaceAll(".*\\.", "").toLowerCase();
		
		String mimeType = mimeTypes.get(extension);
		
		return mimeType;
		
	}
}
