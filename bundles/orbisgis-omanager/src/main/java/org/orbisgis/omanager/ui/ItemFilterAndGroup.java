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

package org.orbisgis.omanager.ui;

import java.util.Collection;

/**
 * A filter that include elements only if all provided filters accept the element.
 * @author Nicolas Fortin
 */
public class ItemFilterAndGroup implements ItemFilter<BundleListModel> {
    private Collection<ItemFilter<BundleListModel>> filters;

    /**
     * Constructor, take a collection of Filters
     * @param filters Filter collection
     */
    public ItemFilterAndGroup(Collection<ItemFilter<BundleListModel>> filters) {
        this.filters = filters;
    }

    @Override
    public boolean include(BundleListModel model, int elementId) {
        for(ItemFilter<BundleListModel> filter : filters) {
            if(!filter.include(model,elementId)) {
                return false;
            }
        }
        return true;
    }
}
