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
package org.gdms.sql.function.cluster;

import java.util.List;

import org.gdms.data.values.Value;

public class DataPoint {
	private double components[];

	// constructors
	public DataPoint(final int dimension) {
		this(new double[dimension]);
	}

	public DataPoint(final double[] components) {
		this.components = components;
	}

	public DataPoint(final Value[] fields, final int cellIndexFieldId) {
		this(fields.length - 1);

		for (int fieldId = 0, d = 0; fieldId < fields.length; fieldId++) {
			if (cellIndexFieldId != fieldId) {
				components[d] = fields[fieldId].getAsDouble();
				d++;
			}
		}
	}

	// getters & setters
	public int getDimension() {
		return components.length;
	}

	public double[] getComponents() {
		return components;
	}

	public void setComponents(double[] components) {
		this.components = components;
	}

	// public methods
	public int findClosestCentroidIndex(final List<DataPoint> listOfCentroids) {
		final int nbOfCentroids = listOfCentroids.size();
		double minOfDistances = Double.MAX_VALUE;
		int closestCentroidIndex = -1;

		for (int i = 0; i < nbOfCentroids; i++) {
			final double tmp = euclideanDistanceTo(listOfCentroids.get(i));
			if (minOfDistances > tmp) {
				minOfDistances = tmp;
				closestCentroidIndex = i;
			}
		}
		return closestCentroidIndex;
	}

	public double euclideanDistanceTo(final DataPoint dataPoint) {
		double sumOfSquares = 0;
		for (int i = 0; i < getDimension(); i++) {
			final double tmp = dataPoint.getComponents()[i] - components[i];
			sumOfSquares += tmp * tmp;
		}
		return Math.sqrt(sumOfSquares);
	}

	public DataPoint addDataPoint(final DataPoint dataPoint) {
		for (int i = 0; i < getDimension(); i++) {
			components[i] += dataPoint.getComponents()[i];
		}
		return this;
	}

	public DataPoint divideBy(final double scalarValue) {
		if (0 == scalarValue) {
			throw new ArithmeticException("Division by 0!");
		} else {
			for (int i = 0; i < getDimension(); i++) {
				components[i] /= scalarValue;
			}
		}
		return this;
	}

	public void print() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < getDimension(); i++) {
			sb.append(components[i]).append(
					(i == getDimension() - 1) ? "" : ", ");
		}
	}
}