package org.opengridseg.fluorescence;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import org.opengridseg.export.ExportBar;
import org.opengridseg.image.FloatPlane;
import org.junit.jupiter.api.Test;

class FluorescencePreviewRendererTest {
    @Test void showsBfGfpAndMCherryUsingSourceRanges() {
        FloatPlane bf=new FloatPlane(20,10);Arrays.fill(bf.pixels(),250f);
        FloatPlane gfp=new FloatPlane(20,10);Arrays.fill(gfp.pixels(),100f);
        FloatPlane cherry=new FloatPlane(20,10);Arrays.fill(cherry.pixels(),500f);
        BarFluorescenceReview review=review(gfp,cherry);review.setBfPreview(bf);
        ij.ImagePlus image=FluorescencePreviewRenderer.create(review,0,1000,0,1000,0,1000,false);
        assertEquals(60,image.getWidth());
        int left=image.getProcessor().getPixel(10,5),middle=image.getProcessor().getPixel(30,5),right=image.getProcessor().getPixel(50,5);
        assertEquals(0x404040,left,0x010101);
        assertEquals(0x001A00,middle,0x000100);
        assertEquals(0x800080,right,0x010001);
    }

    @Test void autoScalesEachPreviewChannelSeparately() {
        FloatPlane bf=twoValuePlane(10,20),gfp=twoValuePlane(100,200),cherry=twoValuePlane(1000,2000);
        BarFluorescenceReview review=review(gfp,cherry);review.setBfPreview(bf);
        ij.ImagePlus image=FluorescencePreviewRenderer.create(review,0,10000,0,10000,0,10000,true);
        assertEquals(0,image.getProcessor().getPixel(0,0));
        assertEquals(0xFFFFFF,image.getProcessor().getPixel(1,0));
        assertEquals(0,image.getProcessor().getPixel(20,0));
        assertEquals(0x00FF00,image.getProcessor().getPixel(21,0));
        assertEquals(0,image.getProcessor().getPixel(40,0));
        assertEquals(0xFF00FF,image.getProcessor().getPixel(41,0));
    }

    private static FloatPlane twoValuePlane(float low,float high){FloatPlane plane=new FloatPlane(20,10);Arrays.fill(plane.pixels(),low);plane.set(1,0,high);return plane;}
    private static BarFluorescenceReview review(FloatPlane gfp,FloatPlane cherry){ExportBar bar=new ExportBar("bar_0001",0,0,0,"observed",1,1,1,1,false);return new BarFluorescenceReview(bar,new FluorescenceBarScore(30,0,2,false,gfp),new FluorescenceBarScore(30,8,9,true,cherry));}
}
