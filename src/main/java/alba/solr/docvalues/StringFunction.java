package alba.solr.docvalues;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.lang.reflect.InvocationTargetException;

import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.StrDocValues;

public class StringFunction extends StrDocValues implements IDynamicFunction {
	
	DynamicDocValuesHelper helper;

	public StringFunction(ValueSource vs, DynamicDocValuesHelper helper) {
		super(vs);
		this.helper = helper;
		
	}

	@Override
	public String strVal(int doc) {
		try {
			return (String)helper.eval(doc);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "eRrrRRrrRoORRRRR";
	}
	
	  @Override
	  public int hashCode() {
	    return super.hashCode() * 29;
	  }
	

}
