/*
 * Bundle sql-parser is part of the OrbisGIS platform
 *
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
 * sql-parser is distributed under LGPL 3 license.
 *
 * Copyright (C) 2020 CNRS (Lab-STICC UMR CNRS 6285)
 *
 *
 * sql-parser is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * sql-parser is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * sql-parser. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.sql;

import static org.junit.jupiter.api.Assertions.*;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.transform.Definition;
import org.geotools.data.transform.TransformFactory;
import org.geotools.filter.text.cql2.CQLException;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SQLExpressionApplyTest {

    @Test
    public void applyExpression() throws IOException, CQLException {
        String name = "landcover2000.shp";
        ShapefileDataStore shapefile = new ShapefileDataStore(this.getClass().getResource(name));
        SimpleFeatureSource fs = shapefile.getFeatureSource();
        SQLParser sqlParser  = new SQLParser();
        List<Definition> definitions = new ArrayList<Definition>();
        definitions.add(new Definition("THE_GEOM",  sqlParser.toExpression("CENTROID(the_geom)")));
        SimpleFeatureSource transformed = TransformFactory.transform((SimpleFeatureSource) fs, "OUTPUT_TABLE_TEST_F", definitions);
        SimpleFeatureCollection simpleFeatureCollection = transformed.getFeatures();
        SimpleFeatureIterator features = simpleFeatureCollection.features();
        try {
            features.hasNext();
            SimpleFeature feature = features.next();
            Geometry geom = (Geometry) feature.getDefaultGeometry();
            assertTrue(geom instanceof Point);
        } finally {
            features.close();
        }

    }
}
