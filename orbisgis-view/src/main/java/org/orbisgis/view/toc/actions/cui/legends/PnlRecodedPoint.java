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
import org.orbisgis.core.Services;
import org.orbisgis.core.renderer.se.CompositeSymbolizer;
import org.orbisgis.core.renderer.se.PointSymbolizer;
import org.orbisgis.core.renderer.se.Rule;
import org.orbisgis.core.renderer.se.Symbolizer;
import org.orbisgis.core.renderer.se.common.Uom;
import org.orbisgis.legend.Legend;
import org.orbisgis.legend.thematic.PointParameters;
import org.orbisgis.legend.thematic.constant.UniqueSymbolPoint;
import org.orbisgis.legend.thematic.recode.AbstractRecodedLegend;
import org.orbisgis.legend.thematic.recode.RecodedPoint;
import org.orbisgis.sif.UIFactory;
import org.orbisgis.sif.UIPanel;
import org.orbisgis.sif.common.ContainerItemProperties;
import org.orbisgis.view.background.BackgroundListener;
import org.orbisgis.view.background.BackgroundManager;
import org.orbisgis.view.background.DefaultJobId;
import org.orbisgis.view.background.JobId;
import org.orbisgis.view.toc.actions.cui.SimpleGeometryType;
import org.orbisgis.view.toc.actions.cui.components.CanvasSE;
import org.orbisgis.view.toc.actions.cui.legend.ISELegendPanel;
import org.orbisgis.view.toc.actions.cui.legends.model.*;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.EventHandler;
import java.util.Map;
import java.util.Set;

/**
 * @author alexis
 */
public class PnlRecodedPoint extends PnlAbstractUniqueValue<PointParameters> {
    public static final Logger LOGGER = Logger.getLogger(PnlRecodedLine.class);
    private static final I18n I18N = I18nFactory.getI18n(PnlRecodedLine.class);
    private String id;
    private CanvasSE fallbackPreview;
    private JComboBox fieldCombo;
    private JCheckBox strokeBox;
    private BackgroundListener background;
    private ContainerItemProperties[] uoms;

    @Override
    public void initializeLegendFields() {
        this.removeAll();
        JPanel glob = new JPanel();
        GridBagLayout grid = new GridBagLayout();
        glob.setLayout(grid);
        int i = 0;
        //Field chooser
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = i;
        i++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        glob.add(getFieldLine(), gbc);
        //Fallback symbol
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = i;
        i++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        glob.add(getFallback(), gbc);
        //UOM
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = i;
        i++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        glob.add(getUOMCombo(),gbc);
        //UOM - symbol size
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = i;
        i++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        glob.add(getSymbolUOMCombo(),gbc);
        //on vertex ?
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = i;
        i++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        glob.add(pnlOnVertex(),gbc);
        //Classification generator
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = i;
        i++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        glob.add(getCreateClassificationPanel(), gbc);
        //Classification generator
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = i;
        i++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        glob.add(getEnableStrokeCheckBox(), gbc);
        //Table for the recoded configurations
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = i;
        i++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        glob.add(getTablePanel(), gbc);
        this.add(glob);
        this.revalidate();
    }

    @Override
    public AbstractRecodedLegend<PointParameters> getEmptyAnalysis() {
        return new RecodedPoint();
    }

    @Override
    public void initPreview() {
        fallbackPreview = new CanvasSE(getFallbackSymbolizer());
        MouseListener l = EventHandler.create(MouseListener.class, this, "onEditFallback", "", "mouseClicked");
        fallbackPreview.addMouseListener(l);
    }
    public void onEditFallback(MouseEvent me){
        ((RecodedPoint)getLegend()).setFallbackParameters(editCanvas(fallbackPreview));
    }

    /**
     * Builds a SIF dialog used to edit the given LineParameters.
     * @param cse The canvas we want to edit
     * @return The LineParameters that must be used at the end of the edition.
     */
    private PointParameters editCanvas(CanvasSE cse){
        RecodedPoint leg = (RecodedPoint) getLegend();
        PointParameters lps = leg.getFallbackParameters();
        UniqueSymbolPoint usa =getFallBackLegend();
        PnlUniquePointSE pls = new PnlUniquePointSE(false, leg.isStrokeEnabled(), false);
        pls.setLegend(usa);
        if(UIFactory.showDialog(new UIPanel[]{pls}, true, true)){
            usa = (UniqueSymbolPoint) pls.getLegend();
            PointParameters nlp = usa.getPointParameters();
            cse.setSymbol(usa.getSymbolizer());
            cse.imageChanged();
            return nlp;
        } else {
            return lps;
        }
    }

