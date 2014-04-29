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
package org.orbisgis.core.geometryUtils;

import org.junit.Test;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.io.WKTReader;

import static org.junit.Assert.*;

/**
 *
 * @author Erwan Bocher
 */
public class GeometryConvertTest {

        public WKTReader wKTReader = new WKTReader();

        /**
         * Convert a geometry into multi-segments
         * @throws Exception
         */
        @Test
        public void testToSegmentsMultiLineString() throws Exception {

                //Test linestring
                Geometry geom = wKTReader.read("LINESTRING(0 8, 1 8 , 3 8,  8  8, 10 8, 20 8)");
                MultiLineString result = GeometryConvert.toSegmentsMultiLineString(geom);
                assertEquals(result.getNumGeometries(), 5);

                //Test linearring
                geom = wKTReader.read("LINEARRING(0 8, 1 8 , 3 8,  8  8, 10 8, 20 8, 0 8)");
                result = GeometryConvert.toSegmentsMultiLineString(geom);
                assertEquals(result.getNumGeometries(), 6);

                //Test duplicate coordinates
                geom = wKTReader.read("LINEARRING(0 8, 1 8 ,1 8, 3 8,  8  8, 10 8, 10 8, 20 8, 0 8)");
                result = GeometryConvert.toSegmentsMultiLineString(geom);
                assertEquals(result.getNumGeometries(), 6);

                //Test point
                geom = wKTReader.read("POINT(0 8)");
                result = GeometryConvert.toSegmentsMultiLineString(geom);
                assertNull(result);

                //Test multipoint
                geom = wKTReader.read("MULTIPOINT((0 8), (1 8))");
                result = GeometryConvert.toSegmentsMultiLineString(geom);
                assertNull(result);
        }
}
