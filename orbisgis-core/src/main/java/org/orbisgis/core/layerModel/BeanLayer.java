/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able
 * to manipulate and create vector and raster spatial information. OrbisGIS
 * is distributed under GPL 3 license. It is produced  by the geo-informatic team of
 * the IRSTV Institute <http://www.irstv.cnrs.fr/>, CNRS FR 2488:
 *    Erwan BOCHER, scientific researcher,
 *    Thomas LEDUC, scientific researcher,
 *    Fernando GONZALEZ CORTES, computer engineer.
 *
 * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OrbisGIS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult:
 *    <http://orbisgis.cerma.archi.fr/>
 *    <http://sourcesup.cru.fr/projects/orbisgis/>
 *
 * or contact directly:
 *    erwan.bocher _at_ ec-nantes.fr
 *    fergonco _at_ gmail.com
 *    thomas.leduc _at_ cerma.archi.fr
 */
package org.orbisgis.core.layerModel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.opengis.ows_context.*;
import org.gdms.data.DataSource;
import org.gdms.driver.DriverException;
import org.gdms.source.Source;
import org.orbisgis.core.renderer.se.SeExceptions.InvalidStyle;
import org.orbisgis.core.renderer.se.Style;
import org.orbisgis.core.renderer.se.common.Description;


/**
 * This class provide bean access to properties and jaxb (de)serialisation
 */
public abstract class BeanLayer extends AbstractLayer {

        //Listener container
        protected transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

        //bean properties
        private Description description;
        private List<Style> styleList;
        private boolean visible = true;
        
        public BeanLayer(String name) {
                super();
                description = new Description();
                description.addTitle(Locale.getDefault(), name);
        }
        
	public BeanLayer(LayerType layerType) {
                super();
                try {
                        description = new Description(layerType);
                } catch (InvalidStyle ex) {
                        LOGGER.error(ex);
                }
	}

        /**
         * Get the value of visible
         *
         * @return the value of visible
         */
        public boolean isVisible() {
                return visible;
        }

        /**
         * Set the value of visible
         *
         * @param visible new value of visible
         */
        public void setVisible(boolean visible) throws LayerException  {
                boolean oldVisible = this.visible;
                this.visible = visible;
                propertyChangeSupport.firePropertyChange(PROP_VISIBLE, oldVisible, visible);
                //Deprectated listener
                fireVisibilityChanged();
        }
        public LayerType getJAXBElement() {
                ObjectFactory ows_context_factory = new ObjectFactory();
                net.opengis.se._2_0.core.ObjectFactory se_of = new net.opengis.se._2_0.core.ObjectFactory();
                LayerType layerType = ows_context_factory.createLayerType();
                description.initJAXBType(layerType);
                ILayer[] childrens = getChildren();
                for(ILayer children : childrens) {
                        layerType.getLayer().add(children.getJAXBElement());
                }
                // If not a Layer Collection
                if(styleList!=null) {
                        StyleListType slt = ows_context_factory.createStyleListType();
                        layerType.setStyleList(slt);
                        for(Style style : styleList) {
                                StyleType st = ows_context_factory.createStyleType();
                                slt.getStyle().add(st);
                                SLDType sltType = ows_context_factory.createSLDType();
                                st.setSLD(sltType);
                                sltType.setAbstractStyle(style.getJAXBElement());
                        }                        
                }
                DataSource dataSource = getDataSource();
                //Serialisation of dataSource as a DataUrl string
                if(dataSource!=null) {
                        //Create jaxb instances
                        URLType dataURL = ows_context_factory.createURLType();
                        OnlineResourceType resource = ows_context_factory.createOnlineResourceType();
                        dataURL.setOnlineResource(resource);
                        //Retrieve data source properties
                        Source src = dataSource.getSource();                                                
                        //Serialisation of the data source into a single string
                        String resourceSerialisation = "";
                        try {
                                URI srcUri = src.getURI();
                                resourceSerialisation = srcUri.toString();
                        } catch (DriverException ex) {
                                LOGGER.error(I18N.tr("Unable to serialise the data source of layer {0}",getName()),ex);
                        }
                        
                        resource.setHref(resourceSerialisation);
                        if(!resourceSerialisation.isEmpty()) {
                                layerType.setDataURL(dataURL);                                
                        }
                }
                return layerType;
        }
        
        
        
