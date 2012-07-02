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
package org.gdms.sql.function.spatial.geometry.create;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import org.apache.log4j.Logger;
import org.orbisgis.progress.ProgressMonitor;

import org.gdms.data.DataSourceFactory;
import org.gdms.data.schema.DefaultMetadata;
import org.gdms.data.schema.Metadata;
import org.gdms.data.schema.MetadataUtilities;
import org.gdms.data.types.Constraint;
import org.gdms.data.types.Dimension3DConstraint;
import org.gdms.data.types.InvalidTypeException;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DataSet;
import org.gdms.driver.DriverException;
import org.gdms.driver.driverManager.DriverLoadException;
import org.gdms.driver.memory.MemoryDataSetDriver;
import org.gdms.sql.function.FunctionException;
import org.gdms.sql.function.FunctionSignature;
import org.gdms.sql.function.ScalarArgument;
import org.gdms.sql.function.table.AbstractTableFunction;
import org.gdms.sql.function.table.TableArgument;
import org.gdms.sql.function.table.TableDefinition;
import org.gdms.sql.function.table.TableFunctionSignature;

public final class ST_Extrude extends AbstractTableFunction {

        private static final GeometryFactory GF = new GeometryFactory();
        private static final Logger LOG = Logger.getLogger(ST_Extrude.class);

        @Override
        public DataSet evaluate(DataSourceFactory dsf, DataSet[] tables,
                Value[] values, ProgressMonitor pm) throws FunctionException {
                LOG.trace("Evaluating");
                try {
                        final String idFieldName = values[0].getAsString();
                        final String heightFieldName = values[1].getAsString();

                        final DataSet sds = tables[0];
                        int spatialFieldIndex;

                        if (3 == values.length) {
                                // if no spatial's field's name is provided, the default (first)
                                // one is arbitrarily chosen.
                                final String geomFieldName = values[2].toString();
                                spatialFieldIndex = sds.getMetadata().getFieldIndex(geomFieldName);
                        } else {
                                spatialFieldIndex = MetadataUtilities.getSpatialFieldIndex(sds.getMetadata());
                        }
                        final int rowCount = (int) sds.getRowCount();
                        pm.startTask("Extruding", rowCount);

                        final int idFieldIndex = sds.getMetadata().getFieldIndex(idFieldName);
                        final int heightFieldIndex = sds.getMetadata().getFieldIndex(heightFieldName);

                        final MemoryDataSetDriver driver = new MemoryDataSetDriver(
                                getMetadata(null));
                        

                        for (int i = 0; i < rowCount; i++) {
                                // TODO
                                // "sds.getPK(rowIndex)" should replace
                                // "sds.getFieldValue(rowIndex, gidFieldIndex)"

                                if (i >= 100 && i % 100 == 0) {
                                        if (pm.isCancelled()) {
                                                break;
                                        } else {
                                                pm.progressTo(i);
                                        }
                                }

                                final Value gid = ValueFactory.createValue(sds.getFieldValue(
                                        i, idFieldIndex).toString());
                                final double height = sds.getFieldValue(i,
                                        heightFieldIndex).getAsDouble();
                                final Geometry g = sds.getFieldValue(i, spatialFieldIndex).getAsGeometry();

                                if (g instanceof Polygon) {
                                        extrudePolygon(gid, (Polygon) g, height, driver);
                                } else if (g instanceof MultiPolygon) {
                                        final MultiPolygon p = (MultiPolygon) g;
                                        for (int j = 0; j < p.getNumGeometries(); j++) {
                                                extrudePolygon(gid, (Polygon) p.getGeometryN(j),
                                                        height, driver);
                                        }
                                } else {
                                        throw new FunctionException(
                                                "Extrude only (Multi-)Polygon geometries");
                                }
                        }
                        pm.progressTo(rowCount);
                        pm.endTask();
                        return driver;
                } catch (DriverException e) {
                        throw new FunctionException(e);
                } catch (DriverLoadException e) {
                        throw new FunctionException(e);
                }
        }

        private LineString getClockWise(final LineString lineString) {
                final Coordinate c0 = lineString.getCoordinateN(0);
                final Coordinate c1 = lineString.getCoordinateN(1);
                final Coordinate c2 = lineString.getCoordinateN(2);

                if (CGAlgorithms.computeOrientation(c0, c1, c2) == CGAlgorithms.CLOCKWISE) {
                        return lineString;
                } else {
                        return (LineString) lineString.reverse();
                }
        }

        private LineString getCounterClockWise(final LineString lineString) {
                final Coordinate c0 = lineString.getCoordinateN(0);
                final Coordinate c1 = lineString.getCoordinateN(1);
                final Coordinate c2 = lineString.getCoordinateN(2);

                if (CGAlgorithms.computeOrientation(c0, c1, c2) == CGAlgorithms.COUNTERCLOCKWISE) {
                        return lineString;
                } else {
                        return (LineString) lineString.reverse();
                }
        }

        private Polygon getClockWise(final Polygon polygon) {
                final LinearRing shell = GF.createLinearRing(getClockWise(
                        polygon.getExteriorRing()).getCoordinates());
                final int nbOfHoles = polygon.getNumInteriorRing();
                final LinearRing[] holes = new LinearRing[nbOfHoles];
                for (int i = 0; i < nbOfHoles; i++) {
                        holes[i] = GF.createLinearRing(getCounterClockWise(
                                polygon.getInteriorRingN(i)).getCoordinates());
                }
                return GF.createPolygon(shell, holes);
        }

