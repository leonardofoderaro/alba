package alba.solr.core;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "results")
@XmlAccessorType (XmlAccessType.FIELD)
public class MyDocs<T> {
	
	@XmlElement(name="city")
	private List<T> docs = null;
	
	public MyDocs() {
		docs = new ArrayList<T>();
	}
	
	public void setDocs(List<T> docs) {
		this.docs = docs;
	}

	
	public List<T> getDocs() {
		return this.docs;
	}
}
