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

package org.orbisgis.view.table;


import org.apache.log4j.Logger;
import org.gdms.driver.DriverException;
import org.orbisgis.view.components.actions.ActionTools;
import org.orbisgis.view.icons.OrbisGISIcon;
import org.orbisgis.view.table.ext.TableEditorActions;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.beans.EventHandler;
import java.beans.PropertyChangeListener;

/**
 * Lock/Unlock table edition action.
 * @author Nicolas Fortin
 */
public class ActionSave extends AbstractAction {
    private final TableEditableElement editable;
    private static final I18n I18N = I18nFactory.getI18n(ActionSave.class);
    private final Logger logger = Logger.getLogger(ActionSave.class);

    public ActionSave(TableEditableElement editable) {
        super(I18N.tr("Save"), OrbisGISIcon.getIcon("save"));
        putValue(ActionTools.MENU_ID, TableEditorActions.A_SAVE);
        putValue(ActionTools.LOGICAL_GROUP, TableEditorActions.LGROUP_EDITION);
        this.editable = editable;
        updateState();
        editable.addPropertyChangeListener(TableEditableElement.PROP_MODIFIED,
                EventHandler.create(PropertyChangeListener.class,this,"updateState"));
    }

    /**
     * Called when the edition state of TableEditableElement change.
     */
    public final void updateState() {
        setEnabled(editable.isModified());
    }
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        try {
            editable.getDataSource().commit();
        } catch (Exception ex) {
            logger.error(ex.getLocalizedMessage(),ex);
        }
    }
}
