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
package org.orbisgis.core.renderer.se.common;

import com.kitfox.svg.app.beans.SVGIcon;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.jai.InterpolationBicubic2;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import net.opengis.se._2_0.core.ExternalGraphicType;
import net.opengis.se._2_0.core.MarkGraphicType;
import net.opengis.se._2_0.core.VariableOnlineResourceType;
import org.gdms.data.values.Value;
import org.orbisgis.core.map.MapTransform;
import org.orbisgis.core.renderer.se.SeExceptions.InvalidStyle;
import org.orbisgis.core.renderer.se.graphic.ExternalGraphicSource;
import org.orbisgis.core.renderer.se.graphic.MarkGraphicSource;
import org.orbisgis.core.renderer.se.graphic.ViewBox;
import org.orbisgis.core.renderer.se.parameter.ParameterException;
import org.orbisgis.core.renderer.se.parameter.SeParameterFactory;
import org.orbisgis.core.renderer.se.parameter.real.RealParameter;
import org.orbisgis.core.renderer.se.parameter.string.StringParameter;

/**
 *
 * @author Maxence Laurent
 * @todo implements MarkGraphicSource
 */
public class VariableOnlineResource implements ExternalGraphicSource, MarkGraphicSource {

    private StringParameter url;
    private PlanarImage rawImage;
    private SVGIcon svgIcon;
    private Double effectiveWidth;
    private Double effectiveHeight;
    private Double svgInitialWidth;
    private Double svgInitialHeight;


    /**
     *
     */
    public VariableOnlineResource() {
        url = null;
        svgIcon = null;
    }


    public VariableOnlineResource(StringParameter url) throws MalformedURLException {
        this.url = url;
        svgIcon = null;
    }


    public VariableOnlineResource(VariableOnlineResourceType onlineResource)
            throws MalformedURLException, InvalidStyle {
        this.url = SeParameterFactory.createStringParameter(onlineResource.getHref());
        svgIcon = null;
    }


    public StringParameter getUrl() {
        return url;
    }


    public void setUrl(StringParameter url) {
        this.url = url;
    }

    //@Override

    /**
     * @deprecated
     * @param viewBox
     * @param sds
     * @param fid
     * @param mt
     * @param mimeType
     * @return
     * @throws IOException
     * @throws ParameterException
     */
    public RenderedImage getPlanarImage(ViewBox viewBox,
                                        Map<String,Value> map,
                                        MapTransform mt, String mimeType)
            throws IOException, ParameterException {

        if (mimeType != null && mimeType.equalsIgnoreCase("image/svg+xml")) {
            return getSvgImage(viewBox, map, mt, mimeType);
        } else {
            return getJAIImage(viewBox, map, mt, mimeType);
        }
    }


    public Rectangle2D.Double getJAIBounds(ViewBox viewBox,
                                          Map<String,Value> map, MapTransform mt,
                                           String mimeType) throws ParameterException {
        try {
            if (rawImage == null) {
                URL link = new URL(url.getValue(map));
                rawImage = JAI.create("url", link);
                Logger.getLogger(VariableOnlineResource.class.getName()).log(Level.INFO, "Download ExternalGraphic from: {0}", url);
            }
        } catch (Exception ex) {
            throw new ParameterException(ex);
        }

        double width = rawImage.getWidth();
        double height = rawImage.getHeight();

        if (viewBox != null && mt != null && viewBox.usable()) {
            if (map == null && !viewBox.dependsOnFeature().isEmpty()) {
                throw new ParameterException("View box depends on feature"); // TODO I18n 
            }

            try {

                Point2D dim = viewBox.getDimensionInPixel(map, height, width, mt.getScaleDenominator(), mt.getDpi());

                effectiveWidth = dim.getX();
                effectiveHeight = dim.getY();

                if (effectiveWidth > 0 && effectiveHeight > 0) {
                    return new Rectangle2D.Double(-effectiveWidth / 2, -effectiveHeight / 2, effectiveWidth, effectiveHeight);
                } else {
                    effectiveWidth = null;
                    effectiveHeight = null;
                }
            } catch (Exception ex) {
                throw new ParameterException(ex);
            }
        }
        // Others cases => native image bounds
        return new Rectangle2D.Double(-width / 2, -height / 2, width, height);
    }


