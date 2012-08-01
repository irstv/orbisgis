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
package org.orbisgis.legend.structure.fill;

import java.awt.Color;
import org.orbisgis.core.renderer.se.fill.SolidFill;
import org.orbisgis.legend.structure.categorize.Categorize2ColorLegend;
import org.orbisgis.legend.structure.literal.RealLiteralLegend;

/**
 * A {@code Legend} that represents a {@code SolidFill} where the color is defined
 * accorgind to a {@code Categorize} operation.
 * @author Alexis Guéganno
 */
public class CategorizedSolidFillLegend extends SolidFillLegend {

        /**
         * Build a new {@code CategorizedSolidFillLegend} using the {@code 
         * SolidFill} and {@code Categorize2ColorLegend} given in parameter.
         * @param fill
         * @param colorLegend
         */
        public CategorizedSolidFillLegend(SolidFill fill, Categorize2ColorLegend colorLegend, RealLiteralLegend opacity) {
                super(fill, colorLegend, opacity);
        }

        /**
         * Get the colour returned if the input data can't be associated to one
         * of the given interval.
         * @return
         */
        public Color getFallBackColor() {
            return ((Categorize2ColorLegend) getColorLegend()).getFallBackColor();
        }

        /**
         * Set the colour returned if the input data can't be associated to one
         * of the given interval.
         * @param col
         */
        public void setFallBackColor(Color col) {
            ((Categorize2ColorLegend) getColorLegend()).setFallBackColor(col);
        }

        /**
         * Get the {@code Color} that is returned for input values that are inferior
         * to the first threshold.
         * @param i
         * The index of the class we want to retrieve the {@code Color} from.
         * @return
         */
        public Color getColor(int i) {
            return ((Categorize2ColorLegend) getColorLegend()).getColor(i);
        }

        /**
         * Set the {@code Color} that is returned for input values that are inferior
         * to the first threshold.
         * @param i
         * The index of the class we want to set the {@code Color}.
         * @param col
         */
        public void setColor(int i, Color col) {
            ((Categorize2ColorLegend) getColorLegend()).setColor(i, col);
        }

        /**
         * Get the number of classes defined in the inner {@code Categorize}.
         * @return
         */
        public int getNumClass() {
            return ((Categorize2ColorLegend) getColorLegend()).getNumClass();
        }

        /**
         * Get the value of the ith threshold.
         * @param i
         * @return
         */
        public double getThreshold(int i) {
            return ((Categorize2ColorLegend) getColorLegend()).getThreshold(i);
        }

        /**
         * Get the value of the ith threshold.
         * @param i
         * @param d
         */
        public void setThreshold(int i, double d) {
            ((Categorize2ColorLegend) getColorLegend()).setThreshold(i, d);
        }

        /**
         * Add a class to the inner {@code Categorize}.
         * @param threshold
         * @param col
         */
        public void addClass(double threshold, Color col){
            ((Categorize2ColorLegend) getColorLegend()).addClass(threshold, col);
        }

        /**
         * Remove the ith class of this {@code Categorize}.
         * @param i
         */
        public void removeClass(int i) {
            ((Categorize2ColorLegend) getColorLegend()).removeClass(i);
        }

}
