package org.opengridseg.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import org.opengridseg.export.ExportBar;
import org.junit.jupiter.api.Test;

final class FullFovHitTestTest {
    private final FovDisplayImage display=new FovDisplayImage(new BufferedImage(400,200,BufferedImage.TYPE_INT_RGB),4000,2000);
    private final java.util.List<ExportBar> bars=Arrays.asList(
        new ExportBar("a",0,0,0,"observed",0,0,100,200,false),
        new ExportBar("b",0,0,1,"observed",0,0,1000,1000,false));
    @Test void findsTheNearestBarMarker(){assertEquals(0,FullFovHitTest.nearest(bars,display,11,20,8));}
    @Test void ignoresClicksAwayFromEveryMarker(){assertEquals(-1,FullFovHitTest.nearest(bars,display,300,150,8));}
}
