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
import org.apache.lucene.queries.function.docvalues.IntDocValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegerFunction extends IntDocValues implements IDynamicFunction {
	
	DynamicDocValuesHelper helper;
	
	Logger logger = LoggerFactory.getLogger(this.getClass().getName());


	public IntegerFunction(ValueSource vs, DynamicDocValuesHelper helper) {
		super(vs);
		this.helper = helper;
	
	}

	@Override
	public int intVal(int doc) {
		try {
			Object obj = helper.eval(doc);
			
			if (obj != null) {
				return (int)obj;
			} else {
				return -1;
			}
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			logger.error("error evaluating intVal - ", e);
		}
		return -1;
	}
	
	  @Override
	  public int hashCode() {
	    return (int) helper.hashCode() + (int) (super.hashCode() >>> 32);
	  }
	

}
