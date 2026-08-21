package org.opengridseg.fluorescence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.opengridseg.image.FloatPlane;

public final class FluorescenceScoreAccumulator {
    private final int width,height;private final FluorescenceParameters parameters;private final float[] maximum;private final List<Double> peaks=new ArrayList<>();private final List<Double> areas=new ArrayList<>();private int persistentFrames;private double maxPeakZ=Double.NEGATIVE_INFINITY;
    public FluorescenceScoreAccumulator(int width,int height,FluorescenceParameters parameters){this.width=width;this.height=height;this.parameters=parameters;maximum=new float[width*height];Arrays.fill(maximum,Float.NEGATIVE_INFINITY);}
    public void accept(FloatPlane plane){if(plane.width()!=width||plane.height()!=height)throw new IllegalArgumentException("Crop dimensions changed");FluorescencePlaneScore score=FluorescenceNoiseScorer.score(plane,parameters);peaks.add(score.peakRobustZ());areas.add((double)score.largestComponentPixels());if(score.peakRobustZ()>=parameters.persistencePeakZThreshold&&score.largestComponentPixels()>=parameters.persistenceAreaThreshold)persistentFrames++;maxPeakZ=Math.max(maxPeakZ,score.peakRobustZ());for(int i=0;i<maximum.length;i++){float value=plane.pixels()[i];if(Float.isFinite(value)&&value>maximum[i])maximum[i]=value;}}
    public FluorescenceBarScore finish(){int frames=peaks.size();double z90=quantile(peaks,0.90),area90=quantile(areas,0.90),persistence=frames==0?0:(double)persistentFrames/frames;int criteria=0;if(z90>=parameters.z90Threshold)criteria++;if(area90>=parameters.area90Threshold)criteria++;if(persistence>=parameters.persistenceThreshold)criteria++;float[] projection=maximum.clone();for(int i=0;i<projection.length;i++)if(projection[i]==Float.NEGATIVE_INFINITY)projection[i]=Float.NaN;return new FluorescenceBarScore(frames,persistentFrames,maxPeakZ,z90,area90,persistence,criteria,criteria>=parameters.criteriaRequired,new FloatPlane(width,height,projection));}
    private static double quantile(List<Double> values,double q){if(values.isEmpty())return Double.NaN;List<Double> sorted=new ArrayList<>(values);Collections.sort(sorted);double position=q*(sorted.size()-1),fraction=position-Math.floor(position);int lower=(int)Math.floor(position),upper=Math.min(lower+1,sorted.size()-1);return sorted.get(lower)*(1-fraction)+sorted.get(upper)*fraction;}
}
