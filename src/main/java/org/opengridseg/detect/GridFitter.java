package org.opengridseg.detect;
import java.util.*;
public final class GridFitter {
    private GridFitter(){}
    public static Pitch estimatePitch(List<Peak> peaks,double min,double max,double crossTol){
        List<Double> dx=new ArrayList<>(),dy=new ArrayList<>();
        for(int i=0;i<peaks.size();i++)for(int j=i+1;j<peaks.size();j++){Peak a=peaks.get(i),b=peaks.get(j);double x=Math.abs(a.x()-b.x()),y=Math.abs(a.y()-b.y());if(y<=crossTol&&x>=min&&x<=max)dx.add(x);if(x<=crossTol&&y>=min&&y<=max)dy.add(y);}
        if(dx.isEmpty()||dy.isEmpty())throw new IllegalArgumentException("Not enough near-row and near-column peak pairs to estimate pitch");
        return new Pitch(median(dx),median(dy));
    }
    public static GridPhase fitPhase(List<Peak> peaks,Pitch pitch,double tolerance){
        int best=-1;double bx=0,by=0;List<Integer> bestInliers=new ArrayList<>();
        for(Peak candidate:peaks){double px=mod(candidate.x(),pitch.x()),py=mod(candidate.y(),pitch.y());List<Integer> inliers=new ArrayList<>();for(int i=0;i<peaks.size();i++){Peak p=peaks.get(i);if(circularDistance(mod(p.x(),pitch.x()),px,pitch.x())<=tolerance&&circularDistance(mod(p.y(),pitch.y()),py,pitch.y())<=tolerance)inliers.add(i);}if(inliers.size()>best){best=inliers.size();bx=px;by=py;bestInliers=inliers;}}
        return new GridPhase(bx,by,bestInliers);
    }
    static double mod(double value,double period){double r=value%period;return r<0?r+period:r;} static double circularDistance(double a,double b,double period){double d=Math.abs(a-b);return Math.min(d,period-d);}
    private static double median(List<Double> values){Collections.sort(values);int n=values.size();return n%2==1?values.get(n/2):(values.get(n/2-1)+values.get(n/2))/2.0;}
}
