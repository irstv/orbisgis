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
package org.gdms.data.values;

import org.gdms.data.stream.GeoStream;
import org.gdms.data.types.Type;

/**
 * Main Stream value.
 * 
 * This values mainly embeds a {@link GeoStream} that will be used to retrieve 
 * geographic features stored in an external server (WMS, for instance).
 *
 * @author Antoine Gourlay
 * @author Vincent Dépériers
 */
class DefaultStreamValue extends AbstractValue implements StreamValue {

        private GeoStream geoStream;

        /**
         * Creates a new DefaultStreamValue.
         *
         * @param geoStream
         */
        DefaultStreamValue(GeoStream geoStream) {
                this.geoStream = geoStream;
        }

        @Override
        public BooleanValue equals(Value obj) {
                if (obj instanceof StreamValue) {
                        return ValueFactory.createValue(geoStream.equals(((StreamValue) obj).getAsStream()));
                } else {
                        return ValueFactory.createValue(false);
                }
        }

        @Override
        public int hashCode() {
                return geoStream.hashCode();
        }

        @Override
        public String getStringValue(ValueWriter writer) {
                return geoStream.getStreamSource().toString();
        }

        @Override
        public int getType() {
                return Type.STREAM;
        }

        @Override
        public byte[] getBytes() {
                return geoStream.getStreamSource().toString().getBytes();
        }

        @Override
        public void setValue(GeoStream value) {
                this.geoStream = value;
        }

        @Override
        public GeoStream getAsStream() {
                return this.geoStream;
        }
}
