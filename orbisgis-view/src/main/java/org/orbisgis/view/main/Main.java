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
package org.orbisgis.view.main;

import java.util.Stack;
import javax.swing.JOptionPane;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * Entry point of User Interface.
 */
final class Main 
{
    private static final I18n I18N = I18nFactory.getI18n(Main.class);
    private static boolean DEBUG_MODE=false;

    //Minimum supported java version
    public static final char MIN_JAVA_VERSION = '7';
    
    /**
     * Utility class
     */
    private Main() {

    }

    private static void parseCommandLine(String[] args) {
        //Read parameters
        Stack<String> sargs=new Stack<String>();
        for(String arg : args) {
            sargs.insertElementAt(arg, 0);
        }
        while(!sargs.empty()) {
            String argument=sargs.pop();
            if(argument.contentEquals("-debug")) {
                DEBUG_MODE=true;
            }
        }
    }

    /**
    * Entry point of User Interface
    */
    public static void main( String[] args )
    {
        parseCommandLine(args);
            //Check if the java version is greater than 1.6+
            if (!isVersion(MIN_JAVA_VERSION)) {
                    JOptionPane.showMessageDialog(null, I18N.tr("OrbisGIS needs at least a java 1.7+"));
            } else {
                    // Listen to future workspace change
                    CoreLauncher coreLauncher = new CoreLauncher();
                    coreLauncher.init(DEBUG_MODE);
                    coreLauncher.launch();
            }
    }

        /**
         * Utility method to check if the java machine is supported.
         *
         * @param minJavaVersion
         * @return
         */
        private static boolean isVersion(char minJavaVersion) {
                String version = System.getProperty("java.version");
                char minor = version.charAt(2);
                if (minor >= minJavaVersion ) {
                        return true;
                }
                return false;
        }
}
