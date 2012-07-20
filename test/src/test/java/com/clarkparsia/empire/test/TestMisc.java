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

package com.clarkparsia.empire.test;

import com.clarkparsia.empire.EmpireOptions;
import com.clarkparsia.empire.impl.EntityManagerFactoryImpl;
import com.clarkparsia.empire.jena.JenaEmpireModule;
import com.clarkparsia.empire.sesametwo.OpenRdfEmpireModule;
import com.clarkparsia.empire.test.api.TestDataSourceFactory;
import com.clarkparsia.empire.test.util.TestModule;
import com.clarkparsia.empire.util.DefaultEmpireModule;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.openrdf.model.Resource;
import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.impl.ValueFactoryImpl;
import com.clarkparsia.empire.test.api.BaseTestClass;
import com.clarkparsia.empire.SupportsRdfId;
import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.util.EmpireUtil;
import com.clarkparsia.empire.annotation.SupportsRdfIdImpl;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.RdfGenerator;
import com.clarkparsia.empire.annotation.InvalidRdfException;
import com.clarkparsia.openrdf.ExtGraph;
import com.clarkparsia.common.util.PrefixMapping;

import javax.persistence.Persistence;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;

/**
 * <p>Various miscellaneous tests for non-JPA parts of the Empire API.</p>
 *
 * @author Michael Grove
 * @version 0.6.4
 * @since 0.7
 */
public class TestMisc {
	// TODO: could use some more SPI tests

    @BeforeClass
    public static void beforeClass() {
        String configPath = TestMisc.class.getResource("/test.empire.config.properties").getPath();
		System.setProperty("empire.configuration.file", configPath );

		Empire.init( new DefaultEmpireModule(), new OpenRdfEmpireModule(),
                new JenaEmpireModule(), new TestModule() );
    }

	@Test
	public void testInvalidFactory() {
		assertTrue(null == Empire.get().persistenceProvider().createEntityManagerFactory("no a factory",
																						 Collections.singletonMap("factory",
																												  "not a factory class name")));
	}

	@Test
	public void testNoFactory() {
		assertTrue(null == Empire.get().persistenceProvider().createEntityManagerFactory("no a factory",
																						 Collections.emptyMap()));
	}

	@Test
	public void testNotMutableDataSource() {
		assertTrue(null == Empire.get().persistenceProvider().createEntityManagerFactory("not mutable",
																						 Collections.singletonMap("factory",
																												  TestDataSourceFactory.class.getName())));
	}

	@Test
	public void testPersistenceHook() {
		assertEquals(Persistence.createEntityManagerFactory("test-data-source").getClass(), EntityManagerFactoryImpl.class);
		assertEquals(Persistence.createEntityManagerFactory("test-data-source", new HashMap()).getClass(), EntityManagerFactoryImpl.class);
	}

	@Test
	public void testEmpireUtil() throws Exception {
		SupportsRdfId aId = new SupportsRdfIdImpl();

		assertTrue(EmpireUtil.asResource(aId) == null);

		Resource aRes = EmpireUtil.asResource(new SupportsRdfIdImpl(new SupportsRdfId.BNodeKey("asdf")));
		assertTrue(aRes instanceof BNode);
		assertEquals(((BNode)aRes).getID(), "asdf");

		aId = EmpireUtil.asSupportsRdfId(java.net.URI.create("urn:foo"));
		assertTrue(aId.getRdfId() instanceof SupportsRdfId.URIKey);
		assertEquals(aId.getRdfId().value(), java.net.URI.create("urn:foo"));

		assertTrue(EmpireUtil.getNamedGraph("") == null);

		SupportsRdfId.RdfKey aKey = EmpireUtil.asPrimaryKey(new URL("http://example.org"));
		assertTrue(aKey instanceof SupportsRdfId.URIKey);
		assertEquals(aKey.value(), new URL("http://example.org").toURI());

		BNode aAnon = ValueFactoryImpl.getInstance().createBNode("foobar");
		aKey = EmpireUtil.asPrimaryKey(aAnon);
		assertTrue(aKey instanceof SupportsRdfId.BNodeKey);
		assertEquals(aKey.value(), "foobar");
	}

	@Test
	public void testTimesTwo() throws InvalidRdfException {
		TestDoubleImpl obj = new TestDoubleImpl();

		Graph g = RdfGenerator.asRdf(obj);

		assertEquals(1, new ExtGraph(g).getValues(EmpireUtil.asResource(obj), ValueFactoryImpl.getInstance().createURI(PrefixMapping.GLOBAL.uri("test:foo"))).size());
	}

	@MappedSuperclass
	public interface TestDouble extends SupportsRdfId {
		@RdfProperty("test:foo")
		public String getFoo();
	}

	@Entity
	@RdfsClass("http://empire.clarkparsia.com/TestDouble")
	public class TestDoubleImpl extends BaseTestClass implements TestDouble {
		@RdfProperty("test:foo")
		public String getFoo() {
			return "foo";
		}
	}
}
