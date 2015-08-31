# alba

Solr Plugins made simple.

Alba is a small framework aimed to simplify the development of Solr plugins prototypes. 

Each plugin is implemented as an annotated method, and the framework takes care of its execution through reflection.

Consider the following scenarios:

1) you need boost/filter/sort your search results with longer description.
2) you need to modify your search results "on the fly" before they are returned to the client, eg. add/modify/delete some fields
3) you need to serialize your search results in a custom format 

of course, to do it "the right way", each of the above scenario would normally require a different strategy.
for example, the best approach for #1 would be to reindex your data with the new field, while #2 and #3 could be done with an xslt filter, or with a custom Solr plugin, but the whole point is that all of them would require some time.

With the Alba Framework you can play with your index in a fraction of that time.

For example, to	implement the first two:

package my.company.albaplugins;

@Plugin
public class MyAlbaPluginsCollection {

  @DocTransformer(name="transformit")
  public void test(SolrDocument doc) {
    doc.setField("foo", "bar");
  }

  @Function(name="len", description="Returns the length of a string")
  public Integer len(@Param(name="s", description="the string to measure") String s) {
    return s.length();
  }

}

in your solrconfig.xml:

  <searchComponent name="loader" class="alba.solr.core.Loader">
    <lst name="packagesToScan">
       <str>my.company.albaplugins</str>
    </lst>
  </searchComponent>

after deploying the jar in you solr's custom lib path and restarting your collection, you can use your new functions:

http://localhost:8983/solr/mycollection/select?q=*:*&fl=*,len:alba(len,s=description),[alba name="transformit"]

what about the #3 ? 

suppose you have this schema:

<field name="id" type="slong" indexed="true!" docValues="true" />
<field name="title" type="text" indexed="true" docValues="true" />
<field name="author" type="text" indexed="true" docValues="true" />

in your plugin project, you'll need two jaxb annotated classes:

@XmlRootElement(name = "books")
@XmlAccessorType (XmlAccessType.FIELD)
public class Books {

	@XmlElement(name="book")
	private List<Book> books = null;

	public Books() {
		books = new ArrayList<Book>();
	}

	public void setBooks(List<Book> books) {
		this.books = books;
	}

	public List<Book> getBooks() {
		return this.books;
	}
}


@XmlRootElement(name="book")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class Book {

	@XmlAttribute(name="id")
	@Field
	public long id;

	@Field
	public String title;

	@Field
	public String author;
}


then in your browser:
http://localhost:8983/solr/albabooks_shard2_replica1/select?q=*:*&wt=alba&arw.beanClass=Book&arw.listClass=Books

and you'll get something like:
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<books>
  <book id="1">
    <title>Classical Mythology</title>
    <author>Mark P. O. Morford</author>
  </book>
  <book id="4">
    <title>
      Flu: The Story of the Great Influenza Pandemic of 1918 and the Search for the Virus That Caused It
    </title>
    <author>Gina Bari Kolata</author>
  </book>
</books>

Please refer to the project wiki and to the repo albabooks-plugins for a more detailed explanation.







