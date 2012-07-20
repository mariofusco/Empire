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

package com.clarkparsia.empire.test.typing;

import com.clarkparsia.empire.annotation.RdfsClass;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.NamedGraph;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

/**
 * <p></p>
 *
 * @author Blazej Bulka <blazej@clarkparsia.com>
 */
@MappedSuperclass
@Entity
@RdfsClass("urn:clarkparsia.com:empire:test:B")
@NamedGraph(type = NamedGraph.NamedGraphType.Instance)
public abstract class B extends A {
	@RdfProperty("urn:clarkparsia.com:empire:test:propB")
	public abstract String getPropB();
	public abstract void setPropB(String b);
}
