/**
 * OrbisGIS is a java GIS application dedicated to research in GIScience.
 * OrbisGIS is developed by the GIS group of the DECIDE team of the 
 * Lab-STICC CNRS laboratory, see <http://www.lab-sticc.fr/>.
 *
 * The GIS group of the DECIDE team is located at :
 *
 * Laboratoire Lab-STICC – CNRS UMR 6285
 * Equipe DECIDE
 * UNIVERSITÉ DE BRETAGNE-SUD
 * Institut Universitaire de Technologie de Vannes
 * 8, Rue Montaigne - BP 561 56017 Vannes Cedex
 * 
 * OrbisGIS is distributed under GPL 3 license.
 *
 * Copyright (C) 2007-2014 CNRS (IRSTV FR CNRS 2488)
 * Copyright (C) 2015-2017 CNRS (Lab-STICC UMR CNRS 6285)
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.mapeditor.map.geometryUtils;

import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

import static org.junit.Assert.*;

/**
 *
 * @author Erwan Bocher
 */
public class GeometryCleanTest {

        public WKTReader wKTReader = new WKTReader();

        /**
         * Test remove duplicate coordinates
         * @throws Exception
         */
        @Test
        public void testRemoveDuplicateCoordinates() throws Exception {

                //Test linestring
                Geometry geom = wKTReader.read("LINESTRING(0 8, 1 8 , 3 8,  8  8, 10 8, 20 8)");
                Geometry result = GeometryClean.removeDuplicateCoordinates(geom);
                assertEquals(result.getCoordinates().length, 6);


                //Test duplicate coordinates
                geom = wKTReader.read("LINESTRING(0 8, 1 8 ,1 8, 3 8,  8  8, 10 8, 10 8, 20 8, 0 8)");
                result = GeometryClean.removeDuplicateCoordinates(geom);
                assertEquals(result.getCoordinates().length, 7);

                //Test point
                geom = wKTReader.read("POINT(0 8)");
                result = GeometryClean.removeDuplicateCoordinates(geom);
                assertEquals(result.getCoordinates().length, 1);

                //Test multipoint
                geom = wKTReader.read("MULTIPOINT((0 8), (1 8))");
                result = GeometryClean.removeDuplicateCoordinates(geom);
                assertEquals(result.getCoordinates().length, 2);

                //Test polygon with hole
                geom = wKTReader.read("POLYGON (( 0 0, 10 0, 10 10 , 0 10, 0 0), (2 2, 7 2, 7 2, 7 7, 2 7, 2 2))");
                result = GeometryClean.removeDuplicateCoordinates(geom);
                assertEquals(result.getCoordinates().length, 10);
        }
}
