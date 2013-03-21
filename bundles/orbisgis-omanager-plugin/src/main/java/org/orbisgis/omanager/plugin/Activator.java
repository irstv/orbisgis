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
package org.orbisgis.omanager.plugin;

import org.orbisgis.view.main.frames.ext.MainFrameAction;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.obr.RepositoryAdmin;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Register the OSGi plugin manager menu item.
 * @author Nicolas Fortin
 */
public class Activator implements BundleActivator {
        private ServiceTracker<RepositoryAdmin,RepositoryAdmin> obrTracker;

        @Override
        public void start(BundleContext bc) throws Exception {
            // Register the new main menu item "manage plug-ins"
            bc.registerService(MainFrameAction.class,new ManagerMenuFactory(bc),null);
            obrTracker = new ServiceTracker<RepositoryAdmin,RepositoryAdmin>(bc,RepositoryAdmin.class,new RepositoryAdminTracker(bc));
            obrTracker.open();
        }

        @Override
        public void stop(BundleContext bc) throws Exception {
            // Save Repository list
            obrTracker.close();
        }
}
