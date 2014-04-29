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
/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2002, Centre for Computational Geography
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.gdms.driver.shapefile;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import org.gdms.driver.DriverException;
import org.gdms.driver.WriteBufferManager;

/**
 * ShapefileWriter allows for the storage of geometries in esris shp format.
 * During writing, an index will also be created. To create a ShapefileWriter,
 * do something like<br>
 * <code>
 *   GeometryCollection geoms;
 *   File shp = new File("myshape.shp");
 *   File shx = new File("myshape.shx");
 *   ShapefileWriter writer = new ShapefileWriter(
 *     shp.getChannel(),shx.getChannel()
 *   );
 *   writer.write(geoms,ShapeType.ARC);
 * </code>
 * This example assumes that each shape in the collection is a LineString.
 *
 * @see org.geotools.data.shapefile.ShapefileDataStore
 * @author jamesm
 * @author aaime
 * @author Ian Schneider
 *
 * @source $URL:
 *         http://svn.geotools.org/geotools/tags/2.3.1/plugin/shapefile/src/org/geotools/data/shapefile/shp/ShapefileWriter.java $
 */
public class ShapefileWriter {

	private FileChannel shpChannel;

	private FileChannel shxChannel;

	private WriteBufferManager shapeBuffer;

	private WriteBufferManager indexBuffer;

	private ShapeHandler handler;

	private ShapeType type;

	private int offset;

	private int cnt;

	/**
	 * Creates a new instance of ShapeFileWriter
	 *
         * @param shpChannel
         * @param shxChannel
         * @throws IOException
	 */
	public ShapefileWriter(FileChannel shpChannel, FileChannel shxChannel)
			throws IOException {
		this.shpChannel = shpChannel;
		this.shxChannel = shxChannel;
		shapeBuffer = new WriteBufferManager(shpChannel);
		indexBuffer = new WriteBufferManager(shxChannel);
	}

	/**
	 * Write the headers for this shapefile including the bounds, shape type,
	 * the number of geometries and the total fileLength (in actual bytes, NOT
	 * 16 bit words).
         *
         * @param bounds
         * @param type
         * @param numberOfGeometries
         * @param fileLength
         * @throws DriverException
         * @throws IOException
         */
	public void writeHeaders(Envelope bounds, ShapeType type,
			int numberOfGeometries, int fileLength) throws DriverException, IOException {
		try {
			handler = type.getShapeHandler();
		} catch (ShapefileException se) {
			throw new DriverException("Error with type " + type, se);
		}
		ShapefileHeader header = new ShapefileHeader();
		header.write(shapeBuffer, type, numberOfGeometries, fileLength / 2,
				bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds
						.getMaxY());
		header.write(indexBuffer, type, numberOfGeometries,
				50 + 4 * numberOfGeometries, bounds.getMinX(),
				bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY());

		offset = 50;
		this.type = type;
		cnt = 0;
	}

	/**
	 * Write a single Geometry to this shapefile. The Geometry must be
	 * compatable with the ShapeType assigned during the writing of the headers.
         *
         * @param g
         * @throws IOException
         */
	public void writeGeometry(Geometry g) throws IOException {
		int length;
		if (g == null) {
			length = 4;
		} else {
			length = handler.getLength(g);
		}

		length /= 2;

		shapeBuffer.order(ByteOrder.BIG_ENDIAN);
		shapeBuffer.putInt(++cnt);
		shapeBuffer.putInt(length);
		shapeBuffer.order(ByteOrder.LITTLE_ENDIAN);
		if (g == null) {
			shapeBuffer.putInt(0);
		} else {
			shapeBuffer.putInt(type.id);
			handler.write(shapeBuffer, g);
		}

		// write to the shx
		indexBuffer.putInt(offset);
		indexBuffer.putInt(length);
		offset += length + 4;
	}

	/**
	 * Close the underlying Channels.
         *
         * @throws IOException 
         */
	public void close() throws IOException {
		indexBuffer.flush();
		shapeBuffer.flush();
		if (shpChannel != null && shpChannel.isOpen()) {
			shpChannel.close();
		}
		if (shxChannel != null && shxChannel.isOpen()) {
			shxChannel.close();
		}
		shpChannel = null;
		shxChannel = null;
		handler = null;
		indexBuffer = null;
		shapeBuffer = null;
	}

}
