package org.opengridseg.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Collections;
import org.opengridseg.export.ExportBar;
import org.junit.jupiter.api.Test;

final class FullFovSelectionPanelTest {
    @Test void clickingABarTogglesItsSelection(){
        FovDisplayImage display=new FovDisplayImage(new BufferedImage(100,100,BufferedImage.TYPE_INT_RGB),1000,1000);
        ExportBar bar=new ExportBar("bar",0,0,0,"observed",0,0,500,500,false);boolean[] selected={true};
        FullFovSelectionPanel panel=new FullFovSelectionPanel(display,Collections.singletonList(bar),i->selected[i],i->selected[i]=!selected[i]);
        panel.dispatchEvent(new MouseEvent(panel,MouseEvent.MOUSE_CLICKED,0,0,50,50,1,false));
        assertFalse(selected[0]);
    }
    @Test void markersCanBeHiddenForVisualInspection(){
        FovDisplayImage display=new FovDisplayImage(new BufferedImage(100,100,BufferedImage.TYPE_INT_RGB),1000,1000);
        ExportBar bar=new ExportBar("bar",0,0,0,"observed",0,0,500,500,false);
        FullFovSelectionPanel panel=new FullFovSelectionPanel(display,Collections.singletonList(bar),i->true,i->{});panel.setSize(100,100);
        BufferedImage shown=paint(panel);assertEquals(128,(shown.getRGB(54,50)>>8)&255,2);panel.setMarkersVisible(false);BufferedImage hidden=paint(panel);
        assertNotEquals(shown.getRGB(54,50),hidden.getRGB(54,50));assertEquals(0,hidden.getRGB(54,50)&0xFFFFFF);
    }
    private static BufferedImage paint(FullFovSelectionPanel panel){BufferedImage image=new BufferedImage(100,100,BufferedImage.TYPE_INT_RGB);java.awt.Graphics2D graphics=image.createGraphics();panel.paint(graphics);graphics.dispose();return image;}
}