    public Rectangle2D.Double getSvgBounds(ViewBox viewBox,
                                           Map<String,Value> map, MapTransform mt,
                                           String mimeType) throws ParameterException {
        try {
            /*
             * Fetch SVG if not already done
             */
            if (svgIcon == null) {
                svgIcon = new SVGIcon();
                svgIcon.setSvgURI(new URI(url.getValue(map)));
                svgIcon.setAntiAlias(true);

                this.svgInitialHeight = (double) svgIcon.getIconHeight();
                this.svgInitialWidth = (double) svgIcon.getIconWidth();
            }

            if (viewBox != null && mt != null && viewBox.usable()) {
                if (map == null && !viewBox.dependsOnFeature().isEmpty()) {
                    throw new ParameterException("View box depends on feature"); // TODO I18n
                }


                Point2D dim = viewBox.getDimensionInPixel(map, svgInitialWidth,
                        svgInitialHeight, mt.getScaleDenominator(), mt.getDpi());

                effectiveWidth = dim.getX();
                effectiveHeight = dim.getY();

                if (effectiveHeight > 0 && effectiveWidth > 0) {
                    return new Rectangle2D.Double(-effectiveWidth / 2, -effectiveHeight / 2, effectiveWidth, effectiveHeight);
                } else {

                    double width = svgInitialWidth;
                    double height = svgInitialHeight;
                    effectiveWidth = null;
                    effectiveHeight = null;
                    return new Rectangle2D.Double(-width / 2, -height / 2, width, height);
                }
            } else {
                double width = svgInitialWidth;
                double height = svgInitialHeight;
                return new Rectangle2D.Double(-width / 2, -height / 2, width, height);
            }
        } catch (URISyntaxException ex) {
            throw new ParameterException("Invalid URI", ex);
        }
    }


    @Override
    public Rectangle2D.Double updateCacheAndGetBounds(ViewBox viewBox,
                                                      Map<String,Value> map, MapTransform mt,
                                                      String mimeType) throws ParameterException {
        effectiveWidth = null;
        effectiveHeight = null;
        if (mimeType != null && mimeType.equalsIgnoreCase("image/svg+xml")) {
            return getSvgBounds(viewBox, map, mt, mimeType);
        } else {
            return getJAIBounds(viewBox, map, mt, mimeType);
        }
    }

    /*
     * Draw the svg on g2
     */

    public void drawSVG(Graphics2D g2, AffineTransform at, double opacity) {
        AffineTransform fat = new AffineTransform(at);

        if (effectiveHeight != null && effectiveWidth != null) {
            svgIcon.setPreferredSize(new Dimension((int) (effectiveWidth + 0.5), (int) (effectiveHeight + 0.5)));
            fat.concatenate(AffineTransform.getTranslateInstance(-effectiveWidth / 2, -effectiveHeight / 2));
        } else {
            svgIcon.setPreferredSize(new Dimension((int) (svgInitialWidth + 0.5), (int) (svgInitialHeight + 0.5)));
            fat.concatenate(AffineTransform.getTranslateInstance(-svgInitialWidth / 2, -svgInitialHeight / 2));
        }
        svgIcon.setScaleToFit(true);
        AffineTransform atMedia = new AffineTransform(g2.getTransform());
        g2.transform(fat);

        svgIcon.paintIcon((Component) null, g2, 0, 0);
        g2.setTransform(atMedia);
    }


    public void drawJAI(Graphics2D g2, AffineTransform at, MapTransform mt,
                        double opacity) {
        AffineTransform fat = new AffineTransform(at);
        double width = rawImage.getWidth();
        double height = rawImage.getHeight();

        if (effectiveHeight != null && effectiveWidth != null) {
            double ratioX = effectiveWidth / width;
            double ratioY = effectiveHeight / height;

            RenderedOp img;

            if (ratioX > 1.0 || ratioY > 1.0) {
                img = JAI.create("scale", rawImage,
                                 (float) ratioX, (float) ratioY,
                                 0.0f, 0.0f,
                                 InterpolationBicubic2.getInstance(InterpolationBicubic2.INTERP_BICUBIC_2),
                                 mt.getRenderingHints());
            } else {
                img = JAI.create("SubsampleAverage", rawImage, ratioX, ratioY, mt.getRenderingHints());
            }
            fat.concatenate(AffineTransform.getTranslateInstance(-img.getWidth() / 2.0, -img.getHeight() / 2.0));
            g2.drawRenderedImage(img, fat);
        } else {
            fat.concatenate(AffineTransform.getTranslateInstance(-width / 2.0, -height / 2.0));
            g2.drawRenderedImage(rawImage, fat);
        }
    }


