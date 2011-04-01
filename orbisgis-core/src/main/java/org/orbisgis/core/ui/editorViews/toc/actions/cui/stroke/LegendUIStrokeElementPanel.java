/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information. OrbisGIS is
 * distributed under GPL 3 license. It is produced by the "Atelier SIG" team of
 * the IRSTV Institute <http://www.irstv.cnrs.fr/> CNRS FR 2488.
 *
 *
 *  Team leader Erwan BOCHER, scientific researcher,
 *
 *  User support leader : Gwendall Petit, geomatic engineer.
 *
 *
 * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
 *
 * Copyright (C) 2010 Erwan BOCHER, Pierre-Yves FADET, Alexis GUEGANNO, Maxence LAURENT
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
 *
 * or contact directly:
 * erwan.bocher _at_ ec-nantes.fr
 * gwendall.petit _at_ ec-nantes.fr
 */

package org.orbisgis.core.ui.editorViews.toc.actions.cui.stroke;

import java.awt.BorderLayout;
import javax.swing.Icon;
import org.orbisgis.core.renderer.se.parameter.real.RealParameter;
import org.orbisgis.core.renderer.se.stroke.StrokeElement;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.LegendUIAbstractPanel;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.LegendUIComponent;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.LegendUIController;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.parameter.real.LegendUIMetaRealPanel;
import org.orbisgis.core.ui.preferences.lookandfeel.OrbisGISIcon;


/**
 *
 * @author maxence
 */
public class LegendUIStrokeElementPanel extends LegendUIComponent {

    private StrokeElement elem;
    private LegendUIMetaStrokePanel stroke;
    private LegendUIMetaRealPanel preGap;
    private LegendUIMetaRealPanel postGap;
    private LegendUIMetaRealPanel length;

    private LegendUIAbstractPanel header;

    public LegendUIStrokeElementPanel(LegendUIController controller, LegendUIComponent parent, StrokeElement element){
        super("", controller, parent, 0, false);
        this.elem = element;


        header = new LegendUIAbstractPanel(controller);
        stroke = new LegendUIMetaStrokePanel(controller, this, element, false);
        stroke.init();

        preGap = new LegendUIMetaRealPanel("PreGap", controller, this, elem.getPreGap(), true) {

            @Override
            public void realChanged(RealParameter newReal) {
                elem.setPreGap(newReal);
            }
        };
        preGap.init();

        postGap = new LegendUIMetaRealPanel("PostGap", controller, this, elem.getPostGap(), true) {

            @Override
            public void realChanged(RealParameter newReal) {
                elem.setPostGap(newReal);
            }
        };
        postGap.init();

        length = new LegendUIMetaRealPanel("Length", controller, this, elem.getLength(), true) {

            @Override
            public void realChanged(RealParameter newReal) {
                elem.setLength(newReal);
            }
        };
        length.init();
    }

    @Override
    public Icon getIcon() {
        return OrbisGISIcon.PALETTE;
    }

    @Override
    protected void mountComponent() {
        header.removeAll();

        header.add(preGap, BorderLayout.WEST);
        header.add(postGap, BorderLayout.CENTER);
        header.add(length, BorderLayout.EAST);

        editor.add(header, BorderLayout.NORTH);
        editor.add(stroke, BorderLayout.SOUTH);
    }

    @Override
    protected void turnOff() {
        throw new UnsupportedOperationException("unreachable");
    }

    @Override
    protected void turnOn() {
        throw new UnsupportedOperationException("unreachable");
    }

    @Override
    public Class getEditedClass() {
        return StrokeElement.class;
    }


}
