/*
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

package org.orbisgis.mapeditor.map.mapsManager;

import org.orbisgis.viewapi.docking.DockingPanelLayout;
import org.orbisgis.viewapi.util.PropertyHost;
import org.orbisgis.viewapi.util.XElement;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data to keep between application start/stop.
 * @author Nicolas Fortin
 */
public class MapsManagerPersistence implements DockingPanelLayout, Serializable, PropertyHost {
    private static final long serialVersionUID = 2L;
    private static final String ORBISGIS_MAPCATALOG_SERVER= "http://services.orbisgis.org/map";
    /**
     * Property of server uri list {@link org.orbisgis.mapeditor.map.MapEditorPersistence#getMapsManagerPersistence()}
     */
    public static final String PROP_SERVER_URI_LIST = "mapCatalogUrlList";
    public static final String PROP_FOLDER_LIST = "mapCatalogUrlList";
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private List<String> mapCatalogUrlList = new ArrayList<>();
    private List<String> mapCatalogFolderList = new ArrayList<>();

    // XML consts
    private static final String URL_LIST_NODE = "mapCatalogUrlList";
    private static final String URL_NODE = "mapcatalog";
    private static final String URL_NODE_PROPERTY = "url";

    private static final String FOLDER_LIST_NODE = "mapCatalogFolderList";
    private static final String FOLDER_NODE = "mapcatalog";
    private static final String FOLDER_NODE_PROPERTY = "path";

    /**
     * Set the map context server list
     * @param urlList Remote map catalog urls
     */
    public void setMapCatalogUrlList(List<String> urlList) {
        List<String> oldList = mapCatalogUrlList;
        mapCatalogUrlList = new ArrayList<>(urlList);
        if(mapCatalogUrlList.isEmpty()) {
            mapCatalogUrlList.add(ORBISGIS_MAPCATALOG_SERVER);
        }
        propertyChangeSupport.firePropertyChange(PROP_SERVER_URI_LIST,oldList,getMapCatalogUrlList());
    }

    /**
     * Retrieve the list of map context
     * @return Remote map catalog urls
     */
    public List<String> getMapCatalogUrlList() {
        return Collections.unmodifiableList(mapCatalogUrlList);
    }

    /**
     * @return The list of user defined map's folders.
     */
    public List<String> getMapCatalogFolderList() {
        return mapCatalogFolderList;
    }

    /**
     * @param mapCatalogFolderList The list of user defined map's folders.
     */
    public void setMapCatalogFolderList(List<String> mapCatalogFolderList) {
        List<String> oldList = this.mapCatalogFolderList;
        this.mapCatalogFolderList = mapCatalogFolderList;
        propertyChangeSupport.firePropertyChange(PROP_FOLDER_LIST,oldList,mapCatalogFolderList);
    }

    @Override
    public void writeStream(DataOutputStream out) throws IOException {
        out.writeLong(serialVersionUID);
        out.writeInt(mapCatalogUrlList.size());
        for(String mcUrl : mapCatalogUrlList) {
            out.writeUTF(mcUrl);
        }
        out.writeInt(mapCatalogFolderList.size());
        for(String mcFolder : mapCatalogFolderList) {
            out.writeUTF(mcFolder);
        }
    }

    @Override
    public void readStream(DataInputStream in) throws IOException {
        // Check version
        long version = in.readLong();
        if(version>=1) {
            List<String> urlList = new ArrayList<>();
            if(version==serialVersionUID) {
                int size = in.readInt();
                for(int i=0;i<size;i++) {
                    urlList.add(in.readUTF());
                }
            }
            setMapCatalogUrlList(urlList);
            if(version >= 2) {
                int size = in.readInt();
                List<String> folderList = new ArrayList<>();
                for(int i=0;i<size;i++) {
                    folderList.add(in.readUTF());
                }
                setMapCatalogFolderList(folderList);
            }
        }
    }

    @Override
    public void writeXML(XElement element) {
        XElement urlList = element.addElement(URL_LIST_NODE);
        urlList.addLong("serialVersionUID", serialVersionUID);
        for(String mcUrl : mapCatalogUrlList) {
            urlList.addElement(URL_NODE).addString(URL_NODE_PROPERTY, mcUrl);
        }
        XElement pathList = element.addElement(FOLDER_LIST_NODE);
        for(String mcUrl : mapCatalogFolderList) {
            pathList.addElement(FOLDER_NODE).addString(FOLDER_NODE_PROPERTY, mcUrl);
        }
    }

    @Override
    public void readXML(XElement element) {
        XElement managerNode = element.getElement(URL_LIST_NODE);
        long version = managerNode.getLong("serialVersionUID");
        List<String> urlList = new ArrayList<>();
        if(version>=1) {
            for(XElement map : managerNode.children()) {
                if(map.getName().equals(URL_NODE)) {
                    urlList.add(map.getString(URL_NODE_PROPERTY));
                }
            }
            if(version >= 2) {
                List<String> folderList = new ArrayList<>();
                XElement folderNode = element.getElement(FOLDER_LIST_NODE);
                for(XElement map : folderNode.children()) {
                    if(map.getName().equals(FOLDER_NODE)) {
                        folderList.add(map.getString(FOLDER_NODE_PROPERTY));
                    }
                }
                setMapCatalogFolderList(folderList);
            }
        }
        setMapCatalogUrlList(urlList);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(String prop, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(prop,listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(String prop, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(prop,listener);
    }

}
