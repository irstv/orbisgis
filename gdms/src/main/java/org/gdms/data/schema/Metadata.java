/**
 * The GDMS library (Generic Datasource Management System)
 * is a middleware dedicated to the management of various kinds of
 * data-sources such as spatial vectorial data or alphanumeric. Based
 * on the JTS library and conform to the OGC simple feature access
 * specifications, it provides a complete and robust API to manipulate
 * in a SQL way remote DBMS (PostgreSQL, H2...) or flat files (.shp,
 * .csv...).
 *
 * Gdms is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV FR CNRS 2488
 *
 * This file is part of Gdms.
 *
 * Gdms is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Gdms is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Gdms. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 *
 * or contact directly:
 * info@orbisgis.org
 */
package org.gdms.data.schema;

import org.gdms.data.types.Type;
import org.gdms.driver.DriverException;

/**
 * Defines the schema of a data table
 */
public interface Metadata {

	/**
	 * Gets the number of fields
	 * 
	 * @return
	 * @throws DriverException
	 */
	int getFieldCount() throws DriverException;

	/**
	 * Gets the name of the field.
	 * 
	 * @param fieldId
     * @return
     * @throws DriverException
	 */
	String getFieldName(int fieldId) throws DriverException;

	/**
	 * Gets the index of the field.
	 * 
	 * @param fieldName
     * @return
     * @throws DriverException
	 */
	int getFieldIndex(String fieldName) throws DriverException;

	/**
	 * Gets the type of the field.
	 * 
     * @param fieldId
     * @return
     * @throws DriverException
	 */
	Type getFieldType(int fieldId) throws DriverException;

    /**
     * Gets the Schema associated with this Metadata. Can be null
     *
     * @return the schema
     */
    Schema getSchema();

    /**
     * Gets the names of all the fields in this Metadata object
     * @return a String array, possibly empty, of the field names
     * @throws DriverException
     */
    String[] getFieldNames() throws DriverException;
}