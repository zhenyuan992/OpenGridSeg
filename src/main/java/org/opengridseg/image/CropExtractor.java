package org.opengridseg.image;
public final class CropExtractor {
    private CropExtractor(){}
    public static FloatPlane extractHorizontal(FloatPlane src,double centerX,double centerY,double angleDeg,int width,int height,Interpolation method){
        float min=src.min(),max=src.max(); FloatPlane out=new FloatPlane(width,height); double a=Math.toRadians(angleDeg),ca=Math.cos(a),sa=Math.sin(a);
        double ox=(width-1)/2.0,oy=(height-1)/2.0;
        for(int y=0;y<height;y++)for(int x=0;x<width;x++){
            double u=x-ox,v=y-oy; double sx=centerX+ca*u-sa*v, sy=centerY+sa*u+ca*v;
            float value=sample(src,sx,sy,method); if(Float.isFinite(value)){if(value<min)value=min;if(value>max)value=max;} out.set(x,y,value);
        }
        return out;
    }
    static float sample(FloatPlane p,double x,double y,Interpolation method){
        if(x<0||y<0||x>p.width()-1||y>p.height()-1)return Float.NaN;
        if(method==Interpolation.NEAREST)return p.get((int)Math.round(x),(int)Math.round(y));
        int x0=(int)Math.floor(x),y0=(int)Math.floor(y); double fx=x-x0,fy=y-y0;
        if(method==Interpolation.BILINEAR){int x1=Math.min(x0+1,p.width()-1),y1=Math.min(y0+1,p.height()-1);return(float)((1-fy)*((1-fx)*p.get(x0,y0)+fx*p.get(x1,y0))+fy*((1-fx)*p.get(x0,y1)+fx*p.get(x1,y1)));}
        double sum=0,weight=0; for(int j=-1;j<=2;j++){int yy=clamp(y0+j,0,p.height()-1);double wy=cubic(fy-j);for(int i=-1;i<=2;i++){int xx=clamp(x0+i,0,p.width()-1);double w=wy*cubic(fx-i);sum+=w*p.get(xx,yy);weight+=w;}}
        return(float)(sum/weight);
    }
    private static double cubic(double x){double a=-0.5,t=Math.abs(x);if(t<=1)return(a+2)*t*t*t-(a+3)*t*t+1;if(t<2)return a*t*t*t-5*a*t*t+8*a*t-4*a;return 0;}
    private static int clamp(int v,int lo,int hi){return Math.max(lo,Math.min(hi,v));}
}
