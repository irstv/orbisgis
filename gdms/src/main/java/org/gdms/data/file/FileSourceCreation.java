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
 * Copyright (C) 2007-2014 IRSTV FR CNRS 2488
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
package org.gdms.data.file;

import java.io.File;

import org.gdms.data.AbstractDataSourceCreation;
import org.gdms.data.DataSourceDefinition;
import org.gdms.data.schema.Metadata;
import org.gdms.driver.Driver;
import org.gdms.driver.DriverException;
import org.gdms.driver.DriverUtilities;
import org.gdms.driver.FileReadWriteDriver;

public class FileSourceCreation extends AbstractDataSourceCreation {

	private File file;

	private Metadata metadata;

	/**
	 * Builds a new FileSourceCreation
	 *
	 * @param file
	 *            Name of the file to create
	 * @param dmd
	 *            Information about the schema of the new source. If null the source is supposed to
         *            exist already.
	 */
	public FileSourceCreation(File file, Metadata dmd) {
		this.file = file;
		this.metadata = dmd;
	}

        @Override
	public DataSourceDefinition create(String tableName) throws DriverException {

                if (metadata != null) {
                        if (!file.exists()) {
                                ((FileReadWriteDriver) getDriver()).createSource(file
                                                .getAbsolutePath(), metadata, getDataSourceFactory());
                        } else {
                                throw new DriverException("File already exists");
                        }
                }

		return new FileSourceDefinition(file, tableName);
	}

	@Override
	protected Driver getDriverInstance() {
		return (FileReadWriteDriver) DriverUtilities.getDriver(
				getDataSourceFactory().getSourceManager().getDriverManager(),
				file);
	}

        /**
         * get the file associated to this.
         * @return
         */
	public File getFile() {
		return file;
	}
}