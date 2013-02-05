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
package org.orbisgis.view.main.bundles;

import org.orbisgis.core.plugin.BundleReference;
import org.orbisgis.core.plugin.BundleTools;
import org.osgi.framework.BundleContext;

/**
 * Reference to OrbisGIS View special bundle loading process.
 * @author Nicolas Fortin
 */
public class BundleFromResources {
        private static final BundleReference[] SPECIFIC_BEHAVIOUR_BUNDLES = {
                new BundleReference("orbisgis-oshell").setAutoStart(false),
                new BundleReference("org.apache.felix.shell.gui").setAutoStart(false),
        };
        private BundleFromResources() {                
        }
        /**
         * Install and configure built-ins bundles into the current framework.
         * @param hostBundle Host bundle context
         */
        public static void installResourceBundles(BundleContext hostBundle) {
                BundleTools.installBundles(hostBundle, SPECIFIC_BEHAVIOUR_BUNDLES);
        }
}
