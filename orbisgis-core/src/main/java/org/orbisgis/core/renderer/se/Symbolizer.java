package org.orbisgis.core.renderer.se;

import com.vividsolutions.jts.geom.Geometry;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBElement;
import net.opengis.se._2_0.core.*;
import net.opengis.se._2_0.raster.RasterSymbolizerType;
import org.gdms.data.DataSource;
import org.gdms.data.values.Value;
import org.gdms.driver.DriverException;
import org.orbisgis.core.map.MapTransform;
import org.orbisgis.core.renderer.RenderContext;
import org.orbisgis.core.renderer.se.SeExceptions.InvalidStyle;
import org.orbisgis.core.renderer.se.parameter.ParameterException;

/**
 * Entry point for all kind of symbolizer
 * This abstract class contains only the name, the way to retrieve the geometry
 * and a description of the symbolizer.
 * @todo Add a general draw method that fit well for vectors and raster; implement fetch default geometry
 * @author maxence, alexis
 */
public abstract class Symbolizer implements SymbolizerNode, Comparable {

    /**
     * The default name affected to a new Symbolizer instance.
     */
    public static final String DEFAULT_NAME = "Default Symbolizer";
    /**
     * The current version of the Symbolizer
     */
    public static final String VERSION = "2.0.0";
    protected String name;
    protected String desc;
    //protected GeometryAttribute the_geom;
    private SymbolizerNode parent;
    protected int level;
    private Set<String> features;
    private Map<String,Value> featuresMap;

    /**
     * Build an empty Symbolizer, with the default name and no description.
     */
    public Symbolizer() {
        name = Symbolizer.DEFAULT_NAME;
        desc = "";
        level = -1;
    }

    /**
     * Build a Symbolizer from a JAXB element. This constructor will only retrieve
     * the name and description - it's up to the inheriting classes to retrieve the other 
     * needed informations.
     * @param st
     * @throws org.orbisgis.core.renderer.se.SeExceptions.InvalidStyle 
     */
    public Symbolizer(JAXBElement<? extends SymbolizerType> st) throws InvalidStyle {
        SymbolizerType t = st.getValue();

        if (t.getName() != null) {
            this.name = t.getName();
        } else {
            this.name = Symbolizer.DEFAULT_NAME;
        }

        if (t.getVersion() != null && !t.getVersion().value().equals(Symbolizer.VERSION)) {
            throw new InvalidStyle("Unsupported version !");
        }

        if (t.getDescription() != null) {
            // TODO  implement ows:Description
        }

        if (t.getExtension() != null) {
            for (ExtensionParameterType param : t.getExtension().getExtensionParameter()) {
                if (param.getName().equalsIgnoreCase("level")) {
                    level = Integer.parseInt(param.getContent());
                    break;
                }
            }
        }
    }

    /**
     * Gets the name of this Symbolizer.
     * @return 
     *  the name of the Symbolizer.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Gets the name of this <code>Symbolizer</code>.
     * @return 
     *  the name of the <code>Symbolizer</code>.
     */
    public String getName() {
        return name;
    }

    /**
     * Set <code>name</code> as the name of this <code>Symbolizer</code>
     * @param name 
     */
    public void setName(String name) {
        if (name == null || name.equalsIgnoreCase("")) {
            this.name = Symbolizer.DEFAULT_NAME;
        } else {
            this.name = name;
        }
    }

    /**
     * Get the description associated to this <code>Symbolizer</code>.
     * @return 
     */
    public String getDescription() {
        return desc;
    }

    /**
     * Set the description associated to this <code>Symbolizer</code>.
     * @param description 
     */
    public void setDescription(String description) {
        desc = description;
    }

    /**
     * Get the display level of this <code>Symbolizer</code>
     * @return 
     */
    public int getLevel() {
        return level;
    }

    /**
     * Set the display level of this <code>Symbolizer</code>
     * @param level 
     */
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public SymbolizerNode getParent() {
        return parent;
    }

    @Override
    public void setParent(SymbolizerNode node) {
        this.parent = node;
    }

