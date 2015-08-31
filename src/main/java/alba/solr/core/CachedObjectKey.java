package alba.solr.core;

public class CachedObjectKey {

	private String name;
	
	private int docNumber;
	
	public CachedObjectKey(String name, int docNumber) {
		this.name = name;
		this.docNumber = docNumber;
	}
	
	public String getName() {
		return name;
	}

	public int getDocNumber() {
		return docNumber;
	}
	
	@Override
	public boolean equals(Object o) {
		
		CachedObjectKey that = (CachedObjectKey)o;
		if (!CachedObjectKey.class.isAssignableFrom(that.getClass())) {
			return false;
		}
		
		boolean bothNamesNull = ((this.name == null) && (that.getName() == null));
		boolean docNumberEquals = (this.docNumber == that.getDocNumber());
		boolean namesEquals = ((!bothNamesNull) && (this.name.equals(that.getName())));
		
		return (bothNamesNull || (docNumberEquals && namesEquals));
		
	}
	
	@Override
	public int hashCode() {
		return (31 * this.docNumber) + this.name.hashCode();
	}
	
	@Override
	public String toString() {
		return this.name + ": " + this.docNumber;
	}
	
	

}
