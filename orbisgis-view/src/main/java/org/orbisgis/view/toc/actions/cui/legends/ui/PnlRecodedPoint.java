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

import org.apache.log4j.Logger;
import org.orbisgis.core.renderer.se.CompositeSymbolizer;
import org.orbisgis.core.renderer.se.Rule;
import org.orbisgis.legend.Legend;
import org.orbisgis.legend.thematic.PointParameters;
import org.orbisgis.legend.thematic.constant.UniqueSymbolPoint;
import org.orbisgis.legend.thematic.recode.RecodedPoint;
import org.orbisgis.sif.UIFactory;
import org.orbisgis.sif.UIPanel;
import org.orbisgis.view.toc.actions.cui.LegendContext;
import org.orbisgis.view.toc.actions.cui.components.CanvasSE;
import org.orbisgis.view.toc.actions.cui.legends.model.KeyEditorRecodedPoint;
import org.orbisgis.view.toc.actions.cui.legends.model.KeyEditorUniqueValue;
import org.orbisgis.view.toc.actions.cui.legends.model.ParametersEditorRecodedPoint;
import org.orbisgis.view.toc.actions.cui.legends.model.TableModelRecodedPoint;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.EventHandler;

/**
 * "Value classification - Point" UI.
 *
 * @author alexis
 */
public class PnlRecodedPoint extends PnlAbstractUniqueValue<PointParameters> {
    public static final Logger LOGGER = Logger.getLogger(PnlRecodedLine.class);
    private static final I18n I18N = I18nFactory.getI18n(PnlRecodedLine.class);

    private String id;

    /**
     * Builds a panel with a new legend.
     *
     * @param lc     LegendContext
     */
    public PnlRecodedPoint(LegendContext lc) {
        this(lc, new RecodedPoint());
    }

    /**
     * Builds a panel based on the given legend.
     *
     * @param lc     LegendContext
     * @param legend Legend
     */
    public PnlRecodedPoint(LegendContext lc, RecodedPoint legend) {
        super(lc, legend);
        initPreview();
        buildUI();
    }

    @Override
    public RecodedPoint getLegend() {
        return (RecodedPoint) super.getLegend();
    }

    @Override
    public RecodedPoint getEmptyAnalysis() {
        return new RecodedPoint();
    }

    @Override
    public void initPreview() {
        fallbackPreview = new CanvasSE(getFallbackSymbolizer());
        MouseListener l = EventHandler.create(MouseListener.class, this, "onEditFallback", "", "mouseClicked");
        fallbackPreview.addMouseListener(l);
    }

    public void onEditFallback(MouseEvent me){
        getLegend().setFallbackParameters(editCanvas(fallbackPreview));
    }

    /**
     * Builds a SIF dialog used to edit the given LineParameters.
     * @param cse The canvas we want to edit
     * @return The LineParameters that must be used at the end of the edition.
     */
    private PointParameters editCanvas(CanvasSE cse){
        RecodedPoint leg = getLegend();
        PointParameters lps = leg.getFallbackParameters();
        UniqueSymbolPoint usa = getFallBackLegend();
        PnlUniquePointSE pls = new PnlUniquePointSE(usa, leg.isStrokeEnabled());
        if(UIFactory.showDialog(new UIPanel[]{pls}, true, true)){
            usa = pls.getLegend();
            PointParameters nlp = usa.getPointParameters();
            cse.setSymbol(usa.getSymbolizer());
            return nlp;
        } else {
            return lps;
        }
    }

    @Override
    public PointParameters getColouredParameters(PointParameters f, Color c) {
        return new PointParameters(f.getLineColor(), f.getLineOpacity(),f.getLineWidth(),f.getLineDash(),
                    c,f.getFillOpacity(),
                    f.getWidth(), f.getHeight(), f.getWkn());
    }

    private UniqueSymbolPoint getFallBackLegend(){
        RecodedPoint leg = getLegend();
        UniqueSymbolPoint usl = new UniqueSymbolPoint(leg.getFallbackParameters());
        usl.setStrokeUom(leg.getStrokeUom());
        usl.setSymbolUom(leg.getSymbolUom());
        if(leg.isOnVertex()){
            usl.setOnVertex();
        } else {
            usl.setOnCentroid();
        }
        return usl;
    }

    @Override
    public AbstractTableModel getTableModel() {
        return new TableModelRecodedPoint(getLegend());
    }

    @Override
    public TableCellEditor getPreviewCellEditor() {
        return new ParametersEditorRecodedPoint();
    }

    @Override
    public KeyEditorUniqueValue<PointParameters> getKeyCellEditor() {
        return new KeyEditorRecodedPoint();
    }

    @Override
    public void setLegend(Legend legend) {
        if (legend instanceof RecodedPoint) {
            if(getLegend() != null){
                Rule rule = getLegend().getSymbolizer().getRule();
                if(rule != null){
                    CompositeSymbolizer compositeSymbolizer = rule.getCompositeSymbolizer();
                    int i = compositeSymbolizer.getSymbolizerList().indexOf(getLegend().getSymbolizer());
                    compositeSymbolizer.setSymbolizer(i, legend.getSymbolizer());
                }
            }
            setLegendImpl((RecodedPoint)legend);
            this.buildUI();
        } else {
            throw new IllegalArgumentException(I18N.tr("You must use recognized RecodedArea instances in"
                        + "this panel."));
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String newId) {
        id = newId;
    }
}
