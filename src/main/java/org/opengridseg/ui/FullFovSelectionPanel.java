package org.opengridseg.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import javax.swing.JPanel;
import org.opengridseg.export.ExportBar;

final class FullFovSelectionPanel extends JPanel {
    private final FovDisplayImage display;private final List<ExportBar> bars;private final IntPredicate selected;private final IntConsumer toggle;private boolean markersVisible=true;
    FullFovSelectionPanel(FovDisplayImage display,List<ExportBar> bars,IntPredicate selected,IntConsumer toggle){
        this.display=display;this.bars=bars;this.selected=selected;this.toggle=toggle;setPreferredSize(new Dimension(display.image().getWidth(),display.image().getHeight()));
        setToolTipText("Green = kept; red = rejected. Click a marker to toggle it.");
        addMouseListener(new MouseAdapter(){public void mouseClicked(MouseEvent event){if(!markersVisible)return;int index=FullFovHitTest.nearest(bars,display,event.getX(),event.getY(),7);if(index>=0){toggle.accept(index);repaint();}}});
    }
    void setMarkersVisible(boolean visible){markersVisible=visible;repaint();}
    protected void paintComponent(Graphics graphics){super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();g.drawImage(display.image(),0,0,null);if(!markersVisible){g.dispose();return;}g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,.5f));g.setStroke(new BasicStroke(2f));for(int index=0;index<bars.size();index++){ExportBar bar=bars.get(index);int x=display.displayX(bar.rotatedX()),y=display.displayY(bar.rotatedY());g.setColor(selected.test(index)?new Color(0,255,80):new Color(255,35,35));g.drawOval(x-4,y-4,8,8);if(!selected.test(index)){g.drawLine(x-3,y-3,x+3,y+3);g.drawLine(x-3,y+3,x+3,y-3);}}g.dispose();}
}