        /**
         * Get the value of description
         *
         * @return the value of description
         */
        public Description getDescription() {
                return description;
        }

        /**
         * Set the value of description
         *
         * @param description new value of description
         */
        public void setDescription(Description description) {
                Description oldDescription = this.description;
                this.description = description;
                propertyChangeSupport.firePropertyChange(PROP_DESCRIPTION, oldDescription, description);
        }
    
        /**
        * Add a property-change listener for all properties.
        * The listener is called for all properties.
        * @param listener The PropertyChangeListener instance
        * @note Use EventHandler.create to build the PropertyChangeListener instance
        */
        public void addPropertyChangeListener(PropertyChangeListener listener) {
                propertyChangeSupport.addPropertyChangeListener(listener);
        }
        /**
        * Add a property-change listener for a specific property.
        * The listener is called only when there is a change to 
        * the specified property.
        * @param prop The static property name PROP_..
        * @param listener The PropertyChangeListener instance
        * @note Use EventHandler.create to build the PropertyChangeListener instance
        */
        public void addPropertyChangeListener(String prop,PropertyChangeListener listener) {
                propertyChangeSupport.addPropertyChangeListener(prop, listener);
        }
        /**
        * Remove the specified listener from the list
        * @param listener The listener instance
        */
        public void removePropertyChangeListener(PropertyChangeListener listener) {
                propertyChangeSupport.removePropertyChangeListener(listener);
        }

        /**
        * Remove the specified listener for a specified property from the list
        * @param prop The static property name PROP_..
        * @param listener The listener instance
        */
        public void removePropertyChangeListener(String prop,PropertyChangeListener listener) {
                propertyChangeSupport.removePropertyChangeListener(prop,listener);
        }
        
	/**
	 * 
	 * @see org.orbisgis.core.layerModel.ILayer#getName()
	 */
	public String getName() {
		return description.getTitle(Locale.getDefault());
	}

	/**
	 * 
	 * @throws LayerException
	 * @see org.orbisgis.core.layerModel.ILayer#setName(java.lang.String)
	 */
	public void setName(final String name) throws LayerException {
                if(name!=null && !name.equals(getName())) {
                        //Set the localised title of this layer
                        final Set<String> allLayersNames = getRoot().getAllLayersNames();
                        allLayersNames.remove(getName());
                        String unUsedName = provideNewLayerName(name, allLayersNames);                
                        description.addTitle(Locale.getDefault(), unUsedName);
                        setDescription(description);
                        fireNameChanged();
                }
	}
       
        /**
         * Initialize the list of styles, letting it empty and not notifying the
         * listeners.
         */
        protected final void initStyles(){
                styleList = new ArrayList<Style>();
        }
        
    @Override
    public List<Style> getStyles() {
        return styleList;
    }

    @Override
    public void setStyles(List<Style> fts) {
        List<Style> oldStyles = this.styleList;
        this.styleList = fts;
        propertyChangeSupport.firePropertyChange(PROP_STYLES, oldStyles, styleList);
        super.setStyles(fts);
    }

    @Override
    public Style getStyle(int i){
            return styleList.get(i);
    }

    @Override
    public void setStyle(int i, Style s){
        if (styleList == null){
            styleList = new ArrayList<Style>(); //out of bound exception instead of null pointer exception
        }
        Style oldStyle = styleList.get(i);
        styleList.set(i, s);
        propertyChangeSupport.fireIndexedPropertyChange(PROP_STYLES, i, oldStyle, s);
        this.fireStyleChanged();
    }

    @Override
    public void addStyle(Style s){
        if (styleList == null){
            styleList = new ArrayList<Style>();
        }
        styleList.add(s);
        propertyChangeSupport.firePropertyChange(PROP_STYLES, null, styleList);
        this.fireStyleChanged();
    }

    @Override
    public void addStyle(int i, Style s){
        if (styleList == null){
            styleList = new ArrayList<Style>();
        }
        styleList.add(i, s);
        propertyChangeSupport.fireIndexedPropertyChange(PROP_STYLES, i, null, s);
        this.fireStyleChanged();
    }

    @Override
    public int indexOf(Style s){
            return styleList == null ? -1 : styleList.indexOf(s);
    }

    @Override
    public void removeStyle(Style s){
            styleList.remove(s);
            propertyChangeSupport.firePropertyChange(PROP_STYLES, s, null);
            this.fireStyleChanged();
    }
}
