/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
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
package org.orbisgis.core.jdbc;

import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.utilities.TableLocation;
import org.junit.BeforeClass;
import org.junit.Test;
import org.orbisgis.core.ReadRowSetImpl;
import org.orbisgis.core.api.ReversibleRowSet;

import javax.sql.DataSource;
import javax.sql.RowSet;
import javax.sql.RowSetEvent;
import javax.sql.RowSetListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

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
        dataSource = SpatialH2UT.createDataSource("ReversibleRowSetTest", true);
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
            try (RowSet rs = new ReadRowSetImpl(dataSource, TableLocation.parse("test"))) {
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
            try (RowSet rs = new ReadRowSetImpl(dataSource, TableLocation.parse("test"))) {
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