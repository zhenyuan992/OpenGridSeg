package org.opengridseg.ui;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JComponent;

public final class Tooltips {
    private Tooltips() {}

    public static void apply(String text,JComponent... roots) {
        for (JComponent root : roots) applyRecursively(text,root);
    }

    private static void applyRecursively(String text,JComponent component) {
        component.setToolTipText(text);
        if (!(component instanceof Container)) return;
        for (Component child : ((Container)component).getComponents()) {
            if (child instanceof JComponent) applyRecursively(text,(JComponent)child);
        }
    }
}
