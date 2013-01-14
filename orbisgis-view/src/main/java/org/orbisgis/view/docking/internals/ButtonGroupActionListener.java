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
package org.orbisgis.view.docking.internals;

import bibliothek.gui.Dockable;
import bibliothek.gui.dock.action.DockAction;
import bibliothek.gui.dock.action.DropDownAction;
import bibliothek.gui.dock.common.action.CRadioGroup;
import bibliothek.gui.dock.event.DropDownActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import org.orbisgis.view.components.actions.DefaultAction;
import org.orbisgis.view.docking.internals.actions.CToggleButton;

/**
 * CDropDownAction is not suitable for CRadioGroup, this listener 
 * will desactivate all CRadioAction when an action occur on a CAction
 */
public class ButtonGroupActionListener implements DropDownActionListener,ActionListener {
    private CRadioGroup radioGroup;

    public ButtonGroupActionListener(CRadioGroup radioGroup) {
        this.radioGroup = radioGroup;
    }
    private void deselectAllButtons() {
        CToggleButton tb = new CToggleButton(new DefaultAction("",""));
        radioGroup.add(tb);
        tb.setSelected(true);
        radioGroup.remove(tb);      
    }
    public void selectionChanged(DropDownAction action, Set<Dockable> dockables, DockAction selection) {
        deselectAllButtons();
    }

    public void actionPerformed(ActionEvent ae) {
        deselectAllButtons();
    }
    
}
