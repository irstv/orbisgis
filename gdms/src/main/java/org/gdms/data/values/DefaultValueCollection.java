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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gdms.data.types.IncompatibleTypesException;
import org.gdms.data.types.Type;

/**
 * Wrapper for heterogeneous array of values.
 *
 * @author Fernando Gonzalez Cortes
 */
final class DefaultValueCollection extends AbstractValue implements ValueCollection {

        private List<Value> values = new ArrayList<Value>();

        @Override
        public BooleanValue equals(Value value) {
                if (value.isNull()) {
                        return ValueFactory.createNullValue();
                }

                if (!(value instanceof ValueCollection)) {
                        throw new IncompatibleTypesException(value + " is not a ValueCollection.");
                }

                ValueCollection arrayValue = (ValueCollection) value;

                for (int i = 0; i < values.size(); i++) {
                        Value res = values.get(i).equals(arrayValue.get(i));

                        if (res.isNull()) {
                                return ValueFactory.createNullValue();
                        } else if (!res.getAsBoolean()) {
                                return ValueFactory.createValue(false);
                        }
                }

                return ValueFactory.createValue(true);
        }

        @Override
        public BooleanValue greater(Value value) {
                if (value.isNull()) {
                        return ValueFactory.createNullValue();
                }

                // check for type
                if (!(value instanceof ValueCollection)) {
                        return super.greater(value);
                }

                ValueCollection v = (ValueCollection) value;
                Value[] vt = v.getValues();

                // check for equal sizes
                if (vt.length != values.size()) {
                        return super.greater(value);
                }

                // check starting from left
                for (int i = 0; i < vt.length; i++) {
                        BooleanValue ret = values.get(i).greater(vt[i]);
                        if (ret.isNull()) {
                                // one of the two is NULL
                                return ValueFactory.createNullValue();
                        } else if (ret.getAsBoolean()) {
                                // v > vt
                                return ValueFactory.createValue(true);
                        } else if (!values.get(i).equals(vt[i]).getAsBoolean()) {
                                // v != vt => v < vt
                                // no need the check the other values
                                return ValueFactory.createValue(false);
                        }
                        // v == vt
                        // we need the check the rest

                }

                // all inner values are equal
                return ValueFactory.createValue(false);
        }

        @Override
        public BooleanValue greaterEqual(Value value) {
                if (value.isNull()) {
                        return ValueFactory.createNullValue();
                }

                // check for type
                if (!(value instanceof ValueCollection)) {
                        return super.greater(value);
                }

                ValueCollection v = (ValueCollection) value;
                Value[] vt = v.getValues();

                // check for equal sizes
                if (vt.length != values.size()) {
                        return super.greater(value);
                }

                // check starting from left
                for (int i = 0; i < vt.length; i++) {
                        // we check for strict comparison
                        // because the equality is a special case: we need to look deeper
                        // in the array.
                        BooleanValue ret = values.get(i).greater(vt[i]);
                        if (ret.isNull()) {
                                // one of the two is NULL
                                return ValueFactory.createNullValue();
                        } else if (ret.getAsBoolean()) {
                                // v > vt
                                return ValueFactory.createValue(true);
                        } else if (!values.get(i).equals(vt[i]).getAsBoolean()) {
                                // v != vt => v < vt
                                // no need the check the other values
                                return ValueFactory.createValue(false);
                        }
                        // v == vt
                        // we need the check the rest

                }

                // all inner values are equal
                return ValueFactory.createValue(true);
        }

        @Override
        public BooleanValue less(Value value) {
                if (value.isNull()) {
                        return ValueFactory.createNullValue();
                }

                // check for type
                if (!(value instanceof ValueCollection)) {
                        return super.greater(value);
                }

                ValueCollection v = (ValueCollection) value;
                Value[] vt = v.getValues();

                // check for equal sizes
                if (vt.length != values.size()) {
                        return super.greater(value);
                }

                // check starting from left
                for (int i = 0; i < vt.length; i++) {
                        BooleanValue ret = values.get(i).less(vt[i]);
                        if (ret.isNull()) {
                                // one of the two is NULL
                                return ValueFactory.createNullValue();
                        } else if (ret.getAsBoolean()) {
                                // v < vt
                                return ValueFactory.createValue(true);
                        } else if (!values.get(i).equals(vt[i]).getAsBoolean()) {
                                // v != vt => v > vt
                                // no need the check the other values
                                return ValueFactory.createValue(false);
                        }
                        // v == vt
                        // we need the check the rest

                }

                // all inner values are equal
                return ValueFactory.createValue(false);
        }

