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

import org.gdms.data.types.IncompatibleTypesException;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;

/**
 * Wrapper value for boolean.
 * 
 * @author Fernando Gonzalez Cortes
 */
final class DefaultBooleanValue extends AbstractValue implements BooleanValue {

        private static final String NOTBOOLEAN = "The specified value is not a boolean:";
        private boolean value;

        /**
         * Creates a new BooleanValue object.
         *
         * @param value boolean value which will be contained in this object
         */
        DefaultBooleanValue(boolean value) {
                this.value = value;
        }

        /**
         * Creates a new BooleanValue object with UNKNOWN value.
         */
        DefaultBooleanValue() {
        }

        /**
         * Test if this and values contain the same boolean value.
         *
         * @param value the value to be compared to.
         * @return a BooleanValue which contains true if this and value contains the same boolean value
         * @throws IncompatibleTypesException if value is not an instance of BooleanValue
         */
        @Override
        public BooleanValue equals(Value value) {
                if (value instanceof BooleanValue) {
                        return ValueFactory.equals(this, (BooleanValue) value);
                } else {
                        throw new IncompatibleTypesException(
                                NOTBOOLEAN
                                + TypeFactory.getTypeName(value.getType()));
                }
        }

        /**
         * Test if this and value are not equal.
         *
         * @param value another BooleanValue
         * @return a BooleanValue that contains true if and only if this and value are different
         * @throws IncompatibleTypesException if value is not an instance of BooleanValue.
         */
        @Override
        public BooleanValue notEquals(Value value) {
                return equals(value).not();
        }

        @Override
        public void setValue(boolean value) {
                this.value = value;
        }

        /**
         * Get the boolean stored in this object.
         *
         * @return
         */
        public boolean getValue() {
                return value;
        }

        @Override
        public String toString() {
                return String.valueOf(value);
        }

        @Override
        public BooleanValue and(Value value) {
                if (value instanceof BooleanValue) {
                        return ValueFactory.and(this, (BooleanValue) value);
                } else {
                        throw new IncompatibleTypesException(
                                NOTBOOLEAN
                                + TypeFactory.getTypeName(value.getType()));
                }
        }

        @Override
        public BooleanValue or(Value value) {
                if (value instanceof BooleanValue) {
                        return ValueFactory.or(this, (BooleanValue) value);
                } else {
                        throw new IncompatibleTypesException(
                                NOTBOOLEAN
                                + TypeFactory.getTypeName(value.getType()));
                }
        }

        @Override
        public int hashCode() {
                return value ? 1234 : 7891;
        }

        @Override
        public String getStringValue(ValueWriter writer) {
                return writer.getStatementString(value);
        }

        @Override
        public Value inverse() {
                return ValueFactory.createValue(!value);
        }

        @Override
        public int getType() {
                return Type.BOOLEAN;
        }

        /**
         * Get the boolean value as a byte.
         *
         * @return A byte table that contain one value : one if ths is true, 0 if this is false.
         */
        @Override
        public byte[] getBytes() {
                return new byte[]{(value) ? (byte) 1 : 0};
        }

        /**
         * Create a new BooleanValue by testing the first element of buffer.
         * If it equals 1, the new Value will be true, falser either
         *
         * @param buffer
         * @return
         * a BooleanValue.
         */
        public static Value readBytes(byte[] buffer) {
                if (buffer.length == 0) {
                        return ValueFactory.createNullValue();
                }
                return ValueFactory.createValue(buffer[0] == 1);
        }

        /**
         * {@inheritDoc }
         * @return the boolean value stored in this object
         * @throws IncompatibleTypesException
         */
        @Override
        public Boolean getAsBoolean() {
                return value;
        }

        @Override
        public BooleanValue not() {
                return ValueFactory.createValue(!value);
        }

        @Override
        public int compareTo(Value o) {
                if (o.isNull()) {
                        // by default, NULL FIRST
                        return -1;
                } else if (o instanceof BooleanValue) {
                        BooleanValue bo = (BooleanValue) o;
                        // 0 if equal
                        // else 1  if this is true
                        //      -1 if this is false
                        return bo.getAsBoolean() == value ? 0 : (value ? 1 : -1);
                } else {
                        return super.compareTo(o);
                }
        }
}
