package alba.solr.core;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.solr.client.solrj.beans.Field;

@XmlRootElement(name="mydoc")
@XmlAccessorType(XmlAccessType.FIELD)
public class MyDoc {
	
	@Field
	public Integer abc;
	
	@Field
	public String label;
	
	@Field
	public String comment;
	
	@Field
	@XmlAttribute(name="id")
	Long id;
	
	@Field
	String uri;
	
	@Field
	String hello;
	
	@XmlElement(name="prova")
	public String getProva() {
		if (label != null) {
			return label.toUpperCase();
		} else {
			return "field 'label' not defined.";
		}
	}
		
}