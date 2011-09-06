/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information. OrbisGIS is
 * distributed under GPL 3 license. It is produced by the "Atelier SIG" team of
 * the IRSTV Institute <http://www.irstv.cnrs.fr/> CNRS FR 2488.
 *
 *
 * Team leader : Erwan BOCHER, scientific researcher,
 *
 * User support leader : Gwendall Petit, geomatic engineer.
 *
 * Previous computer developer : Pierre-Yves FADET, computer engineer, Thomas LEDUC,
 * scientific researcher, Fernando GONZALEZ CORTES, computer engineer.
 *
 * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
 *
 * Copyright (C) 2010 Erwan BOCHER, Alexis GUEGANNO, Maxence LAURENT, Antoine GOURLAY
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
 *
 * or contact directly:
 * info@orbisgis.org
 */
package org.gdms.data.values;

import org.gdms.data.types.Type;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import org.gdms.geometryUtils.GeometryTypeUtil;

abstract class DefaultGeometryValue extends AbstractValue implements GeometryValue {

        private Geometry geom;

        DefaultGeometryValue(Geometry g) {
                this.geom = g;
        }

        @Override
        public String getStringValue(ValueWriter writer) {
                return writer.getStatementString(geom);
        }

        @Override
        public int getType() {
                return Type.GEOMETRY;
        }

        public Geometry getGeom() {
                return geom;
        }

        @Override
        public int hashCode() {
                Coordinate coord = geom.getCoordinate();
                return (int) (coord.x + coord.y);
        }

        @Override
        public BooleanValue equals(Value obj) {
                if (obj.getType() == Type.STRING) {
                        return ValueFactory.createValue(obj.getAsString().equals(this.geom.toText()));
                } else {
                        return ValueFactory.createValue(equalsExact(geom, obj.getAsGeometry()));
                }
        }

        private boolean equalsExact(Geometry geom1, Geometry geom2) {
                if (geom1 instanceof GeometryCollection) {
                        GeometryCollection gc1 = (GeometryCollection) geom1;
                        if (geom2 instanceof GeometryCollection) {
                                GeometryCollection gc2 = (GeometryCollection) geom2;
                                for (int i = 0; i < gc1.getNumGeometries(); i++) {
                                        if (!equalsExact(gc1.getGeometryN(i), gc2.getGeometryN(i))) {
                                                return false;
                                        }
                                }
                                return true;
                        } else {
                                return false;
                        }
                } else {
                        if (geom1.getGeometryType().equals(geom2.getGeometryType())) {
                                Coordinate[] coords1 = geom1.getCoordinates();
                                Coordinate[] coords2 = geom2.getCoordinates();
                                if (coords1.length != coords2.length) {
                                        return false;
                                } else {
                                        for (int i = 0; i < coords2.length; i++) {
                                                Coordinate c1 = coords1[i];
                                                Coordinate c2 = coords2[i];
                                                if (c1.equals(c2)) {
                                                        if (Double.isNaN(c1.z)) {
                                                                return Double.isNaN(c2.z);
                                                        } else if (c1.z != c2.z) {
                                                                return false;
                                                        }
                                                } else {
                                                        return false;
                                                }
                                        }

                                        return true;
                                }
                        } else {
                                return false;
                        }
                }
        }

        @Override
        public BooleanValue notEquals(Value value) {
                return (BooleanValue) equals(value).inverse();
        }

        @Override
        public String toString() {
                //As a geometry can be empty, we must check it is not when trying to access
                //the z value of the value returned by getCoordinate. Indeed, getCoordinate
                //will be null, so trying to know z will result in a NullPointerException.
                boolean useZ = useZForToString(geom);
                if (!useZ) {
                        return WKBUtil.getTextWKTWriter2DInstance().write(geom);
                } else {
                        return WKBUtil.getTextWKTWriter3DInstance().write(geom);
                }
        }

        private boolean useZForToString(Geometry geometry) {
                if (geometry.isEmpty()) {
                        return true;
                }
                for (int i = 0; i < geometry.getNumGeometries(); i++) {
                        Geometry tmp = geometry.getGeometryN(i);
                        if (tmp instanceof GeometryCollection && useZForToString(tmp)) {
                                return true;
                        } else if (!tmp.isEmpty()) {
                                return !Double.isNaN(tmp.getCoordinate().z);
                        }
                }
                return false;
        }

