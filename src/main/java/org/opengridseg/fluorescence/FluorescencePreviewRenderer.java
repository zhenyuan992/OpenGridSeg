package org.opengridseg.fluorescence;

import ij.ImagePlus;
import ij.process.ColorProcessor;
import org.opengridseg.image.FloatPlane;

public final class FluorescencePreviewRenderer {
    private FluorescencePreviewRenderer(){}

    public static ImagePlus create(BarFluorescenceReview review,
            double bfMin,double bfMax,double gfpMin,double gfpMax,double mCherryMin,double mCherryMax,
            boolean autoScale){
        FloatPlane bf=review.bfPreview(),gfp=review.gfp().maxProjection(),cherry=review.mCherry().maxProjection();
        if(bf==null)throw new IllegalArgumentException("BF preview is missing");
        if(bf.width()!=gfp.width()||bf.height()!=gfp.height()||gfp.width()!=cherry.width()||gfp.height()!=cherry.height())
            throw new IllegalArgumentException("BF, GFP, and mCherry preview dimensions differ");
        if(autoScale){double[] range=range(bf);bfMin=range[0];bfMax=range[1];range=range(gfp);gfpMin=range[0];gfpMax=range[1];range=range(cherry);mCherryMin=range[0];mCherryMax=range[1];}
        int width=bf.width(),height=bf.height();ColorProcessor pixels=new ColorProcessor(width*3,height);
        for(int y=0;y<height;y++)for(int x=0;x<width;x++){
            int gray=scale(bf.get(x,y),bfMin,bfMax),green=scale(gfp.get(x,y),gfpMin,gfpMax),magenta=scale(cherry.get(x,y),mCherryMin,mCherryMax);
            pixels.set(x,y,(gray<<16)|(gray<<8)|gray);pixels.set(x+width,y,green<<8);pixels.set(x+2*width,y,(magenta<<16)|magenta);
        }
        String mode=autoScale?"per-channel preview scaling":"source-range preview";
        String title=review.bar().id()+" — BF left, GFP middle (score "+review.gfp().criteriaPassed()+"/3), mCherry right (score "+review.mCherry().criteriaPassed()+"/3) — "+mode;
        return new ImagePlus(title,pixels);
    }

    private static double[] range(FloatPlane plane){double minimum=Double.POSITIVE_INFINITY,maximum=Double.NEGATIVE_INFINITY;for(float value:plane.pixels())if(Float.isFinite(value)){minimum=Math.min(minimum,value);maximum=Math.max(maximum,value);}return new double[]{minimum,maximum};}
    private static int scale(float value,double minimum,double maximum){if(!Float.isFinite(value)||!(maximum>minimum))return 0;double unit=(value-minimum)/(maximum-minimum);return (int)Math.round(255*Math.max(0,Math.min(1,unit)));}
}
