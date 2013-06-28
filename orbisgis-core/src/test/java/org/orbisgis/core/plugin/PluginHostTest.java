/*
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
package org.orbisgis.core.plugin;

import java.io.File;

import org.gdms.driver.Driver;
import org.gdms.driver.driverManager.DriverManager;
import org.gdms.sql.function.Function;
import org.gdms.sql.function.FunctionManager;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.orbisgis.core.AbstractTest;
import org.orbisgis.core.DataManager;
import org.orbisgis.core.plugin.gdms.DummyDriver;
import org.orbisgis.core.plugin.gdms.DummyScalarFunction;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 * Unit test of plugin-system
 * @author Nicolas Fortin
 */
public class PluginHostTest extends AbstractTest {
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        registerDataManager();
    }
    private PluginHost startHost() {
        PluginHost host = new PluginHost(new File("target"+File.separator+"plugins"));
        host.init();
        host.start();
        return host;
    }

    @Test
    public void installGDMSDriverService() throws Exception {
        DataManager dataManager = getDataManager();
        DriverManager dm = dataManager.getDataSourceFactory().getSourceManager().getDriverManager();
        DummyDriver dd = new DummyDriver();
        PluginHost host = startHost();
        try {
            // Register dummy sql function service
            ServiceRegistration<Driver> serv = host.getHostBundleContext()
                    .registerService(Driver.class, dd,null);
            assertTrue(dm.isDriverFileSupported(new File("ut.utest")));

            //The bundle normally unregister the service, but there is no bundle in this unit test
            serv.unregister();
        } finally {
            //end plugin host
            host.stop();
        }
        //test if the function has been successfully removed
        assertFalse(dm.isDriverFileSupported(new File("ut.utest")));
    }

    /**
     * Unit test of gdms function tracker
     * @throws Exception 
     */
    @Test
    public void installGDMSFunctionService() throws Exception {
        DataManager dataManager = getDataManager();
        assertNotNull(dataManager);
        FunctionManager manager = dataManager.getDataSourceFactory().getFunctionManager();
        PluginHost host = startHost();
        try {
            // Register dummy sql function service
            ServiceRegistration<Function> serv = host.getHostBundleContext()
                   .registerService(Function.class, new DummyScalarFunction(), null);

            // check if the function is registered
            assertTrue(manager.contains("dummy"));
            //The bundle normally unregister the service, but there is no bundle in this unit test
            serv.unregister();
            //end plugin host
        } finally {
            host.stop();

        }
        //test if the function has been successfully removed
        assertFalse(manager.contains("dummy"));
    }
    
    /**
     * Unit test of the entire plugin system
     * @throws Exception 
     */
    @Test
    public void installGDMSFunctionBundle() throws Exception {
        DataManager dataManager = getDataManager();
        assertNotNull(dataManager);
        FunctionManager manager = dataManager.getDataSourceFactory().getFunctionManager();        
        PluginHost host = startHost();
        try {
            File bundlePath = new File("target/bundle/plugin-1.0.jar");
            System.out.println("Install plugin :"+bundlePath.getAbsolutePath());
            assertTrue(bundlePath.exists());
            // Install the external package
            Bundle plugin = host.getHostBundleContext().installBundle("file:"+bundlePath.getAbsolutePath());
            // start it
            plugin.start();
            // test if function exists
            assertTrue(manager.contains("dummy"));

            host.getHostBundleContext().getBundle().stop();
        } finally {
            //end plugin host
            host.stop();
        }
        
        //test if the function has been successfully removed
        assertFalse(manager.contains("dummy"));
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
    
    
    
}
