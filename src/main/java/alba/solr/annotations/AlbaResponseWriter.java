package alba.solr.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AlbaResponseWriter {

	String value();

	String responseType();

}
