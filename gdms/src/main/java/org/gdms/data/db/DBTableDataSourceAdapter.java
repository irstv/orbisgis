package org.gdms.data.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.gdms.data.AlreadyClosedException;
import org.gdms.data.Commiter;
import org.gdms.data.DataSource;
import org.gdms.data.DriverDataSource;
import org.gdms.data.FreeingResourcesException;
import org.gdms.data.InnerDBUtils;
import org.gdms.data.edition.DeleteEditionInfo;
import org.gdms.data.edition.EditionInfo;
import org.gdms.data.edition.PhysicalDirection;
import org.gdms.data.metadata.MetadataUtilities;
import org.gdms.data.types.InvalidTypeException;
import org.gdms.data.values.Value;
import org.gdms.driver.DBDriver;
import org.gdms.driver.DBReadWriteDriver;
import org.gdms.driver.DriverException;

/**
 * Adaptador de la interfaz DBDriver a la interfaz DataSource. Adapta las
 * interfaces de los drivers de base de datos a la interfaz DataSource.
 *
 * @author Fernando Gonzalez Cortes
 */
public class DBTableDataSourceAdapter extends DriverDataSource implements
		Commiter {

	private DBDriver driver;

	private DBSource def;

	protected Connection con;

	private int[] cachedPKIndices;

	/**
	 * Creates a new DBTableDataSourceAdapter
	 *
	 */
	public DBTableDataSourceAdapter(String name, String alias, DBSource def,
			DBDriver driver) {
		super(name, alias);
		this.def = def;
		this.driver = driver;
	}

	public void cancel() throws DriverException, AlreadyClosedException {
		driver.close(con);
		try {
			con.close();
			con = null;
		} catch (SQLException e) {
			throw new DriverException(e);
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	public DBDriver getDriver() {
		return driver;
	}

	/**
	 * Get's a connection to the driver
	 *
	 * @return Connection
	 *
	 * @throws SQLException
	 *             if the connection cannot be established
	 */
	private Connection getConnection() throws SQLException {
		if (con == null) {
			con = driver.getConnection(def.getHost(), def.getPort(), def
					.getDbName(), def.getUser(), def.getPassword());
		}
		return con;
	}

	public void open() throws DriverException {
		super.open();
		try {
			con = getConnection();
			((DBDriver) driver).open(con, def.getTableName());

		} catch (SQLException e) {
			throw new DriverException(e);
		}
	}

	public void commit() throws DriverException, FreeingResourcesException {
		try {
			driver.close(con);
			con.close();
			con = null;
		} catch (SQLException e) {
			throw new FreeingResourcesException(e);
		} catch (DriverException e) {
			throw new FreeingResourcesException(e);
		}
	}

	/**
	 * @throws InvalidTypeException
	 * @throws DriverException
	 * @throws InvalidTypeException
	 * @see org.gdms.data.DataSource#getPKNames()
	 */
	private String[] getPKNames() throws DriverException {
		final String[] ret = new String[getPKCardinality()];

		for (int i = 0; i < ret.length; i++) {
			ret[i] = getPKName(i);
		}

		return ret;
	}

	/**
	 * @see org.gdms.data.DataSource#saveData(org.gdms.data.DataSource)
	 */
	public void saveData(DataSource dataSource) throws DriverException {
		dataSource.open();

		if (driver instanceof DBReadWriteDriver) {
			Connection con;
			try {
				con = getConnection();
				((DBReadWriteDriver) driver).beginTrans(con);
			} catch (SQLException e) {
				throw new DriverException(e);
			}
		}

		for (int i = 0; i < dataSource.getRowCount(); i++) {
			Value[] row = new Value[dataSource.getFieldNames().length];
			for (int j = 0; j < row.length; j++) {
				row[j] = dataSource.getFieldValue(i, j);
			}

			try {
				((DBReadWriteDriver) driver).execute(getConnection(),
						InnerDBUtils.createInsertStatement(def.getTableName(),
								row, dataSource.getFieldNames(), driver));
			} catch (SQLException e) {

				if (driver instanceof DBReadWriteDriver) {
					try {
						Connection con = getConnection();
						((DBReadWriteDriver) driver).rollBackTrans(con);
					} catch (SQLException e1) {
						throw new DriverException(e1);
					}
				}

				throw new DriverException(e);
			}
		}

		if (driver instanceof DBReadWriteDriver) {
			try {
				Connection con = getConnection();
				((DBReadWriteDriver) driver).commitTrans(con);
			} catch (SQLException e) {
				throw new DriverException(e);
			}
		}

		dataSource.cancel();
	}

	public long[] getWhereFilter() throws IOException {
		return null;
	}

	public void commit(List<PhysicalDirection> rowsDirections,
			String[] fieldNames, ArrayList<EditionInfo> schemaActions,
			ArrayList<EditionInfo> editionActions,
			ArrayList<DeleteEditionInfo> deletedPKs, DataSource modifiedSource)
			throws DriverException, FreeingResourcesException {
		try {
			((DBReadWriteDriver) driver).beginTrans(getConnection());
		} catch (SQLException e) {
			throw new DriverException(e);
		}

		String sql = null;
		try {
			for (EditionInfo info : schemaActions) {
				sql = info.getSQL(def.getTableName(), getPKNames(), fieldNames,
						(DBReadWriteDriver) driver);
				((DBReadWriteDriver) driver).execute(con, sql);
			}
			for (DeleteEditionInfo info : deletedPKs) {
				sql = info.getSQL(def.getTableName(), getPKNames(), fieldNames,
						(DBReadWriteDriver) driver);
				((DBReadWriteDriver) driver).execute(con, sql);
			}
			for (EditionInfo info : editionActions) {
				sql = info.getSQL(def.getTableName(), getPKNames(), fieldNames,
						(DBReadWriteDriver) driver);
				if (sql != null) {
					((DBReadWriteDriver) driver).execute(con, sql);
				}
			}
		} catch (SQLException e) {
			try {
				((DBReadWriteDriver) driver).rollBackTrans(getConnection());
			} catch (SQLException e1) {
				throw new DriverException(e1);
			}
			throw new DriverException(e.getMessage() + ":" + sql, e);
		}

		try {
			((DBReadWriteDriver) driver).commitTrans(getConnection());
		} catch (SQLException e) {
			throw new DriverException(e);
		}
	}

	/**
	 * @throws InvalidTypeException
	 * @see org.gdms.data.DataSource#getPrimaryKeys()
	 */
	private int[] getPrimaryKeys() throws DriverException {
		if (cachedPKIndices == null) {
			cachedPKIndices = MetadataUtilities.getPKIndices(getMetadata());
		}
		return cachedPKIndices;
	}

	private String getPKName(int fieldId) throws DriverException {
		int[] fieldsId = getPrimaryKeys();
		return getMetadata().getFieldName(fieldsId[fieldId]);
	}

	private int getPKCardinality() throws DriverException {
		return getPrimaryKeys().length;
	}

}