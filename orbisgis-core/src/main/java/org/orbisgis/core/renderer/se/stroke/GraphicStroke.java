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
package org.orbisgis.core.renderer.se.stroke;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import net.opengis.se._2_0.core.GraphicStrokeType;
import net.opengis.se._2_0.core.ObjectFactory;
import org.gdms.data.values.Value;
import org.orbisgis.core.map.MapTransform;
import org.orbisgis.core.renderer.se.GraphicNode;
import org.orbisgis.core.renderer.se.SeExceptions.InvalidStyle;
import org.orbisgis.core.renderer.se.UomNode;
import org.orbisgis.core.renderer.se.common.RelativeOrientation;
import org.orbisgis.core.renderer.se.common.ShapeHelper;
import org.orbisgis.core.renderer.se.common.Uom;
import org.orbisgis.core.renderer.se.graphic.GraphicCollection;
import org.orbisgis.core.renderer.se.graphic.MarkGraphic;
import org.orbisgis.core.renderer.se.parameter.ParameterException;
import org.orbisgis.core.renderer.se.parameter.SeParameterFactory;
import org.orbisgis.core.renderer.se.parameter.UsedAnalysis;
import org.orbisgis.core.renderer.se.parameter.real.RealParameter;
import org.orbisgis.core.renderer.se.parameter.real.RealParameterContext;

/**
 * A {@code GraphicStroke} is used essentially to repeat a a graphic along a line. It is dependant 
 * upon :
 * <ul><li>A {@link GraphicCollection} that contains the graphic to render</li>
 * <li>The length (as a {@link RealParameter}) to reserve along the line to plot 
 * a single {@code Graphic} instance. Must be positive, and is defaulted to the 
 * {@code Graphic} natural length.</li>
 * <li>A relative orientation, as defined in {@link RelativeOrientation}.</li></ul>
 * @author Maxence Laurent, Alexis Guéganno
 */
public final class GraphicStroke extends Stroke implements GraphicNode, UomNode {

    public static final double MIN_LENGTH = 1; // In pixel !

    private GraphicCollection graphic;
    private RealParameter length;
    private RelativeOrientation orientation;
    private RealParameter relativePosition;

    /**
     * Build a new {@code GraphicStroke} using the {@code JAXBElement} given in argument.
     * @param elem
     * @throws org.orbisgis.core.renderer.se.SeExceptions.InvalidStyle 
     */
    GraphicStroke(JAXBElement<GraphicStrokeType> elem) throws InvalidStyle {
        this(elem.getValue());
    }

    /**
     * Build a new {@code GraphicStroke} using the JAXB type given in argument.
     * @param gst
     * @throws org.orbisgis.core.renderer.se.SeExceptions.InvalidStyle 
     */
    GraphicStroke(GraphicStrokeType gst) throws InvalidStyle {
        super(gst);

        if (gst.getGraphic() != null) {
            this.setGraphicCollection(new GraphicCollection(gst.getGraphic(), this));
        }

        if (gst.getLength() != null) {
            this.setLength(SeParameterFactory.createRealParameter(gst.getLength()));
        }

        if (gst.getRelativeOrientation() != null) {
            this.setRelativeOrientation(RelativeOrientation.readFromToken(gst.getRelativeOrientation()));
        } else {
            this.setRelativeOrientation(RelativeOrientation.NORMAL);
        }

        if (gst.getRelativePosition() != null) {
            setRelativePosition(SeParameterFactory.createRealParameter(gst.getRelativePosition()));
        }
    }

    /**
     * Build a new, default, {@code GraphicStroke}. It is defined with a default
     * {@link MarkGraphic}, as defined in {@link MarkGraphic#MarkGraphic() the default constructor}.
     */
    public GraphicStroke() {
        super();
        this.graphic = new GraphicCollection();
        MarkGraphic mg = new MarkGraphic();
        mg.setTo3mmCircle();
        graphic.addGraphic(mg);
    }


    @Override
    public void setGraphicCollection(GraphicCollection graphic) {
        this.graphic = graphic;
    }


    @Override
    public GraphicCollection getGraphicCollection() {
        return graphic;
    }

