/**
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 *
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier
 * SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
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
 * For more information, please consult: <http://www.orbisgis.org/> or contact
 * directly: info_at_ orbisgis.org
 */
package org.orbisgis.view.geocatalog.sourceWizards.wms;

import java.util.ArrayList;
import javax.swing.AbstractListModel;
import javax.swing.ListModel;

/**
 * 
 * @author Erwan Bocher
 */
public class SRSListModel extends AbstractListModel implements ListModel {

	private String[] srsNames;
	private String nameFilter;
	private String[] srsNamesIn;

        /**
         * Set the list of SRS names managed by the listModel.
         * @param srsNamesIn 
         */
	public SRSListModel(String[] srsNamesIn) {
		this.srsNamesIn = srsNamesIn;
		refresh();
	}

	@Override
	public Object getElementAt(int index) {
		return srsNames[index];
	}

	@Override
	public int getSize() {
		return srsNames.length;
	}

        /**
         * A method to refresh the list of SRS names
         * @param text 
         */
	public void filter(String text) {
		if (text.trim().length() == 0) {
			text = null;
		}
		this.nameFilter = text;
		refresh();
	}

        /**
         * update the array of SRS names and event the listModel. 
         */
	private void refresh() {
		srsNames = srsNamesIn;
		if (nameFilter != null) {

			ArrayList<String> names = new ArrayList<String>();

			for (String srsName : srsNames) {
				if (srsName.contains(nameFilter)) {
					names.add(srsName);
				}
			}
			this.srsNames = names.toArray(new String[names.size()]);

		}

		fireIntervalRemoved(this, 0, getSize());
		fireIntervalAdded(this, 0, getSize());

	}
	
}
