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
package org.gdms.sql.engine;

import org.gdms.sql.engine.step.parsing.ErrorLocation;

/**
 * This exception denotes that something happened during the parsing of a SQL script.
 *
 * @author Antoine Gourlay
 */
public final class ParseException extends Exception {

        private static final long serialVersionUID = 7891440179352170045L;
        
        private ErrorLocation location;

        /**
         * Creates a new instance of
         * <code>ParseException</code> without detail message.
         */
        public ParseException() {
        }

        /**
         * Constructs an instance of
         * <code>ParseException</code> with the specified detail message.
         *
         * @param msg the detail message.
         */
        public ParseException(String msg) {
                super(msg);
        }
        
        public ParseException(String msg, ErrorLocation location) {
                super(msg);
                this.location = location;
        }

        public ParseException(Throwable cause) {
                super(cause);
        }

        public ParseException(String message, Throwable cause) {
                super(message, cause);
        }
        
        public ParseException(String message, ErrorLocation location, Throwable cause) {
                super(message, cause);
                this.location = location;
        }

        public ErrorLocation getLocation() {
                return location;
        }
}
