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
package org.orbisgis.view.toc.actions.cui.legends;

import net.miginfocom.swing.MigLayout;
import org.apache.log4j.Logger;
import org.gdms.driver.DriverException;
import org.orbisgis.core.renderer.classification.ClassificationUtils;
import org.orbisgis.core.renderer.se.fill.SolidFill;
import org.orbisgis.core.renderer.se.parameter.ParameterException;
import org.orbisgis.core.renderer.se.parameter.real.RealAttribute;
import org.orbisgis.core.renderer.se.stroke.PenStroke;
import org.orbisgis.legend.Legend;
import org.orbisgis.legend.structure.fill.constant.ConstantSolidFill;
import org.orbisgis.legend.structure.fill.constant.ConstantSolidFillLegend;
import org.orbisgis.legend.structure.fill.constant.NullSolidFillLegend;
import org.orbisgis.legend.structure.stroke.constant.ConstantPenStroke;
import org.orbisgis.legend.structure.stroke.constant.ConstantPenStrokeLegend;
import org.orbisgis.legend.structure.stroke.constant.NullPenStrokeLegend;
import org.orbisgis.legend.thematic.PointParameters;
import org.orbisgis.legend.thematic.constant.UniqueSymbolPoint;
import org.orbisgis.legend.thematic.proportional.ProportionalPoint;
import org.orbisgis.sif.UIFactory;
import org.orbisgis.sif.UIPanel;
import org.orbisgis.sif.components.WideComboBox;
import org.orbisgis.view.toc.actions.cui.LegendContext;
import org.orbisgis.view.toc.actions.cui.SimpleGeometryType;
import org.orbisgis.view.toc.actions.cui.components.CanvasSE;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * "Proportional Point" UI.
 *
 * @author Alexis Guéganno
 */
public class PnlProportionalPointSE extends PnlUniquePointSE {

        private static final Logger LOGGER = Logger.getLogger("gui."+PnlProportionalPointSE.class);
        private static final I18n I18N = I18nFactory.getI18n(PnlProportionalPointSE.class);
        private ProportionalPoint proportionalPoint;
        private WideComboBox fieldCombo;
        MouseListener l;

        @Override
        public Legend getLegend() {
                return proportionalPoint;
        }

        @Override
        public void setLegend(Legend legend) {
                if (legend instanceof ProportionalPoint) {
                        proportionalPoint = (ProportionalPoint) legend;
                        ConstantPenStroke cps = proportionalPoint.getPenStroke();
                        if(cps instanceof ConstantPenStrokeLegend ){
                                setPenStrokeMemory((ConstantPenStrokeLegend) cps);
                        } else {
                                setPenStrokeMemory(new ConstantPenStrokeLegend(new PenStroke()));
                        }
                        ConstantSolidFill csf = proportionalPoint.getFillLegend();
                        if(csf instanceof ConstantSolidFillLegend){
                                setSolidFillMemory((ConstantSolidFillLegend) csf);
                        } else {
                                setSolidFillMemory(new ConstantSolidFillLegend(new SolidFill()));
                        }
                        initPreview();
                        getPreview().setDisplayed(false);

                        this.initializeLegendFields();
                } else {
                        throw new IllegalArgumentException("The given Legend is not"
                                + "a ProportionalPoint");
                }
        }

        /**
         * Initialize the panel. This method is called just after the panel
         * creation.
         *
         * @param lc LegendContext is useful to get some information about the
         * layer in edition.
         */
        @Override
        public void initialize(LegendContext lc) {
                initialize(lc, new ProportionalPoint());
        }

        @Override
        public Legend copyLegend(){
                return new ProportionalPoint();
        }

        @Override
        public boolean acceptsGeometryType(int geometryType) {
                return (geometryType & SimpleGeometryType.ALL) != 0;
        }

        @Override
        public String validateInput() {
                if(fieldCombo.getSelectedItem() != null){
                        return null;
                } else {
                        return I18N.tr("A valid numeric field must be selected !");
                }
                
        }

        @Override
        public URL getIconURL() {
                return UIFactory.getDefaultIcon();
        }

        @Override
        public String getTitle() {
                return "Proportional Points";
        }

