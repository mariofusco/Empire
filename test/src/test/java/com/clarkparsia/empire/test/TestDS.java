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

import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.EmpireOptions;
import com.clarkparsia.empire.ds.DataSource;
import com.clarkparsia.empire.ds.DataSourceException;
import com.clarkparsia.empire.ds.DataSourceUtil;
import com.clarkparsia.empire.ds.TripleSource;
import com.clarkparsia.empire.jena.JenaEmpireModule;
import com.clarkparsia.empire.sesametwo.OpenRdfEmpireModule;
import com.clarkparsia.empire.test.util.TestModule;
import com.clarkparsia.empire.test.util.TestUtil;
import com.clarkparsia.empire.util.DefaultEmpireModule;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * <p>Tests for the DataSource APIs</p>
 *
 * @author Michael Grove
 * @version 0.7
 * @since 0.7
 */
public class TestDS {

    @BeforeClass
    public static void beforeClass() {
        String configPath = TestDS.class.getResource("/test.empire.config.properties").getPath();
		System.setProperty("empire.configuration.file", configPath );

		Empire.init( new DefaultEmpireModule(), new OpenRdfEmpireModule(),
                new JenaEmpireModule(), new TestModule() );
    }

	@Test
	public void testTripleSourceCreate() throws DataSourceException {
		Empire.init(new DefaultEmpireModule(), new TestModule(), new OpenRdfEmpireModule());
		
		DataSource aTestSource = TestUtil.createTestSource();

		// this is a test source, so it should not be a TripleSource...

		assertFalse(aTestSource instanceof TripleSource);

		TripleSource aTripleSrc = DataSourceUtil.asTripleSource(aTestSource);

		assertTrue(aTestSource != aTripleSrc);

		Map<String, Object> aMap = new HashMap<String, Object>();
		aMap.put("factory", "sesame");

        try {
            aMap.put("files", new File(TestDS.class.getResource("/data/lite.nasa.nt").toURI()));
        } catch ( URISyntaxException e ) {
            fail( e.getMessage() );
        }

        DataSource aSesameSource = Empire.get().persistenceProvider().createDataSource("test-sesame", aMap);

		aTripleSrc = DataSourceUtil.asTripleSource(aSesameSource);

		// Sesame sources are triple sources
		assertTrue(aSesameSource == aTripleSrc);
	}

	@Test(expected=DataSourceException.class)
	public void testInvalidCreateTripleSource() throws DataSourceException {
		DataSourceUtil.asTripleSource(null);
	}

	@Test @Ignore
	public void testDataSourceOps() {
		// TODO: test DataSourceUtil operations like getType, exists, etc.
	}

	@Test @Ignore
	public void testTripleSourceAccess() {
		// TODO: verify triple store level operations work
	}

	@Test @Ignore
	public void testDelegateSource() {
		// TODO: implement me
	}
}
