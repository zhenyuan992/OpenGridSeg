package org.opengridseg.detect;

import ij.plugin.filter.GaussianBlur;
import ij.process.FloatProcessor;
import java.util.*;
import org.opengridseg.image.FloatPlane;

public final class MatchedBarDetector {
    private MatchedBarDetector() {}

    public static List<Peak> detect(FloatPlane image, DetectionParameters p) {
        int w=image.width(), h=image.height(), n=w*h;
        float[] filledPixels=new float[n], maskPixels=new float[n];
        int[] invalidIntegral=new int[(w+1)*(h+1)];
        for(int y=0;y<h;y++) for(int x=0;x<w;x++) {
            int i=y*w+x; float v=image.pixels()[i];
            if(Float.isFinite(v)){filledPixels[i]=v;maskPixels[i]=1f;}
            int invalid=Float.isFinite(v)?0:1;
            invalidIntegral[(y+1)*(w+1)+x+1]=invalid+invalidIntegral[y*(w+1)+x+1]+invalidIntegral[(y+1)*(w+1)+x]-invalidIntegral[y*(w+1)+x];
        }
        FloatProcessor numerator=new FloatProcessor(w,h,filledPixels), weight=new FloatProcessor(w,h,maskPixels);
        double backgroundSigma=Math.max(2*p.barLength,20); GaussianBlur blur=new GaussianBlur();
        blur.blurGaussian(numerator,backgroundSigma,backgroundSigma,0.01); blur.blurGaussian(weight,backgroundSigma,backgroundSigma,0.01);
        FloatProcessor dark=new FloatProcessor(w,h);
        for(int i=0;i<n;i++) if(Float.isFinite(image.pixels()[i])) dark.setf(i,numerator.getf(i)/Math.max(weight.getf(i),1e-6f)-image.pixels()[i]);
        FloatProcessor inner=(FloatProcessor)dark.duplicate(), outer=(FloatProcessor)dark.duplicate();
        blur.blurGaussian(inner,p.barLength/3.0,p.barWidth/3.0,0.01);
        blur.blurGaussian(outer,p.barLength/1.3,p.barWidth*1.2,0.01);
        int halfX=(int)Math.ceil(1.2*p.barLength), halfY=(int)Math.ceil(2.5*p.barWidth);
        float[] response=new float[n], sample=new float[(n+15)/16]; int sn=0;
        for(int y=0;y<h;y++) for(int x=0;x<w;x++) {
            int i=y*w+x;
            if(x<halfX||x>=w-halfX||y<halfY||y>=h-halfY||invalidCount(invalidIntegral,w,x-halfX,y-halfY,x+halfX,y+halfY)>0) response[i]=Float.NaN;
            else response[i]=inner.getf(i)-outer.getf(i);
            if(i%16==0&&Float.isFinite(response[i])) sample[sn++]=response[i];
        }
        Arrays.sort(sample,0,sn); double median=median(sample,sn);
        for(int i=0;i<sn;i++) sample[i]=(float)Math.abs(sample[i]-median);
        Arrays.sort(sample,0,sn); double mad=median(sample,sn);
        double threshold=median+p.thresholdSigma*Math.max(1.4826*mad,1e-7);
        List<Peak> candidates=new ArrayList<>();
        for(int y=halfY;y<h-halfY;y++) for(int x=halfX;x<w-halfX;x++) {
            float v=response[y*w+x]; if(!Float.isFinite(v)||v<threshold) continue;
            boolean maximum=true;
            for(int dy=-1;dy<=1&&maximum;dy++) for(int dx=-1;dx<=1;dx++) if((dx!=0||dy!=0)&&response[(y+dy)*w+x+dx]>v){maximum=false;break;}
            if(maximum)candidates.add(new Peak(y,x,v));
        }
        candidates.sort(new Comparator<Peak>(){public int compare(Peak a,Peak b){return Double.compare(b.response(),a.response());}});
        List<Peak> kept=new ArrayList<>(); double min2=p.minPeakDistance*(double)p.minPeakDistance;
        for(Peak c:candidates){boolean far=true;for(Peak q:kept){double dx=c.x()-q.x(),dy=c.y()-q.y();if(dx*dx+dy*dy<min2){far=false;break;}}if(far)kept.add(c);}
        return kept;
    }

    private static int invalidCount(int[] integral,int width,int x0,int y0,int x1,int y1){int stride=width+1;int ax=x0,ay=y0,bx=x1+1,by=y1+1;return integral[by*stride+bx]-integral[ay*stride+bx]-integral[by*stride+ax]+integral[ay*stride+ax];}
    private static double median(float[] values,int n){if(n==0)return 0;return n%2==1?values[n/2]:0.5*(values[n/2-1]+values[n/2]);}
}
