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
package org.orbisgis.legend.thematic;

import java.awt.Color;
import org.orbisgis.core.renderer.se.LineSymbolizer;
import org.orbisgis.core.renderer.se.Symbolizer;
import org.orbisgis.core.renderer.se.common.Uom;
import org.orbisgis.legend.LegendStructure;
import org.orbisgis.legend.structure.stroke.ConstantColorAndDashesPSLegend;
import org.orbisgis.legend.structure.stroke.PenStrokeLegend;

/**
 * Abstract a representation of line whose {@code Color} and dash array are
 * constant, but where the management of the width is unknown.
 * @author Alexis Guéganno
 */
public abstract class ConstantColorAndDashesLine extends SymbolizerLegend {

    private LineSymbolizer lineSymbolizer;

    /**
     * Basically sets the inner {@code LineSymbolizer}.
     * @param symbolizer
     */
    public ConstantColorAndDashesLine(LineSymbolizer symbolizer){
        lineSymbolizer = symbolizer;
    }

    /**
     * Get the associated {@code LineSymbolizer};
     * @return
     */
    @Override
    public Symbolizer getSymbolizer() {
        return lineSymbolizer;
    }

    /**
     * Get the {@code LegendStructure} associated to the {@code Stroke} of this
     * {@code ConstantColorAndDashesLine}. This method is abstract, as it is the
     * only particularity of the implementations.
     * @return
     */
    public abstract LegendStructure getStrokeLegend();


    /**
     * Get the {@code Color} of that will be used to draw lines.
     * @return
     */
    public Color getLineColor() {
        return ((ConstantColorAndDashesPSLegend) getStrokeLegend()).getLineColor();
    }

    /**
     * Set the {@code Color} of that will be used to draw lines.
     * @param col
     */
    public void setLineColor(Color col) {
        ((ConstantColorAndDashesPSLegend) getStrokeLegend()).setLineColor(col);
    }

    @Override
    public Uom getStrokeUom(){
            return ((PenStrokeLegend) getStrokeLegend()).getStrokeUom();
    }

    @Override
    public void setStrokeUom(Uom u){
            ((PenStrokeLegend) getStrokeLegend()).setStrokeUom(u);
    }
}
