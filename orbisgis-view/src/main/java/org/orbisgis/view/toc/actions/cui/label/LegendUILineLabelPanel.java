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
package org.orbisgis.view.toc.actions.cui.label;

import java.awt.BorderLayout;
import javax.swing.Icon;
import org.orbisgis.core.renderer.se.label.Label;
import org.orbisgis.core.renderer.se.label.LineLabel;
import org.orbisgis.core.renderer.se.label.StyledText;
import org.orbisgis.view.toc.actions.cui.LegendUIAbstractPanel;
import org.orbisgis.view.toc.actions.cui.LegendUIComponent;
import org.orbisgis.view.toc.actions.cui.LegendUIController;
import org.orbisgis.view.toc.actions.cui.components.ComboBoxInput;
import org.orbisgis.view.toc.actions.cui.components.UomInput;
import org.orbisgis.view.icons.OrbisGISIcon;


/**
 *
 * @author Maxence Laurent
 */
public class LegendUILineLabelPanel extends LegendUIComponent implements LegendUILabelComponent {

    private LineLabel label;

    private UomInput uom;
    private LegendUIStyledText text;
    private ComboBoxInput vAlign;
    private ComboBoxInput hAlign;

	public LegendUILineLabelPanel(LegendUIController controller, LegendUIComponent parent, LineLabel pl, boolean isNullable) {
        super("Line label", controller, parent, 0, isNullable);
        this.label = pl;

        label.getLabel();

        uom = new UomInput(label);

        text = new LegendUIStyledText(controller, parent, label.getLabel(), isNullable) {

            @Override
            public void textChanged(StyledText newText) {
                // Unreachable
                label.setLabel(newText);
            }
        };

        vAlign = new ComboBoxInput(Label.VerticalAlignment.getList(), label.getVerticalAlign().ordinal()) {
            @Override
            protected void valueChanged(int i) {
                label.setVerticalAlign(Label.VerticalAlignment.values()[i]);
            }
        };

        hAlign = new ComboBoxInput(Label.HorizontalAlignment.getList(), label.getHorizontalAlign().ordinal()) {
            @Override
            protected void valueChanged(int i) {
                label.setHorizontalAlign(Label.HorizontalAlignment.values()[i]);
            }
        };
    }

    @Override
    public Icon getIcon() {
        return OrbisGISIcon.getIcon("palette");
    }

    @Override
    protected void mountComponent() {
        LegendUIAbstractPanel content1 = new LegendUIAbstractPanel(controller);
        LegendUIAbstractPanel content2 = new LegendUIAbstractPanel(controller);

        content1.add(hAlign, BorderLayout.WEST);
        content1.add(vAlign, BorderLayout.CENTER);
        content1.add(uom, BorderLayout.EAST);

        content2.add(text, BorderLayout.CENTER);

        editor.add(content1, BorderLayout.NORTH);
        editor.add(content2, BorderLayout.SOUTH);
    }

    @Override
    protected void turnOff() {
    }

    @Override
    protected void turnOn() {
    }

    @Override
    public Class getEditedClass() {
        return LineLabel.class;
    }

    @Override
    public Label getLabel() {
        return label;
    }
}
