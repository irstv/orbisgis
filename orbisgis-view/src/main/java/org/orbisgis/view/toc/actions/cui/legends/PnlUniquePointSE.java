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
package org.orbisgis.view.toc.actions.cui.legends;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.beans.EventHandler;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.orbisgis.core.renderer.se.fill.SolidFill;
import org.orbisgis.core.renderer.se.stroke.PenStroke;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.legends.GeometryProperties;
import org.orbisgis.legend.Legend;
import org.orbisgis.legend.analyzer.FillAnalyzer;
import org.orbisgis.legend.analyzer.PenStrokeAnalyzer;
import org.orbisgis.legend.structure.fill.constant.ConstantSolidFill;
import org.orbisgis.legend.structure.fill.constant.ConstantSolidFillLegend;
import org.orbisgis.legend.structure.stroke.constant.ConstantPenStroke;
import org.orbisgis.legend.structure.stroke.constant.ConstantPenStrokeLegend;
import org.orbisgis.legend.thematic.constant.UniqueSymbolPoint;
import org.orbisgis.sif.UIFactory;
import org.orbisgis.sif.components.JNumericSpinner;
import org.orbisgis.view.toc.actions.cui.LegendContext;
import org.orbisgis.view.toc.actions.cui.components.CanvasSE;
import org.orbisgis.view.toc.actions.cui.legend.ILegendPanel;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 *
 * @author Alexis Guéganno
 */
public class PnlUniquePointSE extends PnlUniqueAreaSE {
        private static final I18n I18N = I18nFactory.getI18n(PnlUniquePointSE.class);

        /**
         * Here we can put all the Legend instances we want... but they have to
         * be unique symbol (ie constant) Legends.
         */
        private UniqueSymbolPoint uniquePoint;

        @Override
        public Component getComponent() {
                return this;
        }

        @Override
        public Legend getLegend() {
                return uniquePoint;
        }

        @Override
        public void setLegend(Legend legend) {
                if (legend instanceof UniqueSymbolPoint) {
                        uniquePoint = (UniqueSymbolPoint) legend;
                        ConstantPenStroke cps = uniquePoint.getPenStroke();
                        if(cps instanceof ConstantPenStrokeLegend ){
                                setPenStrokeMemory((ConstantPenStrokeLegend) cps);
                        } else {
                                PenStrokeAnalyzer psa = new PenStrokeAnalyzer(new PenStroke());
                                setPenStrokeMemory((ConstantPenStrokeLegend) psa.getLegend());
                        }
                        ConstantSolidFill csf = uniquePoint.getFillLegend();
                        if(csf instanceof ConstantSolidFillLegend){
                                setSolidFillMemory((ConstantSolidFillLegend) csf);
                        } else {
                                FillAnalyzer fa = new FillAnalyzer(new SolidFill());
                                setSolidFillMemory((ConstantSolidFillLegend) fa.getLegend());
                        }
                        initPreview();
                        this.initializeLegendFields();
                } else {
                        throw new IllegalArgumentException("The given Legend is not"
                                + "a UniqueSymbolPoint");
                }
        }

        /**
         * Initialize the panel. This method is called just after the panel
         * creation.</p> <p>WARNING : the panel will be empty after calling this
         * method. Indeed, there won't be any {@code Legend} instance associated
         * to it. Use the
         * {@code setLegend} method to achieve this goal.
         *
         * @param lc LegendContext is useful to get some information about the
         * layer in edition.
         */
        @Override
        public void initialize(LegendContext lc) {
                if (uniquePoint == null) {
                        setLegend(new UniqueSymbolPoint());
                }
        }

        @Override
        public boolean acceptsGeometryType(int geometryType) {
                return (geometryType & GeometryProperties.ALL) != 0;
        }

        @Override
        public ILegendPanel newInstance() {
                return new PnlUniquePointSE();
        }

        @Override
        public String validateInput() {
                return null;
        }

        @Override
        public URL getIconURL() {
                return UIFactory.getDefaultIcon();
        }

        @Override
        public String getTitle() {
                return "Unique symbol for lines.";
        }
        

        @Override
        public Legend copyLegend() {
                return new UniqueSymbolPoint();
        }

        private void initializeLegendFields() {
                this.removeAll();
                JPanel glob = new JPanel();
                GridBagLayout grid = new GridBagLayout();
                glob.setLayout(grid);
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                JPanel p1 = getLineBlock(uniquePoint.getPenStroke(), I18N.tr("Line configuration"));
                glob.add(p1, gbc);
                gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.insets = new Insets(5, 0, 5, 0);
                JPanel p2 = getAreaBlock(uniquePoint.getFillLegend(), I18N.tr("Fill configuration"));
                glob.add(p2, gbc);
                gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 2;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.insets = new Insets(5, 0, 5, 0);
                JPanel p3 = getPointBlock(uniquePoint, I18N.tr("Mark configuration"));
                glob.add(p3, gbc);
                gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 3;
                glob.add(getPreview(), gbc);
                this.add(glob);
        }

        /**
         * Builds the UI block used to configure the fill color of the
         * symbolizer.
         * @param fillLegend
         * @param title
         * @return
         */
        public JPanel getPointBlock(UniqueSymbolPoint point, String title) {
                if(getPreview() == null && getLegend() != null){
                        initPreview();
                }
                JPanel glob = new JPanel();
                glob.setLayout(new BoxLayout(glob, BoxLayout.Y_AXIS));
                JPanel jp = new JPanel();
                GridLayout grid = new GridLayout(2,2);
                grid.setVgap(5);
                jp.setLayout(grid);
                //Mark width
                jp.add(buildText(I18N.tr("Mark width :")));
                jp.add(getMarkWidth(point));
                //Mark height
                jp.add(buildText(I18N.tr("Mark height :")));
                jp.add(getMarkHeight(point));
                glob.add(jp);
                //We add a canvas to display a preview.
                glob.setBorder(BorderFactory.createTitledBorder(title));
                return glob;
        }

        private JPanel getMarkWidth(UniqueSymbolPoint point){
                CanvasSE prev = getPreview();
                final JNumericSpinner jns = new JNumericSpinner(4, Integer.MIN_VALUE, Integer.MAX_VALUE, 0.01);
                ChangeListener cl = EventHandler.create(ChangeListener.class, point, "viewBoxWidth", "source.value");
                jns.addChangeListener(cl);
                jns.setValue(point.getViewBoxWidth() == null? point.getViewBoxHeight() : point.getViewBoxWidth());
                jns.setMaximumSize(new Dimension(60,30));
                jns.setPreferredSize(new Dimension(60,30));
                ChangeListener cl2 = EventHandler.create(ChangeListener.class, prev, "repaint");
                jns.addChangeListener(cl2);
                return jns;
        }

        private JPanel getMarkHeight(UniqueSymbolPoint point){
                CanvasSE prev = getPreview();
                final JNumericSpinner jns = new JNumericSpinner(4, Integer.MIN_VALUE, Integer.MAX_VALUE, 0.01);
                ChangeListener cl = EventHandler.create(ChangeListener.class, point, "viewBoxHeight", "source.value");
                jns.addChangeListener(cl);
                jns.setValue(point.getViewBoxHeight() == null? point.getViewBoxWidth() : point.getViewBoxHeight());
                jns.setMaximumSize(new Dimension(60,30));
                jns.setPreferredSize(new Dimension(60,30));
                ChangeListener cl2 = EventHandler.create(ChangeListener.class, prev, "repaint");
                jns.addChangeListener(cl2);
                return jns;
        }

}
