/*
 * Copyright (c) 2009-2012 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

import com.clarkparsia.empire.jena.JenaEntityManagerTestSuite;
import com.clarkparsia.empire.sesametwo.SesameEntityManagerTestSuite;
import com.clarkparsia.empire.test.codegen.CodegenTests;

import org.junit.runners.Suite;

import org.junit.runner.RunWith;

import org.junit.BeforeClass;

/**
 * <p>Empire test suite.</p>
 *
 * @author Michael Grove
 * @since 0.7
 * @version 0.7.1
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({TestRdfConvert.class, TestMisc.class,
					 TestConfig.class, TestDS.class, CodegenTests.class,
					 SesameEntityManagerTestSuite.class, JenaEntityManagerTestSuite.class})
public class EmpireTestSuite {

	@BeforeClass
	public static void beforeClass () {

        // moved to each individual test class, otherwise it's not possible to run them individually
//        String configPath = EmpireTestSuite.class.getResource("/test.empire.config.properties").getPath();
//		System.setProperty("empire.configuration.file", configPath );
//
//		Empire.init(new DefaultEmpireModule(), new OpenRdfEmpireModule(),
//					new JenaEmpireModule(), new TestModule());

		// TODO: tests for TripleSource stuff
		// TODO: tests for persistence injectors
		// TODO: tests for transactions
		// TODO: more failure tests -- badly annotated beans, misconfigured datasources, etc.
		// TODO: 4store & sparql endpoint test configurations
		// TODO: delegating data source tests
		// TODO: named query tests
	}

}
