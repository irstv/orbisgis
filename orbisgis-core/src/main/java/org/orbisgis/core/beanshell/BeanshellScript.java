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
package org.orbisgis.core.beanshell;

import bsh.EvalError;
import bsh.Interpreter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.orbisgis.core.context.main.MainContext;
import org.orbisgis.core.plugin.BundleReference;
import org.orbisgis.core.plugin.BundleTools;
import org.orbisgis.core.workspace.CoreWorkspaceImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 *
 * @author Erwan Bocher
 */
public final class BeanshellScript {
        private static MainContext mainContext;
        private static Interpreter interpreter;
        public static final String ARG_WORKSPACE = "-wks";
        public static final String ARG_APPFOLDER = "-af";
        public static final String ARG_DEBUG = "debug";

        /**
         * Entry point.
         * @param args
         * @throws EvalError
         * @throws FileNotFoundException
         */
        public static void main(String[] args) throws EvalError, FileNotFoundException {
                if (args.length == 0) {
                        printHelp();
                } else {
                        // Check script file
                        String script = args[0];
                        if (script != null && !script.isEmpty()) {
                            File file = new File(script);
                            if (!file.isFile()){
                                printHelp();
                            } else if (!file.exists()) {
                                System.err.println("The file doesn't exist.");
                            } else {
                                // Script file is here, init the engine
                                try {
                                    if(init(args)) {
                                        execute(args);
                                    }
                                } finally {
                                    dispose();
                                }
                            }
                        } else {
                            System.err.print("The second parameter must be not null.\n");
                        }
                }
        }

        /**
         * This class is used to load a datasourcefactory
         */
        private static void servicesRegister(Map<String,String> parameters) throws IllegalArgumentException {
                CoreWorkspaceImpl coreWorkspace = new CoreWorkspaceImpl();
                if(parameters.containsKey(ARG_APPFOLDER)) {
                    coreWorkspace.setApplicationFolder(new File(parameters.get(ARG_APPFOLDER)).getAbsolutePath());
                }
                if(parameters.containsKey(ARG_WORKSPACE)) {
                    coreWorkspace.setWorkspaceFolder(new File(parameters.get(ARG_WORKSPACE)).getAbsolutePath());
                } else {
                    File defaultWorkspace = coreWorkspace.readDefaultWorkspacePath();
                    if(defaultWorkspace==null) {
                        List<File> workspacesPath = coreWorkspace.readKnownWorkspacesPath();
                        if(!workspacesPath.isEmpty()) {
                            coreWorkspace.setWorkspaceFolder(workspacesPath.get(0).getAbsolutePath());
                        }
                    }
                }
                // Create workspace folder if it does no exists
                File workspace = new File(coreWorkspace.getWorkspaceFolder());
                if(!workspace.exists()) {
                    if(!workspace.mkdirs()) {
                        throw new IllegalArgumentException("Could not create workspace folder, check disk space and rights");
                    }
                }
                MainContext.initConsoleLogger(true);
                mainContext = new MainContext(parameters.containsKey(ARG_DEBUG),coreWorkspace,true);
                // Read user default workspace, or use predefined one
                // Launch OSGi
                mainContext.startBundleHost(new BundleReference[0]);
                // Show active bundles
                if(parameters.containsKey(ARG_DEBUG)) {
                    printActiveBundles(mainContext.getPluginHost().getHostBundleContext());
                }
                // Init BDD
                try {
                    mainContext.initDataBase("","");
                } catch (SQLException ex) {
                    throw new IllegalArgumentException("Cannot connect to the database "+ex.getLocalizedMessage(),ex);
                }
        }

        /**
         * Show the list of active bundles and their services.
         * @param context Active bundle context
         */
        public static void printActiveBundles(BundleContext context) {
            for (Bundle bundle : context.getBundles()) {
                System.out.println(
                        "[" + bundle.getBundleId() + "]\t"
                                + BundleTools.getStateString(bundle.getState())
                                + bundle.getSymbolicName());
                // List services
                ServiceReference[] serviceReferences = bundle.getRegisteredServices();
                if(serviceReferences!=null) {
                    for(ServiceReference service : serviceReferences) {
                        System.out.println(
                                "\t"+service.toString());
                    }
                }
            }
        }
        /**
         *
         * @param offset Read args from this index
         * @param args command line arguments
         * @return Map of key value of arguments
         */
        private static Map<String,String> parseArgs(int offset,String[] args) {
            //Pairs
            Map<String,String> pairs = new HashMap<String, String>();
            for(int i=offset;i<args.length;i++) {
                String key = args[i];
                if(i+1<args.length && key.startsWith("-")) {
                    i++;
                    pairs.put(key,args[i]);
                } else {
                    pairs.put(key,"");
                }
            }
            return pairs;
        }

        /**
         * Static init made public for unit test
         * @return True if ready to call execute method
         */
        public static boolean init(String[] args)  throws EvalError, FileNotFoundException {
            servicesRegister(parseArgs(1,args));
            interpreter = new Interpreter();
            interpreter.set("bsh.args", args);
            interpreter.set("bsh.bundleContext", mainContext.getPluginHost().getHostBundleContext());
            interpreter.set("bsh.dataSource", mainContext.getDataSource());
            interpreter.eval("setAccessibility(true)");
            return true;
        }

        /**
         * Static dispose made public for unit test
         */
        public static void dispose() {
            if(mainContext!=null) {
                mainContext.dispose();
            }
        }


        /**
         * Here the method to execute the beanshell script.Made public for unit test. Must be called after init() and before dipose()
         * @param  args Arguments [Script file, ..]
         * @throws EvalError
         * @throws FileNotFoundException 
         */
        public static void execute(String[] args) throws EvalError, FileNotFoundException {
                interpreter.set("bsh.args", args);
                interpreter.set("bsh.dataManager", mainContext.getDataManager());
                interpreter.setOut(System.out);
                interpreter.setClassLoader(mainContext.getClass().getClassLoader());
                FileReader reader = new FileReader(new File(args[0]));
                interpreter.eval(reader);
        }

        /**
         * Print the help associated to this executable.
         */
        public static void printHelp() {
                System.out.print(getHelp());
        }

        /**
         * Get the help associated to this executable.
         */
        public static String getHelp() {
                return "Beanshell script arguments. The first argument must be  a path to the script file.\n" +
                        "orbisshell.sh scriptpath [options]\n" +
                        "Options :\n" +
                        "\t"+ARG_WORKSPACE+" path\tWorkspace folder\n" +
                        "\t"+ARG_APPFOLDER+" path\tApplication folder\n" +
                        "\t"+ARG_DEBUG+"\t\tDebug mode\n";
        }

        private BeanshellScript() {
        }

        /**
         * @return Application context
         */
        public static MainContext getMainContext() {
            return mainContext;
        }
}