    /**
     * Fill the {@code SymbolizerType s} with the properties contained in this
     * {@code Symbolizer}.
     * @param s 
     */
    public void setJAXBProperty(SymbolizerType s) {
        ObjectFactory of = new ObjectFactory();

        // TODO Load description from XML
        s.setDescription(null);
        s.setName(name);
        s.setVersion(VersionType.VALUE_1);

        ExtensionType exts = of.createExtensionType();
        ExtensionParameterType param = of.createExtensionParameterType();
        param.setName("level");
        param.setContent("" + level);
        exts.getExtensionParameter().add(param);

        s.setExtension(exts);
    }
    
    /**
     * Using the given JAXBElement, this method tries to build the correct 
     * spacialization of {@code Symbolizer}.
     * @param st
     * @return
     * @throws org.orbisgis.core.renderer.se.SeExceptions.InvalidStyle 
     */
    public static Symbolizer createSymbolizerFromJAXBElement(JAXBElement<? extends SymbolizerType> st) throws InvalidStyle {
        if (st.getDeclaredType() == AreaSymbolizerType.class) {
            return new AreaSymbolizer((JAXBElement<AreaSymbolizerType>) st);
        } else if (st.getDeclaredType() == LineSymbolizerType.class) {
            return new LineSymbolizer((JAXBElement<LineSymbolizerType>) st);
        } else if (st.getDeclaredType() == PointSymbolizerType.class) {
            return new PointSymbolizer((JAXBElement<PointSymbolizerType>) st);
        } else if (st.getDeclaredType() == TextSymbolizerType.class) {
            return new TextSymbolizer((JAXBElement<TextSymbolizerType>) st);
        } else if (st.getDeclaredType() == RasterSymbolizerType.class) {
            return new RasterSymbolizer((JAXBElement<RasterSymbolizerType>) st);
        } else {
            return null;
        }
    }

    /**
     * Makes a comparison between this and o. Be aware that <b>this operation is absolutely
     * not concistent with <code>equals(Object o)</code> !!!</b>
     * @param o
     * @return 
     * <ul><li>-1 if <code>(!o instanceof Symbolizer) || o.level &lt; this.level </code></li>
     * <li>0 if <code>(o instanceof Symbolizer) &amp;&amp; s.level == this.level</code></li>
     * <li>1 otherwise</li>
     * </ul> 
     */
    @Override
    public int compareTo(Object o) {
        if (o instanceof Symbolizer) {
            Symbolizer s = (Symbolizer) o;

            if (s.level < this.level) {
                return 1;
            } else if (s.level == this.level) {
                return 0;
            } else {
                return -1;
            }
        }
        return -1;
    }

    /**
     * Go through parents and return the rule
     */
    public Rule getRule() {
        SymbolizerNode pIt = this.parent;
        while (!(pIt instanceof Rule)) {
            pIt = pIt.getParent();
        }

        return (Rule) pIt;
    }

    /**
     * Get the faetures that are needed to build this Symbolizer in a {@code
     * Map<String,Value>}. This method is based on {@link
     * SymbolizerNode#dependsOnFeature()}. Using the field names retrieved with
     * this method, we serach for {@code Values} at index {@code fid} in {@code
     * sds}.
     * @param sds
     * @param fid
     * @return
     * @throws DriverException
     */
    public Map<String,Value> getFeaturesMap(DataSource sds, long fid) throws DriverException{
        if(features==null){
            features = dependsOnFeature();
        }
        if(featuresMap == null){
            featuresMap = new HashMap<String,Value>();
        }
        for(String s : features){
            featuresMap.put(s, sds.getFieldValue(fid, sds.getFieldIndexByName(s)));
        }
        return featuresMap;
    }

    /**
     * Draw the symbols in g2, using infos that are found in sds at index fid.
     * @param g2
     * @param sds
     * @param fid
     * @param selected
     * @param mt
     * @param theGeom
     * @param perm
     * @throws ParameterException
     * @throws IOException
     * @throws DriverException 
     */
    public abstract void draw(Graphics2D g2, DataSource sds, long fid,
            boolean selected, MapTransform mt, Geometry theGeom, RenderContext perm)
            throws ParameterException, IOException, DriverException;

    /**
     * Get a JAXB representation of this Symbolizer.
     * @return 
     */
    public abstract JAXBElement<? extends SymbolizerType> getJAXBElement();
}
