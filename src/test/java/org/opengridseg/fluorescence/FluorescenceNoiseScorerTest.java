package org.opengridseg.fluorescence;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Random;
import org.opengridseg.image.FloatPlane;
import org.junit.jupiter.api.Test;

class FluorescenceNoiseScorerTest {
    @Test void classifiesGaussianBackgroundAsBackground() {
        FluorescenceParameters parameters=new FluorescenceParameters();
        FluorescenceScoreAccumulator score=new FluorescenceScoreAccumulator(96,48,parameters);
        for(int frame=0;frame<30;frame++) score.accept(gaussianPlane(frame,false));
        FluorescenceBarScore result=score.finish();
        assertFalse(result.signalLike());
        assertTrue(result.criteriaPassed()<2);
    }

    @Test void detectsARepeatedLocalizedFluorescenceSignal() {
        FluorescenceParameters parameters=new FluorescenceParameters();
        FluorescenceScoreAccumulator score=new FluorescenceScoreAccumulator(96,48,parameters);
        for(int frame=0;frame<30;frame++) score.accept(gaussianPlane(frame,frame>=5&&frame<12));
        FluorescenceBarScore result=score.finish();
        assertTrue(result.signalLike());
        assertTrue(result.criteriaPassed()>=2);
        assertTrue(result.z90()>=parameters.z90Threshold);
        assertTrue(result.persistence()>=parameters.persistenceThreshold);
    }

    @Test void ignoresOneFrameHotPixel() {
        FluorescenceParameters parameters=new FluorescenceParameters();
        FluorescenceScoreAccumulator score=new FluorescenceScoreAccumulator(96,48,parameters);
        for(int frame=0;frame<30;frame++) {
            FloatPlane plane=gaussianPlane(frame,false);
            if(frame==4)plane.set(48,24,5000f);
            score.accept(plane);
        }
        FluorescenceBarScore result=score.finish();
        assertFalse(result.signalLike());
        assertTrue(result.persistence()<parameters.persistenceThreshold);
    }

    @Test void detectsARepeatedMeanShiftInsideTheBarWindow() {
        FluorescenceParameters parameters=new FluorescenceParameters();
        FluorescenceScoreAccumulator score=new FluorescenceScoreAccumulator(96,48,parameters);
        for(int frame=0;frame<30;frame++) {
            FloatPlane plane=gaussianPlane(frame,false);
            if(frame>=3&&frame<13)for(int y=15;y<33;y++)for(int x=24;x<72;x++)plane.set(x,y,plane.get(x,y)+40f);
            score.accept(plane);
        }
        assertTrue(score.finish().signalLike());
    }

    private static FloatPlane gaussianPlane(int frame,boolean addSignal) {
        Random random=new Random(1000+frame);
        FloatPlane plane=new FloatPlane(96,48);
        for(int y=0;y<48;y++)for(int x=0;x<96;x++)plane.set(x,y,(float)(200+10*random.nextGaussian()));
        if(addSignal)for(int y=19;y<=29;y++)for(int x=41;x<=55;x++){
            double dx=(x-48)/4.0,dy=(y-24)/3.0;
            plane.set(x,y,plane.get(x,y)+(float)(100*Math.exp(-0.5*(dx*dx+dy*dy))));
        }
        return plane;
    }
}
