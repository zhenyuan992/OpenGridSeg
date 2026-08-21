package org.opengridseg.ui;

import ij.ImagePlus;
import java.awt.BorderLayout;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

final class FluorescencePreviewPanel extends JPanel {
    private final ImageIcon icon;
    FluorescencePreviewPanel(ImagePlus preview){
        super(new BorderLayout(4,4));
        int width=preview.getWidth()*4,height=preview.getHeight()*4;
        Image scaled=preview.getBufferedImage().getScaledInstance(width,height,Image.SCALE_REPLICATE);
        icon=new ImageIcon(scaled);
        add(new JLabel(preview.getTitle(),SwingConstants.CENTER),BorderLayout.NORTH);
        add(new JLabel(icon),BorderLayout.CENTER);
    }
    int previewWidth(){return icon.getIconWidth();}
    int previewHeight(){return icon.getIconHeight();}
}
