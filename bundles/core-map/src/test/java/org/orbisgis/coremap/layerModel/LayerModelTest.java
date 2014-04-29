/**
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 *
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
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
package org.orbisgis.coremap.layerModel;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.h2spatialext.CreateSpatialExtension;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.orbisgis.corejdbc.DataManager;
import org.orbisgis.corejdbc.internal.DataManagerImpl;
import org.orbisgis.coremap.renderer.se.Style;
import org.orbisgis.coremap.renderer.se.common.Description;

import javax.sql.DataSource;


public class LayerModelTest {
    private static Connection connection;
    private static DataManager dataManager;
    private String colorRecodeFile = LayerModelTest.class.getResource("../renderer/se/colorRecode.se").getFile();

    @BeforeClass
    public static void tearUpClass() throws Exception {
        DataSource dataSource = SpatialH2UT.createDataSource(LayerModelTest.class.getSimpleName(), false);
        connection = dataSource.getConnection();
        CreateSpatialExtension.initSpatialExtension(connection);
        dataManager = new DataManagerImpl(dataSource);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        connection.close();
        dataManager.dispose();
    }

    private DataManager getDataManager() {
        return dataManager;
    }

    private Connection getConnection() {
        return connection;
    }

    private MapContext mc = new OwsMapContext(dataManager);

	private static final String dummy = "vector1";
	private static final String dummy2 = "vector2";
	private static final String dummy3 = "vector3";

    @Before
    public void setUp() throws Exception {
            Statement st = getConnection().createStatement();
            st.execute("create table "+dummy+"(the_geom GEOMETRY)");
            st.execute("create table "+dummy2+"(the_geom GEOMETRY)");
            st.execute("create table "+dummy3+"(the_geom GEOMETRY)");
    }

    @After
    public void tearDown() throws SQLException {
        Statement st = getConnection().createStatement();
        st.execute("drop table "+dummy+","+dummy2+","+dummy3);
    }

    /** TODO raster
	public void testTreeExploring() throws Exception {
		ILayer vl = mc.createLayer("vector1");
		ILayer rl = mc.createLayer("my tiff",
                getDataManager().getDataSource(new File("src/test/resources/data/ace.tiff").toURI()));
		ILayer lc = mc.createLayerCollection("my data");
		lc.addLayer(vl);
		lc.addLayer(rl);

		ILayer layer = lc;
		if (layer instanceof LayerCollection) {
			lc = (ILayer) layer;
			lc.getChildren();
		} else {
			if (layer.getDataSource().isRaster()) {
				GeoRaster fc = layer.getDataSource().getRaster(0);
				assertTrue(fc != null);
			} else if (layer.getDataSource().isVectorial()) {
				DataSource fc = layer.getDataSource();
				assertTrue(fc != null);
			}
		}
	}
    */
        @Test
	public void testLayerEvents() throws Exception {
		TestLayerListener listener = new TestLayerListener();
		ILayer vl = mc.createLayer(dummy);
		ILayer lc = mc.createLayerCollection("root");
		vl.addLayerListener(listener);
		lc.addLayerListener(listener);
		ILayer vl1 = mc.createLayer(dummy);
		lc.addLayer(vl1);
		assertTrue(listener.la == 1);
		lc.setName("new name");
		assertTrue(listener.nc == 1);
		lc.setVisible(false);
		assertTrue(listener.vc == 1);
		vl.open();
		int refsc = listener.sc;
		vl.addStyle(new Style(vl, colorRecodeFile));
		assertTrue(listener.sc == refsc + 1);
		vl.setStyle(0,new Style(vl, colorRecodeFile));
		assertTrue(listener.sc == refsc + 2);
                List<Style> styles = new ArrayList<Style>();
		vl.setStyles(styles);
		assertTrue(listener.sc == refsc + 3);
		vl.addStyle(0,new Style(vl, colorRecodeFile));
		assertTrue(listener.sc == refsc + 4);
		lc.remove(vl1.getName());
		assertTrue(listener.lr == 1);
		assertTrue(listener.lring == 1);
		assertTrue(lc.getLayerCount() == 0);
		vl.close();
	}

        @Test
	public void testLayerRemovalCancellation() throws Exception {
		TestLayerListener listener = new TestLayerListener() {
			@Override
			public boolean layerRemoving(LayerCollectionEvent arg0) {
				return false;
			}
		};
		ILayer vl = mc.createLayer(dummy);
		ILayer lc = mc.createLayerCollection("root");
		lc.addLayer(vl);
		lc.addLayerListener(listener);
		assertTrue(lc.remove(vl) == null);
		assertTrue(lc.remove(vl.getName()) == null);
		assertTrue(lc.remove(vl, false) == null);
		assertTrue(lc.remove(vl, true) != null);
	}

        @Test
	public void testRepeatedName() throws Exception {
		ILayer lc1 = mc.createLayerCollection("firstLevel");
		ILayer lc2 = mc.createLayerCollection("secondLevel");
		ILayer lc3 = mc.createLayerCollection("thirdLevel");
		ILayer vl1 = mc.createLayer(dummy);
		ILayer vl2 = mc.createLayer(dummy2);
		ILayer vl3 = mc.createLayer(dummy3);
		lc1.addLayer(vl1);
		lc2.addLayer(vl2);
		lc1.addLayer(lc2);
		lc3.addLayer(vl3);
		lc2.addLayer(lc3);
		vl3.setName("vector2");
		assertTrue(!vl3.getName().equals("vector2"));
		vl3.setName("firstLevel");
		assertTrue(!vl3.getName().equals("firstLevel"));
		lc1.setName("vector2");
		assertTrue(!lc1.getName().equals("vector2"));
	}

        @Test
	public void testAddWithSameName() throws Exception {
		String tableReference = getDataManager().registerDataSource(new File(
                LayerModelTest.class.getResource("../../../../data/bv_sap.shp").getFile()).toURI());
		ILayer lc = mc.createLayerCollection("firstLevel");
		ILayer vl1 = mc.createLayer(tableReference);
		ILayer vl2 = mc.createLayer(tableReference);
		lc.addLayer(vl1);
		lc.addLayer(vl2);
		assertTrue(!vl1.getName().equals(vl2.getName()));

	}

        @Test
	public void testAddToChild() throws Exception {
		ILayer lc1 = mc.createLayerCollection("firstLevel");
		ILayer lc2 = mc.createLayerCollection("secondLevel");
		ILayer lc3 = mc.createLayerCollection("thirdLevel");
		ILayer lc4 = mc.createLayerCollection("fourthLevel");
		lc1.addLayer(lc2);
		lc2.addLayer(lc3);
		lc3.addLayer(lc4);
		try {
			lc2.moveTo(lc4);
			assertTrue(false);
		} catch (LayerException e) {
		}

		TestLayerListener listener = new TestLayerListener();
		lc1.addLayerListenerRecursively(listener);
		lc3.moveTo(lc1);
		assertTrue(lc3.getParent() == lc1);
		assertTrue(lc2.getChildren().length == 0);
		assertTrue(listener.la == 0);
		assertTrue(listener.lr == 0);
		assertTrue(listener.lring == 0);
		assertTrue(listener.lm == 2);
	}

        @Test
	public void testContainsLayer() throws Exception {
		ILayer lc = mc.createLayerCollection("root");
		ILayer l2 = mc.createLayerCollection("secondlevel");
		ILayer vl1 = mc.createLayer("vector1");
		lc.addLayer(l2);
		l2.addLayer(vl1);
		assertTrue(lc.getAllLayersNames().contains(vl1.getName()));
	}

        @Test
	public void testGetLayerByName() throws Exception {
		ILayer lc = mc.createLayerCollection("root");
		ILayer l2 = mc.createLayerCollection("secondlevel");
		ILayer l3 = mc.createLayerCollection("secondlevelbis");
		ILayer vl1 = mc.createLayer("vector1");
		l2.addLayer(vl1);
		lc.addLayer(l2);
		lc.addLayer(l3);

		assertTrue(lc.getLayerByName("secondlevel") == l2);
		assertTrue(lc.getLayerByName("secondlevelbis") == l3);
		assertTrue(lc.getLayerByName("vector1") == vl1);
	}

        @Test
        public void testInternationalizedTitle() throws Exception {
                Layer bl = new Layer("youhou", "vector1", getDataManager());
                Description desc = new Description();
                desc.addTitle(Locale.FRENCH, "youhou title");
                bl.setDescription(desc);
                Locale l = Locale.getDefault();
                Locale.setDefault(new Locale("en","EN"));
                assertNotNull(bl.getDescription());
                Locale.setDefault(l);
        }

	private class TestLayerListener implements LayerListener {

		private int nc = 0;

		private int vc = 0;

		private int la = 0;

		private int lm = 0;

		private int lr = 0;

		private int lring = 0;

		private int sc = 0;

		public void nameChanged(LayerListenerEvent e) {
			nc++;
		}

		public void visibilityChanged(LayerListenerEvent e) {
			vc++;
		}

		public void layerAdded(LayerCollectionEvent listener) {
			la++;
		}

		public void layerMoved(LayerCollectionEvent listener) {
			lm++;
		}

		public void layerRemoved(LayerCollectionEvent listener) {
			lr++;
		}

		public void styleChanged(LayerListenerEvent e) {
			sc++;
		}

		public void selectionChanged(SelectionEvent e) {
		}

		@Override
		public boolean layerRemoving(LayerCollectionEvent layerCollectionEvent) {
			lring++;
			return true;
		}
	}
}
