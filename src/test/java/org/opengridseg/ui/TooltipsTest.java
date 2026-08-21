package org.opengridseg.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import org.junit.jupiter.api.Test;

class TooltipsTest {
    @Test void appliesHelpToLabelControlIconAndNestedControl() {
        JLabel label=new JLabel("Pitch X / Y");
        JPanel pair=new JPanel();
        JSpinner spinner=new JSpinner();
        pair.add(spinner);
        JLabel icon=new JLabel("ⓘ");

        Tooltips.apply("Independent X and Y pitch",label,pair,icon);

        assertEquals("Independent X and Y pitch",label.getToolTipText());
        assertEquals("Independent X and Y pitch",pair.getToolTipText());
        assertEquals("Independent X and Y pitch",spinner.getToolTipText());
        assertEquals("Independent X and Y pitch",icon.getToolTipText());
    }
}