        @Override
        public byte[] getBytes() {
                if (GeometryTypeUtil.is25Geometry(geom)) {
                        return WKBUtil.getWKBWriter3DInstance().write(geom);
                } else {
                        return WKBUtil.getWKBWriter2DInstance().write(geom);
                }
        }

        public static Value readBytes(byte[] buffer) {
                try {
                        return ValueFactory.createValue(WKBUtil.getWKBReaderInstance().read(buffer));
                } catch (ParseException e) {
                        throw new IllegalStateException(e);
                }
        }

        @Override
        public Geometry getAsGeometry() {
                return geom;
        }

        public static Value parseString(String text) throws ParseException {
                Geometry readGeometry = WKBUtil.getWKTReaderInstance().read(text);
                if (readGeometry != null) {
                        return ValueFactory.createValue(readGeometry);
                } else {
                        throw new ParseException("Cannot parse geometry: " + text);
                }
        }

        @Override
        public void setValue(Geometry value) {
                this.geom = value;
        }

        @Override
        public Value toType(int typeCode) {
                final int myType = getType();
                // Everything can be cast to geometry,
                // and takes care of POINT, LINESTRING, POLYGON that can only be cast to themselves
                if (typeCode == Type.GEOMETRY || typeCode == myType) {
                        return this;
                }

                switch (typeCode) {
                        case Type.GEOMETRYCOLLECTION:
                                switch (myType) {
                                        case Type.MULTILINESTRING:
                                        case Type.MULTIPOINT:
                                        case Type.MULTIPOLYGON:
                                                return ValueFactory.createValue((GeometryCollection) geom);
                                        case Type.POINT:
                                        case Type.LINESTRING:
                                        case Type.POLYGON:
                                                return ValueFactory.createValue(
                                                        geom.getFactory().createGeometryCollection(new Geometry[]{geom}));
                                        default:
                                                super.toType(typeCode);
                                }

                        // special cases for GEOMETRYCOLLECTION to its subtypes
                        case Type.MULTILINESTRING:
                                if (myType == Type.GEOMETRYCOLLECTION || myType == Type.LINESTRING) {
                                        LineString[] geoms = new LineString[geom.getNumGeometries()];
                                        for (int i = 0; i < geoms.length; i++) {
                                                final Geometry geometryN = geom.getGeometryN(i);
                                                if ((geometryN instanceof LineString)) {
                                                        geoms[i] = (LineString) geometryN;
                                                } else {
                                                        // error message is handled above.
                                                        return super.toType(typeCode);
                                                }
                                        }
                                        return ValueFactory.createValue(geom.getFactory().createMultiLineString(geoms));
                                }
                        case Type.MULTIPOINT:
                                if (myType == Type.GEOMETRYCOLLECTION || myType == Type.POINT) {
                                        Point[] geoms = new Point[geom.getNumGeometries()];
                                        for (int i = 0; i < geoms.length; i++) {
                                                final Geometry geometryN = geom.getGeometryN(i);
                                                if ((geometryN instanceof Point)) {
                                                        geoms[i] = (Point) geometryN;
                                                } else {
                                                        // error message is handled above.
                                                        return super.toType(typeCode);
                                                }
                                        }
                                        return ValueFactory.createValue(geom.getFactory().createMultiPoint(geoms));
                                }
                        case Type.MULTIPOLYGON:
                                if (myType == Type.GEOMETRYCOLLECTION || myType == Type.POLYGON) {
                                        Polygon[] geoms = new Polygon[geom.getNumGeometries()];
                                        for (int i = 0; i < geoms.length; i++) {
                                                final Geometry geometryN = geom.getGeometryN(i);
                                                if ((geometryN instanceof Polygon)) {
                                                        geoms[i] = (Polygon) geometryN;
                                                } else {
                                                        // error message is handled above.
                                                        return super.toType(typeCode);
                                                }
                                        }
                                        return ValueFactory.createValue(geom.getFactory().createMultiPolygon(geoms));
                                }
                        default:
                                return super.toType(typeCode);
                }
        }
}
