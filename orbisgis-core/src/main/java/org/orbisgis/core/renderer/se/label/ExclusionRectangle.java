/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.orbisgis.core.renderer.se.label;

import java.util.HashSet;
import javax.xml.bind.JAXBElement;
import net.opengis.se._2_0.core.ExclusionRectangleType;
import net.opengis.se._2_0.core.ObjectFactory;
import org.orbisgis.core.renderer.se.SeExceptions.InvalidStyle;
import org.orbisgis.core.renderer.se.common.Uom;
import org.orbisgis.core.renderer.se.parameter.SeParameterFactory;
import org.orbisgis.core.renderer.se.parameter.UsedAnalysis;
import org.orbisgis.core.renderer.se.parameter.real.RealLiteral;
import org.orbisgis.core.renderer.se.parameter.real.RealParameter;
import org.orbisgis.core.renderer.se.parameter.real.RealParameterContext;

/**
 * An {@code ExclusionZone} where the forbidden area is defined as a rectangle. It is 
 * defined thanks to a x and y values. Their meaning is of course dependant of the inner
 * UOM instance.
 * @author alexis, maxence
 */
public final class ExclusionRectangle extends ExclusionZone {

    private RealParameter x;
    private RealParameter y;

    /**
     * Build a {@code ExclusionZone} with default width and length set to 3. 
     */
    public ExclusionRectangle(){
        this.setX(new RealLiteral(3));
        this.setY(new RealLiteral(3));
    }

    /**
     * Build a {@code ExclusionZone} from the JAXBElement given in argument. 
     */
    ExclusionRectangle(JAXBElement<ExclusionRectangleType> ert) throws InvalidStyle {
        ExclusionRectangleType e = ert.getValue();

        if (e.getX() != null){
            setX(SeParameterFactory.createRealParameter(e.getX()));
        }

        if (e.getY() != null){
            setY(SeParameterFactory.createRealParameter(e.getY()));
        }

        if (e.getUom() != null){
            setUom(Uom.fromOgcURN(e.getUom()));
        }
    }

    /**
     * Get the x-length of the rectangle.
     * @return 
     * the x-length as a {@code RealParameter} 
     */
    public RealParameter getX() {
        return x;
    }

    /**
     * Set the x-length of the rectangle.
     * @param x 
     */
    public void setX(RealParameter x) {
        this.x = x;
		if (x != null){
			x.setContext(RealParameterContext.NON_NEGATIVE_CONTEXT);
		}
    }

    /**
     * Get the y-length of the rectangle.
     * @return 
     * the y-length as a {@code RealParameter} 
     */
    public RealParameter getY() {
        return y;
    }

    /**
     * Set the y-length of the rectangle.
     * @param x 
     */
    public void setY(RealParameter y) {
        this.y = y;
		if (this.y != null){
			y.setContext(RealParameterContext.NON_NEGATIVE_CONTEXT);
		}
    }

    @Override
    public JAXBElement<ExclusionRectangleType> getJAXBElement() {
        ExclusionRectangleType r = new ExclusionRectangleType();

        if (getUom() != null) {
            r.setUom(getUom().toString());
        }

        if (x != null) {
            r.setX(x.getJAXBParameterValueType());
        }

        if (y != null) {
            r.setY(y.getJAXBParameterValueType());
        }

        ObjectFactory of = new ObjectFactory();

        return of.createExclusionRectangle(r);
    }

	@Override
	public HashSet<String> dependsOnFeature() {
            HashSet<String> result = null;
            if (x != null) {
                result = x.dependsOnFeature();
            }
            if (y != null) {
                if(result == null){
                    result = y.dependsOnFeature();
                } else {
                    result.addAll(y.dependsOnFeature());
                }
            }
            return result;
	}

    @Override
    public UsedAnalysis getUsedAnalysis(){
        UsedAnalysis ua = new UsedAnalysis();
        ua.include(x);
        ua.include(y);
        return ua;
    }

}
