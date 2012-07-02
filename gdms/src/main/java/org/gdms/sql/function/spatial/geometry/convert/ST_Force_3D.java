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
package org.gdms.sql.function.spatial.geometry.convert;

import org.gdms.data.DataSourceFactory;
import org.gdms.data.types.Type;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.geometryUtils.GeometryEdit;
import org.gdms.sql.function.BasicFunctionSignature;
import org.gdms.sql.function.FunctionException;
import org.gdms.sql.function.FunctionSignature;
import org.gdms.sql.function.ScalarArgument;
import org.gdms.sql.function.spatial.geometry.AbstractScalarSpatialFunction;

/**
 * 
 * @deprecated this function is equivalent to ST_AddZ(the_geom, 0) or ST_AddZ(the_geom, value).
 *   It does not change any metadata, that is up to the target table to set any interesting
 *   constraint on its geometry column.
 */
@Deprecated
public final class ST_Force_3D extends AbstractScalarSpatialFunction {

        @Override
        public Value evaluate(DataSourceFactory dsf, Value... args)
                throws FunctionException {
                if (!args[0].isNull()) {
                        if (args.length == 2) {
                                return ValueFactory.createValue(GeometryEdit.force3D(args[0].getAsGeometry(), 
                                        args[1].getAsDouble(), true), args[0].getCRS());
                        } else {
                                return ValueFactory.createValue(GeometryEdit.force3D(args[0].getAsGeometry(), 0, false), 
                                        args[0].getCRS());
                        }
                }
                return ValueFactory.createNullValue();
        }

        @Override
        public String getDescription() {
                return "Forces the geometries into XYZ mode. A specific z can be added.";
        }

        @Override
        public String getName() {
                return "ST_Force_3D";
        }

        @Override
        public String getSqlOrder() {
                return "select ST_Force_3D(the_geom[, z]) from myTable";
        }

        @Override
        public int getType(int[] argsTypes) {
                return argsTypes[0];
        }
        
        @Override
        public FunctionSignature[] getFunctionSignatures() {
                return new FunctionSignature[] {
                  new BasicFunctionSignature(Type.GEOMETRY, ScalarArgument.GEOMETRY),
                  new BasicFunctionSignature(Type.GEOMETRY, ScalarArgument.GEOMETRY, ScalarArgument.DOUBLE)
                };
        }
}
