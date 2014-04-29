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
package org.gdms.drivers;

import org.gdms.driver.Driver;
import org.gdms.data.types.Dimension3DConstraint;
import org.gdms.data.types.LengthConstraint;
import org.gdms.data.types.PrecisionConstraint;
import org.gdms.data.types.ReadOnlyConstraint;
import org.gdms.data.types.UniqueConstraint;
import org.gdms.data.types.AutoIncrementConstraint;
import org.gdms.data.types.NotNullConstraint;
import org.gdms.data.types.PrimaryKeyConstraint;
import org.junit.Test;
import org.junit.Before;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;

import java.io.File;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;


import org.gdms.TestBase;
import org.gdms.data.DataSource;
import org.gdms.data.DataSourceCreation;
import org.gdms.data.DataSourceCreationException;
import org.gdms.data.DataSourceFactory;
import org.gdms.data.DigestUtilities;
import org.gdms.data.file.FileSourceCreation;
import org.gdms.data.schema.DefaultMetadata;
import org.gdms.data.types.Constraint;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DriverException;
import org.grap.model.GeoRaster;
import org.grap.model.GeoRasterFactory;
import org.grap.model.RasterMetadata;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.gdms.data.types.MaxConstraint;
import org.gdms.data.types.MinConstraint;
import org.gdms.data.types.PatternConstraint;
import org.gdms.data.types.RasterTypeConstraint;
import org.gdms.data.types.ScaleConstraint;
import org.gdms.driver.gdms.GdmsReader;
import org.gdms.source.SourceManager;

import static org.junit.Assert.*;

import org.gdms.TestResourceHandler;

public class GDMSDriverTest extends TestBase {

        @Before
        public void setUp() throws Exception {
                super.setUpTestsWithEdition(false);
        }

        @Test
        public void testSaveASGDMS() throws Exception {
                File source = super.getAnySpatialResource();
                saveAs(source);
        }

        private void saveAs(File source) throws Exception {
                File gdmsFile = getTempFile(".gdms");
                sm.register("gdms", gdmsFile);
                DataSource ds = dsf.getDataSource(source);
                ds.open();
                dsf.saveContents("gdms", ds);
                Value[][] c1 = getDataSourceContents(ds);
                ds.close();
                DataSource ds2 = dsf.getDataSource(gdmsFile);
                ds2.open();
                Value[][] c2 = getDataSourceContents(ds2);
                ds2.close();

                assertTrue(equals(c1, c2));
        }

        @Test
        public void testAllTypes() throws Exception {
                DefaultMetadata metadata = new DefaultMetadata();
                metadata.addField("binary", TypeFactory.createType(Type.BINARY));
                metadata.addField("boolean", TypeFactory.createType(Type.BOOLEAN));
                metadata.addField("byte", TypeFactory.createType(Type.BYTE));
                metadata.addField("collection", TypeFactory.createType(Type.COLLECTION));
                metadata.addField("date", TypeFactory.createType(Type.DATE));
                metadata.addField("double", TypeFactory.createType(Type.DOUBLE));
                metadata.addField("float", TypeFactory.createType(Type.FLOAT));
                metadata.addField("geometry", TypeFactory.createType(Type.GEOMETRY));
                metadata.addField("int", TypeFactory.createType(Type.INT));
                metadata.addField("long", TypeFactory.createType(Type.LONG));
                metadata.addField("raster", TypeFactory.createType(Type.RASTER));
                metadata.addField("short", TypeFactory.createType(Type.SHORT));
                metadata.addField("string", TypeFactory.createType(Type.STRING));
                metadata.addField("time", TypeFactory.createType(Type.TIME));
                metadata.addField("timestamp", TypeFactory.createType(Type.TIMESTAMP));

                File file = getTempFile("toto.gdms");
                DataSourceCreation dsc = new FileSourceCreation(file, metadata);
                file.delete();
                dsf.createDataSource(dsc);

                DataSource ds = dsf.getDataSource(file);
                ds.open();
                ds.insertEmptyRow();
                ds.setBinary(0, 0, new byte[]{3, 3});
                ds.setBoolean(0, 1, true);
                ds.setByte(0, 2, (byte) 5);
                ds.setFieldValue(0, 3, ValueFactory.createValue(new Value[]{ValueFactory.createValue(true)}));
                ds.setDate(0, 4, new Date());
                ds.setDouble(0, 5, 4d);
                ds.setFloat(0, 6, 5.2f);
                ds.setFieldValue(0, 7, ValueFactory.createValue(new GeometryFactory().createPoint(new Coordinate(3, 3))));
                ds.setInt(0, 8, 4);
                ds.setLong(0, 9, 5L);
                GeoRaster gr = GeoRasterFactory.createGeoRaster(new File(TestResourceHandler.TESTRESOURCES, "sample.png").getAbsolutePath());
                Value grValue = ValueFactory.createValue(gr);
                ds.setFieldValue(0, 10, grValue);
                ds.setShort(0, 11, (short) 34);
                ds.setString(0, 12, "sd");
                ds.setTime(0, 13, new Time(12424L));
                ds.setTimestamp(0, 14, new Timestamp(2525234L));

                Value[] nullValues = new Value[15];
                for (int i = 0; i < nullValues.length; i++) {
                        nullValues[i] = ValueFactory.createNullValue();
                }
                ds.insertFilledRow(nullValues);
                String digest = DigestUtilities.getBase64Digest(ds);
                ds.commit();
                ds.close();

                ds = dsf.getDataSource(file);
                DataSource ds2 = dsf.getDataSource(file);
                ds.open();
                ds2.open();
                assertTrue(digest.equals(DigestUtilities.getBase64Digest(ds2)));
                ds2.close();
                ds.close();
        }

