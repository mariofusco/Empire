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

package com.clarkparsia.empire;

/**
 * <p>Catch-all class for global Empire options and configuration</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @version 0.7
 */
public class EmpireOptions {

	/**
	 * Whether or not to force strong typing of literals during I/O from the database.  When this is true, literals
	 * written to the database will always contain a datatype, and on input are expected to have a datatype or else
	 * the conversion will fail.  When false, datatype information will be ignored both during reads and writes.
	 * The recommended value is true because that will give the most accurate conversions, and allow the most
	 * appropriate design of your Java beans, but if you are using 3rd party data which does not use datatypes
	 * disabling this mode can be useful.  The default value is true.
	 */
    public static boolean STRONG_TYPING = true;

	/**
	 * Flag to enable whether or not language tags are used when setting the values for fields from RDF string literals.
	 * By specifying a language on the {@link com.clarkparsia.empire.annotation.RdfProperty} and enabling this mode,
	 * only literal values with the specified language will be considered for valid values for a field.
	 */
	public static boolean ENABLE_LANG_AWARE = false;
}
