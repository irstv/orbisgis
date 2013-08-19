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
package org.orbisgis.view.toc.actions.cui.legends.ui;

import org.orbisgis.sif.UIFactory;
import org.orbisgis.sif.UIPanel;

import java.net.URL;

/**
 * Root class for unique symbol UIs.
 *
 * @author Alexis Guéganno
 * @author Adam Gouge
 */
public abstract class PnlUniqueSymbolSE extends PnlNonClassification
        implements UIPanel {

    protected final boolean showCheckbox;
    protected final boolean displayUOM;
    protected final boolean borderEnabled;

    /**
     * Constructor
     *
     * @param showCheckbox  Draw the Enable checkbox?
     * @param displayUOM    Display the unit of measure?
     * @param borderEnabled Is the border enabled?
     */
    protected PnlUniqueSymbolSE(boolean showCheckbox,
                                boolean displayUOM,
                                boolean borderEnabled) {
        this.showCheckbox = showCheckbox;
        this.displayUOM = displayUOM;
        this.borderEnabled = borderEnabled;
    }

    // ************************** UIPanel ****************************
    // Note: The validateInput() method of UIPanel is implemented in
    // PnlNonClassification.
    @Override
    public URL getIconURL() {
        return UIFactory.getDefaultIcon();
    }
}
