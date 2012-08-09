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
package org.gdms.drivers;

import java.io.File;
import org.junit.Test;
import org.junit.Before;
import org.gdms.data.DataSource;
import org.gdms.data.DataSourceCreationException;
import org.gdms.data.NoSuchTableException;
import org.gdms.data.types.Constraint;
import org.gdms.data.values.Value;
import org.gdms.driver.DriverException;
import org.gdms.driver.driverManager.DriverLoadException;
import org.gdms.sql.engine.SemanticException;
import org.gdms.sql.engine.ParseException;
import com.vividsolutions.jts.geom.Geometry;
import org.gdms.TestBase;
import org.gdms.data.types.Dimension3DConstraint;
import org.gdms.sql.engine.Engine;

import static org.junit.Assert.*;
import static org.junit.Assume.*;

import org.gdms.TestResourceHandler;

public class ExportTest extends AbstractDBTest {

        @Before
        @Override
        public void setUp() throws Exception {
                super.setUp();
                if (postGisAvailable) {
                        deleteTable(getPostgreSQLSource("pglandcoverfromshp"));
                }
        }

        @Test
        public void testSHP2H22PostgreSQL2SHP_2D() throws Exception {
                assumeTrue(TestBase.postGisAvailable);
                testSHP2H22PostgreSQL2SHP("CALL register('" 
                        + new File(TestResourceHandler.TESTRESOURCES,"p3d.shp").getAbsolutePath() + "', "
                        + "'landcover2000');", "gid", 2);
        }

        @Test
        public void testSHP2H22PostgreSQL2SHP_3D() throws Exception {
                assumeTrue(TestBase.postGisAvailable);
                sm.remove("landcover2000");
                testSHP2H22PostgreSQL2SHP(
                        "CALL register('" + TestResourceHandler.TESTRESOURCES + "p3d.shp', "
                        + "'landcover2000');", "gid", 3);
        }

        private void testSHP2H22PostgreSQL2SHP(String script, String orderField,
                int dim) throws Exception {
                script += "CALL register('h2','', '0', '"
                        + TestResourceHandler.TESTRESOURCES + "backup/h2landcoverfromshp',"
                        + "'sa','','h2landcoverfromshp', 'h2landcoverfromshp');";
                script += "create table h2landcoverfromshp as select * from landcover2000;";

                script += "CALL register('postgresql','127.0.0.1', '5432', "
                        + "'gdms','postgres','postgres','pglandcoverfromshp', 'pglandcoverfromshp');";
                if (dim == 2) {
                        script += "create table pglandcoverfromshp as "
                                + "select * from h2landcoverfromshp;";
                } else {
                        script += "create table pglandcoverfromshp as "
                                + "select constraint3d(the_geom), gid from h2landcoverfromshp;";
                }

                script += "CALL register('" + TestResourceHandler.TESTRESOURCES + "backup/landcoverfrompg.shp', 'res');";
                script += "create table res as select * from pglandcoverfromshp;";
                check(script, orderField);
        }

        private void check(String script, String orderField)
                throws DriverException, ParseException,
                DataSourceCreationException, NoSuchTableException,
                DriverLoadException,
                SemanticException {
                executeGDMSScript(script);

                DataSource dsRes = dsf.getDataSourceFromSQL("select the_geom"
                        + " from res order by " + orderField + ";");
                DataSource ds = dsf.getDataSourceFromSQL("select the_geom"
                        + " from landcover2000 order by " + orderField + ";");
                ds.open();
                dsRes.open();
                Dimension3DConstraint dc1 = (Dimension3DConstraint) ds.getMetadata().getFieldType(0).getConstraint(Constraint.DIMENSION_3D_GEOMETRY);
                Dimension3DConstraint dc2 = (Dimension3DConstraint) dsRes.getMetadata().getFieldType(0).getConstraint(Constraint.DIMENSION_3D_GEOMETRY);
                assertTrue((dc2 == null) || (dc1 == null)
                        || (dc1.getDimension() == dc2.getDimension()));
                for (int i = 0; i < ds.getRowCount(); i++) {
                        Value v1 = ds.getFieldValue(i, 0);
                        Geometry g1 = v1.getAsGeometry();
                        Value v2 = dsRes.getFieldValue(i, 0);
                        Geometry g2 = v2.getAsGeometry();

                        if (dc1.getDimension() == 2) {
                                assertEquals(g1, g2);
                        } else {
                                assertTrue(v1.equals(v2).getAsBoolean());
                        }
                }
                ds.close();
                dsRes.close();
        }

//	public void testSHP2PostgreSQL2H22SHP_2D() throws Exception {
//		testSHP2PostgreSQL2H22SHP("select register('../../datas2tests/shp/"
//				+ "mediumshape2D/landcover2000.shp', " + "'landcover2000');",
//				"gid", 2);
//	}
        @Test
        public void testSHP2PostgreSQL2H22SHP_3D() throws Exception {
                assumeTrue(TestBase.postGisAvailable);
                sm.remove("landcover2000");
                testSHP2PostgreSQL2H22SHP(
                        "CALL register('" + TestResourceHandler.TESTRESOURCES + "p3d.shp', "
                        + "'landcover2000');", "gid", 3);
        }

        private void testSHP2PostgreSQL2H22SHP(String script, String orderField,
                int dim) throws Exception {
                script += "CALL register('postgresql','127.0.0.1', '5432', "
                        + "'gdms','postgres','postgres','pglandcoverfromshp', 'pglandcoverfromshp');";
                script += "create table pglandcoverfromshp as select * from landcover2000;";

                script += "CALL register('h2','', '0', "
                        + "'" + TestResourceHandler.TESTRESOURCES + "backup/h2landcoverfromshp',"
                        + "'sa','','h2landcoverfromshp', 'h2landcoverfromshp');";
                script += "create table h2landcoverfromshp as select * from pglandcoverfromshp;";

                script += "CALL register('" + TestResourceHandler.TESTRESOURCES + "backup/landcoverfrompg.shp', 'res');";
                if (dim == 2) {
                        script += "create table res as "
                                + "select * from h2landcoverfromshp;";
                } else {
                        script += "create table res as "
                                + "select constraint3d(the_geom), gid from h2landcoverfromshp;";
                }
                check(script, orderField);
        }

        private void executeGDMSScript(String script) throws
                DriverException, ParseException, SemanticException {
                Engine.executeScript(script, dsf);
        }
}