    /**
     * Set the length used to plot the embedded graphic. If set to null, then this length
     * is defaulted to the natural length of the graphic.
     * @param length 
     */
    public void setLength(RealParameter length) {
        this.length = length;
        if (this.length != null) {
            this.length.setContext(RealParameterContext.NON_NEGATIVE_CONTEXT);
            this.length.setParent(this);
        }
    }

    /**
     * Get the length used to plot the embedded graphic. If {@code null}, then the length
     * of the embedded {@code Graphic} is used.
     * @return 
     */
    public RealParameter getLength() {
        return length;
    }

    /**
     * Set the orientation of the graphic.
     * @param orientation 
     */
    public void setRelativeOrientation(RelativeOrientation orientation) {
        this.orientation = orientation;
    }

    /**
     * Get the orientation of the graphic.
     * @return 
     */
    public RelativeOrientation getRelativeOrientation() {
        if (orientation != null) {
            return orientation;
        } else {
            return RelativeOrientation.PORTRAYAL;
        }
    }


    public RealParameter getRelativePosition() {
        return relativePosition;
    }


    public void setRelativePosition(RealParameter relativePosition) {
        this.relativePosition = relativePosition;
        if (this.relativePosition != null){
            this.relativePosition.setContext(RealParameterContext.PERCENTAGE_CONTEXT);
            this.relativePosition.setParent(this);
        }
    }


    @Override
    public Double getNaturalLength(Map<String,Value> map, Shape shp, MapTransform mt) throws ParameterException, IOException {
        double naturalLength;

        if (length != null) {
            double lineLength = ShapeHelper.getLineLength(shp);
            Double value = length.getValue(map);
            if (value != null) {
                naturalLength = Uom.toPixel(value, getUom(), mt.getDpi(), mt.getScaleDenominator(), lineLength);
                //if (naturalLength <= GraphicStroke.MIN_LENGTH || naturalLength > lineLength) {
                if (naturalLength < 1e-5 || Double.isInfinite(naturalLength)){
                    return Double.POSITIVE_INFINITY;
                }

                if (naturalLength > lineLength) {
                    naturalLength = lineLength;
                }

                return naturalLength;
            }
        }

        return getGraphicWidth(map, mt);
    }

    private double getGraphicWidth(Map<String,Value> map, MapTransform mt) throws ParameterException, IOException {
        RelativeOrientation rOrient = this.getRelativeOrientation();
        Rectangle2D bounds = graphic.getBounds(map, false, mt);

        double gWidth = bounds.getWidth();
        double gHeight = bounds.getHeight();

        switch (rOrient) {
            case LINE:
                return gHeight;
            case NORMAL:
            case NORMAL_UP:
            case PORTRAYAL:
            default:
                return gWidth;
        }
    }


