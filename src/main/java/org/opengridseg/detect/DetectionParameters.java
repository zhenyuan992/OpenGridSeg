package org.opengridseg.detect;
public final class DetectionParameters {
    public double barLength=60.0,barWidth=18.0,thresholdSigma=7.0,minPitch=90.0,maxPitch=130.0,gridTolerance=9.0,pitchX=Double.NaN,pitchY=Double.NaN;
    public int minPeakDistance=45,arraySize=20,macroPeriodSlots=30,minArrayPeaks=5;
}
