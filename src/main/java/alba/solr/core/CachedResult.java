package alba.solr.core;


public class CachedResult {
	private long expireTimestamp;
	
	private Object object;
	
	boolean isExpired() {
		long currentTimestamp = (System.currentTimeMillis() / 1000L);
		boolean isExpired =  (currentTimestamp > expireTimestamp);
		if (isExpired) {
			this.object = null;
		}
		return isExpired;
	}
	
	public CachedResult(Object object, int expireAfter) {
		this.object = object;
		this.expireTimestamp = (System.currentTimeMillis() / 1000L) + expireAfter;
	}
	
	public Object getObject() throws ExpiredCachedObjectException {
		if (isExpired()) {
			throw new ExpiredCachedObjectException();
		} else {
			return this.object;
		}
	}
	
	public void dump() {
		System.out.println("current timestamp " + (System.currentTimeMillis() / 1000L));
		System.out.println("expire on " + this.expireTimestamp);
		System.out.println("expired? " + this.isExpired());
	}
	
	
	public static void main(String[] args) throws InterruptedException {
		String n = "hi";
		
		CachedResult c = new CachedResult(n, 3);
		
		c.dump();
		Thread.sleep(1000);
		c.dump();
		Thread.sleep(1000);
		c.dump();
		Thread.sleep(1000);
		c.dump();
		Thread.sleep(1000);
		c.dump();
		Thread.sleep(1000);
		c.dump();
		Thread.sleep(1000);
		c.dump();
		Thread.sleep(1000);
		
	}
}