    @Override
    public void draw(Graphics2D g2, Map<String,Value> map,
            Shape shape, boolean selected, MapTransform mt, double offset)
            throws ParameterException, IOException {

        List<Shape> shapes;

        if (!this.isOffsetRapport() && Math.abs(offset) > 0.0) {
            shapes = ShapeHelper.perpendicularOffset(shape, offset);
            // Setting offset to 0.0 let be sure the offset will never been applied twice!
            offset = 0.0;
        } else {
            shapes = new ArrayList<Shape>();
            // TODO : Extract holes as separate shape !
            shapes.add(shape);
        }


        double gWidth = getGraphicWidth(map, mt);
        for (Shape shp : shapes) {
            double segLength = getNaturalLength(map, shp, mt);
            double lineLength = ShapeHelper.getLineLength(shp);

            if (segLength > lineLength){
                segLength = lineLength;
            }
                
            RelativeOrientation rOrient = this.getRelativeOrientation();
            List<Shape> segments = null;

            double nbSegments;

            //int nbToDraw;

            if (this.isLengthRapport()) {
                nbSegments = (int) ((lineLength / segLength) + 0.5);
                segments = ShapeHelper.splitLine(shp, (int) nbSegments);
                //segLength = lineLength / nbSegments;
                //nbToDraw = (int) nbSegments;
            } else {
                nbSegments = lineLength / segLength;
                if (nbSegments == 0 && getParent() instanceof StrokeElement) {
                    nbSegments = 1;
                }
                if (nbSegments > 0) {
                    // TODO remove half of extra space at the beginning of the line
                    //shp = ShapeHelper.splitLine(shp, (nbSegments - nbToDraw)/2.0).get(1);
                    segments = ShapeHelper.splitLineInSeg(shp, segLength);
                }
            }

            if (segments != null) {
                for (Shape seg : segments) {
                    List<Shape> oSegs;
                    if (this.isOffsetRapport() && Math.abs(offset) > 0.0) {
                        oSegs = ShapeHelper.perpendicularOffset(seg, offset);
                    } else {
                        oSegs = new ArrayList<Shape>();
                        oSegs.add(seg);
                    }

                    for (Shape oSeg : oSegs) {
                        if (oSeg != null) {
                            double realSegLength = ShapeHelper.getLineLength(oSeg);
                            // Is there enough space on the real segment ?  otherwise is the graphic part of a compound stroke ?
                            if (realSegLength > 0.9 * segLength || (getParent() instanceof StrokeElement && segLength == 0.0)) {
                                Point2D.Double pt;
                                double relativePos = 0.5;

                                if (relativePosition != null) {
                                    relativePos = relativePosition.getValue(map);
                                }

                                if (segLength < MIN_LENGTH) {
                                    pt = ShapeHelper.getPointAt(oSeg, 0);
                                } else {
                                    // TODO Replace with relative position !
                                    pt = ShapeHelper.getPointAt(oSeg, realSegLength * relativePos);
                                }
                                AffineTransform at = AffineTransform.getTranslateInstance(pt.x, pt.y);

                                if (rOrient != RelativeOrientation.PORTRAYAL) {
                                    Point2D.Double ptA;
                                    Point2D.Double ptB;

                                    if (segLength < MIN_LENGTH) {
                                        ptA = pt;
                                        ptB = ShapeHelper.getPointAt(oSeg, gWidth);
                                    } else {
                                        ptA = ShapeHelper.getPointAt(oSeg, relativePos * realSegLength - (gWidth*0.5));
                                        ptB = ShapeHelper.getPointAt(oSeg, relativePos * realSegLength + (gWidth*0.5));
                                    }

                                    double theta = Math.atan2(ptB.y - ptA.y, ptB.x - ptA.x);
                                    switch (rOrient) {
                                        case LINE:
                                            theta += 0.5 * Math.PI;
                                            break;
                                        case NORMAL_UP:
                                            if (theta < -Math.PI / 2 || theta > Math.PI / 2) {
                                                theta += Math.PI;
                                            }
                                            break;
                                    }
                                    at.concatenate(AffineTransform.getRotateInstance(theta));
                                }

                                graphic.draw(g2, map, selected, mt, at);
                            }
                        }
                    }
                }
            }
        }
    }


    @Override
    public HashSet<String> dependsOnFeature() {
        HashSet<String> result = null;
        if (graphic != null) {
            result = graphic.dependsOnFeature();
        }
        if (length != null) {
            if(result == null){
                result = length.dependsOnFeature();
            } else {
                result.addAll(length.dependsOnFeature());
            }
        }
        return result == null ? new HashSet<String>() : result;
    }

    @Override
    public UsedAnalysis getUsedAnalysis() {
        UsedAnalysis result = new UsedAnalysis();
        if (graphic != null) {
            result.merge(graphic.getUsedAnalysis());
        }
        if (length != null) {
                result.merge(length.getUsedAnalysis());
        }
        if (relativePosition != null) {
                result.merge(relativePosition.getUsedAnalysis());
        }
        return result;
    }

    @Override
    public JAXBElement<GraphicStrokeType> getJAXBElement() {
        ObjectFactory of = new ObjectFactory();
        return of.createGraphicStroke(this.getJAXBType());
    }


    private GraphicStrokeType getJAXBType() {
        GraphicStrokeType s = new GraphicStrokeType();

        this.setJAXBProperties(s);


        if (getOwnUom() != null) {
            s.setUom(getOwnUom().toURN());
        }

        if (graphic != null) {
            s.setGraphic(graphic.getJAXBElement());
        }

        if (length != null) {
            s.setLength(length.getJAXBParameterValueType());
        }

        if (orientation != null) {
            s.setRelativeOrientation(orientation.getJAXBType());
        }

        if (relativePosition != null) {
            s.setRelativePosition(relativePosition.getJAXBParameterValueType());
        }
        return s;
    }


}
