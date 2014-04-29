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
package org.gdms.data.values;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.gdms.data.types.Type;
import org.gdms.sql.parser.GdmSQLParser;

/**
 * This utility class provides methods to build Gdms {@link Value} objects from the SQL parser.
 *
 * @author Antoine Gourlay
 */
public final class SQLValueFactory {

        private static final Map<String, Integer> TYPES = new HashMap<String, Integer>();

        /**
         * Creates a Value instance with the specified literal and the specified parser code.
         *
         * @param text text containing the value
         * @param type type of literal. SQL parser constant:
         *
         * GdmSQLParser.T_TRUE,
         * GdmSQLParser.NUMBER or
         * GdmSQLParser.QUOTED_STRING
         *
         * @return a Gdms Value
         * @throws IllegalArgumentException if the literal type is not valid
         */
        public static Value createValue(String text, int type) {
                switch (type) {
                        case GdmSQLParser.T_TRUE:
                        case GdmSQLParser.T_FALSE:

                                return ValueFactory.createValue(Boolean.parseBoolean(text));

                        case GdmSQLParser.QUOTED_STRING:

                                return ValueFactory.createValue(text.substring(1, text.length() - 1));

                        case GdmSQLParser.NUMBER:

                                try {
                                        return ValueFactory.createValue(Integer.parseInt(text));
                                } catch (NumberFormatException e) {
                                        try {
                                                return ValueFactory.createValue(Long.parseLong(text));
                                        } catch (NumberFormatException e2) {
                                                try {
                                                        return ValueFactory.createValue(Float.parseFloat(text));
                                                } catch (NumberFormatException e3) {
                                                        return ValueFactory.createValue(Double.parseDouble(text));
                                                }
                                        }

                                }


                        default:
                                throw new IllegalArgumentException("Unexpected literal type: "
                                        + text + "->" + type);
                }
        }

        private static void populateSqlTypes() {
                TYPES.clear();

                // binary
                TYPES.put("binary", Type.BINARY);
                TYPES.put("bytea", Type.BINARY);

                // boolean
                TYPES.put("boolean", Type.BOOLEAN);
                TYPES.put("bool", Type.BOOLEAN);

                // byte
                TYPES.put("byte", Type.BYTE);

                // date
                TYPES.put("date", Type.DATE);

                // double
                TYPES.put("double", Type.DOUBLE);
                TYPES.put("float8", Type.DOUBLE);

                // geometry
                TYPES.put("geometry", Type.GEOMETRY);
                TYPES.put("geometrycollection", Type.GEOMETRYCOLLECTION);
                TYPES.put("linestring", Type.LINESTRING);
                TYPES.put("multilinestring", Type.MULTILINESTRING);
                TYPES.put("multipoint", Type.MULTIPOINT);
                TYPES.put("multipolygon", Type.MULTIPOLYGON);
                TYPES.put("point", Type.POINT);
                TYPES.put("polygon", Type.POLYGON);

                // integer
                TYPES.put("integer", Type.INT);
                TYPES.put("int", Type.INT);
                TYPES.put("int4", Type.INT);

                // long
                TYPES.put("long", Type.LONG);
                TYPES.put("int8", Type.LONG);

                // float
                TYPES.put("float", Type.FLOAT);
                TYPES.put("float4", Type.FLOAT);
                TYPES.put("real", Type.FLOAT);

                // short
                TYPES.put("short", Type.SHORT);
                TYPES.put("int2", Type.SHORT);
                TYPES.put("smallint", Type.SHORT);

                // string
                TYPES.put("string", Type.STRING);
                TYPES.put("text", Type.STRING);

                // time
                TYPES.put("time", Type.TIME);

                // timestamp
                TYPES.put("timestamp", Type.TIMESTAMP);
        }

        /**
         * Gets the typeCode corresponding to a sql String identifier.
         *
         * @param id identifier
         * @return an Gdms typeCode
         * @throws IllegalArgumentException if the
         * <code>id</code> does not correspond to any Gdms type.
         */
        public static int getTypeCodeFromSqlIdentifier(String id) {
                if (TYPES.isEmpty()) {
                        populateSqlTypes();
                }

                String low = id.toLowerCase();
                Integer i = TYPES.get(low);
                if (i == null) {
                        throw new IllegalArgumentException("There is no type called '" + id + "' defined.");
                } else {
                        return i;
                }
        }

        /**
         * Gets the set of all valid SQL types (aliases included).
         *
         * @return a read-only set
         */
        public static Set<String> getValidSQLTypes() {
                if (TYPES.isEmpty()) {
                        populateSqlTypes();
                }

                return Collections.unmodifiableSet(TYPES.keySet());
        }

        /**
         * Private constructor for utility class.
         */
        private SQLValueFactory() {
        }
}
