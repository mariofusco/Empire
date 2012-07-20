/*
 * Copyright (c) 2009-2010 Clark & Parsia, LLC. <http://www.clarkparsia.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clarkparsia.empire.util.apt;

import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessors;
import com.sun.mirror.apt.RoundCompleteListener;
import com.sun.mirror.apt.RoundCompleteEvent;

import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.ClassDeclaration;

import com.sun.mirror.util.SimpleDeclarationVisitor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.clarkparsia.empire.annotation.RdfsClass;
import com.clarkparsia.common.collect.Iterables2;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;

import javax.persistence.NamedQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;

/**
 * <p>Implementation of an APT-based annotation processor to pull out Annotation information
 * relevant to Empire, such as the classes using the {@link RdfsClass} or {@link NamedQuery} annotations. This
 * information is written in Java Properties format to a file called "empire.config" in the same directory APT
 * is executed from.  This file can then be put into the current dir of the application using Empire</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @version 0.7
 * @see <a href="http://java.sun.com/j2se/1.5.0/docs/guide/apt/GettingStarted.html">Sun APT Docs</a>
 */
public final class EmpireAnnotationProcessorFactory implements AnnotationProcessorFactory {

	/**
	 * A map of annotation class names to the fully qualified class names of classes which have the specific annotation.
	 */
	private Map<String, Collection<String>> mAnnotationClassMap;

	/**
	 * Create a new EmpireAnnotationProcessorFactory
	 */
	public EmpireAnnotationProcessorFactory() {
		mAnnotationClassMap = new HashMap<String, Collection<String>>();
	}

	/**
	 * @inheritDoc
	 */
	public Collection<String> supportedOptions() {
		return Collections.emptyList();
	}

	/**
	 * @inheritDoc
	 */
	public Collection<String> supportedAnnotationTypes() {
		return Arrays.asList(RdfsClass.class.getName(),
							 NamedQuery.class.getName(),
							 NamedQueries.class.getName(),
							 NamedNativeQuery.class.getName(),
							 NamedNativeQueries.class.getName());
	}

	/**
	 * @inheritDoc
	 */
	public AnnotationProcessor getProcessorFor(final Set<AnnotationTypeDeclaration> theDeclarations,
											   final AnnotationProcessorEnvironment theEnvironment) {
		AnnotationProcessor aProcessor;

		if (theDeclarations.isEmpty()) {
			aProcessor = AnnotationProcessors.NO_OP;
		}
		else {
			aProcessor = new SimpleAnnotationProcessor(theEnvironment);
		}

		return aProcessor;
	}

	/**
	 * An AnnotationProcessor which will collect all the classes with the given set of annotations and write them
	 * into a Properties file when the processing is done.
	 */
	private class SimpleAnnotationProcessor implements AnnotationProcessor {

		private AnnotationProcessorEnvironment mEnv;

		private SimpleAnnotationProcessor(final AnnotationProcessorEnvironment theEnv) {
			mEnv = theEnv;

			mEnv.addListener(new RoundCompleteListener() {
				public void roundComplete(final RoundCompleteEvent theEvent) {

					Properties aProps = new Properties();

					for (String aClass : mAnnotationClassMap.keySet()) {
						aProps.setProperty(aClass, Joiner.on(",").join(mAnnotationClassMap.get(aClass)));
					}

					// TODO: configure where this file is written (and what it's named) based on an APT option
					OutputStream aStream = null;
					try {
                        System.err.println( theEnv.getOptions() );
                        if (theEnv.getOptions().containsKey("-d")) {
                            String tgtPath = theEnv.getOptions().get("-d");
                            File tgtDir = new File(tgtPath);
                            if (! tgtDir.exists()) {
                                tgtDir.mkdirs();
                            }
                            aStream = new FileOutputStream(new File(tgtPath+"/empire.apt.config"));
                        } else {
						    aStream = new FileOutputStream(new File("empire.apt.config"));						    
                        }
                        aProps.store(aStream, "Empire Config generated by APT");
					}
					catch (IOException e) {
						System.err.println("There was a failure generating Empire config using APT");

						e.printStackTrace();
					}
					finally {
						if (aStream != null) {
							try {
								aStream.flush();
								aStream.close();
							}
							catch (IOException e) {
								// oh well.
							}
						}
					}
				}
			});
		}

		/**
	 	* @inheritDoc
	 	*/
		public void process() {

			for (String aClass : supportedAnnotationTypes()) {
				Collection<String> aCollection = new HashSet<String>();

				// I think APT can do this in multiple passes, so if we've already collected some information, lets
				// reuse it.
				if (mAnnotationClassMap.containsKey(aClass)) {
					aCollection = mAnnotationClassMap.get(aClass);
				}

				Iterables2.each(mEnv.getDeclarationsAnnotatedWith((AnnotationTypeDeclaration) mEnv.getTypeDeclaration(aClass)),
								new Collector(aCollection));

				mAnnotationClassMap.put(aClass, aCollection);
			}
		}
	}

	/**
	 * Executable for applying a Visitor to a Declaration in order to collect the class declaration information
	 */
	private class Collector implements Predicate<Declaration> {

		/**
		 * The visitor that will be applied
		 */
		private CollectorVisitor mVisitor;

		/**
		 * Create a new Collector
		 * @param theCollection the collector the visitor will append it's information to.
		 */
		private Collector(final Collection<String> theCollection) {
			mVisitor = new CollectorVisitor(theCollection);
		}

		/**
		 * @inheritDoc
		 */
		public boolean apply(Declaration theDeclaration) {
			theDeclaration.accept(mVisitor);
			return true;
		}
	}

	/**
	 * APT Visitor implementation that will inspect the class declarations with the given annotation and collect
	 * the list of classes with the annotation.
	 */
	private static class CollectorVisitor extends SimpleDeclarationVisitor {

		/**
		 * The list of class names with the annotation
		 */
		private Collection<String> mCollection;

		/**
		 * Create a new CollectorVisitor
		 * @param theCollection the collection to add to
		 */
		private CollectorVisitor(final Collection<String> theCollection) {
			mCollection = theCollection;
		}

		/**
	 	* @inheritDoc
	 	*/
		public void visitClassDeclaration(ClassDeclaration theDeclaration) {
			mCollection.add(theDeclaration.getQualifiedName());
		}
	}
}
