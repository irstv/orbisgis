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

import ij.ImagePlus;
import ij.process.*;
import java.io.*;
import org.gdms.data.InitializationException;
import org.gdms.data.types.IncompatibleTypesException;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.grap.io.RasterReader;
import org.grap.model.GeoRaster;
import org.grap.model.GeoRasterFactory;
import org.grap.model.RasterMetadata;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.orbisgis.utils.ByteUtils;

final class DefaultRasterValue extends AbstractValue implements RasterValue {

        /**
         * the length, in bytes, of a RasterMetadata object. Used to be stored efficiently in GDMS, particularly in
         * {@link org.gdms.driver.gdms.GdmsReader}
         */
        private static int rasterHeaderSize = -1;
        private CoordinateReferenceSystem crs;

        static int getHeaderSize() {
                if (rasterHeaderSize == -1) {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        DataOutputStream dos = new DataOutputStream(bos);
                        RasterMetadata rasterMetadata = new RasterMetadata(0, 0, 10, 10, 2, 2);
                        try {
                                writeMetadata(dos, rasterMetadata, 0, 1, ImagePlus.COLOR_256);
                        } catch (IOException e) {
                                throw new InitializationException("Problem initializing raster metadatas", e);
                        }

                        rasterHeaderSize = bos.toByteArray().length;
                }

                return rasterHeaderSize;

        }
        
        //The Geoaster that contains the value.
        private GeoRaster geoRaster;

        /**
         * Package-public constructor
         * @param geoRaster the {@link org.grap.model.GeoRaster} which stores the data
         */
        DefaultRasterValue(GeoRaster geoRaster) {
                this(geoRaster, null);
        }
        
        /**
         * Package-public constructor
         * @param geoRaster the {@link org.grap.model.GeoRaster} which stores the data
         * @param crs the associated crs
         */
        DefaultRasterValue(GeoRaster geoRaster, CoordinateReferenceSystem crs) {
                this.geoRaster = geoRaster;
                this.crs = crs;
        }

        /**
         * Get the type of the Value as a String
         * @param writer
         * @return "Raster"
         */
        @Override
        public String getStringValue(ValueWriter writer) {
                return "Raster";
        }

        /**
         * get the type of the value as a type
         * @return Type.RASTER
         */
        @Override
        public int getType() {
                return Type.RASTER;
        }

        /**
         *
         * @return the HashCode associated to the GeoRaster.
         */
        @Override
        public int hashCode() {
                return 19 * 5 + geoRaster.hashCode();
        }

        @Override
        public BooleanValue equals(Value value) {
                if (value.isNull()) {
                        return ValueFactory.createNullValue();
                }
                
                if (value instanceof RasterValue) {
                        return ValueFactory.createValue(geoRaster.equals(value.getAsRaster()));
                } else {
                        throw new IncompatibleTypesException(
                                "The specified value is not a raster value:"
                                + TypeFactory.getTypeName(value.getType()));
                }
        }

        @Override
        public byte[] getBytes() {
                try {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        DataOutputStream dos = new DataOutputStream(bos);
                        geoRaster.open();
                        // Write metadata
                        RasterMetadata metadata = geoRaster.getMetadata();
                        int imageType = geoRaster.getType();
                        writeMetadata(dos, metadata, geoRaster.getMin(),
                                geoRaster.getMax(), imageType);
                        // Write data
                        int ncols = metadata.getNCols();
                        int nrows = metadata.getNRows();
                        ImageProcessor processor = geoRaster.getImagePlus().getProcessor();
                        switch (imageType) {
                                case ImagePlus.COLOR_256:
                                case ImagePlus.GRAY8:
                                        byte[] bytePixels = new byte[ncols * nrows];
                                        for (int i = 0; i < ncols; i++) {
                                                for (int j = 0; j < nrows; j++) {
                                                        bytePixels[j * ncols + i] = (byte) processor.get(i, j);
                                                }
                                        }
                                        dos.write(bytePixels);
                                        break;
                                case ImagePlus.GRAY16:
                                        short[] shortPixels = new short[ncols * nrows];
                                        for (int i = 0; i < ncols; i++) {
                                                for (int j = 0; j < nrows; j++) {
                                                        shortPixels[j * ncols + i] = (short) processor.get(i, j);
                                                }
                                        }
                                        dos.write(ByteUtils.shortsToBytes(shortPixels));
                                        break;
                                case ImagePlus.GRAY32:
                                        float[] floatPixels = new float[ncols * nrows];
                                        for (int i = 0; i < ncols; i++) {
                                                for (int j = 0; j < nrows; j++) {
                                                        floatPixels[j * ncols + i] = processor.getPixelValue(i,
                                                                j);
                                                }
                                        }
                                        dos.write(ByteUtils.floatsToBytes(floatPixels));
                                        break;
                                case ImagePlus.COLOR_RGB:
                                        int[] intPixels = new int[ncols * nrows];
                                        int[] rasterPixels = (int[]) processor.getPixels();
                                        for (int i = 0; i < ncols; i++) {
                                                for (int j = 0; j < nrows; j++) {
                                                        intPixels[j * ncols + i] = rasterPixels[j
                                                                * processor.getWidth() + i];
                                                }
                                        }
                                        dos.write(ByteUtils.intsToBytes(intPixels));
                                        break;
                                default:
                                        throw new UnsupportedOperationException("Unrecognized image type");
                        }

                        return bos.toByteArray();
                } catch (IOException e) {
                        throw new IllegalStateException(e);
                }
        }

