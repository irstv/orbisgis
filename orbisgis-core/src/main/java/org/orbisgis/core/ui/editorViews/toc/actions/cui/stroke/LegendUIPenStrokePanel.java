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
import org.orbisgis.core.renderer.se.parameter.string.StringParameter;
import org.orbisgis.core.renderer.se.stroke.PenStroke;
import org.orbisgis.core.renderer.se.stroke.PenStroke.LineCap;
import org.orbisgis.core.renderer.se.stroke.PenStroke.LineJoin;
import org.orbisgis.core.renderer.se.stroke.Stroke;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.LegendUIAbstractPanel;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.LegendUIComponent;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.LegendUIController;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.components.ComboBoxInput;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.components.UomInput;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.fill.LegendUIMetaFillPanel;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.parameter.real.LegendUIMetaRealPanel;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.parameter.string.LegendUIMetaStringPanel;
import org.orbisgis.core.ui.preferences.lookandfeel.OrbisGISIcon;

/**
 *
 * @author maxence
 */
public abstract class LegendUIPenStrokePanel extends LegendUIComponent implements LegendUIStrokeComponent{

	private final PenStroke penStroke;

    private final LegendUIMetaFillPanel fill;

	private final LegendUIMetaRealPanel strokeWidth;

	//private final LegendUIMetaRealPanel opacity;

	private final LegendUIMetaRealPanel dashOffset;

	private UomInput uom;

	private ComboBoxInput lineCap;

	private ComboBoxInput lineJoin;

	private final LineCap[] lCapValues;

	private final LineJoin[] lJoinValues;

	private LegendUIAbstractPanel content;
	private LegendUIAbstractPanel content2;
	private LegendUIAbstractPanel header;

	//private TextInput dashArray;
	private LegendUIMetaStringPanel dashArray;


	public LegendUIPenStrokePanel(LegendUIController controller, LegendUIComponent parent, PenStroke pStroke, boolean isNullable) {
		super("pen stroke", controller, parent, 0, isNullable);
		//this.setLayout(new GridLayout(0,2));
		this.header = new LegendUIAbstractPanel(controller);
		this.penStroke = pStroke;

		this.content = new LegendUIAbstractPanel(controller);
		this.content2 = new LegendUIAbstractPanel(controller);

		uom = new UomInput(pStroke);

		lCapValues = LineCap.values();
		lJoinValues = LineJoin.values();

		lineCap = new ComboBoxInput(lCapValues, penStroke.getLineCap().ordinal()) {

			@Override
			protected void valueChanged(int i) {
				penStroke.setLineCap(lCapValues[i]);
			}
		};


		lineJoin = new ComboBoxInput(lJoinValues, penStroke.getLineJoin().ordinal()) {

			@Override
			protected void valueChanged(int i) {
				penStroke.setLineJoin(lJoinValues[i]);
			}
		};



        this.fill = new LegendUIMetaFillPanel(controller, this, pStroke, false);
        fill.init();

		this.strokeWidth = new LegendUIMetaRealPanel("Width", controller, this, penStroke.getWidth(), true) {

			@Override
			public void realChanged(RealParameter newReal) {
				penStroke.setWidth(newReal);
			}
		};
		strokeWidth.init();

		this.dashOffset = new LegendUIMetaRealPanel("Dash Offset", controller, this, penStroke.getDashOffset(), true) {

			@Override
			public void realChanged(RealParameter newReal) {
				penStroke.setDashOffset(newReal);
			}
		};
		dashOffset.init();

		dashArray = new LegendUIMetaStringPanel("Dash Array", controller, this, penStroke.getDashArray(), true) {

			@Override
			public void stringChanged(StringParameter newString) {
				penStroke.setDashArray(newString);
			}
		};
		dashArray.init();
	}

	@Override
	public Icon getIcon() {
		return OrbisGISIcon.PENCIL;
	}

	@Override
	protected void mountComponent() {
		header.removeAll();
		header.add(uom, BorderLayout.WEST);
		header.add(lineCap, BorderLayout.CENTER);
		header.add(lineJoin, BorderLayout.EAST);

		editor.add(header, BorderLayout.NORTH);

		content.removeAll();

		content.add(fill, BorderLayout.NORTH);
		content.add(strokeWidth, BorderLayout.CENTER);
		//content.add(opacity, BorderLayout.SOUTH);

		content2.removeAll();
		content2.add(content, BorderLayout.NORTH);
		content2.add(dashArray, BorderLayout.CENTER);
		content2.add(dashOffset, BorderLayout.SOUTH);

		editor.add(content2, BorderLayout.SOUTH);
	}

	@Override
	public Stroke getStroke() {
		return this.penStroke;
	}

	@Override
	public Class getEditedClass() {
		return PenStroke.class;
	}
}
