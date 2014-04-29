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
package org.gdms.data;

import org.gdms.data.schema.Metadata;
import org.gdms.data.types.Type;
import org.gdms.data.values.Value;
import org.gdms.driver.DataSet;
import org.gdms.driver.DriverException;

/**
 * Performs the necessary check on the underlying DataSource before doing any action.
 * 
 * This mostly checks that the DataSource is actually open before doing the action.
 */
public final class StatusCheckDecorator extends AbstractDataSourceDecorator {
        private static final String MUST_BE_OPEN = "The data source must be open to call this method";

	public StatusCheckDecorator(DataSource ds) {
		super(ds);
	}

        @Override
	public void addField(String name, Type driverType) throws DriverException {
		if (isOpen()) {
			getDataSource().addField(name, driverType);
		} else {
			throw new ClosedDataSourceException(
					MUST_BE_OPEN);
		}
	}

        @Override
	public String check(int fieldId, Value value) throws DriverException {
		if (isOpen()) {
			return getDataSource().check(fieldId, value);
		} else {
			throw new ClosedDataSourceException(
					MUST_BE_OPEN);
		}
	}

        @Override
	public void commit() throws DriverException, NonEditableDataSourceException {
		if (isOpen()) {
			if (isEditable()) {
				getDataSource().commit();
			} else {
				throw new NonEditableDataSourceException("The source is not editable");
			}
		} else {
			throw new ClosedDataSourceException(
					MUST_BE_OPEN);
		}
	}

        @Override
	public void deleteRow(long rowId) throws DriverException {
		if (isOpen()) {
			getDataSource().deleteRow(rowId);
		} else {
			throw new ClosedDataSourceException(
					MUST_BE_OPEN);
		}
	}

        @Override
	public Metadata getMetadata() throws DriverException {
		if (isOpen()) {
			return getDataSource().getMetadata();
		} else {
			throw new ClosedDataSourceException(
					MUST_BE_OPEN);
		}
	}

        @Override
	public int getFieldIndexByName(String fieldName) throws DriverException {
		if (isOpen()) {
			return getDataSource().getFieldIndexByName(fieldName);
		} else {
			throw new ClosedDataSourceException(
					MUST_BE_OPEN);
		}
	}

        @Override
	public Value getFieldValue(long rowIndex, int fieldId)
			throws DriverException {
		if (isOpen()) {
			return getDataSource().getFieldValue(rowIndex, fieldId);
		} else {
			throw new ClosedDataSourceException(
					MUST_BE_OPEN);
		}
	}

        @Override
	public long getRowCount() throws DriverException {
		if (isOpen()) {
			return getDataSource().getRowCount();
		} else {
			throw new ClosedDataSourceException(
					MUST_BE_OPEN);
		}
	}

        @Override
	public Number[] getScope(int dimension) throws DriverException {
		if (isOpen()) {
			return getDataSource().getScope(dimension);
		} else {
			throw new ClosedDataSourceException(
					MUST_BE_OPEN);
		}
	}

        @Override
	public void insertEmptyRow() throws DriverException {
		if (isOpen()) {
			getDataSource().insertEmptyRow();
		} else {
			throw new ClosedDataSourceException(
					MUST_BE_OPEN);
		}
	}

        @Override
	public void insertEmptyRowAt(long index) throws DriverException {
		if (isOpen()) {
			getDataSource().insertEmptyRowAt(index);
		} else {
			throw new ClosedDataSourceException(
					MUST_BE_OPEN);
		}
	}

        @Override
	public void insertFilledRow(Value[] values) throws DriverException {
		if (isOpen()) {
			getDataSource().insertFilledRow(values);
		} else {
			throw new ClosedDataSourceException(
					MUST_BE_OPEN);
		}
	}

        @Override
	public void insertFilledRowAt(long index, Value[] values)
			throws DriverException {
		if (isOpen()) {
			getDataSource().insertFilledRowAt(index, values);
		} else {
			throw new ClosedDataSourceException(
					MUST_BE_OPEN);
		}
	}

        @Override
	public boolean isModified() {
		if (isOpen()) {
			return getDataSource().isModified();
		} else {
			throw new ClosedDataSourceException(
					MUST_BE_OPEN);
		}
	}

        @Override
	public void redo() throws DriverException {
		if (isOpen()) {
			getDataSource().redo();
		} else {
			throw new ClosedDataSourceException(
					MUST_BE_OPEN);
		}
	}

        @Override
	public void removeField(int index) throws DriverException {
		if (isOpen()) {
			getDataSource().removeField(index);
		} else {
			throw new ClosedDataSourceException(
					MUST_BE_OPEN);
		}
	}

        @Override
	public void saveData(DataSet ds) throws DriverException {
		if (isOpen()) {
			throw new IllegalStateException(
					"The data source must be closed to call this method");
		} else {
			getDataSource().saveData(ds);
		}
	}

        @Override
	public void setFieldName(int index, String name) throws DriverException {
		if (isOpen()) {
			getDataSource().setFieldName(index, name);
		} else {
			throw new ClosedDataSourceException(
					MUST_BE_OPEN);
		}
	}

        @Override
	public void setFieldValue(long row, int fieldId, Value value)
			throws DriverException {
		if (isOpen()) {
			getDataSource().setFieldValue(row, fieldId, value);
		} else {
			throw new ClosedDataSourceException(
					MUST_BE_OPEN);
		}
	}

        @Override
	public void undo() throws DriverException {
		if (isOpen()) {
			getDataSource().undo();
		} else {
			throw new ClosedDataSourceException(
					MUST_BE_OPEN);
		}
	}
}