        @Test
        public void testAllConstraints() throws Exception {
                DefaultMetadata metadata = new DefaultMetadata();
                Type[] types = new Type[]{
                        TypeFactory.createType(Type.BINARY, new PrimaryKeyConstraint()),
                        TypeFactory.createType(Type.BOOLEAN, new UniqueConstraint()),
                        TypeFactory.createType(Type.BYTE, new MinConstraint(0)),
                        TypeFactory.createType(Type.COLLECTION, new NotNullConstraint()),
                        TypeFactory.createType(Type.DATE, new ReadOnlyConstraint()),
                        TypeFactory.createType(Type.DOUBLE, new AutoIncrementConstraint()),
                        TypeFactory.createType(Type.FLOAT),
                        TypeFactory.createType(Type.LINESTRING, new Dimension3DConstraint(3)),
                        TypeFactory.createType(Type.INT),
                        TypeFactory.createType(Type.LONG),
                        TypeFactory.createType(Type.RASTER, new RasterTypeConstraint(ImagePlus.COLOR_256)),
                        TypeFactory.createType(Type.SHORT, new MaxConstraint(4),
                        new PrecisionConstraint(0),
                        new ScaleConstraint(2)),
                        TypeFactory.createType(Type.STRING, new LengthConstraint(4),
                        new PatternConstraint("%L")),
                        TypeFactory.createType(Type.TIME),
                        TypeFactory.createType(Type.TIMESTAMP)};
                for (int i = 0; i < types.length; i++) {
                        metadata.addField("field" + i, types[i]);
                }

                File file = getTempFile(".gdms");
                DataSourceCreation dsc = new FileSourceCreation(file, metadata);
                file.delete();
                dsf.createDataSource(dsc);

                DataSource ds = dsf.getDataSource(file);
                ds.open();
                for (int i = 0; i < ds.getMetadata().getFieldCount(); i++) {
                        checkType(ds.getFieldType(i), types[i]);
                        assertEquals(ds.getFieldName(i), "field" + i);
                }

                ds.close();
        }

        private void checkType(Type fieldType, Type type) {
                assertEquals(fieldType.getTypeCode(), type.getTypeCode());
                Constraint[] cons = fieldType.getConstraints();
                Constraint[] cons2 = type.getConstraints();
                assertEquals(cons.length, cons2.length);
                for (int i = 0; i < cons2.length; i++) {
                        assertEquals(cons[i].getConstraintValue(), cons2[i].getConstraintValue());
                }
        }

        @Test
        public void testRemoveRasterField() throws Exception {
                File file = new File(TestResourceHandler.TESTRESOURCES, "sample.png");
                DataSource ds = dsf.getDataSource(file);
                ds.open();
                String digest = DigestUtilities.getBase64Digest(ds);
                ds.removeField(0);
                ds.undo();
                assertTrue(digest.equals(DigestUtilities.getBase64Digest(ds)));
                ds.close();
        }

        @Test
        public void testKeepFullExtent() throws Exception {
                File vectFile = getTempFile(".gdms");
                vectFile.delete();
                testFullExtent(vectFile, new File(TestResourceHandler.TESTRESOURCES, "landcover2000.shp"));
                File rasterFile = getTempFile(".gdms");
                rasterFile.delete();
                testFullExtent(rasterFile, new File(TestResourceHandler.TESTRESOURCES, "sample.png"));
        }

        private void testFullExtent(File gdmsFile, File original)
                throws DataSourceCreationException, DriverException {
                String name = sm.nameAndRegister(gdmsFile);
                DataSource ds = dsf.getDataSource(original);
                ds.open();
                Envelope fe = ds.getFullExtent();
                dsf.saveContents(name, ds);
                ds.close();

                ds = dsf.getDataSource(gdmsFile);
                ds.open();
                assertEquals(fe, ds.getFullExtent());
                ds.close();
        }

        @Test
        public void testDifferentValueTypeAndFieldType() throws Exception {
                File source = new File(TestResourceHandler.TESTRESOURCES, "points.shp");
                saveAs(source);
        }