        private Polygon getCounterClockWise(final Polygon polygon) {
                final LinearRing shell = GF.createLinearRing(getCounterClockWise(polygon.getExteriorRing()).getCoordinates());
                final int nbOfHoles = polygon.getNumInteriorRing();
                final LinearRing[] holes = new LinearRing[nbOfHoles];
                for (int i = 0; i < nbOfHoles; i++) {
                        holes[i] = GF.createLinearRing(getClockWise(
                                polygon.getInteriorRingN(i)).getCoordinates());
                }
                return GF.createPolygon(shell, holes);
        }

        private void extrudePolygon(final Value gid, final Polygon polygon,
                final double high, final MemoryDataSetDriver driver)
                throws DriverException {
                Value wallType = ValueFactory.createValue("wall");

                /* exterior ring */
                final LineString shell = getClockWise(polygon.getExteriorRing());
                Value shellHoleId = ValueFactory.createValue((short) -1);
                for (int i = 1; i < shell.getNumPoints(); i++) {
                        final Polygon wall = extrudeEdge(shell.getCoordinateN(i - 1), shell.getCoordinateN(i), high);
                        driver.addValues(new Value[]{gid, shellHoleId, wallType,
                                        ValueFactory.createValue((short) (i - 1)),
                                        ValueFactory.createValue(wall)});
                }

                /* holes */
                final int nbOfHoles = polygon.getNumInteriorRing();
                for (int i = 0; i < nbOfHoles; i++) {
                        final LineString hole = getCounterClockWise(polygon.getInteriorRingN(i));
                        shellHoleId = ValueFactory.createValue((short) i);
                        for (int j = 1; j < hole.getNumPoints(); j++) {
                                final Polygon wall = extrudeEdge(hole.getCoordinateN(j - 1),
                                        hole.getCoordinateN(j), high);

                                driver.addValues(new Value[]{gid, shellHoleId, wallType,
                                                ValueFactory.createValue((short) (j - 1)),
                                                ValueFactory.createValue(wall)});
                        }
                }

                /* floor */
                shellHoleId = ValueFactory.createValue((short) -1);
                wallType = ValueFactory.createValue("floor");
                driver.addValues(new Value[]{gid, shellHoleId, wallType,
                                ValueFactory.createValue((short) 0),
                                ValueFactory.createValue(getClockWise(polygon))});

                /* roof */
                wallType = ValueFactory.createValue("ceiling");

                final LinearRing upperShell = translate(polygon.getExteriorRing(), high);
                final LinearRing[] holes = new LinearRing[nbOfHoles];
                for (int i = 0; i < nbOfHoles; i++) {
                        holes[i] = translate(polygon.getInteriorRingN(i), high);
                }
                final Polygon pp = GF.createPolygon(upperShell, holes);
                driver.addValues(new Value[]{gid, shellHoleId, wallType,
                                ValueFactory.createValue((short) 0),
                                ValueFactory.createValue(getCounterClockWise(pp))});
        }

        private Polygon extrudeEdge(final Coordinate beginPoint,
                Coordinate endPoint, final double high) {
                if (Double.isNaN(beginPoint.z)) {
                        beginPoint.z = 0d;
                }
                if (Double.isNaN(endPoint.z)) {
                        endPoint.z = 0d;
                }

                return GF.createPolygon(GF.createLinearRing(new Coordinate[]{
                                beginPoint,
                                new Coordinate(beginPoint.x, beginPoint.y, beginPoint.z
                                + high),
                                new Coordinate(endPoint.x, endPoint.y, endPoint.z
                                + high), endPoint, beginPoint}), null);
        }

        private LinearRing translate(final LineString ring, final double high) {
                final Coordinate[] src = ring.getCoordinates();
                final Coordinate[] dst = new Coordinate[src.length];
                for (int i = 0; i < src.length; i++) {
                        if (Double.isNaN(src[i].z)) {
                                src[i].z = 0d;
                        }
                        dst[i] = new Coordinate(src[i].x, src[i].y, src[i].z + high);
                }
                return GF.createLinearRing(dst);
        }

        @Override
        public String getName() {
                return "ST_Extrude";
        }

        @Override
        public String getSqlOrder() {
                return "select * from ST_Extrude(table, id, height[, the_geom]);";
        }

        @Override
        public String getDescription() {
                return "Extrude a 2D polygon using a height field value";
        }

        @Override
        public Metadata getMetadata(Metadata[] tables) throws DriverException {
                try {
                        return new DefaultMetadata(new Type[]{
                                        TypeFactory.createType(Type.STRING),
                                        TypeFactory.createType(Type.SHORT),
                                        TypeFactory.createType(Type.STRING),
                                        TypeFactory.createType(Type.SHORT),
                                        TypeFactory.createType(Type.POLYGON, new Constraint[]{
                                                new Dimension3DConstraint(3)})}, new String[]{
                                        "gid", "shellHoleId", "type", "index", "the_geom"});
                } catch (InvalidTypeException e) {
                        throw new DriverException(
                                "InvalidTypeException in metadata instantiation", e);
                }
        }

        public TableDefinition[] geTablesDefinitions() {
                return new TableDefinition[]{TableDefinition.GEOMETRY};
        }

        @Override
        public FunctionSignature[] getFunctionSignatures() {
                return new FunctionSignature[]{
                                new TableFunctionSignature(TableDefinition.GEOMETRY,
                                new TableArgument(TableDefinition.GEOMETRY),
                                ScalarArgument.STRING,
                                ScalarArgument.STRING),
                                new TableFunctionSignature(TableDefinition.GEOMETRY,
                                new TableArgument(TableDefinition.GEOMETRY),
                                ScalarArgument.STRING,
                                ScalarArgument.STRING,
                                ScalarArgument.STRING)
                        };
        }
}
