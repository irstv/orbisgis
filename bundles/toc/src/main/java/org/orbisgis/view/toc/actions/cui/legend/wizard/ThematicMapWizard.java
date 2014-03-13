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
package org.orbisgis.view.toc.actions.cui.legend.wizard;

import org.apache.log4j.Logger;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;
import org.orbisgis.core.layerModel.ILayer;
import org.orbisgis.core.map.MapTransform;
import org.orbisgis.sif.UIFactory;
import org.orbisgis.sif.UIPanel;
import org.orbisgis.view.toc.actions.cui.LegendContext;
import org.orbisgis.view.toc.actions.cui.SimpleGeometryType;
import org.orbisgis.view.toc.actions.cui.legend.ILegendPanel;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This UIPanel intends to host a ILegendPanel instance that will
 * be added to a layer
 * @author Alexis Guéganno, Erwan Bocher
 */
public class ThematicMapWizard implements UIPanel, LegendContext {

    private static final Logger LOGGER = Logger.getLogger(ThematicMapWizard.class);
    private static final I18n I18N = I18nFactory.getI18n(ThematicMapWizard.class);
    private int geometryType = SimpleGeometryType.ALL;
    private ILegendPanel inner;
    private JPanel jp;
    private ILayer layer;
    private MapTransform mt;
    private JTextField field;

    /**
     * Builds the WizardPanel. The objects it will create will be linked to {@code l}.
     * @param l The parent layer.
     * @param m Needed for LegendContext implementation.
     */
    public ThematicMapWizard(ILayer l, MapTransform m){
        layer = l;
        try(Connection connection = l.getDataManager().getDataSource().getConnection()) {
            TableLocation tableLocation = TableLocation.parse(l.getTableReference());
            int type = SFSUtilities.getGeometryType(connection, tableLocation,
                    SFSUtilities.getGeometryFields(connection, tableLocation).get(0));
            this.geometryType = SimpleGeometryType.getSimpleType(type);
            mt = m;
        } catch (SQLException e) {
            LOGGER.error("Error while reading the data source");
        }

    }

    @Override
    public URL getIconURL() {
        return UIFactory.getDefaultIcon();
    }

    @Override
    public String getTitle() {
        return I18N.tr("Create a thematic map");
    }

    @Override
    public String validateInput() {
        StringBuilder sb = new StringBuilder();
        if(field.getText().isEmpty()){
            sb.append(I18N.tr("The name shall not be empty."));
        }
        if(inner != null){
            String s = inner.validateInput();
            if(s!=null){
                sb.append("\n");
                sb.append(s);
            }
        }
        String ret = sb.toString();
        return ret.isEmpty() ? null : ret;
    }

    @Override
    public Component getComponent() {
        if(jp == null){
            prepareJPanel();
        }
        return jp;
    }

    /**
     * Prepare the inner JPanel.
     */
    private void prepareJPanel(){
        jp = new JPanel();
        BoxLayout layout = new BoxLayout(jp, BoxLayout.PAGE_AXIS);
        jp.setLayout(layout);
        JPanel text = new JPanel();
        text.add(new JLabel(I18N.tr("Name")));
        field = new JTextField(25);
        text.add(field);
        text.setAlignmentX(Component.CENTER_ALIGNMENT);
        jp.add(text);
        if(inner != null){
            jp.add(inner.getComponent());
        }

    }

    /**
     * Replace the inner legend with the given one. If the input is null,
     * the child component is simply removed.
     * @param ilp The new ILegendPanel.
     */
    public void setInnerLegend(ILegendPanel ilp){
        if(jp==null){
            prepareJPanel();
        }
        if(inner != null){
            Component comp = inner.getComponent();
            if(jp.isAncestorOf(comp)){
                jp.remove(comp);
            }
        }
        inner = ilp;
        if(ilp !=null){
            jp.add(inner.getComponent());            
            field.setText(ilp.getLegend().getLegendTypeName());
        }
    }

    /**
     * Gets the name that has been entered by the user.
     * @return The name entered by the user.
     */
    public String getName(){
        return field.getText();
    }

    /**
     *Retrieve the inner ILegendPanel.
     */
    public ILegendPanel getInnerLegend(){
        return inner;
    }

    @Override
    public int getGeometryType() {
        return geometryType;
    }

    @Override
    public boolean isPoint() {
        return (geometryType & SimpleGeometryType.POINT) > 0;
    }

    @Override
    public boolean isLine() {
        return (geometryType & SimpleGeometryType.LINE) > 0;
    }

    @Override
    public boolean isPolygon() {
        return (geometryType & SimpleGeometryType.POLYGON) > 0;
    }

    @Override
    public ILayer getLayer() {
        return layer;
    }

    @Override
    public MapTransform getCurrentMapTransform() {
        return mt;
    }
}
