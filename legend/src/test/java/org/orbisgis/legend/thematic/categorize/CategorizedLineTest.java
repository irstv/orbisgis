package org.orbisgis.legend.thematic.categorize;

import org.junit.Test;
import org.orbisgis.core.renderer.se.LineSymbolizer;
import org.orbisgis.core.renderer.se.Style;
import org.orbisgis.legend.AnalyzerTest;
import org.orbisgis.legend.thematic.LineParameters;

import java.awt.*;
import java.util.SortedSet;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Alexis Guéganno
 */
public class CategorizedLineTest extends AnalyzerTest{

    @Test
    public void testInstanciation() throws Exception{
        CategorizedLine cl = new CategorizedLine(getLineSymbolizer());
        assertTrue(true);
    }

    @Test
    public void testImpossibleInstanciation() throws Exception {
        Style s = getStyle(DASH_RECODE);
        LineSymbolizer ls = (LineSymbolizer) s.getRules().get(0).getCompositeSymbolizer().getChildren().get(0);
        try{
            CategorizedLine cl = new CategorizedLine(ls);
            fail();
        } catch (IllegalArgumentException uoe){
            assertTrue(true);
        }

    }

    @Test
    public void testGet() throws Exception {
        CategorizedLine cl = getCategorizedLine();
        assertTrue(cl.get(Double.NEGATIVE_INFINITY).equals(new LineParameters(Color.decode("#113355"),.75,.5,"2 2")));
        assertTrue(cl.get(70000.0 ).equals(new LineParameters(Color.decode("#dd66ee"),.75,1.0 ,"2 2")));
        assertTrue(cl.get(80000.0 ).equals(new LineParameters(Color.decode("#dd66ee"),.75,1.25,"2 2")));
        assertTrue(cl.get(100000.0).equals(new LineParameters(Color.decode("#ffaa99"),.75,1.5 ,"2 2")));
    }

    @Test
    public void testKeySet() throws Exception {
        CategorizedLine cl = getCategorizedLine();
        SortedSet<Double> doubles = cl.keySet();
        assertTrue(doubles.contains(Double.NEGATIVE_INFINITY));
        assertTrue(doubles.contains(70000.0));
        assertTrue(doubles.contains(80000.0));
        assertTrue(doubles.contains(100000.0));
    }

    @Test
    public void testContainsValue() throws Exception {
        CategorizedLine cl = getCategorizedLine();
        assertTrue(cl.containsValue(new LineParameters(Color.decode("#113355"),.75,  .5,"2 2")));
        assertTrue(cl.containsValue(new LineParameters(Color.decode("#dd66ee"),.75, 1.0,"2 2")));
        assertTrue(cl.containsValue(new LineParameters(Color.decode("#dd66ee"),.75,1.25,"2 2")));
        assertTrue(cl.containsValue(new LineParameters(Color.decode("#ffaa99"),.75, 1.5,"2 2")));
    }

    @Test
    public void testContainsKey() throws Exception {
        CategorizedLine cl = getCategorizedLine();
        assertTrue(cl.containsKey(Double.NEGATIVE_INFINITY));
        assertTrue(cl.containsKey( 70000.0));
        assertTrue(cl.containsKey( 80000.0));
        assertTrue(cl.containsKey(100000.0));
    }

    private LineSymbolizer getLineSymbolizer() throws Exception {
        Style s = getStyle(CATEGORIZED_LINE);
        return (LineSymbolizer) s.getRules().get(0).getCompositeSymbolizer().getChildren().get(0);
    }

    private CategorizedLine getCategorizedLine() throws Exception {
        return new CategorizedLine(getLineSymbolizer());
    }
}
