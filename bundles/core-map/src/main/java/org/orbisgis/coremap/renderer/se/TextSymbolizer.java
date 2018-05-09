/**
 * OrbisGIS is a java GIS application dedicated to research in GIScience.
 * OrbisGIS is developed by the GIS group of the DECIDE team of the 
 * Lab-STICC CNRS laboratory, see <http://www.lab-sticc.fr/>.
 *
 * The GIS group of the DECIDE team is located at :
 *
 * Laboratoire Lab-STICC – CNRS UMR 6285
 * Equipe DECIDE
 * UNIVERSITÉ DE BRETAGNE-SUD
 * Institut Universitaire de Technologie de Vannes
 * 8, Rue Montaigne - BP 561 56017 Vannes Cedex
 * 
 * OrbisGIS is distributed under GPL 3 license.
 *
 * Copyright (C) 2007-2014 CNRS (IRSTV FR CNRS 2488)
 * Copyright (C) 2015-2017 CNRS (Lab-STICC UMR CNRS 6285)
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
package org.orbisgis.coremap.renderer.se;

import org.locationtech.jts.geom.Geometry;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import net.opengis.se._2_0.core.ObjectFactory;
import net.opengis.se._2_0.core.TextSymbolizerType;
import org.orbisgis.coremap.map.MapTransform;
import org.orbisgis.coremap.renderer.se.SeExceptions.InvalidStyle;
import org.orbisgis.coremap.renderer.se.common.ShapeHelper;
import org.orbisgis.coremap.renderer.se.common.Uom;
import org.orbisgis.coremap.renderer.se.label.Label;
import org.orbisgis.coremap.renderer.se.label.PointLabel;
import org.orbisgis.coremap.renderer.se.parameter.ParameterException;
import org.orbisgis.coremap.renderer.se.parameter.SeParameterFactory;
import org.orbisgis.coremap.renderer.se.parameter.geometry.GeometryAttribute;
import org.orbisgis.coremap.renderer.se.parameter.real.RealParameter;
import org.orbisgis.coremap.renderer.se.parameter.real.RealParameterContext;

/**
 * {@code TextSymbolizer} instances are used to style text labels. In addition to
 * the {@link VectorSymbolizer} parameters, it is computed given these arguments :
 * <ul><li>Perpendicular Offset : Transformation according a line parallel to the
 * original geometry</li>
 * <li>A {@link Label} that gathers all the informations needed to print the 
 * text. This element is compulsory.</li></ul>
 * @author Alexis Guéganno, Maxence Laurent
 */
public final class TextSymbolizer extends VectorSymbolizer {

        private RealParameter perpendicularOffset;
        private Label label;

        /**
         * Build a new {@code TextSymbolizer} using the informations contained 
         * in the {@code JAXBElement} given in argument.
         * @param st
         * @throws org.orbisgis.coremap.renderer.se.SeExceptions.InvalidStyle
         */
        public TextSymbolizer(JAXBElement<TextSymbolizerType> st) throws InvalidStyle {
                super(st);
                TextSymbolizerType tst = st.getValue();

                if (tst.getGeometry() != null) {
                        this.setGeometryAttribute(new GeometryAttribute(tst.getGeometry()));
                }

                if (tst.getUom() != null) {
                        setUom(Uom.fromOgcURN(tst.getUom()));
                }

                if (tst.getPerpendicularOffset() != null) {
                        this.setPerpendicularOffset(SeParameterFactory.createRealParameter(tst.getPerpendicularOffset()));
                }

                if (tst.getLabel() != null) {
                        this.setLabel(Label.createLabelFromJAXBElement(tst.getLabel()));
                }
        }

        /**
         * Build a new {@code TextSymbolizer}, named {@code Label}. It is defined
         * using a default {@link PointLabel#PointLabel() PointLabel}, and is
         * measured in {@link Uom#MM}.
         */
        public TextSymbolizer() {
                super();
                this.name = "Label";
                setLabel(new PointLabel());
                setUom(Uom.MM);
        }

        /**
         * Set the label contained in this {@code TextSymbolizer}.
         * @param label 
         * The new {@code Label} contained in this {@code TextSymbolizer}. Must 
         * be non-{@code null}.
         */
        public void setLabel(Label label) {
                label.setParent(this);
                this.label = label;
        }

        /**
         * Get the label contained in this {@code TextSymbolizer}.
         * @return 
         * The label currently contained in this {@code TextSymbolizer}.
         */
        public Label getLabel() {
                return label;
        }

        /**
         * Get the offset currently associated to this {@code TextSymbolizer}.
         * @return 
         * The current perpendicular offset as a {@code RealParameter}. If null, 
         * the offset is considered to be equal to {@code 0}.
         */
        public RealParameter getPerpendicularOffset() {
                return perpendicularOffset;
        }

        /**
         * Set the perpendicular offset associated to this {@code TextSymbolizer}.
         * @param perpendicularOffset 
         */
        public void setPerpendicularOffset(RealParameter perpendicularOffset) {
                this.perpendicularOffset = perpendicularOffset;
                if (this.perpendicularOffset != null) {
                        this.perpendicularOffset.setContext(RealParameterContext.REAL_CONTEXT);
                        this.perpendicularOffset.setParent(this);
                }
        }

        @Override
        public void draw(Graphics2D g2, ResultSet rs, long fid,
                boolean selected, MapTransform mt, Geometry the_geom)
                throws ParameterException, IOException, SQLException {
                Shape shape = this.getShape(rs, fid, mt, the_geom, false);
                Map<String,Object> map = getFeaturesMap(rs, fid);
                if (shape != null) {
                        List<Shape> shps;
                        if (perpendicularOffset != null) {
                                Double pOffset = perpendicularOffset.getValue(map);
                                shps = ShapeHelper.perpendicularOffset(shape, pOffset);
                        } else {
                                shps = new LinkedList<Shape>();
                                shps.add(shape);
                        }
                        for (Shape s : shps) {
                                label.draw(g2, map, s, selected, mt);
                        }
                }


        }

        @Override
        public JAXBElement<TextSymbolizerType> getJAXBElement() {

                ObjectFactory of = new ObjectFactory();
                TextSymbolizerType s = of.createTextSymbolizerType();

                this.setJAXBProperty(s);

                if (this.getGeometryAttribute() != null) {
                        s.setGeometry(getGeometryAttribute().getJAXBGeometryType());
                }

                if (this.getUom() != null) {
                        s.setUom(this.getUom().toURN());
                }

                if (perpendicularOffset != null) {
                        s.setPerpendicularOffset(perpendicularOffset.getJAXBParameterValueType());
                }

                if (label != null) {
                        s.setLabel(label.getJAXBElement());
                }

                return of.createTextSymbolizer(s);
        }

        @Override
        public List<SymbolizerNode> getChildren() {
                List<SymbolizerNode> ls = new ArrayList<SymbolizerNode>();
                if(this.getGeometryAttribute()!=null){
                    ls.add(this.getGeometryAttribute());
                }
                if (perpendicularOffset != null) {
                        ls.add(perpendicularOffset);
                }
                if (label != null) {
                        ls.add(label);
                }
                return ls;
        }
}