        private static void writeMetadata(DataOutputStream dos,
                RasterMetadata metadata, double min, double max, int imageType)
                throws IOException {
                // Write metadata
                dos.writeDouble(metadata.getXulcorner());
                dos.writeDouble(metadata.getYulcorner());
                dos.writeInt(metadata.getNRows());
                dos.writeInt(metadata.getNCols());
                dos.writeFloat(metadata.getPixelSize_X());
                dos.writeFloat(metadata.getPixelSize_Y());
                dos.writeDouble(metadata.getRotation_X());
                dos.writeDouble(metadata.getRotation_Y());
                dos.writeFloat(metadata.getNoDataValue());
                // Write cached values
                dos.writeDouble(min);
                dos.writeDouble(max);
                dos.writeInt(imageType);
        }

        @Override
        public GeoRaster getAsRaster() {
                return geoRaster;
        }

        public static Value readBytes(byte[] buffer) {
                ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
                DataInputStream dis = new DataInputStream(bis);
                try {
                        // Read metadata
                        RasterMetadata metadata = readMetadata(dis);
                        int nRows = metadata.getNRows();
                        int nCols = metadata.getNCols();
                        // Read cached values
                        double min = dis.readDouble();
                        double max = dis.readDouble();
                        // Read image
                        int imageType = dis.readInt();
                        GeoRaster gr;
                        switch (imageType) {
                                case ImagePlus.COLOR_256:
                                case ImagePlus.GRAY8:
                                        byte[] bytePixels = new byte[nRows * nCols];
                                        dis.read(bytePixels);
                                        gr = GeoRasterFactory.createGeoRaster(bytePixels, metadata,
                                                imageType, min, max);
                                        return new DefaultRasterValue(gr);
                                case ImagePlus.GRAY16:
                                        byte[] shortPixels = new byte[2 * nRows * nCols];
                                        dis.read(shortPixels);
                                        gr = GeoRasterFactory.createGeoRaster(ByteUtils.bytesToShorts(shortPixels), metadata, imageType, min,
                                                max);
                                        return new DefaultRasterValue(gr);
                                case ImagePlus.GRAY32:
                                        byte[] floatPixels = new byte[4 * nRows * nCols];
                                        dis.read(floatPixels);
                                        gr = GeoRasterFactory.createGeoRaster(ByteUtils.bytesToFloats(floatPixels), metadata, imageType, min,
                                                max);
                                        return new DefaultRasterValue(gr);
                                case ImagePlus.COLOR_RGB:
                                        byte[] intPixels = new byte[4 * nRows * nCols];
                                        dis.read(intPixels);
                                        gr = GeoRasterFactory.createGeoRaster(ByteUtils.bytesToInts(intPixels), metadata, imageType, min, max);
                                        return new DefaultRasterValue(gr);
                                default:
                                        throw new UnsupportedOperationException("Unrecognized image type: "
                                                + imageType);
                        }

                } catch (IOException e) {
                        throw new IllegalArgumentException(e);
                }
        }

