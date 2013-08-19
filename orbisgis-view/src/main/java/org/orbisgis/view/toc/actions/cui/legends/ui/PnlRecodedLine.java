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
import org.orbisgis.legend.thematic.LineParameters;
import org.orbisgis.legend.thematic.constant.UniqueSymbolLine;
import org.orbisgis.legend.thematic.recode.AbstractRecodedLegend;
import org.orbisgis.legend.thematic.recode.RecodedLine;
import org.orbisgis.sif.UIFactory;
import org.orbisgis.sif.UIPanel;
import org.orbisgis.view.toc.actions.cui.LegendContext;
import org.orbisgis.view.toc.actions.cui.components.CanvasSE;
import org.orbisgis.view.toc.actions.cui.legends.model.KeyEditorRecodedLine;
import org.orbisgis.view.toc.actions.cui.legends.model.KeyEditorUniqueValue;
import org.orbisgis.view.toc.actions.cui.legends.model.ParametersEditorRecodedLine;
import org.orbisgis.view.toc.actions.cui.legends.model.TableModelRecodedLine;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.EventHandler;

/**
 * "Value classification - Line" UI.
 *
 * <p></p>This panel must be used to manage all the parameters of a line symbolizer
 * which is configured thanks to a "simple" recoded PenStroke. All the parameters
 * of the PenStroke must be configured either with a Recode or a Literal, all
 * the Recode must be done with the same analysis field.</p>
 *
 * @author Alexis Guéganno
 * @author Adam Gouge
 */
public final class PnlRecodedLine extends PnlAbstractUniqueValue<LineParameters>{
    public static final Logger LOGGER = Logger.getLogger(PnlRecodedLine.class);
    private static final I18n I18N = I18nFactory.getI18n(PnlRecodedLine.class);

    /**
     * Builds a panel with a new legend.
     *
     * @param lc     LegendContext
     */
    public PnlRecodedLine(LegendContext lc) {
        this(lc, new RecodedLine());
    }

    /**
     * Builds a panel based on the given legend.
     *
     * @param lc     LegendContext
     * @param legend Legend
     */
    public PnlRecodedLine(LegendContext lc, RecodedLine legend) {
        super(lc, legend);
        initPreview();
        buildUI();
    }

    /**
     * This methods is called by EventHandler when the user clicks on the fall back's preview. It opens an UI that lets
     * the user edit the parameters of the fall back configuration and that apply it if the user clicks OK.
     * @param me The MouseEvent that caused the call to this method.
     */
    public void onEditFallback(MouseEvent me){
        getLegend().setFallbackParameters(editCanvas(fallbackPreview));
    }

    /**
     * Builds a SIF dialog used to edit the given LineParameters.
     * @param cse The canvas we want to edit
     * @return The LineParameters that must be used at the end of the edition.
     */
    private LineParameters editCanvas(CanvasSE cse){
        LineParameters lps = getLegend().getFallbackParameters();
        UniqueSymbolLine usl = new UniqueSymbolLine(lps);
        usl.setStrokeUom(getLegend().getStrokeUom());
        PnlUniqueLineSE pls = new PnlUniqueLineSE(usl, false);
        if(UIFactory.showDialog(new UIPanel[]{pls}, true, true)){
            usl = pls.getLegend();
            LineParameters nlp = usl.getLineParameters();
            cse.setSymbol(usl.getSymbolizer());
            return nlp;
        } else {
            return lps;
        }
    }

    /**
     * Initializes the preview for the fallback configuration
     */
    public void initPreview() {
        fallbackPreview = new CanvasSE(getFallbackSymbolizer());
        MouseListener l = EventHandler.create(MouseListener.class, this, "onEditFallback", "", "mouseClicked");
        fallbackPreview.addMouseListener(l);
    }

    @Override
    public RecodedLine getEmptyAnalysis(){
        return new RecodedLine();
    }

    @Override
    public AbstractTableModel getTableModel(){
        return new TableModelRecodedLine((AbstractRecodedLegend<LineParameters>) getLegend());
    }

    @Override
    public TableCellEditor getPreviewCellEditor(){
        return new ParametersEditorRecodedLine();
    }

    @Override
    public KeyEditorUniqueValue<LineParameters> getKeyCellEditor(){
        return new KeyEditorRecodedLine();
    }

    @Override
    public LineParameters getColouredParameters(LineParameters lp, Color newCol){
        return new LineParameters(newCol, lp.getLineOpacity(), lp.getLineWidth(), lp.getLineDash());
    }

    @Override
    public void setLegend(Legend legend) {
        if (legend instanceof RecodedLine) {
            if(getLegend() != null){
                Rule rule = getLegend().getSymbolizer().getRule();
                if(rule != null){
                    CompositeSymbolizer compositeSymbolizer = rule.getCompositeSymbolizer();
                    int i = compositeSymbolizer.getSymbolizerList().indexOf(this.getLegend().getSymbolizer());
                    compositeSymbolizer.setSymbolizer(i, legend.getSymbolizer());
                }
            }
            setLegendImpl((RecodedLine)legend);
            this.buildUI();
        } else {
            throw new IllegalArgumentException(I18N.tr("You must use recognized RecodedLine instances in"
                        + "this panel."));
        }
    }
}