    @Override
    public void draw(Graphics2D g2, AffineTransform at, MapTransform mt,
                     double opacity, String mimeType) {
        if (mimeType != null && mimeType.equalsIgnoreCase("image/svg+xml")) {
            drawSVG(g2, at, opacity);
        } else {
            drawJAI(g2, at, mt, opacity);
        }
    }


    /**
     * @deprecated
     */
    public RenderedImage getSvgImage(ViewBox viewBox,
                                     Map<String,Value> map,
                                     MapTransform mt, String mimeType)
            throws IOException, ParameterException {
        try {
            SVGIcon icon = new SVGIcon();
            icon.setSvgURI(new URI(url.getValue(map)));
            BufferedImage img;

            if (viewBox != null && mt != null && viewBox.usable()) {
                if (map == null && !viewBox.dependsOnFeature().isEmpty()) {
                    throw new ParameterException("View box depends on feature"); // TODO I18n
                }

                double width = icon.getIconWidth();
                double height = icon.getIconHeight();

                Point2D dim = viewBox.getDimensionInPixel(map, height, width, mt.getScaleDenominator(), mt.getDpi());

                double widthDst = dim.getX();
                double heightDst = dim.getY();

                if (widthDst > 0 && heightDst > 0) {
                    img = new BufferedImage((int) (widthDst + 0.5), (int) (heightDst + 0.5), BufferedImage.TYPE_4BYTE_ABGR);
                    icon.setPreferredSize(new Dimension((int) (widthDst + 0.5), (int) (heightDst + 0.5)));
                    icon.setScaleToFit(true);
                } else {
                    img = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
                }
            } else {
                img = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
            }
            icon.setAntiAlias(true);
            Graphics2D g2 = (Graphics2D) img.getGraphics();
            if (mt != null) {
                g2.addRenderingHints(mt.getRenderingHints());
            }
            icon.paintIcon((Component) null, g2, 0, 0);
            return img;
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }


    /**
     * @deprecated
     * @param viewBox
     * @param sds
     * @param fid
     * @param mt
     * @param mimeType
     * @return
     * @throws IOException
     * @throws ParameterException
     */
    public PlanarImage getJAIImage(ViewBox viewBox,
                                   Map<String,Value> map,
                                   MapTransform mt, String mimeType)
            throws IOException, ParameterException {

        try {
            if (rawImage == null) {
                rawImage = JAI.create("url", url);
                Logger.getLogger(VariableOnlineResource.class.getName()).log(Level.INFO, "Download ExternalGraphic from: {0}", url);
            }
        } catch (Exception ex) {
            throw new IOException(ex);
        }

        PlanarImage img = rawImage;


        if (viewBox != null && mt != null && viewBox.usable()) {
            if (map == null && !viewBox.dependsOnFeature().isEmpty()) {
                throw new ParameterException("View box depends on feature"); // TODO I18n
            }

            try {
                double width = img.getWidth();
                double height = img.getHeight();

                Point2D dim = viewBox.getDimensionInPixel(map, height, width, mt.getScaleDenominator(), mt.getDpi());

                double widthDst = dim.getX();
                double heightDst = dim.getY();

                if (widthDst > 0 && heightDst > 0) {
                    double ratioX = widthDst / width;
                    double ratioY = heightDst / height;

                    if (ratioX > 1.0 || ratioY > 1.0) {
                        return JAI.create("scale", rawImage,
                                          (float) ratioX, (float) ratioY,
                                          0.0f, 0.0f,
                                          InterpolationBicubic2.getInstance(InterpolationBicubic2.INTERP_BICUBIC_2),
                                          mt.getRenderingHints());
                    } else {
                        //return JAI.create("SubsampleAverage", pb, mt.getRenderingHints());
                        return JAI.create("SubsampleAverage", img, ratioX, ratioY, mt.getRenderingHints());
                    }
                } else {
                    return img;
                }

            } catch (Exception ex) {
                throw new ParameterException(ex);
            }

        } else {
            return img;
        }
    }


    @Override
    public void setJAXBSource(ExternalGraphicType e) {
        VariableOnlineResourceType o = new VariableOnlineResourceType();

        o.setHref(url.getJAXBParameterValueType());
        e.setOnlineResource(o);
    }


    public Font getFont(Map<String,Value> map) {
        InputStream iStream;
        try {
            URL u = new URL(this.url.getValue(map));
            iStream = u.openStream();
            return Font.createFont(Font.TRUETYPE_FONT, iStream);
        } catch (FontFormatException ex) {
        } catch (ParameterException ex) {
        } catch (IOException ex) {
        }
        return null;
    }


    private Shape getTrueTypeGlyph(ViewBox viewBox,
                                   Map<String,Value> map,
                                   Double scale, Double dpi,
                                   RealParameter markIndex) throws ParameterException, IOException {

        try {
            URL u = new URL(this.url.getValue(map));
            InputStream iStream = u.openStream();
            Font font = Font.createFont(Font.TRUETYPE_FONT, iStream);
            iStream.close();

            double value = markIndex.getValue(map);

            char[] data = {(char) value};

            String text = String.copyValueOf(data);

            // Scale is used to have an high resolution
            AffineTransform at = AffineTransform.getTranslateInstance(0, 0);

            FontRenderContext fontCtx = new FontRenderContext(at, true, true);
            TextLayout tl = new TextLayout(text, font, fontCtx);

            Shape glyphOutline = tl.getOutline(at);

            Rectangle2D bounds2D = glyphOutline.getBounds2D();

            double width = bounds2D.getWidth();
            double height = bounds2D.getHeight();

            if (viewBox != null && viewBox.usable()) {
                Point2D dim = viewBox.getDimensionInPixel(map, height, width, scale, dpi);
                if (Math.abs(dim.getX()) <= 0 || Math.abs(dim.getY()) <= 0) {
                    return null;
                }

                at = AffineTransform.getScaleInstance(dim.getX() / width,
                                                      dim.getY() / height);

                fontCtx = new FontRenderContext(at, true, true);
                tl = new TextLayout(text, font, fontCtx);
                glyphOutline = tl.getOutline(at);

            }
            Rectangle2D gb = glyphOutline.getBounds2D();
            at = AffineTransform.getTranslateInstance(-gb.getCenterX(), -gb.getCenterY());

            return at.createTransformedShape(glyphOutline);

        } catch (FontFormatException ex) {
            Logger.getLogger(VariableOnlineResource.class.getName()).log(Level.SEVERE, null, ex);
            throw new ParameterException(ex);
        }
    }


    @Override
    public Shape getShape(ViewBox viewBox, Map<String,Value> map, Double scale, 
            Double dpi, RealParameter markIndex, String mimeType) throws ParameterException, IOException {

        if (mimeType != null) {
            if (mimeType.equalsIgnoreCase("application/x-font-ttf")) {
                return getTrueTypeGlyph(viewBox, map, scale, dpi, markIndex);
            }
        }

        throw new ParameterException("Unknown MIME type: " + mimeType);
    }

    //@Override

    public void setJAXBSource(MarkGraphicType m) {
        VariableOnlineResourceType o = new VariableOnlineResourceType();

        o.setHref(url.getJAXBParameterValueType());

        m.setOnlineResource(o);

    }


    @Override
    public double getDefaultMaxWidth(Map<String,Value> map,
                                     Double scale, Double dpi,
                                     RealParameter markIndex, String mimeType)
            throws IOException, ParameterException {

        if (mimeType != null) {
            if (mimeType.equalsIgnoreCase("application/x-font-ttf")) {
                return getTrueTypeGlyphMaxSize(map, /*scale, dpi,*/ markIndex);
            }
        }

        return 0.0;

    }


    private double getTrueTypeGlyphMaxSize(Map<String,Value> map,
                                           /*Double scale, Double dpi,*/ RealParameter markIndex)
            throws IOException, ParameterException {
        try {
            URL u = new URL(url.getValue(map));
            InputStream iStream = u.openStream();
            Font font = Font.createFont(Font.TRUETYPE_FONT, iStream);
            iStream.close();

            double value = markIndex.getValue(map);
            char[] data = {(char) value};

            String text = String.copyValueOf(data);

            // Scale is used to have an high resolution
            AffineTransform at = AffineTransform.getTranslateInstance(0, 0);

            FontRenderContext fontCtx = new FontRenderContext(at, true, true);
            TextLayout tl = new TextLayout(text, font, fontCtx);

            Shape glyphOutline = tl.getOutline(at);

            Rectangle2D bounds2D = glyphOutline.getBounds2D();

            return Math.max(bounds2D.getWidth(), bounds2D.getHeight());

        } catch (FontFormatException ex) {
            Logger.getLogger(VariableOnlineResource.class.getName()).log(Level.SEVERE, null, ex);
            throw new ParameterException(ex);
        }
    }


}
