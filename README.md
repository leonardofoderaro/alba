# Alba

Solr Plugins made simple.

Alba is a small framework aimed to simplify the development of Solr plugins prototypes. 

Each plugin is implemented as an annotated method, and the framework takes care of its execution through reflection.

Consider the following scenarios:

1) you need boost/filter/sort your search results with longer description.

2) you need to modify your search results "on the fly" before they are returned to the client, eg. add/modify/delete some fields

3) you need to serialize your search results in a custom XML schema 

of course, to do it "the right way", each of the above scenario would normally require a different strategy.
for example, the best approach for #1 would be to reindex your data with the new field, while #2 and #3 could be done with an xslt filter, or with a custom Solr plugin, but the whole point is that all of them would require some time.

With the Alba Framework you can play with your index in a fraction of that time:

    @AlbaPlugin(name="myPluginsLibrary")
    public class MyPlugins {
        
      @FunctionQuery(name="len", description="returns the length of a string")
      public Integer len(@Param(name="string", description="the string to measure") String s) {
        return s.length();
      }
      
      @DocTransformer(name="helloworld")
      public void hello(SolrDocument doc) {
        doc.setField("message", "Hello, World!");
      }
      
    }

Please refer to the wiki for detailed examples.

Build
==
mvn -Dmaven.skip.test=true package install

copy target/alba-0.1.0-SNAPSHOT.jar /your/solrcloud/path/custom-libs/

[edit](https://github.com/leonardofoderaro/alba/wiki/Setup) your sorlconfig.xml

wget -O - "http://localhost:8983/solr/admin/collections?action=RELOAD&name=yourcollection"

