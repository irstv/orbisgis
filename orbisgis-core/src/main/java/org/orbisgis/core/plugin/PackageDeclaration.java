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

import org.osgi.framework.Version;

/**
 * Declaration of an host package, with version number.
 * @author Nicolas Fortin
 */
public class PackageDeclaration {
    
    private String packageName;
    private Version version;
    /**
     * Full information on a package
     * @param packageName Package identifier
     * @param majorVersion 
     * @param minorVersion
     * @param revisionVersion 
     */
    public PackageDeclaration(String packageName, int majorVersion, int minorVersion, int revisionVersion) {
        this.packageName = packageName;
        this.version = new Version(majorVersion,minorVersion,revisionVersion);
    }

    /**
     * Declaration of a package without a version number, not recommended.
     * @param packageName Package identifier
     */
    public PackageDeclaration(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Declaration of a package without a version number, not recommended.
     * @param packageName Package identifier
     */
    public PackageDeclaration(String packageName,Version version) {
        this.packageName = packageName;
        this.version = version;
    }
    /**
     * @return If revisionVersion has been set
     */
    public boolean isVersionDefined() {
        return version!=null && !version.equals(new Version(0,0,0));
    }   

    /**
     * Get the value of packageName
     *
     * @return the value of packageName
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Get the package version
     * @return null or instance of Version.
     */
    public Version getVersion() {
        return version;
    }
}
