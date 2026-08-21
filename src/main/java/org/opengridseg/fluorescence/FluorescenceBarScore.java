package org.opengridseg.fluorescence;

import org.opengridseg.image.FloatPlane;

public final class FluorescenceBarScore {
    private final int totalFrames,persistentFrames,criteriaPassed;
    private final double maxPeakZ,z90,area90,persistence;
    private final boolean signalLike;
    private final FloatPlane maxProjection;
    FluorescenceBarScore(int totalFrames,int persistentFrames,double maxPeakZ,double z90,double area90,double persistence,int criteriaPassed,boolean signalLike,FloatPlane maxProjection){this.totalFrames=totalFrames;this.persistentFrames=persistentFrames;this.maxPeakZ=maxPeakZ;this.z90=z90;this.area90=area90;this.persistence=persistence;this.criteriaPassed=criteriaPassed;this.signalLike=signalLike;this.maxProjection=maxProjection;}
    FluorescenceBarScore(int totalFrames,int signalFrames,double maxPeakZ,boolean signalLike,FloatPlane maxProjection){this(totalFrames,signalFrames,maxPeakZ,maxPeakZ,0,totalFrames==0?0:(double)signalFrames/totalFrames,signalLike?2:0,signalLike,maxProjection);}
    public int totalFrames(){return totalFrames;}
    public int signalFrames(){return persistentFrames;}
    public int persistentFrames(){return persistentFrames;}
    public double maxPeakRobustZ(){return maxPeakZ;}
    public double z90(){return z90;}
    public double area90(){return area90;}
    public double persistence(){return persistence;}
    public int criteriaPassed(){return criteriaPassed;}
    public boolean signalLike(){return signalLike;}
    public FloatPlane maxProjection(){return maxProjection;}
}
