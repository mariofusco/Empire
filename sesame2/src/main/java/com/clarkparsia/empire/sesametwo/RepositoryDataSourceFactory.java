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

package com.clarkparsia.empire.sesametwo;

import com.clarkparsia.empire.config.ConfigKeys;
import com.clarkparsia.empire.ds.DataSourceFactory;
import com.clarkparsia.empire.ds.DataSource;
import com.clarkparsia.empire.ds.DataSourceException;
import com.clarkparsia.empire.ds.Alias;

import com.clarkparsia.common.net.NetUtils;

import java.util.Map;
import java.io.File;
import java.io.FileInputStream;

import com.google.common.collect.Maps;
import com.google.common.base.Splitter;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.model.Statement;

/**
 * <p>Implementation of the {@link DataSourceFactory} interface for creating Sesame 2.x Repository objects.</p>
 *
 * @author Michael Grove
 * @since 0.6
 * @version 0.7
 */
@Alias(RepositoryDataSourceFactory.ALIAS)
public final class RepositoryDataSourceFactory implements DataSourceFactory, RepositoryFactoryKeys {
	/**
	 * @inheritDoc
	 */
	public boolean canCreate(final Map<String, Object> theMap) {
		return true;
//		Object aURL = theMap.get(URL);
//		Object aRepo = theMap.get(REPO);
//		Object aFiles = theMap.get(FILES);
//		Object aDir = theMap.get(DIR);
//		Object aPhysRepo = theMap.get(REPO_HANDLE);
//
//		return (aURL != null && aRepo != null) || aFiles != null || aDir != null || (aPhysRepo != null && aPhysRepo instanceof Repository);
	}

	/**
	 * @inheritDoc
	 */
	public DataSource create(final Map<String, Object> theMap) throws DataSourceException {
		if (!canCreate(theMap)) {
			throw new DataSourceException("Invalid configuration map: " + theMap);
		}

        Object aName = theMap.get(ConfigKeys.NAME);
		Object aURL = theMap.get(URL);
		Object aRepo = theMap.get(REPO);
		Object aFiles = theMap.get(FILES);
		Object aDir = theMap.get(DIR);
		Object aPhysRepo = theMap.get(REPO_HANDLE);

		Repository aRepository;

		try {
			if (aPhysRepo != null) {
				aRepository = (Repository) aPhysRepo;
			}
			else if (aURL != null && aRepo != null) {
				aRepository = new HTTPRepository(aURL.toString(), aRepo.toString());

				aRepository.initialize();
			}
			else if (aFiles != null) {
				aRepository = new SailRepository(new MemoryStore());

				try {
					aRepository.initialize();
				
					RepositoryConnection aConn = null;

                    try {
                        aConn = aRepository.getConnection();
                        aConn.setAutoCommit(false);

                        for (String aFile : Splitter.on(',').omitEmptyStrings().trimResults().split(aFiles.toString())) {
                            RDFParser aParser = Rio.createParser(Rio.getParserFormatForFileName(aFile));

                            aParser.setRDFHandler(new SailBuilderRDFHandler(aConn));

                            if (NetUtils.isURL(aFile)) {
                                aParser.parse(new java.net.URL(aFile).openStream(), "");
                            }
                            else {
                                aParser.parse(new FileInputStream(aFile), "");
                            }
                        }

                        aConn.commit();
                    }
                    finally {
                        if (aConn != null) {
                            aConn.close();
                        }
                    }
                }
				catch (Exception e) {
					throw new DataSourceException(e);
				}
			}
			else if (aDir != null) {
				aRepository = new SailRepository(new MemoryStore(new File(aDir.toString())));

				aRepository.initialize();
			}
			else {
				aRepository = new SailRepository(new MemoryStore());
				aRepository.initialize();
			}

			return new RepositoryDataSource(aRepository, theMap.containsKey(QUERY_LANG) && theMap.get(QUERY_LANG).toString().equalsIgnoreCase(LANG_SERQL));
		}
		catch (RepositoryException e) {
			throw new DataSourceException(e);
		}
	}

	/**
	 * Handler implementation which listens to the RDF parsing events and adds each statement to a RepositoryConnection
	 */
	private static class SailBuilderRDFHandler extends RDFHandlerBase {
		private RepositoryConnection mConnection;

		private SailBuilderRDFHandler(final RepositoryConnection theConnection) {
			mConnection = theConnection;
		}

		/**
		 * @inheritDoc
		 */
		@Override
		public void handleStatement(Statement theStmt) {
			try {
				mConnection.add(theStmt.getSubject(), theStmt.getPredicate(), theStmt.getObject(), theStmt.getSubject());
			}
			catch (RepositoryException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