    /**
     * Returns the panel used to configure if the symbol must be drawn on vertex or on centroid.
     * @return The panel with the radio buttons.
     */
    private JPanel pnlOnVertex(){
        JPanel jp = new JPanel();
        RecodedPoint point = (RecodedPoint) getLegend();
        JRadioButton bVertex = new JRadioButton(I18N.tr("On vertex"));
        JRadioButton bCentroid = new JRadioButton(I18N.tr("On centroid"));
        ButtonGroup bg = new ButtonGroup();
        bg.add(bVertex);
        bg.add(bCentroid);
        ActionListener actionV = EventHandler.create(ActionListener.class, point, "setOnVertex");
        ActionListener actionC = EventHandler.create(ActionListener.class, point, "setOnCentroid");
        ActionListener actionRefV = EventHandler.create(ActionListener.class, this, "onClickVertex");
        ActionListener actionRefC= EventHandler.create(ActionListener.class, this, "onClickCentroid");
        bVertex.addActionListener(actionV);
        bVertex.addActionListener(actionRefV);
        bCentroid.addActionListener(actionC);
        bCentroid.addActionListener(actionRefC);
        bVertex.setSelected(((PointSymbolizer)point.getSymbolizer()).isOnVertex());
        bCentroid.setSelected(!((PointSymbolizer)point.getSymbolizer()).isOnVertex());
        jp.add(bVertex);
        jp.add(bCentroid);
        return jp;
    }

    /**
     * called when the user wants to put the points on the vertices of the geometry.
     */
    public void onClickVertex(){
        changeOnVertex(true);
    }

    /**
     * called when the user wants to put the points on the centroid of the geometry.
     */
    public void onClickCentroid(){
        changeOnVertex(false);
    }

    /**
     * called when the user wants to put the points on the vertices or ont the centroid of the geometry.
     * @param b If true, the points are set on the vertices.
     */
    private void changeOnVertex(boolean b){
        CanvasSE prev = getPreview();
        ((PointSymbolizer)prev.getSymbol()).setOnVertex(b);
        prev.imageChanged();
        updateTable();
    }

    @Override
    public PointParameters getColouredParameters(PointParameters f, Color c) {
        return new PointParameters(f.getLineColor(), f.getLineOpacity(),f.getLineWidth(),f.getLineDash(),
                    c,f.getFillOpacity(),
                    f.getWidth(), f.getHeight(), f.getWkn());
    }

    @Override
    public Symbolizer getFallbackSymbolizer() {
        return getFallBackLegend().getSymbolizer();
    }

    private UniqueSymbolPoint getFallBackLegend(){
        RecodedPoint leg = (RecodedPoint)getLegend();
        UniqueSymbolPoint usl = new UniqueSymbolPoint(leg.getFallbackParameters());
        usl.setStrokeUom(leg.getStrokeUom());
        usl.setSymbolUom(leg.getSymbolUom());
        if(leg .isOnVertex()){
            usl.setOnVertex();
        } else {
            usl.setOnCentroid();
        }
        return usl;
    }

    @Override
    public AbstractTableModel getTableModel() {
        return new TableModelRecodedPoint((AbstractRecodedLegend<PointParameters>) getLegend());
    }

    @Override
    public TableCellEditor getParametersCellEditor() {
        return new ParametersEditorRecodedPoint();
    }

    @Override
    public KeyEditorUniqueValue<PointParameters> getKeyCellEditor() {
        return new KeyEditorRecodedPoint();
    }

    @Override
    public CanvasSE getPreview() {
        return fallbackPreview;
    }

    @Override
    public void setLegend(Legend legend) {
        if (legend instanceof RecodedPoint) {
            if(getLegend() != null){
                Rule rule = getLegend().getSymbolizer().getRule();
                if(rule != null){
                    CompositeSymbolizer compositeSymbolizer = rule.getCompositeSymbolizer();
                    int i = compositeSymbolizer.getSymbolizerList().indexOf(this.getLegend().getSymbolizer());
                    compositeSymbolizer.setSymbolizer(i, legend.getSymbolizer());
                }
            }
            setLegendImpl(legend);
            this.initializeLegendFields();
        } else {
            throw new IllegalArgumentException(I18N.tr("You must use recognized RecodedArea instances in"
                        + "this panel."));
        }
    }

    @Override
    public void setGeometryType(int type) {
    }

    @Override
    public boolean acceptsGeometryType(int geometryType) {
        return (geometryType & SimpleGeometryType.ALL) != 0;
    }

    @Override
    public Legend copyLegend() {
        RecodedPoint rl = new RecodedPoint();
        RecodedPoint leg = (RecodedPoint) getLegend();
        leg.setStrokeEnabled(rl.isStrokeEnabled());
        Set<Map.Entry<String,PointParameters>> entries = leg.entrySet();
        for(Map.Entry<String,PointParameters> entry : entries){
            rl.put(entry.getKey(),entry.getValue());
        }
        rl.setFallbackParameters(leg.getFallbackParameters());
        rl.setLookupFieldName(leg.getLookupFieldName());
        return rl;
    }

    @Override
    public Component getComponent() {
        initializeLegendFields();
        return this;
    }

