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
package org.orbisgis.sif.multiInputPanel;

import java.awt.Component;
import javax.swing.JList;
import javax.swing.JScrollPane;
import org.orbisgis.sif.SQLUIPanel;

public class ListChoice implements InputType {
	public static final String SEPARATOR = "#";
	private JList comp;

	public ListChoice(String... choices) {
		comp = new JList(choices);
	}

	public Component getComponent() {
		return new JScrollPane(comp);
	}

	public int getType() {
		return SQLUIPanel.STRING;
	}

	public String getValue() {
		final Object[] selectedValues = comp.getSelectedValues();
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < selectedValues.length; i++) {
			sb.append(selectedValues[i]);
			if (i + 1 != selectedValues.length) {
				sb.append(SEPARATOR);
			}
		}
		return sb.toString();
	}

	public void setSelectionMode(int selectionMode) {
		comp.setSelectionMode(selectionMode);
	}

	public void setValue(String value) {
		if (null != value) {
			comp.setListData(value.split(SEPARATOR));
		}
	}

	public boolean isPersistent() {
		return true;
	}
}