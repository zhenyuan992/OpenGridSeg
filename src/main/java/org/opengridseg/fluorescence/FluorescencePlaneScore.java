package org.opengridseg.fluorescence;

public final class FluorescencePlaneScore {
    private final double backgroundMedian,backgroundSigma,peakZ;
    private final int largestComponent;
    FluorescencePlaneScore(double backgroundMedian,double backgroundSigma,double peakZ,int largestComponent){this.backgroundMedian=backgroundMedian;this.backgroundSigma=backgroundSigma;this.peakZ=peakZ;this.largestComponent=largestComponent;}
    public double median(){return backgroundMedian;}
    public double robustSigma(){return backgroundSigma;}
    public double peakRobustZ(){return peakZ;}
    public int largestComponentPixels(){return largestComponent;}
}
