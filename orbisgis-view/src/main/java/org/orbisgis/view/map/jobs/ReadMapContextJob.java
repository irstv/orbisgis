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
package org.orbisgis.view.map.jobs;

import org.apache.log4j.Logger;
import org.orbisgis.core.Services;
import org.orbisgis.progress.ProgressMonitor;
import org.orbisgis.view.background.BackgroundJob;
import org.orbisgis.view.edition.EditorManager;
import org.orbisgis.view.map.MapElement;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * Open the MapContext and 
 * @author Nicolas Fortin
 */
public class ReadMapContextJob implements BackgroundJob {

        private static final I18n I18N = I18nFactory.getI18n(ReadMapContextJob.class);   
        MapElement editableMap;

        public ReadMapContextJob(MapElement editableMap) {
                this.editableMap = editableMap;
        }
        

        @Override
        public void run(ProgressMonitor pm) {
                EditorManager editors = Services.getService(EditorManager.class);
                editableMap.open(pm);
                editors.openEditable(editableMap);
        }

        @Override
        public String getTaskName() {
                return I18N.tr("Open the map context");
        }
}