        private static RasterMetadata readMetadata(DataInputStream dis)
                throws IOException {
                double upperLeftX = dis.readDouble();
                double upperLeftY = dis.readDouble();
                int nRows = dis.readInt();
                int nCols = dis.readInt();
                float pixelWidth = dis.readFloat();
                float pixelHeight = dis.readFloat();
                double rotationX = dis.readDouble();
                double rotationY = dis.readDouble();
                float noDataValue = dis.readFloat();
                return new RasterMetadata(upperLeftX, upperLeftY, pixelWidth,
                        pixelHeight, nCols, nRows, rotationX, rotationY, noDataValue);
        }

        /**
         * Builds a lazy RasterValue. The metadata and cached values are recovered
         * immediately and the pixel data will be read through the specified
         * {@link ByteProvider}
         *
         * @param buffer
         *            Buffer containing the metadata
         * @param byteProvider
         *            Interface to access the pixel information
         * @return
         */
        public static RasterValue readBytes(byte[] buffer, ByteProvider byteProvider) {
                try {
                        ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
                        DataInputStream dis = new DataInputStream(bis);
                        RasterMetadata metadata = readMetadata(dis);
                        // read cached data
                        double min = dis.readDouble();
                        double max = dis.readDouble();
                        // Read image type
                        int imageType = dis.readInt();
                        GeoRaster geoRaster = GeoRasterFactory.createGeoRaster(
                                new ByteArrayFileReader(byteProvider, metadata), imageType,
                                min, max);
                        return ValueFactory.createValue(geoRaster);
                } catch (IOException e) {
                        throw new IllegalStateException("Problem reading memory buffer.", e);
                }
        }

        @Override
        public void setValue(GeoRaster value) {
                this.geoRaster = value;
        }

        private static class ByteArrayFileReader implements RasterReader {

                private final RasterMetadata metadata;
                private final ByteProvider byteProvider;

                ByteArrayFileReader(ByteProvider byteProvider,
                        RasterMetadata metadata) {
                        this.byteProvider = byteProvider;
                        this.metadata = metadata;
                }

                @Override
                public ImagePlus readImagePlus() throws IOException {
                        byte[] bytes = byteProvider.getBytes();
                        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                        DataInputStream dis = new DataInputStream(bis);
                        // Read metadata
                        RasterMetadata theMetadata = readMetadata(dis);
                        int nRows = theMetadata.getNRows();
                        int nCols = theMetadata.getNCols();
                        // Read cached values
                        dis.readDouble();
                        dis.readDouble();
                        // Read image
                        int imageType = dis.readInt();
                        switch (imageType) {
                                case ImagePlus.COLOR_256:
                                case ImagePlus.GRAY8:
                                        byte[] bytePixels = new byte[nRows * nCols];
                                        dis.read(bytePixels);
                                        return new ImagePlus("", new ByteProcessor(nCols, nRows,
                                                bytePixels, null));
                                case ImagePlus.GRAY16:
                                        byte[] shortBytes = new byte[2 * nRows * nCols];
                                        dis.read(shortBytes);
                                        short[] shortPixels = ByteUtils.bytesToShorts(shortBytes);
                                        return new ImagePlus("", new ShortProcessor(nCols, nRows,
                                                shortPixels, null));
                                case ImagePlus.GRAY32:
                                        byte[] floatBytes = new byte[4 * nRows * nCols];
                                        dis.read(floatBytes);
                                        float[] floatPixels = ByteUtils.bytesToFloats(floatBytes);
                                        return new ImagePlus("", new FloatProcessor(nCols, nRows,
                                                floatPixels, null));
                                case ImagePlus.COLOR_RGB:
                                        byte[] intBytes = new byte[4 * nRows * nCols];
                                        dis.read(intBytes);
                                        int[] intPixels = ByteUtils.bytesToInts(intBytes);
                                        return new ImagePlus("", new ColorProcessor(nCols, nRows,
                                                intPixels));
                                default:
                                        throw new UnsupportedOperationException("Unrecognized image type: "
                                                + imageType);
                        }
                }

                @Override
                public RasterMetadata readRasterMetadata() throws IOException {
                        return metadata;
                }
        }

        @Override
        public CoordinateReferenceSystem getCRS() {
                return crs;
        }
}
