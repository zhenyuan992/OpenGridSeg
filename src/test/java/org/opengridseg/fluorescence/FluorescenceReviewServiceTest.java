package org.opengridseg.fluorescence;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.opengridseg.export.ExportBar;
import org.opengridseg.image.FloatPlane;
import org.opengridseg.image.Interpolation;
import org.opengridseg.io.Channel;
import org.junit.jupiter.api.Test;

class FluorescenceReviewServiceTest {
    @Test void keepsBarWithRepeatedGfpSignalAndRejectsGaussianBackgroundBar() throws Exception {
        ExportBar signal=new ExportBar("signal",0,0,0,"observed",40,40,40,40,false);
        ExportBar background=new ExportBar("background",0,0,1,"observed",100,40,100,40,false);
        List<BarFluorescenceReview> reviews=FluorescenceReviewService.score(
            Arrays.asList(signal,background),30,0,32,24,Interpolation.BICUBIC,
            new FluorescenceParameters(),(channel,time)->sourcePlane(channel,time));
        assertTrue(reviews.get(0).autoSelected(SignalLogic.EITHER_CHANNEL));
        assertFalse(reviews.get(1).autoSelected(SignalLogic.EITHER_CHANNEL));
        assertFalse(reviews.get(0).autoSelected(SignalLogic.BOTH_CHANNELS));
    }

    @Test void reportsProgressWhileScoringBars() throws Exception {
        ExportBar bar=new ExportBar("bar",0,0,0,"observed",40,40,40,40,false);
        List<String> updates=new ArrayList<>();
        FluorescenceReviewService.score(Arrays.asList(bar),2,0,32,24,Interpolation.BILINEAR,
            new FluorescenceParameters(),(channel,time)->sourcePlane(channel,time),
            (completed,total,message)->updates.add(completed+"/"+total+" "+message));
        assertEquals("4/4 Scoring mCherry frame 2 / 2 — 1 / 1 bars processed",updates.get(updates.size()-1));
    }

    private static FloatPlane sourcePlane(Channel channel,int time) {
        Random random=new Random(9000+channel.ordinal()*100+time);
        FloatPlane plane=new FloatPlane(140,80);
        for(int y=0;y<80;y++)for(int x=0;x<140;x++)plane.set(x,y,(float)(200+10*random.nextGaussian()));
        if(channel==Channel.GFP&&time>=5&&time<12)for(int y=34;y<=46;y++)for(int x=33;x<=47;x++){
            double dx=(x-40)/4.0,dy=(y-40)/3.0;
            plane.set(x,y,plane.get(x,y)+(float)(100*Math.exp(-0.5*(dx*dx+dy*dy))));
        }
        return plane;
    }
}
