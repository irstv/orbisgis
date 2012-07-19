/**
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 *
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-1012 IRSTV (FR CNRS 2488)
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

import java.io.File;

import org.gdms.source.SourceManager;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class FormatTest extends AbstractTest {

	private SourceManager sourceManager;

	@Override
        @Before
	public void setUp() throws Exception {
		super.setUp();
		super.registerDataManager();
		sourceManager = ((DataManager) Services.getService(DataManager.class))
				.getDataSourceFactory().getSourceManager();
		sourceManager.removeAll();
		
	}

        @Test
	public void testTiff() throws Exception {
		File file = new File("src/test/resources/data/ace.tiff");
		sourceManager.register("tiff", file);
		getDataManager().createLayer("tiff");
                assertTrue(true);
	}

        @Test
	public void testAsc() throws Exception {
		File file = new File("src/test/resources/data/3x3.asc");
		sourceManager.register("asc", file);
		getDataManager().createLayer("asc");
                assertTrue(true);
	}

        @Test
	public void testShapefile() throws Exception {
		File file = new File("src/test/resources/data/bv_sap.shp");
		sourceManager.register("shp", file);
		getDataManager().createLayer("shp");
                assertTrue(true);
	}

}
