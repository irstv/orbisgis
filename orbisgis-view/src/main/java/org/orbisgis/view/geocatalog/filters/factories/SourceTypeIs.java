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
package org.orbisgis.view.geocatalog.filters.factories;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import org.orbisgis.view.components.ContainerItemProperties;
import org.orbisgis.view.components.filter.ActiveFilter;
import org.orbisgis.view.components.filter.FilterFactory;
import org.orbisgis.view.geocatalog.filters.*;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * @brief Source Type filter factory
 */
public class SourceTypeIs implements FilterFactory<IFilter> {
    private static final I18n I18N = I18nFactory.getI18n(SourceTypeIs.class);
    private Map<String,IFilter> filters = new HashMap<String,IFilter>();
    private List<ContainerItemProperties> filterLabelsId = new ArrayList<ContainerItemProperties>();
    private static final String DEFAULT_FILTER = "geocatalog.filters.GeoFilter"; //Filter shown in the GUI by default
    
    /**
     * Add a new type filter
     * @param name The internal name of the filter
     * @param label The GUI label of the filter
     * @param filter The filter instance
     */
    private void addFilter(String name,String label,IFilter filter) {
        filters.put(name,filter);
        filterLabelsId.add(new ContainerItemProperties(name,label));
    }
    /**
     * Initialise built-in source type filters
     */
    public SourceTypeIs() {
        addFilter("geocatalog.filters.AlphanumericFilter",
                I18N.tr("Alphanumeric"),
                new AlphanumericFilter());
        addFilter("geocatalog.filters.DbsFilter",
                I18N.tr("DataBase"),
                new DBsFilter());
        
        addFilter("geocatalog.filters.FilesFilter",
                I18N.tr("Files"),
                new FilesFilter());
        
        addFilter("geocatalog.filters.GeoFilter",
                I18N.tr("Geometry"),
                new GeoFilter());

        addFilter("geocatalog.filters.RasterFilter",
                I18N.tr("Raster"),
                new RasterFilter());

        addFilter("geocatalog.filters.VectorialFilter",
                I18N.tr("Vectorial"),
                new VectorialFilter());
        
        addFilter("geocatalog.filters.WMSFilter",
                I18N.tr("Web Map Services"),
                new WMSFilter());
        
        addFilter("geocatalog.filters.TableSystemFilter",
                I18N.tr("System Table"),
                new TableSystemFilter());
    }
    
    /**
     * The factory ID
     *
     * @return Internal name of the filter type
     */
    @Override
    public String getFactoryId() {
        return "type_is";
    }

    /**
     * The user see this label when choosing a filter from a list
     *
     * @return
     */
    @Override
    public String getFilterLabel() {
        return I18N.tr("Source type is");
    }

    /**
     * Make the filter corresponding to the filter value
     *
     * @param filterValue The new value fired by PropertyChangeEvent
     * @return
     */
    @Override
    public IFilter getFilter(String filterValue) {
        if(!filters.containsKey(filterValue)) {
            return filters.get(DEFAULT_FILTER);
        } else {
            return filters.get(filterValue);            
        }
    }


    /**
     * The DataSourceFilterFactory build the component that let the user to
     * define the filter parameters. 
     * @param filterValue When the control change the ActiveFilter value must be updated
     * @return The swing component.
     */
    @Override
    public Component makeFilterField(ActiveFilter filterValue) {
        JComboBox filterField = new JComboBox(filterLabelsId.toArray());
        if(filters.containsKey(filterValue.getCurrentFilterValue())) {
            filterField.setSelectedItem(new ContainerItemProperties(filterValue.getCurrentFilterValue(), ""));
        } else {
            filterField.setSelectedItem(new ContainerItemProperties(DEFAULT_FILTER,""));
        }
        //Add listener to update the filterValue
        filterField.addActionListener(EventHandler.create(ActionListener.class,
                filterValue,"setCurrentFilterValue",
                "source.getSelectedItem.getKey"
                ));
        return filterField;
    }
}