        @Override
        public BooleanValue lessEqual(Value value) {
                if (value.isNull()) {
                        return ValueFactory.createNullValue();
                }

                // check for type
                if (!(value instanceof ValueCollection)) {
                        return super.greater(value);
                }

                ValueCollection v = (ValueCollection) value;
                Value[] vt = v.getValues();

                // check for equal sizes
                if (vt.length != values.size()) {
                        return super.greater(value);
                }

                // check starting from left
                for (int i = 0; i < vt.length; i++) {
                        // we check for strict comparison
                        // because the equality is a special case: we need to look deeper
                        // in the array.
                        BooleanValue ret = values.get(i).less(vt[i]);
                        if (ret.isNull()) {
                                // one of the two is NULL
                                return ValueFactory.createNullValue();
                        } else if (ret.getAsBoolean()) {
                                // v < vt
                                return ValueFactory.createValue(true);
                        } else if (!values.get(i).equals(vt[i]).getAsBoolean()) {
                                // v != vt => v > vt
                                // no need the check the other values
                                return ValueFactory.createValue(false);
                        }
                        // v == vt
                        // we need the check the rest

                }

                // all inner values are equal
                return ValueFactory.createValue(true);
        }

        /**
         * Gets the ith value of the array
         *
         * @param i
         *
         * @return
         */
        @Override
        public Value get(int i) {
                return values.get(i);
        }

        /**
         * Gets the array size
         *
         * @return int
         */
        public int getValueCount() {
                return values.size();
        }

        /**
         * Adds a value to the end of the array
         *
         * @param value
         * value to add
         */
        public void add(Value value) {
                values.add(value);
        }

        @Override
        public int hashCode() {
                int acum = 13 * 7;

                for (int i = 0; i < values.size(); i++) {
                        acum += values.get(i).hashCode();
                }

                return acum;
        }

        /**
         * Sets the values of this ValueCollection
         *
         * @param values
         */
        @Override
        public void setValues(Value[] values) {
                this.values.clear();
                this.values.addAll(Arrays.asList(values));
        }

        /**
         * Gets the Value objects in this ValueCollection
         *
         * @return an array of Value
         */
        @Override
        public Value[] getValues() {
                return values.toArray(new Value[values.size()]);
        }

        @Override
        public String getStringValue(ValueWriter writer) {
                return "Value collection";
        }

        @Override
        public int getType() {
                return Type.COLLECTION;
        }

        @Override
        public byte[] getBytes() {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bytes);
                try {
                        for (int i = 0; i < values.size(); i++) {
                                dos.writeInt(values.get(i).getType());
                                byte[] valueBytes = values.get(i).getBytes();
                                dos.writeInt(valueBytes.length);
                                dos.write(valueBytes);
                        }
                } catch (IOException e) {
                        throw new IllegalStateException(e);
                }
                return bytes.toByteArray();
        }

        public static Value readBytes(byte[] buffer) {
                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
                        buffer));

                ArrayList<Value> ret = new ArrayList<Value>();
                try {
                        while (true) {
                                int valueType = dis.readInt();
                                int size = dis.readInt();
                                byte[] temp = new byte[size];
                                dis.read(temp);
                                ret.add(ValueFactory.createValue(valueType, temp));
                        }
                } catch (EOFException e) {
                        // normal termination
                        // TODO : see dis.readInt() javadoc on why EOFException is thrown
                } catch (IOException e) {
                        throw new IllegalStateException(e);
                }

                ValueCollection valueCollection = new DefaultValueCollection();
                valueCollection.setValues(ret.toArray(new Value[ret.size()]));
                return valueCollection;
        }

        @Override
        public ValueCollection getAsValueCollection() {
                return this;
        }

        @Override
        public int compareTo(Value o) {
                if (o.isNull()) {
                        // by default, NULL FIRST
                        return -1;
                } else if (o instanceof ValueCollection) {
                        Value[] vc = ((ValueCollection) o).getValues();
                        if (vc.length != values.size()) {
                                throw new IllegalArgumentException("Cannot compare two ValueCollection with different"
                                        + " sizes. Found " + vc.length + ", expected " + values.size());
                        }
                        for (int i = 0; i < values.size(); i++) {
                                int c = values.get(i).compareTo(vc[i]);
                                if (c != 0) {
                                        return c;
                                }
                        }
                        return 0;
                } else {
                        return super.compareTo(o);
                }
        }
}