        @Test
        public void testKeepNoDataValue() throws Exception {
                DataSource ds = dsf.getDataSource(new File(TestResourceHandler.OTHERRESOURCES, "tif440606.gdms"));
                ds.open();
                GeoRaster gr = ds.getRaster(0);
                gr.setNodataValue(345);
                assertEquals(gr.getNoDataValue(), 345, 0);
                gr = ds.getRaster(0);
                assertEquals(gr.getNoDataValue(), 345, 0);
                ds.close();
        }

        @Test
        public void testRasterMetadataPixelArrayInconsistencyFloat()
                throws Exception {
                RasterMetadata rm = new RasterMetadata(0, 0, 10, 10, 2, 2);
                FloatProcessor fp = new FloatProcessor(3, 3);
                float[] pixels = (float[]) fp.getPixels();
                for (int i = 0; i < pixels.length; i++) {
                        pixels[i] = i;
                }
                GeoRaster gr = GeoRasterFactory.createGeoRaster(fp, rm);

                float[] result = (float[]) testRasterMetadataPixelArrayInconsistency(gr);
                assertEquals(result[2], 3, 0);
                assertEquals(result.length, 4);
        }

        @Test
        public void testRasterMetadataPixelArrayInconsistencyInt() throws Exception {
                RasterMetadata rm = new RasterMetadata(0, 0, 10, 10, 2, 2);
                ColorProcessor fp = new ColorProcessor(3, 3);
                int[] pixels = (int[]) fp.getPixels();
                for (int i = 0; i < pixels.length; i++) {
                        pixels[i] = i;
                }
                GeoRaster gr = GeoRasterFactory.createGeoRaster(fp, rm);

                int[] result = (int[]) testRasterMetadataPixelArrayInconsistency(gr);
                assertEquals(result[2], 3);
                assertEquals(result.length, 4);
        }

        @Test
        public void testRasterMetadataPixelArrayInconsistencyByte()
                throws Exception {
                RasterMetadata rm = new RasterMetadata(0, 0, 10, 10, 2, 2);
                ByteProcessor fp = new ByteProcessor(3, 3);
                byte[] pixels = (byte[]) fp.getPixels();
                for (int i = 0; i < pixels.length; i++) {
                        pixels[i] = (byte) i;
                }
                GeoRaster gr = GeoRasterFactory.createGeoRaster(fp, rm);

                byte[] result = (byte[]) testRasterMetadataPixelArrayInconsistency(gr);
                assertEquals(result[2], 3);
                assertEquals(result.length, 4);
        }

        @Test
        public void testRasterMetadataPixelArrayInconsistencyShort()
                throws Exception {
                RasterMetadata rm = new RasterMetadata(0, 0, 10, 10, 2, 2);
                ShortProcessor fp = new ShortProcessor(3, 3);
                short[] pixels = (short[]) fp.getPixels();
                for (int i = 0; i < pixels.length; i++) {
                        pixels[i] = (short) i;
                }
                GeoRaster gr = GeoRasterFactory.createGeoRaster(fp, rm);

                short[] result = (short[]) testRasterMetadataPixelArrayInconsistency(gr);
                assertEquals(result[2], 3);
                assertEquals(result.length, 4);
        }

        private Object testRasterMetadataPixelArrayInconsistency(GeoRaster gr)
                throws Exception {
                // Create the source
                File out = getTempFile(".gdms");
                DefaultMetadata dm = new DefaultMetadata(new Type[]{TypeFactory.createType(Type.RASTER)}, new String[]{"raster"});
                FileSourceCreation fsc = new FileSourceCreation(out, dm);
                dsf.createDataSource(fsc);

                // Register the source
                DataSource ds = dsf.getDataSource(out);
                ds.open();
                ds.insertFilledRow(new Value[]{ValueFactory.createValue(gr)});
                ds.commit();

                GeoRaster readRaster = ds.getFieldValue(0, 0).getAsRaster();
                Object pixels = readRaster.getImagePlus().getProcessor().getPixels();
                assertEquals(readRaster.getMetadata().getNCols(), 2);
                ds.close();
                return pixels;
        }

        @Test
        public void testCompatibleWith2_0() throws Exception {
                DataSource ds = dsf.getDataSource(new File(TestResourceHandler.OTHERRESOURCES, "version2.gdms"));
                ds.open();
                ds.getAsString();
                ds.close();
        }

        @Test
        public void testOpenBadFile() throws Exception {
                GdmsReader reader = new GdmsReader(new File(TestResourceHandler.OTHERRESOURCES, "badgdms.gdms"));
                try {
                        reader.open();
                        reader.readMetadata();
                        fail();
                } catch (DriverException e) {
                } finally {
                        reader.close();
                }
        }

        @Test
        public void testGetType() throws Exception {
                DataSource ds = dsf.getDataSource(new File(TestResourceHandler.OTHERRESOURCES, "version2.gdms"));
                ds.open();
                Driver d = ds.getDriver();
                assertEquals(d.getSupportedType(), SourceManager.FILE | SourceManager.RASTER | SourceManager.VECTORIAL);
                assertEquals(d.getType(), SourceManager.FILE | SourceManager.VECTORIAL);
                ds.close();
        }
}
