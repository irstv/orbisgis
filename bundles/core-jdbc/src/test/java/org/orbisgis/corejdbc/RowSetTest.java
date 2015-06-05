/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.corejdbc;

import com.vividsolutions.jts.geom.Envelope;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.h2spatialext.CreateSpatialExtension;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;
import org.junit.BeforeClass;
import org.junit.Test;
import org.orbisgis.corejdbc.internal.DataManagerImpl;
import org.orbisgis.corejdbc.internal.ReadRowSetImpl;
import org.orbisgis.commons.progress.NullProgressMonitor;

import javax.sql.DataSource;
import javax.sql.RowSetEvent;
import javax.sql.RowSetListener;
import javax.sql.rowset.JdbcRowSet;
import javax.sql.rowset.RowSetFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Nicolas Fortin
 */
public class RowSetTest {
    private static DataSource dataSource;

    @BeforeClass
    public static void tearUp() throws Exception {
        dataSource = SFSUtilities.wrapSpatialDataSource(SpatialH2UT.createDataSource(RowSetTest.class.getSimpleName(), false));
        try(Connection connection = dataSource.getConnection()) {
            CreateSpatialExtension.initSpatialExtension(connection);
        }
    }

    @Test
    public void testReadTableSelectionEnvelope() throws SQLException {
        DataManager dataManager = new DataManagerImpl(dataSource);
        try(Connection connection = dataSource.getConnection();
            Statement st = connection.createStatement()) {
            st.execute("DROP TABLE IF EXISTS PTS");
            st.execute("CREATE TABLE PTS(id integer primary key auto_increment, the_geom POINT)");
            st.execute("INSERT INTO PTS(the_geom) VALUES ('POINT(10 10)'),('POINT(15 15)'),('POINT(20 20)'),('POINT(25 25)')");
            assertEquals(new Envelope(10,25,10,25), ReadTable.getTableSelectionEnvelope(dataManager, "PTS", getRows(1l,2l,3l,4l), new NullProgressMonitor()));
            assertEquals(new Envelope(10,15,10,15), ReadTable.getTableSelectionEnvelope(dataManager, "PTS", getRows(1l, 2l), new NullProgressMonitor()));
            assertEquals(new Envelope(15,20,15,20), ReadTable.getTableSelectionEnvelope(dataManager, "PTS", getRows(2l, 3l), new NullProgressMonitor()));
            assertEquals(new Envelope(25,25,25,25), ReadTable.getTableSelectionEnvelope(dataManager, "PTS", getRows(4l, 4l), new NullProgressMonitor()));
            st.execute("DROP TABLE IF EXISTS PTS");
        }
    }

    private static SortedSet<Long> getRows(Long... rowPk) {
        SortedSet<Long> rows = new TreeSet<>();
        Collections.addAll(rows, rowPk);
        return rows;
    }

    @Test
    public void testRowSetListener() throws SQLException {
        UnitTestRowSetListener rowSetListener = new UnitTestRowSetListener();
        try (
                Connection connection = dataSource.getConnection();
                Statement st = connection.createStatement()) {
            st.execute("drop table if exists test");
            st.execute("create table test (id integer, str varchar(30), flt float)");
            st.execute("insert into test values (42, 'marvin', 10.1010), (666, 'satan', 1/3)");
            try (ReadRowSet rs = new ReadRowSetImpl(dataSource)) {
                rs.setCommand("select * from TEST");
                rs.execute();
                rs.addRowSetListener(rowSetListener);
                assertFalse(rowSetListener.isCursorMoved());
                assertTrue(rs.next());
                assertTrue(rowSetListener.isCursorMoved());
                rowSetListener.setCursorMoved(false);
                assertFalse(rs.previous());
                assertTrue(rowSetListener.isCursorMoved());
                rowSetListener.setCursorMoved(false);
                assertTrue(rs.absolute(2));
                assertTrue(rowSetListener.isCursorMoved());
            }
            st.execute("drop table if exists test");
        }
    }

