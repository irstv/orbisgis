package org.orbisgis.view.toc.actions.cui.legends.panels;

import net.miginfocom.swing.MigLayout;
import org.orbisgis.sif.ComponentUtil;
import org.orbisgis.sif.UIFactory;
import org.orbisgis.sif.components.ColorPicker;
import org.orbisgis.view.toc.actions.cui.legends.PnlUniqueSymbolSE;
import org.orbisgis.sif.components.WideComboBox;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.EventHandler;
import java.util.*;
import java.util.List;

/**
 * A panel with two filled labels whose colour can be changed. It is then possible to retrieve the colour
 * of both.
 * @author Alexis Guéganno
 */
public class ColorConfigurationPanel extends JPanel {
    private static final I18n I18N = I18nFactory.getI18n(ColorConfigurationPanel.class);
    private JComboBox pal;
    private JPanel grad;
    private JLabel endCol;
    private JLabel startCol;
    private JRadioButton bGrad;
    private JRadioButton bPal;
    private JComboBox schemes;
    private List<String> names;

    /**
     * Builds a panel with two filled labels whose colour can be changed. It is then possible to retrieve the colour
     * of both.
     * @param names the list of names that shall be used to build the combo box of palettes. They will be
     *              used to retrieve the associated ColorScheme instances. If null is given, the concatenation
     *              of the range and then discrete ColorScheme names will be built.
     */
    public ColorConfigurationPanel(List<String> names){
        super();
        JPanel intOne = new JPanel(new MigLayout("wrap 2", "[align l][align c]"));
        if(names == null){
            this.names = new ArrayList<String>(ColorScheme.rangeColorSchemeNames());
            this.names.addAll(ColorScheme.discreteColorSchemeNames());
        } else {
            this.names = new ArrayList<String>(names);
        }
        grad = getGradientPanel();
        pal = getPalettesPanel();
        initButtons();
        intOne.add(bGrad);
        intOne.add(grad);
        intOne.add(bPal);
        intOne.add(pal, "width 115!");
        this.add(intOne);
    }

    /**
     * Get a JLabel of dimensions {@link PnlUniqueSymbolSE#FILLED_LABEL_WIDTH} and {@link PnlUniqueSymbolSE#FILLED_LABEL_HEIGHT}
     * opaque and with a background of Color {@code c}.
     * @param c The background color of the label we want.
     * @return the label with c as a background colour.
     */
    public JLabel getFilledLabel(Color c){
        JLabel lblFill = new JLabel();
        lblFill.setBackground(c);
        lblFill.setBorder(BorderFactory.createLineBorder(Color.black));
        lblFill.setPreferredSize(new Dimension(PnlUniqueSymbolSE.FILLED_LABEL_HEIGHT, PnlUniqueSymbolSE.FILLED_LABEL_HEIGHT));
        lblFill.setMaximumSize(new Dimension(PnlUniqueSymbolSE.FILLED_LABEL_HEIGHT, PnlUniqueSymbolSE.FILLED_LABEL_HEIGHT));
        lblFill.setOpaque(true);
        MouseListener ma = EventHandler.create(MouseListener.class, this, "chooseFillColor", "", "mouseClicked");
        lblFill.addMouseListener(ma);
        return lblFill;
    }

    /**
     * Builds and return the panel used for the simple gradient computation. It contains
     * two labels whose color can be changed to define the start and end colour of the
     * gradient, and two other labels describing them.
     * @return The labels in a JPanel.
     */
    private JPanel getGradientPanel(){
        JPanel gp = new JPanel(new MigLayout());
        gp.add(new JLabel(I18N.tr("Gradient ")));
        startCol = getFilledLabel(Color.BLUE);
        gp.add(startCol);
        gp.add(new JLabel(I18N.tr(" to ")));
        endCol = getFilledLabel(Color.RED);
        gp.add(endCol);
        return gp;
    }

    /**
     * Gets the panel containing the palette configuration.
     * @return The JPanel that contains the combo where we put the palettes.
     */
    private JComboBox getPalettesPanel(){
        schemes = new WideComboBox(names.toArray(new String[names.size()]));
        schemes.setRenderer(new ColorSchemeListCellRenderer(new JList()));
        return schemes;
    }

    /**
     * Initializes the buttons used to configure how coloured classification must be generated.
     */
    private void  initButtons(){
        bGrad = new JRadioButton("");
        bPal = new JRadioButton("");
        bGrad.addActionListener(
                EventHandler.create(ActionListener.class, this, "onClickGrad"));
        bPal.addActionListener(
                EventHandler.create(ActionListener.class, this, "onClickPal"));
        bPal.setSelected(true);
        ButtonGroup bg = new ButtonGroup();
        bg.add(bGrad);
        bg.add(bPal);
        onClickPal();
    }

    /**
     * The user clicked on the gradient radio button. Used by EventHandler.
     */
    public void onClickGrad(){
        ComponentUtil.setFieldState(true, this.grad);
        ComponentUtil.setFieldState(false, this.pal);
    }

    /**
     * The user clicked on the palette button. Used by EventHandler.
     */
    public void onClickPal(){
        ComponentUtil.setFieldState(false,this.grad);
        ComponentUtil.setFieldState(true,this.pal);
    }

    /**
     * Retrieve the current ColorScheme.
     * @return The ColorScheme.
     */
    public ColorScheme getColorScheme(){
        if(bGrad.isSelected()){
            List<Color> cols = new ArrayList<Color>();
            cols.add(getStartColor());
            cols.add(getEndCol());
            ColorScheme ret = new ColorScheme("grad",cols);
            return ret;
        } else {
            String name = (String)schemes.getSelectedItem();
            return ColorScheme.create(name);
        }
    }

    /**
     * Gets the start Color for the classification generation
     * @return The start Color
     */
    public Color getStartColor(){
        return startCol.getBackground();
    }

    /**
     * Gets the end Color for the classification generation
     * @return The end Color
     */
    public Color getEndCol() {
        return endCol.getBackground();
    }

    /**
     * This method will let the user choose a color that will be set as the
     * background of the source of the event.
     * @param e The input MouseEvent
     */
    public void chooseFillColor(MouseEvent e) {
        Component source = (Component)e.getSource();
        if(source.isEnabled()){
            JLabel lab = (JLabel) source;
            ColorPicker picker = new ColorPicker(lab.getBackground());
            if (UIFactory.showDialog(picker, false, true)) {
                Color color = picker.getColor();
                source.setBackground(color);
            }
        }
    }
}