        @Override
        public void initializeLegendFields() {
                this.removeAll();
                JPanel glob = new JPanel(new MigLayout());

                glob.add(getProportionalBlock(proportionalPoint,
                                              I18N.tr(MARK_SETTINGS)));
                // PREVIEW
                CanvasSE prev = getPreview();

                // The preview is created only once while the other panels are
                // created twice, currently. Adds a locally stored listener to
                // avoid having it twice because of this double call of
                // initializeLegendFields.
                if(l == null || prev.getMouseListeners().length == 0){
                    l = EventHandler.create(MouseListener.class, this, "onClickOnPreview", "", "mouseClicked");
                    prev.addMouseListener(l);
                }
                glob.add(getPreviewPanel());
                this.add(glob);
        }

        /**
         * Opens a OK - Cancel window used to edit the symbol configuration, size excepted.
         */
        public void onClickOnPreview(MouseEvent mouseEvent){
            //We create a copy of the constant part of the symbol
            PointParameters pp = new PointParameters(proportionalPoint.getPenStroke().getLineColor(),
                        proportionalPoint.getPenStroke().getLineOpacity(),
                        proportionalPoint.getPenStroke().getLineWidth(),
                        proportionalPoint.getPenStroke().getDashArray(),
                        proportionalPoint.getFillLegend().getColor(),
                        proportionalPoint.getFillLegend().getOpacity(),
                        3.0,
                        3.0,
                        proportionalPoint.getWellKnownName());
            UniqueSymbolPoint usp = new UniqueSymbolPoint(pp);
            if(proportionalPoint.getPenStroke() instanceof NullPenStrokeLegend){
                usp.setPenStroke(new NullPenStrokeLegend());
            }
            if(proportionalPoint.getFillLegend() instanceof NullSolidFillLegend){
                usp.setFillLegend(new NullSolidFillLegend());
            }
            usp.setStrokeUom(proportionalPoint.getStrokeUom());
            usp.setSymbolUom(proportionalPoint.getSymbolUom());
            ConfigPanel cp = new ConfigPanel(usp);
            if(UIFactory.showDialog(cp)){
                proportionalPoint.getPenStroke().setLineColor(usp.getPenStroke().getLineColor());
                proportionalPoint.getPenStroke().setLineOpacity(usp.getPenStroke().getLineOpacity());
                proportionalPoint.getPenStroke().setLineWidth(usp.getPenStroke().getLineWidth());
                proportionalPoint.getPenStroke().setDashArray(usp.getPenStroke().getDashArray());
                proportionalPoint.getFillLegend().setColor(usp.getFillLegend().getColor());
                proportionalPoint.getFillLegend().setOpacity(usp.getFillLegend().getOpacity());
                proportionalPoint.setWellKnownName(usp.getWellKnownName());
                getPreview().imageChanged();
            }
        }

        /**
         * Gets the block that will contain the configuration of the size of
         * a proportional point symbol.
         * @param prop The input proportional point
         * @param title The title of the block
         * @return The JPanel containing the fields that sets the symbol min and max size.
         */
        public JPanel getProportionalBlock(ProportionalPoint prop, String title){

                JPanel jp = new JPanel(new MigLayout("wrap 2", COLUMN_CONSTRAINTS));
                jp.setBorder(BorderFactory.createTitledBorder(title));

                // Field
                initFieldCombo();
                jp.add(new JLabel(I18N.tr(FIELD)));
                jp.add(fieldCombo, COMBO_BOX_CONSTRAINTS);
                // Unit of measure - symbol size
                jp.add(new JLabel(I18N.tr(SYMBOL_SIZE_UNIT)));
                jp.add(getPointUomCombo(), COMBO_BOX_CONSTRAINTS);
                // Symbol
                jp.add(new JLabel(I18N.tr(SYMBOL)));
                jp.add(getWKNCombo(prop), COMBO_BOX_CONSTRAINTS);
                // Max size
                jp.add(new JLabel(I18N.tr("Max. size")));
                jp.add(getSecondConf(prop), "growx");
                // Min size
                jp.add(new JLabel(I18N.tr("Min. size")));
                jp.add(getFirstConf(prop), "growx");
                // If geometryType != POINT, we must let the user choose if he
                // wants to draw symbols on centroid or on vertices.
                if(getGeometryType() != SimpleGeometryType.POINT){
                    jp.add(new JLabel(I18N.tr(PLACE_SYMBOL_ON)), "span 1 2");
                    jp.add(OnVertexHelper.pnlOnVertex(this, prop, I18N), "span 1 2");
                }
                return jp;
        }

