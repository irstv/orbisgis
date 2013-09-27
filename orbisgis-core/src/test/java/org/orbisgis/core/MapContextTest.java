/**
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 *
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.orbisgis.core.layerModel.ILayer;
import org.orbisgis.core.layerModel.LayerException;
import org.orbisgis.core.layerModel.MapContext;
import org.orbisgis.core.layerModel.OwsMapContext;
import org.orbisgis.progress.NullProgressMonitor;

import static org.junit.Assert.assertTrue;

public class MapContextTest extends AbstractTest {

	@Override
        @Before
	public void setUp() throws Exception {
		super.setUp();
		AbstractTest.registerDataManager();
	}

        @Test
	public void testRemoveSelectedLayer() throws Exception {
		MapContext mc = new OwsMapContext();
		mc.open(null);
		ILayer layer = mc.createLayer(getDataManager().getDataSource(
				new File("src/test/resources/data/bv_sap.shp").toURI()));
		mc.getLayerModel().addLayer(layer);
		mc.setSelectedLayers(new ILayer[] { layer });
		assertTrue(mc.getSelectedLayers().length == 1);
		assertTrue(mc.getSelectedLayers()[0] == layer);
		mc.getLayerModel().remove(layer);
		assertTrue(mc.getSelectedLayers().length == 0);
		mc.close(null);
	}

        @Test
	public void testSetBadLayerSelection() throws Exception {
		MapContext mc = new OwsMapContext();
		mc.open(null);
		ILayer layer = mc.createLayer(getDataManager().getDataSource(
				new File("src/test/resources/data/bv_sap.shp").toURI()));
		ILayer layer2 = mc.createLayer(getDataManager().getDataSource(
				new File("src/test/resources/data/linestring.shp").toURI()));
		mc.getLayerModel().addLayer(layer);
		mc.setSelectedLayers(new ILayer[] { layer2 });
		assertTrue(mc.getSelectedLayers().length == 0);
		mc.setSelectedLayers(new ILayer[] { layer });
		assertTrue(mc.getSelectedLayers().length == 1);
		mc.close(null);
	}

        @Test
	public void testRemoveActiveLayer() throws Exception {
		MapContext mc = new OwsMapContext();
		mc.open(null);
		ILayer layer = mc.createLayer(getDataManager().getDataSource(
				new File("src/test/resources/data/bv_sap.shp").toURI()));
		mc.getLayerModel().addLayer(layer);
		mc.setActiveLayer(layer);
		mc.getLayerModel().remove(layer);
		assertTrue(mc.getActiveLayer() == null);
		mc.close(null);
	}

//        @Test
//	public void testSaveAndRecoverMapContext() throws Exception {
//		MapContext mc = new OwsMapContext();
//		mc.open(null);
//		ILayer layer1 = mc.createLayer(
//				new File("src/test/resources/data/linestring.shp"));
//		ILayer layer2 = mc.createLayer(
//				new File("src/test/resources/data/bv_sap.shp"));
//		mc.getLayerModel().addLayer(layer1);
//		mc.getLayerModel().addLayer(layer2);
//		Symbol sym1 = layer1.getVectorLegend()[0].getSymbol(layer1
//				.getDataSource(), 0);
//		Symbol sym2 = layer2.getVectorLegend()[0].getSymbol(layer2
//				.getDataSource(), 0);
//		Object persistence = mc.getJAXBObject();
//		OwsMapContext mc2 = new OwsMapContext();
//		mc2.setJAXBObject(persistence);
//		mc2.open(null);
//		assertTrue(mc2.getLayers().length == 2);
//		Legend legend1 = mc2.getLayerModel().getLayer(0).getVectorLegend()[0];
//		assertTrue(legend1.getSymbol(layer1.getDataSource(), 0)
//				.getPersistentProperties().equals(
//						sym1.getPersistentProperties()));
//		Legend legend2 = mc2.getLayerModel().getLayer(1).getVectorLegend()[0];
//		assertTrue(legend2.getSymbol(layer2.getDataSource(), 0)
//				.getPersistentProperties().equals(
//						sym2.getPersistentProperties()));
//		mc.close(null);
//		mc2.close(null);
//	}

        @Test
	public void testSaveAndRecoverTwoNestedCollections() throws Exception {
                //Define a MapContext
		MapContext mc = new OwsMapContext();
		mc.open(null);
        ILayer layer1 = mc.createLayerCollection("a");
        ILayer layer2 = mc.createLayerCollection("a");
        ILayer layer3 = mc.createLayer("linestring",
                getDataManager().getDataSource(new File("src/test/resources/data/linestring.shp").toURI()));
		mc.getLayerModel().addLayer(layer1);
		layer1.addLayer(layer2);
		layer2.addLayer(layer3);
                ByteArrayOutputStream map = new ByteArrayOutputStream();
		mc.write(map);
		mc.close(null);
                
                //Define a new MapContext from previous MapContext serialisation
		mc = new OwsMapContext();
		mc.read(new ByteArrayInputStream(map.toByteArray()));
		mc.open(null);
		ILayer layer1_ = mc.getLayerModel().getLayer(0);
		assertTrue(layer1_.getLayerCount() == 1);
		assertTrue(layer1_.getLayer(0).getLayerCount() == 1);
		assertTrue(layer1_.getLayer(0).getLayer(0).getName().equals(
				"linestring"));
		mc.close(null);
	}

        @Test
	public void testOperateOnClosedMapContext() throws Exception {
		MapContext mc = new OwsMapContext();
		try {
			mc.getSelectedLayers();
			assertTrue(false);
		} catch (IllegalStateException e) {
		}
		try {
			mc.draw(null, null);
			assertTrue(false);
		} catch (IllegalStateException e) {
		}
		try {
			mc.getActiveLayer();
			assertTrue(false);
		} catch (IllegalStateException e) {
		}
		try {
			mc.getLayerModel();
			assertTrue(false);
		} catch (IllegalStateException e) {
		}
		try {
			mc.getLayers();
			assertTrue(false);
		} catch (IllegalStateException e) {
		}
		try {
			mc.setActiveLayer(null);
			assertTrue(false);
		} catch (IllegalStateException e) {
		}
		try {
			mc.setSelectedLayers(null);
			assertTrue(false);
		} catch (IllegalStateException e) {
		}
	}

        @Test
	public void testIsOpen() throws Exception {
		MapContext mc = new OwsMapContext();
		assertTrue(!mc.isOpen());
		mc.open(new NullProgressMonitor());
		assertTrue(mc.isOpen());
	}

        @Test
	public void testOpenTwice() throws Exception {
		MapContext mc = new OwsMapContext();
		mc.open(new NullProgressMonitor());
		try {
			mc.open(new NullProgressMonitor());
			assertTrue(false);
		} catch (IllegalStateException e) {
		}
	}

        @Test
	public void testCloseClosedMap() throws Exception {
		MapContext mc = new OwsMapContext();
		try {
			mc.close(new NullProgressMonitor());
			assertTrue(false);
		} catch (IllegalStateException e) {
		}
	}

        @Test
	public void testWriteOnOpenMap() throws Exception {
		MapContext mc = new OwsMapContext();
                
                

                ByteArrayOutputStream map = new ByteArrayOutputStream();
		mc.write(map);
		mc.open(new NullProgressMonitor());
		try {
                        mc.read(new ByteArrayInputStream(map.toByteArray()));
			assertTrue(false);
		} catch (IllegalStateException e) {
		}
	}

        @Test
	public void testRemoveSource() throws Exception {
		MapContext mc = new OwsMapContext();
		mc.open(null);
		ILayer layer = mc.createLayer("linestring",
                getDataManager().getDataSource(new File("src/test/resources/data/linestring.shp").toURI()));
		mc.getLayerModel().addLayer(layer);
		getDataManager().getSourceManager().remove("linestring");
		assertTrue(mc.getLayerModel().getLayerCount() == 0);
		mc.close(null);
	}

        @Test
	public void testReadWriteMapContext() throws Exception {
		MapContext mc = getSampleMapContext();

                ByteArrayOutputStream map = new ByteArrayOutputStream();
		mc.write(map);

		MapContext mc2 = new OwsMapContext();
                mc2.read(new ByteArrayInputStream(map.toByteArray()));
		mc2.open(null);
		assertTrue(mc2.getLayerModel().getLayerCount() == 1);
		mc2.close(null);

                mc2.read(new ByteArrayInputStream(map.toByteArray()));
		mc2.open(null);
		assertTrue(mc2.getLayerModel().getLayerCount() == 1);
		mc2.close(null);
	}

	private MapContext getSampleMapContext() throws LayerException {
		MapContext mc = new OwsMapContext();
		mc.open(null);
		ILayer layer = getDataManager().createLayerCollection("a");
		mc.getLayerModel().addLayer(layer);
		mc.close(null);
		return mc;
	}

        @Test
	public void testSetJAXBOpenTwice() throws Exception {
		MapContext mc = getSampleMapContext();
                ByteArrayOutputStream map = new ByteArrayOutputStream();
		mc.write(map);

		MapContext mc2 = new OwsMapContext();
		mc2.read(new ByteArrayInputStream(map.toByteArray()));
		mc2.open(null);
		assertTrue(mc2.getLayerModel().getLayerCount() == 1);
		ILayer layer = getDataManager().createLayerCollection("b");
		mc2.getLayerModel().addLayer(layer);
		assertTrue(mc2.getLayerModel().getLayerCount() == 2);
		mc2.close(null);

		mc2.open(null);
		assertTrue(mc2.getLayerModel().getLayerCount() == 2);
		mc2.close(null);
	}

//        @Test
//	public void testLegendPersistenceOpeningTwice() throws Exception {
//		MapContext mc = new OwsMapContext();
//		mc.open(null);
//		ILayer layer = mc.createLayer("bv_sap",
//				new File("src/test/resources/data/bv_sap.shp"));
//		mc.getLayerModel().addLayer(layer);
//		UniqueSymbolLegend legend = LegendFactory.createUniqueSymbolLegend();
//		Symbol symbol = SymbolFactory.createPolygonSymbol(Color.pink);
//		legend.setSymbol(symbol);
//		layer.setLegend(legend);
//		assertTrue(legend.getSymbol().getPersistentProperties().equals(
//				symbol.getPersistentProperties()));
//		mc.close(null);
//		MapContext mc2 = new OwsMapContext();
//		mc2.setJAXBObject(mc.getJAXBObject());
//		mc2.open(null);
//		assertTrue(legend.getSymbol().getPersistentProperties().equals(
//				symbol.getPersistentProperties()));
//		mc2.close(null);
//		mc2.open(null);
//		layer = mc2.getLayerModel().getLayerByName("bv_sap");
//		legend = (UniqueSymbolLegend) layer.getVectorLegend()[0];
//		assertTrue(legend.getSymbol().getPersistentProperties().equals(
//				symbol.getPersistentProperties()));
//		mc2.close(null);
//	}

        @Test
	public void testWriteAfterReadModifyAndClose() throws Exception {
		MapContext mc = getSampleMapContext();
                ByteArrayOutputStream map = new ByteArrayOutputStream();
		mc.write(map);

		MapContext mc2 = new OwsMapContext();
		// set DATA
                mc2.read(new ByteArrayInputStream(map.toByteArray()));
		// modify
		mc2.open(null);
		assertTrue(mc2.getLayerModel().getLayerCount() == 1);
		ILayer layer = getDataManager().createLayerCollection("b");
		mc2.getLayerModel().addLayer(layer);
		assertTrue(mc2.getLayerModel().getLayerCount() == 2);
		// close
		mc2.close(null);
                ByteArrayOutputStream map2 = new ByteArrayOutputStream();
		mc2.write(map2);
		// check obj is good
		MapContext mc3 = new OwsMapContext();
		mc3.read(new ByteArrayInputStream(map2.toByteArray()));
		mc3.open(null);
		assertTrue(mc3.getLayerModel().getLayerCount() == 2);
		mc3.close(null);
	}

        @Test
	public void testActiveLayerClearedOnClose() throws Exception {
		MapContext mc = new OwsMapContext();
		mc.open(null);
		ILayer layer = mc.createLayer(getDataManager().getDataSource(
				new File("src/test/resources/data/bv_sap.shp").toURI()));
		mc.getLayerModel().addLayer(layer);
		mc.setActiveLayer(layer);
		mc.close(null);
		mc.open(null);
		assertTrue(mc.getActiveLayer() == null);
	}


        @Test
	public void testMapOpensWithBadLayer() throws Exception {
		File shp = new File("target/bv_sap.shp");
		File dbf = new File("target/bv_sap.dbf");
		File shx = new File("target/bv_sap.shx");
		File originalShp = new File("src/test/resources/data/bv_sap.shp");
		FileUtils.copyFile(originalShp, shp);
		FileUtils.copyFile(new File("src/test/resources/data/bv_sap.dbf"), dbf);
		FileUtils.copyFile(new File("src/test/resources/data/bv_sap.shx"), shx);
		MapContext mc = new OwsMapContext();
		mc.open(null);
		mc.getLayerModel().addLayer(mc.createLayer("youhou",getDataManager().getDataSource(shp.toURI())));
		mc.getLayerModel().addLayer(mc.createLayer("yaha",getDataManager().getDataSource(originalShp.toURI())));
		mc.close(null);
		shp.delete();
		dbf.delete();
		shx.delete();
		failErrorManager.setIgnoreWarnings(true);
		failErrorManager.setIgnoreErrors(true);
		mc.open(null);
		failErrorManager.setIgnoreWarnings(false);
		failErrorManager.setIgnoreErrors(false);
		assertTrue(mc.getLayerModel().getLayerCount() == 1);
		mc.close(null);
	}
}
