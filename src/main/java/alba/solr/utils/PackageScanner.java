package alba.solr.utils;

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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.lucene.queries.function.FunctionValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alba.common.interfaces.ILightFunctionQuery;
import alba.solr.annotations.SolrLightPlugin;
import alba.solr.searchcomponents.AlbaRequestHandler;
import alba.solr.searchcomponents.ISolrLightPlugin;


public class PackageScanner {

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	public List<Class<?>> scanPackage(String packageName, Class c) {
		List<ISolrLightPlugin> plugins = new ArrayList<ISolrLightPlugin>();
		
		URL root = this.getClass().getClassLoader().getResource(packageName.replace(".", "/"));

		if (root == null) {
			logger.error("unable to find classes in " + packageName + ". Please check if jars are present.");
			return null;
		}
		
		List<String> fileNames = getFilenames(root);

		List<Class<?>> classes = getClassesByInterface(packageName, fileNames, c);

		return classes;
	}




	private List<Class<?>> getClassesByInterface(String packageName, List<String> fileNames, Class<?> c) {

		
		if (fileNames == null) {
			logger.error("fileNames can't be null.");
			return null;
		}
		
		List<Class<?>> results = new ArrayList<Class<?>>();



		/* this is a workaround in order to be able to perform the JUnit tests
		 * I suppose Maven instantiate its own class loader (isolated?) 
		 * which -I don't know why- don't see our classes when performing a class.forName
		 * thanks for the solution:
		 * http://stackoverflow.com/questions/1010919/adding-files-to-java-classpath-at-runtime
		 */

		// TODO this shuold REALLY be parametric.......
		File jarToAdd = new File("/opt/solr/solr-5.2.1/custom-libs/pluggablefunctions-0.0.1-SNAPSHOT.jar");

		try {
			new URLClassLoader(((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs()) {
				@Override
				public void addURL(URL url) {
					super.addURL(url);
				}
			}.addURL(jarToAdd.toURI().toURL());
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			logger.error("malformed url", e1);
		}

		// Find classes implementing ICommand.
		for (String fileName : fileNames) {
			String className = fileName.replaceAll(".class$", "").replaceAll("/", ".");
			Class<?> cls = null;


			ClassLoader l = this.getClass().getClassLoader();


			try {
				cls = Class.forName(className);
			} catch (ClassNotFoundException e) {
				try {
					cls = Class.forName(packageName + "." + className, true, ClassLoader.getSystemClassLoader());
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					logger.error("error while instantiating " + className, e);
				}

			}

			//logger.error("checking class " + cls.getName());

			if (c == null || c.isAssignableFrom(cls)) {
				//logger.error("OOOOOOOOOK " + cls.getName());
				results.add(cls);
			}


		}

		return results;

	}




	private List<String> getFilenames(URL root) {

		List<String> results = new ArrayList<String>();

		if (root.getFile().contains("!")) {
			JarFile jarFile;
			try {
				jarFile = new JarFile(root.getFile().replaceAll("!.*", "").replace("file:", ""));
				Enumeration allEntries = jarFile.entries();
				while (allEntries.hasMoreElements()) {
					JarEntry entry = (JarEntry) allEntries.nextElement();
					String name = entry.getName();

					if (name.contains(".class")) {
						results.add(name);
					}


				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			System.out.println("root = " + root.getFile());

			File[] fl = new File(root.getFile()).listFiles();
			for (File f : fl) {
				logger.error("found file " + f.getAbsolutePath());
			}

			// Filter .class files.
			File[] files = new File(root.getFile()).listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".class");
				}
			});

			for (File f : files) {
				results.add(f.getName());
			}

		}

		return results;
	}




}