    @Test
    public void testReadTable() throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement st = connection.createStatement()) {
            st.execute("drop table if exists test");
            st.execute("create table test (id integer, str varchar(30), flt float)");
            st.execute("insert into test values (42, 'marvin', 10.1010), (666, 'satan', 1/3)");
            try (ReadRowSet rs = new ReadRowSetImpl(dataSource)) {
                rs.setCommand("select * from TEST");
                rs.execute();
                assertEquals(1, rs.findColumn("ID"));
                assertEquals(2, rs.findColumn("STR"));
                assertEquals(3, rs.findColumn("FLT"));
                assertTrue(rs.next());
                assertEquals(42, rs.getInt(1));
                assertEquals("marvin", rs.getString(2));
                assertEquals(10.1010, rs.getFloat(3), 1e-6);
                assertTrue(rs.next());
                assertEquals(666, rs.getInt(1));
                assertEquals("satan", rs.getString(2));
                assertEquals(1/3, rs.getFloat(3), 1e-6);
                assertFalse(rs.next());
                assertTrue(rs.previous());
                assertEquals(666, rs.getInt(1));
                assertEquals("satan", rs.getString(2));
                assertEquals(1/3, rs.getFloat(3), 1e-6);
                assertTrue(rs.first());
                assertEquals(42, rs.getInt(1));
                assertEquals("marvin", rs.getString(2));
                assertEquals(10.1010, rs.getFloat(3), 1e-6);
                assertTrue(rs.absolute(1));
                assertEquals(42, rs.getInt(1));
                assertEquals("marvin", rs.getString(2));
                assertEquals(10.1010, rs.getFloat(3), 1e-6);
            }
            st.execute("drop table if exists test");
        }
    }

    @Test
    public void testReadTableWithPk() throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement st = connection.createStatement()) {
            st.execute("drop table if exists test");
            st.execute("create table test (id integer primary key, str varchar(30), flt float)");
            st.execute("insert into test values (42, 'marvin', 5), (666, 'satan', 1/3)");
            st.execute("update test set  flt = 10.1010 where id = 42");
            TableLocation table = TableLocation.parse("TEST");
            try (ReadRowSetImpl rs = new ReadRowSetImpl(dataSource)) {
                rs.initialize(table, "id",new NullProgressMonitor());
                assertTrue(rs.next());
                assertEquals(42, rs.getInt(1));
                assertEquals("marvin", rs.getString(2));
                assertEquals(10.1010, rs.getFloat(3), 1e-6);
                assertTrue(rs.next());
                assertEquals(666, rs.getInt(1));
                assertEquals("satan", rs.getString(2));
                assertEquals(1/3, rs.getFloat(3), 1e-6);
                assertFalse(rs.next());
                assertTrue(rs.previous());
                assertEquals(666, rs.getInt(1));
                assertEquals("satan", rs.getString(2));
                assertEquals(1/3, rs.getFloat(3), 1e-6);
                assertTrue(rs.first());
                assertEquals(42, rs.getInt(1));
                assertEquals("marvin", rs.getString(2));
                assertEquals(10.1010, rs.getFloat(3), 1e-6);
                assertTrue(rs.absolute(1));
                assertEquals(42, rs.getInt(1));
                assertEquals("marvin", rs.getString(2));
                assertEquals(10.1010, rs.getFloat(3), 1e-6);
            }
            st.execute("drop table if exists test");
        }
    }

    @Test
    public void testReversibleRowSet() throws SQLException {
        RowSetFactory factory = new DataManagerImpl(dataSource);
        JdbcRowSet rs = factory.createJdbcRowSet();
        try (
                Connection connection = dataSource.getConnection();
                Statement st = connection.createStatement()) {
                st.execute("drop table if exists test");
                st.execute("create table test (id integer primary key, str varchar(30), flt float)");
                st.execute("insert into test values (42, 'marvin', 10.1010), (666, 'satan', 1/3)");
                rs.setCommand("SELECT * FROM TEST");
                rs.execute();
                assertTrue(rs.next());
                assertEquals(42, rs.getInt(1));
                assertEquals("marvin", rs.getString(2));
                assertEquals(10.1010, rs.getFloat(3), 1e-6);
                assertTrue(rs.next());
                assertEquals(666, rs.getInt(1));
                assertEquals("satan", rs.getString(2));
                assertEquals(1 / 3, rs.getFloat(3), 1e-6);
                assertFalse(rs.next());
                assertTrue(rs.previous());
                assertEquals(666, rs.getInt(1));
                assertEquals("satan", rs.getString(2));
                assertEquals(1 / 3, rs.getFloat(3), 1e-6);
                assertTrue(rs.first());
                assertEquals(42, rs.getInt(1));
                assertEquals("marvin", rs.getString(2));
                assertEquals(10.1010, rs.getFloat(3), 1e-6);
                assertTrue(rs.absolute(1));
                assertEquals(42, rs.getInt(1));
                assertEquals("marvin", rs.getString(2));
                assertEquals(10.1010, rs.getFloat(3), 1e-6);
        }
    }

    @Test
    public void testBatch() throws SQLException {
        RowSetFactory factory = new DataManagerImpl(dataSource);
        JdbcRowSet rs = factory.createJdbcRowSet();
        try (
                Connection connection = dataSource.getConnection();
                Statement st = connection.createStatement()) {
            st.execute("drop table if exists test");
            st.execute("create table test (id integer primary key, y float) as select X, SQRT(X::float) SQ from SYSTEM_RANGE(1, 2000)");
            rs.setCommand("SELECT * FROM TEST");
            rs.execute();
            // Test forward access mode
            for(int i =0; i < 2000; i++) {
                assertTrue(rs.next());
                assertEquals(i+1, rs.getInt("ID"));
                assertEquals(Math.sqrt(i+1), rs.getDouble(2), 1e-6);
            }
            assertFalse(rs.next());

            rs.refreshRow();
            // Test Random access mode
            for(int i : Arrays.asList(ReadRowSetImpl.DEFAULT_FETCH_SIZE + 1, 500, 800, 15, 1850)) {
                assertTrue(rs.absolute(i + 1));
                assertEquals(i+1, rs.getInt("ID"));
                assertEquals(Math.sqrt(i+1), rs.getDouble(2), 1e-6);
            }

            rs.refreshRow();
            // Test backward access mode
            rs.afterLast();
            for(int i = 1999; i >= 0; i--) {
                assertTrue(rs.previous());
                assertEquals(i+1, rs.getInt("ID"));
                assertEquals(Math.sqrt(i+1), rs.getDouble(2), 1e-6);
            }
            assertFalse(rs.previous());
        }
    }

    /**
     * @throws SQLException
     */
    public void testRowNumExtraction() throws SQLException {

        DataManager factory = new DataManagerImpl(dataSource);
        ReadRowSet rs = factory.createReadRowSet();
        try (Connection connection = dataSource.getConnection();
                Statement st = connection.createStatement()) {
            st.execute("drop table if exists test");
            st.execute("create table test (id integer primary key, y float) as select X * 10 id from " +
                    "SYSTEM_RANGE(1, 50)");
            rs.setCommand("SELECT * FROM TEST");
            rs.execute();
            assertEquals(new TreeSet<>(Arrays.asList(1, 5, 10)), rs.getRowNumberFromRowPk(new TreeSet<>(Arrays.asList
                    (10l, 50l, 100l))));
            assertEquals(new TreeSet<>(Arrays.asList(1, 50)), rs.getRowNumberFromRowPk(new TreeSet<>(Arrays
                    .asList(10l, 500l))));
        }
    }

    private static class UnitTestRowSetListener implements RowSetListener {
        private boolean cursorMoved = false;

        public boolean isCursorMoved() {
            return cursorMoved;
        }

        private void setCursorMoved(boolean cursorMoved) {
            this.cursorMoved = cursorMoved;
        }

        @Override
        public void cursorMoved(RowSetEvent rowSetEvent) {
            cursorMoved = true;
        }

        @Override
        public void rowSetChanged(RowSetEvent rowSetEvent) {
        }

        @Override
        public void rowChanged(RowSetEvent rowSetEvent) {
        }
    }

}