        private JFormattedTextField getSecondConf(ProportionalPoint prop){
                CanvasSE prev = getPreview();
                JFormattedTextField jftf = new JFormattedTextField(new DecimalFormat());
                try {
                        jftf.setValue(prop.getSecondValue());
                } catch (ParameterException ex) {
                        LOGGER.error(I18N.tr("Can't retrieve the maximum value of"
                                + " the symbol"), ex);
                }
                jftf.addPropertyChangeListener(
                        "value",
                        EventHandler.create(PropertyChangeListener.class, prop, "secondValue", "source.value"));
                jftf.addPropertyChangeListener(
                        "value",
                        EventHandler.create(PropertyChangeListener.class, prev, "imageChanged"));
                jftf.setHorizontalAlignment(SwingConstants.RIGHT);
                return jftf;
        }

        private JFormattedTextField getFirstConf(ProportionalPoint prop){
                JFormattedTextField jftf = new JFormattedTextField(new DecimalFormat());
                try {
                        jftf.setValue(prop.getFirstValue());
                } catch (ParameterException ex) {
                        LOGGER.error(I18N.tr("Can't retrieve the minimum value of"
                                + " the symbol"), ex);
                }
                jftf.addPropertyChangeListener(
                        "value",
                        EventHandler.create(PropertyChangeListener.class, prop, "firstValue", "source.value"));
                jftf.setHorizontalAlignment(SwingConstants.RIGHT);
                return jftf;
        }

        private void initFieldCombo(){
                if(ds != null){
                        fieldCombo = getNumericFieldCombo(ds);
                        ActionListener acl2 = EventHandler.create(ActionListener.class,
                                this, "updateField", "source.selectedItem");
                        String field = proportionalPoint.getLookupFieldName();
                        if(field != null && !field.isEmpty()){
                                fieldCombo.setSelectedItem(field);
                        }
                        fieldCombo.addActionListener(acl2);
                        updateField((String)fieldCombo.getSelectedItem());
                        ((JLabel)fieldCombo.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                        fieldCombo = new WideComboBox();
                }
        }

        /**
         * Used when the field against which the analysis is made changes.
         * @param obj  The new name.
         */
        public void updateField(String obj){
                try {
                        double[] mnm=ClassificationUtils.getMinAndMax(ds, new RealAttribute(obj));
                        proportionalPoint.setFirstData(Math.sqrt(mnm[0]));
                        proportionalPoint.setSecondData(Math.sqrt(mnm[1]));
                        proportionalPoint.setLookupFieldName(obj);
                        Map<String, Object> sample = new HashMap<String, Object>();
                        sample.put(obj, mnm[1]);
                        getPreview().setSampleDatasource(sample);
                        getPreview().setDisplayed(true);
                        getPreview().imageChanged();
                } catch (DriverException ex) {
                        LOGGER.error("", ex);
                } catch (ParameterException ex) {
                        LOGGER.error("", ex);
                }
        }

    private class ConfigPanel extends JPanel implements UIPanel {

        private UniqueSymbolPoint usp;

        public ConfigPanel(UniqueSymbolPoint point){
            usp = point;
        }

        @Override
        public URL getIconURL() {
            return null;
        }

        @Override
        public String getTitle() {
            return I18N.tr("Stroke and fill settings");
        }

        @Override
        public String validateInput() {
            return null;
        }

        @Override
        public Component getComponent() {
            JPanel glob = new JPanel();
            GridBagLayout grid = new GridBagLayout();
            glob.setLayout(grid);
            int i = 0;
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            i++;
            JPanel p1 = getLineBlock(usp.getPenStroke(),
                                     I18N.tr(LINE_SETTINGS));
            glob.add(p1, gbc);
            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 0, 5, 0);
            JPanel p2 = getAreaBlock(usp.getFillLegend(),
                                     I18N.tr(FILL_SETTINGS));
            glob.add(p2, gbc);
            return glob;
        }
    }
}
