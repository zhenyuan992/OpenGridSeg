package org.opengridseg.fluorescence;

public final class FluorescenceParameters {
    public double gaussianSigma=1.0;
    public double signalMarginXFraction=12.0/96.0;
    public double signalMarginYFraction=10.0/48.0;
    public double componentZThreshold=3.0;
    public double persistencePeakZThreshold=4.0;
    public int persistenceAreaThreshold=6;
    public double z90Threshold=5.0;
    public double area90Threshold=12.0;
    public double persistenceThreshold=0.20;
    public int criteriaRequired=2;
}
