package org.opengridseg.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.opengridseg.ProgressListener;
import org.opengridseg.export.ExportBar;
import org.opengridseg.fluorescence.BarFluorescenceReview;
import org.opengridseg.fluorescence.FluorescenceParameters;
import org.opengridseg.fluorescence.FluorescenceReviewService;
import org.opengridseg.fluorescence.SignalLogic;
import org.opengridseg.image.FloatPlane;
import org.opengridseg.image.Interpolation;
import org.junit.jupiter.api.Test;

final class BarReviewSelectionTest {
    @Test void bulkClickSetsTheFiveByFiveBlockOppositeToTheClickedBar()throws Exception{
        List<BarFluorescenceReview> reviews=reviews();
        List<Integer> changed=BarReviewSelection.toggle(reviews,SignalLogic.EITHER_CHANNEL,14,true);
        assertEquals(25,changed.size());
        for(int index=0;index<reviews.size();index++)assertEquals(changed.contains(index),reviews.get(index).selected(SignalLogic.EITHER_CHANNEL));
        BarReviewSelection.toggle(reviews,SignalLogic.EITHER_CHANNEL,14,true);
        for(int index:changed)assertFalse(reviews.get(index).selected(SignalLogic.EITHER_CHANNEL));
    }
    @Test void normalClickChangesOnlyOneBar()throws Exception{
        List<BarFluorescenceReview> reviews=reviews();
        List<Integer> changed=BarReviewSelection.toggle(reviews,SignalLogic.EITHER_CHANNEL,14,false);
        assertEquals(1,changed.size());assertTrue(reviews.get(14).selected(SignalLogic.EITHER_CHANNEL));assertFalse(reviews.get(13).selected(SignalLogic.EITHER_CHANNEL));
    }
    private static List<BarFluorescenceReview> reviews()throws Exception{
        List<ExportBar> bars=new ArrayList<>();for(int row=0;row<6;row++)for(int col=0;col<6;col++)bars.add(new ExportBar("b"+row+"-"+col,1,row,col,"observed",10,10,col,row,false));
        return FluorescenceReviewService.score(bars,1,0,12,8,Interpolation.NEAREST,new FluorescenceParameters(),(channel,time)->new FloatPlane(24,24),ProgressListener.NONE);
    }
}
