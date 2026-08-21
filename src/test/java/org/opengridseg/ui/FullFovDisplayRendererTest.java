package org.opengridseg.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import org.opengridseg.image.FloatPlane;
import org.junit.jupiter.api.Test;

final class FullFovDisplayRendererTest {
    @Test void rendersTheWholeGfpFovAtAViewableSize(){
        FloatPlane plane=new FloatPlane(4,2);Arrays.fill(plane.pixels(),0f);for(int y=0;y<2;y++)for(int x=2;x<4;x++)plane.set(x,y,100f);
        FovDisplayImage display=FullFovDisplayRenderer.render(plane,2,2,FullFovDisplayRenderer.ChannelColor.GREEN);
        assertEquals(2,display.image().getWidth());assertEquals(1,display.image().getHeight());
        assertEquals(0,display.image().getRGB(0,0)&0xFFFFFF);assertEquals(0x00FF00,display.image().getRGB(1,0)&0xFFFFFF);
        assertEquals(4,display.sourceWidth());assertEquals(2,display.sourceHeight());
    }
    @Test void ignoresDeadPixelsByScalingFromFifthToNinetyFifthPercentile(){
        FloatPlane plane=new FloatPlane(100,1);for(int x=0;x<49;x++)plane.set(x,0,10f);for(int x=49;x<98;x++)plane.set(x,0,90f);plane.set(98,0,-10000f);plane.set(99,0,10000f);
        FovDisplayImage display=FullFovDisplayRenderer.render(plane,100,1,FullFovDisplayRenderer.ChannelColor.GRAY);
        assertEquals(0,display.image().getRGB(0,0)&0xFFFFFF);assertEquals(0xFFFFFF,display.image().getRGB(50,0)&0xFFFFFF);
    }
}
