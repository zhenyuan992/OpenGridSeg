package org.opengridseg.ui;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import org.opengridseg.image.FloatPlane;

final class FullFovDisplayRenderer {
    enum ChannelColor {GRAY,GREEN,MAGENTA}
    private FullFovDisplayRenderer(){}
    static FovDisplayImage render(FloatPlane source,int maxWidth,int maxHeight,ChannelColor color){
        double ratio=Math.min(1.0,Math.min(maxWidth/(double)source.width(),maxHeight/(double)source.height()));
        int width=Math.max(1,(int)Math.round(source.width()*ratio)),height=Math.max(1,(int)Math.round(source.height()*ratio));
        float[] samples=new float[width*height],finite=new float[width*height];int count=0;
        for(int y=0;y<height;y++)for(int x=0;x<width;x++){
            int sx=Math.min(source.width()-1,(int)Math.floor((x+.5)*source.width()/width));int sy=Math.min(source.height()-1,(int)Math.floor((y+.5)*source.height()/height));float value=source.get(sx,sy);samples[y*width+x]=value;if(Float.isFinite(value))finite[count++]=value;
        }
        Arrays.sort(finite,0,count);double lower=count==0?Double.NaN:finite[(int)Math.floor(.05*(count-1))],upper=count==0?Double.NaN:finite[(int)Math.ceil(.95*(count-1))];
        BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        for(int y=0;y<height;y++)for(int x=0;x<width;x++){int level=scale(samples[y*width+x],lower,upper);int rgb=color==ChannelColor.GRAY?(level<<16)|(level<<8)|level:color==ChannelColor.GREEN?level<<8:(level<<16)|level;image.setRGB(x,y,rgb);}
        return new FovDisplayImage(image,source.width(),source.height());
    }
    private static int scale(float value,double minimum,double maximum){if(!Float.isFinite(value))return 0;if(!(maximum>minimum))return 128;return (int)Math.round(255*Math.max(0,Math.min(1,(value-minimum)/(maximum-minimum))));}
}
