package org.opengridseg.fluorescence;

import java.util.Arrays;
import org.opengridseg.image.FloatPlane;

public final class FluorescenceNoiseScorer {
    private FluorescenceNoiseScorer() {}
    public static FluorescencePlaneScore score(FloatPlane input,FluorescenceParameters p){
        int w=input.width(),h=input.height();float[] smooth=gaussian(input.pixels(),w,h,p.gaussianSigma);
        int x0=(int)Math.round(w*p.signalMarginXFraction),x1=w-x0,y0=(int)Math.round(h*p.signalMarginYFraction),y1=h-y0;
        if(x1-x0<4||y1-y0<4)throw new IllegalArgumentException("Fluorescence crop is too small for the signal/background mask");
        int edge=Math.max(1,(int)Math.ceil(3*p.gaussianSigma));double[] background=new double[w*h];int n=0;
        for(int y=edge;y<h-edge;y++)for(int x=edge;x<w-edge;x++)if(!(x>=x0&&x<x1&&y>=y0&&y<y1)){float v=smooth[y*w+x];if(Float.isFinite(v))background[n++]=v;}
        if(n<16)return new FluorescencePlaneScore(Double.NaN,Double.NaN,Double.NaN,0);
        Arrays.sort(background,0,n);double median=median(background,n);for(int i=0;i<n;i++)background[i]=Math.abs(background[i]-median);Arrays.sort(background,0,n);double sigma=Math.max(1.4826*median(background,n),1e-6);
        boolean[] high=new boolean[w*h];double peak=Double.NEGATIVE_INFINITY;
        for(int y=y0;y<y1;y++)for(int x=x0;x<x1;x++){float v=smooth[y*w+x];if(!Float.isFinite(v))continue;double z=(v-median)/sigma;if(z>peak)peak=z;if(z>=p.componentZThreshold)high[y*w+x]=true;}
        return new FluorescencePlaneScore(median,sigma,peak,largestComponent(high,w,h,x0,x1,y0,y1));
    }
    private static float[] gaussian(float[] source,int w,int h,double sigma){int radius=Math.max(1,(int)Math.ceil(3*sigma));double[] kernel=new double[2*radius+1];double total=0;for(int i=-radius;i<=radius;i++){double value=Math.exp(-0.5*i*i/(sigma*sigma));kernel[i+radius]=value;total+=value;}for(int i=0;i<kernel.length;i++)kernel[i]/=total;float[] tmp=convolve(source,w,h,kernel,radius,true);return convolve(tmp,w,h,kernel,radius,false);}
    private static float[] convolve(float[] source,int w,int h,double[] kernel,int radius,boolean horizontal){float[] out=new float[source.length];Arrays.fill(out,Float.NaN);for(int y=0;y<h;y++)for(int x=0;x<w;x++){double sum=0,weight=0;for(int k=-radius;k<=radius;k++){int xx=horizontal?x+k:x,yy=horizontal?y:y+k;if(xx<0||yy<0||xx>=w||yy>=h)continue;float value=source[yy*w+xx];if(!Float.isFinite(value))continue;double wk=kernel[k+radius];sum+=wk*value;weight+=wk;}if(weight>0)out[y*w+x]=(float)(sum/weight);}return out;}
    private static double median(double[] values,int n){return n%2==1?values[n/2]:(values[n/2-1]+values[n/2])/2.0;}
    private static int largestComponent(boolean[] high,int w,int h,int x0,int x1,int y0,int y1){boolean[] seen=new boolean[high.length];int[] queue=new int[high.length];int largest=0;for(int y=y0;y<y1;y++)for(int x=x0;x<x1;x++){int start=y*w+x;if(!high[start]||seen[start])continue;int head=0,tail=0;queue[tail++]=start;seen[start]=true;while(head<tail){int at=queue[head++],ax=at%w,ay=at/w;for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++){if(dx==0&&dy==0)continue;int nx=ax+dx,ny=ay+dy;if(nx<x0||nx>=x1||ny<y0||ny>=y1)continue;int next=ny*w+nx;if(high[next]&&!seen[next]){seen[next]=true;queue[tail++]=next;}}}largest=Math.max(largest,tail);}return largest;}
}
