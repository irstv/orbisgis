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
package org.orbisgis.view.toc.actions.cui.legends;

import org.apache.log4j.Logger;
import org.gdms.data.DataSource;
import org.gdms.data.schema.Metadata;
import org.gdms.data.types.TypeFactory;
import org.gdms.driver.DriverException;
import org.orbisgis.core.renderer.se.common.Uom;
import org.orbisgis.legend.Legend;
import org.orbisgis.legend.thematic.uom.StrokeUom;
import org.orbisgis.sif.UIFactory;
import org.orbisgis.sif.common.ContainerItemProperties;
import org.orbisgis.sif.components.ColorPicker;
import org.orbisgis.sif.components.WideComboBox;
import org.orbisgis.view.toc.actions.cui.components.CanvasSE;
import org.orbisgis.view.toc.actions.cui.legends.panels.UomCombo;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.EventHandler;

/**
 * Some useful methods that will be available for all thematic panels.
 * @author alexis
 */
public abstract class AbstractFieldPanel extends JPanel {
    /**
     * Horizontal gap between cells in a GridBagLayout.
     */
    public static final int HGAP = 10;
    /**
     * Vertical gap between cells in a GridBagLayout.
     */
    public static final int VGAP = 3;
    private static final Logger LOGGER = Logger.getLogger("gui."+AbstractFieldPanel.class);
    private static final I18n I18N = I18nFactory.getI18n(AbstractFieldPanel.class);
    /**
     * Width used for the rectangles that displays the color parameters of the symbols.
     */
    public final static int FILLED_LABEL_WIDTH = 55;
    /**
     * Height used for the rectangles that displays the color parameters of the symbols.
     */
    public final static int FILLED_LABEL_HEIGHT = 15;

    private ContainerItemProperties[] strokeUoms;

    /**
     * Build a {@code JLabel} from {@code name} with x-alignment set to
     * {@code SwingConstants.RIGHT}.
     *
     * @param name Text
     * @return Right-aligned JLabel with the given name.
     */
    public JLabel buildText(String name){
        JLabel c1 = new JLabel(name);
        c1.setHorizontalAlignment(SwingConstants.RIGHT);
        return c1;
    }

    /**
     * Initialize a {@code JComboBox} whose values are set according to the
     * not spatial fields of {@code ds}.
     * @param ds The original DataSource
     * @return A JComboBox.
     */
    public WideComboBox getFieldCombo(DataSource ds){
        WideComboBox combo = new WideComboBox();
        if(ds != null){
            try {
                Metadata md = ds.getMetadata();
                int fc = md.getFieldCount();
                for (int i = 0; i < fc; i++) {
                    if(!TypeFactory.isSpatial(md.getFieldType(i).getTypeCode())){
                        combo.addItem(md.getFieldName(i));
                    }
                }
            } catch (DriverException ex) {
                LOGGER.error(ex);
            }
        }
        return combo;
    }

    /**
     * Initialize a {@code JComboBo} whose values are set according to the
     * numeric fields of {@code ds}.
     * @param ds The original DataSource
     * @return A JComboBox.
     */
    public JComboBox getNumericFieldCombo(DataSource ds){
        JComboBox combo = new JComboBox();
        if(ds != null){
            try {
                Metadata md = ds.getMetadata();
                int fc = md.getFieldCount();
                for (int i = 0; i < fc; i++) {
                    if(TypeFactory.isNumerical(md.getFieldType(i).getTypeCode())){
                        combo.addItem(md.getFieldName(i));
                    }
                }
            } catch (DriverException ex) {
                LOGGER.error(ex);
            }
        }
        return combo;
    }

    /**
     * Get a JLabel of dimensions {@link PnlUniqueSymbolSE#FILLED_LABEL_WIDTH} and {@link PnlUniqueSymbolSE#FILLED_LABEL_HEIGHT}
     * opaque and with a background of Color {@code c}.
     * @param c The background color of the label we want.
     * @return the label with c as a background colour.
     */
    public JLabel getFilledLabel(Color c){
        JLabel lblFill = new JLabel();
        lblFill.setBackground(c);
        lblFill.setBorder(BorderFactory.createLineBorder(Color.black));
        lblFill.setPreferredSize(new Dimension(FILLED_LABEL_WIDTH, FILLED_LABEL_HEIGHT));
        lblFill.setMaximumSize(new Dimension(FILLED_LABEL_WIDTH, FILLED_LABEL_HEIGHT));
        lblFill.setOpaque(true);
        lblFill.setHorizontalAlignment(JLabel.LEFT);
        lblFill.addMouseListener(
                EventHandler.create(MouseListener.class, this, "chooseFillColor", "", "mouseClicked"));
        return lblFill;
    }

    /**
     * This method will let the user choose a color that will be set as the
     * background of the source of the event.
     * @param e The input event.
     */
    public void chooseFillColor(MouseEvent e) {
        Component source = (Component)e.getSource();
        if(source.isEnabled()){
            JLabel lab = (JLabel) source;
            ColorPicker picker = new ColorPicker(lab.getBackground());
            if (UIFactory.showDialog(picker,false, true)) {
                Color color = picker.getColor();
                source.setBackground(color);
            }
        }
    }

    /**
     * ComboBox to configure the unit of measure used to draw th stroke. The generated {@link JComboBox} only updates
     * the UOM of the given {@code StrokeUom}.
     * @param input The StrokeUom instance we get the unit from.
     * @return The JComboBox that can be used to change the UOM of {@code input}.
     */
    public UomCombo getLineUomCombo(StrokeUom input){
        strokeUoms = getUomProperties();
        UomCombo puc = new UomCombo(input.getStrokeUom(),
                strokeUoms,
                I18N.tr("Unit of measure - stroke width :"));
        ActionListener acl2 = EventHandler.create(ActionListener.class, this, "updateLUComboBox", "source.selectedIndex");
        puc.addActionListener(acl2);
        return puc;
    }

    /**
     * Gets a preview for the fallback value of the symbol.
     * @return The Preview in a CanvasSE.
     */
    public abstract CanvasSE getPreview();

    /**
     * Gets the legend we want to edit.
     * @return
     */
    public abstract Legend getLegend();

    /**
     * Gets the value contained in the {@code Uom} enum with their
     * internationalized representation in a {@code
     * ContainerItemProperties} array.
     * @return Uoms in an array of containers.
     */
    public ContainerItemProperties[] getUomProperties(){
        Uom[] us = Uom.values();
        ContainerItemProperties[] cips = new ContainerItemProperties[us.length];
        for(int i = 0; i<us.length; i++){
            Uom u = us[i];
            ContainerItemProperties cip = new ContainerItemProperties(u.name(), u.toLocalizedString());
            cips[i] = cip;
        }
        return cips;
    }

    /**
     * Sets the underlying graphic to use the ith element of the ComboBox
     * as its well-known name. Used when changing the ComboBox selection.
     * @param index The index of the unit of measure.
     */
    public void updateLUComboBox(int index){
        ((StrokeUom)getLegend()).setStrokeUom(Uom.fromString(strokeUoms[index].getKey()));
    }
}