    @Override
    public ISELegendPanel newInstance() {
        return new PnlRecodedPoint();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String newId) {
        id = newId;
    }

    @Override
    public String validateInput() {
        return "";
    }

    /**
     * Called to build a classification from the given data source and field. Makes a SELECT DISTINCT field FROM ds;
     * and feeds the legend that has been cleared prior to that.
     */
    public void onCreateClassification(ActionEvent e){
        if(e.getActionCommand().equals("click")){
            String fieldName = fieldCombo.getSelectedItem().toString();
            SelectDistinctJob selectDistinct = new SelectDistinctJob(fieldName);
            BackgroundManager bm = Services.getService(BackgroundManager.class);
            JobId jid = new DefaultJobId(JOB_NAME);
            if(background == null){
                background = new OperationListener();
                bm.addBackgroundListener(background);
            }
            bm.nonBlockingBackgroundOperation(jid, selectDistinct);
        }
    }

    /**
     * Build the panel used to select the classification field.
     *
     * @return The JPanel where the user will choose the classification field.
     */
    private JPanel getFieldLine() {
        JPanel jp = new JPanel();
        jp.add(new JLabel(I18N.tr("Classification field : ")));
        fieldCombo =getFieldComboBox();
        jp.add(fieldCombo);
        return jp;
    }

    /**
     * Builds the panel used to display and configure the fallback symbol
     *
     * @return The Panel where the fallback configuration is displayed.
     */
    private JPanel getFallback() {
        JPanel jp = new JPanel();
        jp.add(new JLabel(I18N.tr("Fallback Symbol")));
        initPreview();
        jp.add(fallbackPreview);
        return jp;
    }

    private JPanel getUOMCombo(){
        JPanel pan = new JPanel();
        JComboBox jcb = getLineUomCombo(((RecodedPoint)getLegend()));
        ActionListener aclUom = EventHandler.create(ActionListener.class, this, "updatePreview", "source");
        jcb.addActionListener(aclUom);
        pan.add(new JLabel(I18N.tr("Unit of measure - width :")));
        pan.add(jcb);
        return pan;
    }

    /**
     * A JPanel containing the combo returned bu getPointUomCombo
     * @return The JComboBox with a JLabel in a JPanel.
     */
    private JPanel getSymbolUOMCombo(){
        JPanel pan = new JPanel();
        JComboBox jcb = getPointUomCombo();
        pan.add(new JLabel(I18N.tr("Unit of measure - size :")));
        pan.add(jcb);
        return pan;
    }


    /**
     * ComboBox to configure the unit of measure used to draw th stroke.
     * @return A JComboBox the user can use to set the unit of measure of the symbol's dimensions.
     */
    private JComboBox getPointUomCombo(){
        uoms= getUomProperties();
        String[] values = new String[uoms.length];
        for (int i = 0; i < values.length; i++) {
                values[i] = I18N.tr(uoms[i].toString());
        }
        final JComboBox jcc = new JComboBox(values);
        ActionListener acl2 = EventHandler.create(ActionListener.class, this, "updateSUComboBox", "source.selectedIndex");
        jcc.addActionListener(acl2);
        jcc.setSelectedItem(((RecodedPoint)getLegend()).getSymbolUom().toString().toUpperCase());
        return jcc;
    }
    /**
     * Sets the underlying graphic to use the ith element of the combo box
     * as its uom. Used when changing the combo box selection.
     * @param index The index of the selected unit of measure.
     */
    public void updateSUComboBox(int index){
        RecodedPoint leg = (RecodedPoint)getLegend();
        leg.setSymbolUom(Uom.fromString(uoms[index].getKey()));
        CanvasSE prev = getPreview();
        prev.setSymbol(getFallbackSymbolizer());
        updateTable();
    }

    /**
     * Gets the panel used to set if the stroke will be drawable or not.
     * @return The configuration panel for the stroke use.
     */
    public JPanel getEnableStrokeCheckBox(){
        JPanel ret = new JPanel();
        ret.add(new JLabel(I18N.tr("Enable Stroke:")));
        strokeBox = new JCheckBox(I18N.tr(""));
        RecodedPoint ra = (RecodedPoint) getLegend();
        strokeBox.setSelected(ra.isStrokeEnabled());
        strokeBox.addActionListener(EventHandler.create(ActionListener.class, this, "onEnableStroke"));
        ret.add(strokeBox);
        return ret;
    }

    /**
     * Action done when the checkbox used to activate the stroke is pressed.
     */
    public void onEnableStroke(){
        RecodedPoint ra = (RecodedPoint) getLegend();
        ra.setStrokeEnabled(strokeBox.isSelected());
        PointSymbolizer ps = (PointSymbolizer) new UniqueSymbolPoint(ra.getFallbackParameters()).getSymbolizer();
        ps.setOnVertex(ra.isOnVertex());
        getPreview().setSymbol(ps);
        getPreview().imageChanged();
        updateTable();
    }
}
